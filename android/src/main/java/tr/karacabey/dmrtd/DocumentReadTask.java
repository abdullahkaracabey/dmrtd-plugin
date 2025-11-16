package tr.karacabey.dmrtd;


import static android.content.ContentValues.TAG;
import static org.jmrtd.PassportService.DEFAULT_MAX_BLOCKSIZE;
import static org.jmrtd.PassportService.NORMAL_MAX_TRANCEIVE_LENGTH;

import android.content.Context;
import android.graphics.Bitmap;
import android.nfc.tech.IsoDep;
import android.util.Log;

import net.sf.scuba.smartcards.CardFileInputStream;
import net.sf.scuba.smartcards.CardService;
import net.sf.scuba.smartcards.CardServiceException;

import org.jmrtd.BACKeySpec;
import org.jmrtd.PassportService;
import org.jmrtd.lds.CardAccessFile;
import org.jmrtd.lds.CardSecurityFile;
import org.jmrtd.lds.DisplayedImageInfo;
import org.jmrtd.lds.PACEInfo;
import org.jmrtd.lds.SecurityInfo;
import org.jmrtd.lds.icao.DG11File;
import org.jmrtd.lds.icao.DG12File;
import org.jmrtd.lds.icao.DG15File;
import org.jmrtd.lds.icao.DG1File;
import org.jmrtd.lds.icao.DG2File;
import org.jmrtd.lds.icao.DG3File;
import org.jmrtd.lds.icao.DG5File;
import org.jmrtd.lds.icao.DG7File;
import org.jmrtd.lds.icao.MRZInfo;
import org.jmrtd.lds.iso19794.FaceImageInfo;
import org.jmrtd.lds.iso19794.FaceInfo;
import org.jmrtd.lds.iso19794.FingerImageInfo;
import org.jmrtd.lds.iso19794.FingerInfo;

import java.io.UnsupportedEncodingException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import tr.karacabey.dmrtd.model.DocumentReadException;
import tr.karacabey.dmrtd.model.PersonDetails;
import tr.karacabey.dmrtd.model.DocType;
import tr.karacabey.dmrtd.model.EDocument;
import tr.karacabey.dmrtd.model.DocumentDetails;
import tr.karacabey.dmrtd.model.ReadResult;
import tr.karacabey.dmrtd.util.DateUtil;
import tr.karacabey.dmrtd.util.Image;
import tr.karacabey.dmrtd.util.ImageUtil;

class DocumentReadTask implements Callable<ReadResult> {
    private IsoDep isoDep;
    private BACKeySpec bacKey;
    private Context context;
    private DocumentReadStatusCallback statusCallback;

    public interface DocumentReadStatusCallback {
        void onChange(String result);
    }


    public DocumentReadTask(Context context, IsoDep isoDep, BACKeySpec bacKey, DocumentReadStatusCallback statusCallback) {
        this.isoDep =isoDep;
        this.bacKey= bacKey;
        this.context = context;
        this.statusCallback = statusCallback;
    }

    @Override
    public ReadResult call() throws Exception{

        EDocument eDocument = new EDocument();
        DocType docType = DocType.other;
        DocumentDetails documentDetails = new DocumentDetails();
        PersonDetails personDetails = new PersonDetails();

        try {


            CardService cardService = CardService.getInstance(isoDep);
            cardService.open();
            cardService.close();

            PassportService service = new PassportService(cardService, NORMAL_MAX_TRANCEIVE_LENGTH, DEFAULT_MAX_BLOCKSIZE, true, false);
            service.open();

            boolean paceSucceeded = false;
            try {
                Log.i("NFC_READ", "Attempting PACE authentication...");
                
                // Read CardAccess from Master File (similar to iOS implementation)
                // CardAccess is stored in EF.CardAccess (file 0x011C) in the Master File
                // We need to read this before doing PACE
                CardFileInputStream cardAccessStream = service.getInputStream(PassportService.EF_CARD_ACCESS);
                CardAccessFile cardAccessFile = new CardAccessFile(cardAccessStream);
                Collection<SecurityInfo> securityInfoCollection = cardAccessFile.getSecurityInfos();
                
                for (SecurityInfo securityInfo : securityInfoCollection) {
                    if (securityInfo instanceof PACEInfo) {
                        PACEInfo paceInfo = (PACEInfo) securityInfo;
                        Log.i("NFC_READ", "PACE Info found: " + paceInfo.getObjectIdentifier());
                        service.doPACE(bacKey, paceInfo.getObjectIdentifier(), PACEInfo.toParameterSpec(paceInfo.getParameterId()), null);
                        paceSucceeded = true;
                        Log.i("NFC_READ", "PACE authentication succeeded!");
                        break; // Only use first PACE info
                    }
                }
                if (!paceSucceeded) {
                    Log.i("NFC_READ", "No PACE info found, will try BAC");
                }
            } catch (Exception e) {
                Log.w(TAG, "PACE failed: " + e.getMessage(), e);
                Log.i("NFC_READ", "PACE failed, will fall back to BAC");


                if(e instanceof CardServiceException){

                    if(e.getMessage().equals("Tag was lost")){


                        throw new DocumentReadException("tag-lost");
                    }
                }
            }

            service.sendSelectApplet(paceSucceeded);
            Log.i("NFC_READ", "Applet selected (after " + (paceSucceeded ? "PACE" : "attempting PACE") + ")");

            if (!paceSucceeded) {
                try {
                    service.getInputStream(PassportService.EF_COM).read();
                    Log.i("NFC_READ", "EF_COM readable without BAC");
                } catch (Exception e) {
                    Log.i("NFC_READ", "Performing BAC authentication...");
                    service.doBAC(bacKey);
                    Log.i("NFC_READ", "BAC authentication succeeded!");
                }
            }

            statusCallback.onChange("readDocumentDetails");
            // -- Personal Details -- //
            Log.i("NFC_READ", "Reading DG1 (MRZ)...");
            CardFileInputStream dg1In = service.getInputStream(PassportService.EF_DG1);
            DG1File dg1File = new DG1File(dg1In);

            MRZInfo mrzInfo = dg1File.getMRZInfo();
            try {
                String str = new String(mrzInfo.getEncoded(), "UTF-8");

                switch(str.length()) {
                    case 90: /* ID1 */
                        documentDetails.setMrzContent( str.substring(0, 30) + "\n"
                                + str.substring(30, 60) + "\n"
                                + str.substring(60, 90));
                    case 88: /* ID3 */
                        documentDetails.setMrzContent(str.substring(0, 44) + "\n"
                                + str.substring(44, 88));
                }
            } catch (UnsupportedEncodingException uee) {
                throw new IllegalStateException(uee);
            }
            
            documentDetails.setName(mrzInfo.getSecondaryIdentifier().replace("<", " ").trim());
            documentDetails.setSurname(mrzInfo.getPrimaryIdentifier().replace("<", " ").trim());
            documentDetails.setPersonalNumber(mrzInfo.getPersonalNumber());
            documentDetails.setGender(mrzInfo.getGender().toString());
            // documentDetails.setBirthDate(DateUtil.convertFromMrzDate(dg1File.getMRZInfo().getDateOfBirth()));
            documentDetails.setExpiryDate(DateUtil.convertFromMrzDate(dg1File.getMRZInfo().getDateOfExpiry()));
            documentDetails.setSerialNumber(mrzInfo.getDocumentNumber());
            documentDetails.setNationality(mrzInfo.getNationality());


            if("I".equals(mrzInfo.getDocumentCode())) {
                docType = DocType.idCard;
            } else if("P".equals(mrzInfo.getDocumentCode())) {
                docType = DocType.passport;
            }

            // -- Face Image -- //
            Log.i("NFC_READ", "Reading DG2 (Face Image)...");
            CardFileInputStream dg2In = service.getInputStream(PassportService.EF_DG2);
            DG2File dg2File = new DG2File(dg2In);

            List<FaceInfo> faceInfos = dg2File.getFaceInfos();
            List<FaceImageInfo> allFaceImageInfos = new ArrayList<>();
            for (FaceInfo faceInfo : faceInfos) {
                allFaceImageInfos.addAll(faceInfo.getFaceImageInfos());
            }

            if (!allFaceImageInfos.isEmpty()) {
                FaceImageInfo faceImageInfo = allFaceImageInfos.iterator().next();
                Image image = ImageUtil.getImage(context, faceImageInfo);
                documentDetails.setFaceImageBase64(image.getBase64Image());
            }

            // -- Fingerprint (if exist)-- //
            try {
                CardFileInputStream dg3In = service.getInputStream(PassportService.EF_DG3);
                DG3File dg3File = new DG3File(dg3In);

                List<FingerInfo> fingerInfos = dg3File.getFingerInfos();
                List<FingerImageInfo> allFingerImageInfos = new ArrayList<>();
                for (FingerInfo fingerInfo : fingerInfos) {
                    allFingerImageInfos.addAll(fingerInfo.getFingerImageInfos());
                }

                List<String> fingerprintsImage = new ArrayList<>();

                if (!allFingerImageInfos.isEmpty()) {

                    for(FingerImageInfo fingerImageInfo : allFingerImageInfos) {
                        Image image = ImageUtil.getImage(context, fingerImageInfo);
                        fingerprintsImage.add(image.getBase64Image());
                    }

                   documentDetails.setFingerPrints(fingerprintsImage);

                }
            } catch (Exception e) {
                Log.w(TAG, e);
            }

            // -- Portrait Picture -- //
            try {
                CardFileInputStream dg5In = service.getInputStream(PassportService.EF_DG5);
                DG5File dg5File = new DG5File(dg5In);

                List<DisplayedImageInfo> displayedImageInfos = dg5File.getImages();
                if (!displayedImageInfos.isEmpty()) {
                    DisplayedImageInfo displayedImageInfo = displayedImageInfos.iterator().next();
                    Image image = ImageUtil.getImage(context, displayedImageInfo);
                  //  documentDetails.setPortraitImage(image.getBitmapImage());
                    documentDetails.setPortraitImageBase64(image.getBase64Image());
                }
            } catch (Exception e) {
                Log.w(TAG, e);
            }

            // -- Signature (if exist) -- //
            try {
                CardFileInputStream dg7In = service.getInputStream(PassportService.EF_DG7);
                DG7File dg7File = new DG7File(dg7In);

                List<DisplayedImageInfo> signatureImageInfos = dg7File.getImages();
                if (!signatureImageInfos.isEmpty()) {
                    DisplayedImageInfo displayedImageInfo = signatureImageInfos.iterator().next();
                    Image image = ImageUtil.getImage(context, displayedImageInfo);
                   // documentDetails.setPortraitImage(image.getBitmapImage());
                    documentDetails.setPortraitImageBase64(image.getBase64Image());
                }

            } catch (Exception e) {
                Log.w(TAG, e);
            }

            statusCallback.onChange("readPersonalDetails");
            // -- Additional Details (if exist) -- //
            try {
                Log.i("NFC_READ", "Reading DG11 (Additional Personal Details)...");
                CardFileInputStream dg11In = service.getInputStream(PassportService.EF_DG11);
                DG11File dg11File = new DG11File(dg11In);

                if(dg11File.getLength() > 0) {
                    documentDetails.setBirthDate(dg11File.getFullDateOfBirth());
                    personDetails.setCustodyInformation(dg11File.getCustodyInformation());
                    personDetails.setNameOfHolder(dg11File.getNameOfHolder());
                    personDetails.setFullDateOfBirth(dg11File.getFullDateOfBirth());
                    personDetails.setOtherNames(dg11File.getOtherNames());
                    personDetails.setOtherValidTDNumbers(dg11File.getOtherValidTDNumbers());
                    personDetails.setPermanentAddress(dg11File.getPermanentAddress());
                    personDetails.setPersonalNumber(dg11File.getPersonalNumber());
                    personDetails.setPersonalSummary(dg11File.getPersonalSummary());
                    personDetails.setPlaceOfBirth(dg11File.getPlaceOfBirth());
                    personDetails.setProfession(dg11File.getProfession());
                    personDetails.setProofOfCitizenship(dg11File.getProofOfCitizenship());
                    personDetails.setTag(dg11File.getTag());
                    personDetails.setTagPresenceList(dg11File.getTagPresenceList());
                    personDetails.setTelephone(dg11File.getTelephone());
                    personDetails.setTitle(dg11File.getTitle());
                }
            } catch (Exception e) {
                Log.w(TAG, e);
            }

            try {
                Log.i("NFC_READ", "Reading DG12 (Document Issuing Data)...");
                CardFileInputStream dg12In = service.getInputStream(PassportService.EF_DG12);
                DG12File dg12File = new DG12File(dg12In);

                if (dg12File.getLength() > 0) {
                   documentDetails.setIssueDate(dg12File.getDateOfIssue());
                    documentDetails.setIssuerAuthority(dg12File.getIssuingAuthority());
                }
            } catch (Exception e) {
                Log.w(TAG, e);
            }

            // -- Document Public Key -- //
            try {
                CardFileInputStream dg15In = service.getInputStream(PassportService.EF_DG15);
                DG15File dg15File = new DG15File(dg15In);
                PublicKey publicKey = dg15File.getPublicKey();
              //  eDocument.setDocPublicKey(publicKey);
            } catch (Exception e) {
                Log.w(TAG, e);
            }
            service.close();

            // Log all read data groups with details
            Log.i("NFC_READ", "");
            Log.i("NFC_READ", "=== ANDROID NFC READ COMPLETED ===");
            Log.i("NFC_READ", "Authentication: " + (paceSucceeded ? "PACE" : "BAC"));
            Log.i("NFC_READ", "");
            
            Log.i("NFC_READ", "--- Document Details (DG1, DG2) ---");
            Log.i("NFC_READ", "Name: " + documentDetails.getName());
            Log.i("NFC_READ", "Surname: " + documentDetails.getSurname());
            Log.i("NFC_READ", "Personal Number: " + documentDetails.getPersonalNumber());
            Log.i("NFC_READ", "Gender: " + documentDetails.getGender());
            Log.i("NFC_READ", "Birth Date: " + documentDetails.getBirthDate());
            Log.i("NFC_READ", "Expiry Date: " + documentDetails.getExpiryDate());
            Log.i("NFC_READ", "Serial Number: " + documentDetails.getSerialNumber());
            Log.i("NFC_READ", "Nationality: " + documentDetails.getNationality());
            Log.i("NFC_READ", "Issuer Authority: " + documentDetails.getIssuerAuthority());
            Log.i("NFC_READ", "Issue Date: " + documentDetails.getIssueDate());
            Log.i("NFC_READ", "Document Type: " + docType);
            Log.i("NFC_READ", "");
            
            Log.i("NFC_READ", "--- Images (DG2, DG3, DG5, DG7) ---");
            Log.i("NFC_READ", "Face Image: " + (documentDetails.getFaceImageBase64() != null ? "YES (" + documentDetails.getFaceImageBase64().length() + " chars)" : "NO"));
            Log.i("NFC_READ", "Portrait Image: " + (documentDetails.getPortraitImageBase64() != null ? "YES" : "NO"));
            Log.i("NFC_READ", "Signature: " + (documentDetails.getSignatureBase64() != null ? "YES" : "NO"));
            Log.i("NFC_READ", "");
            
            Log.i("NFC_READ", "--- Person Details (DG11, DG12) ---");
            Log.i("NFC_READ", "Full Date of Birth: " + personDetails.getFullDateOfBirth());
            Log.i("NFC_READ", "Place of Birth: " + (personDetails.getPlaceOfBirth() != null ? String.join(", ", personDetails.getPlaceOfBirth()) : "null"));
            Log.i("NFC_READ", "Permanent Address: " + (personDetails.getPermanentAddress() != null ? String.join(", ", personDetails.getPermanentAddress()) : "null"));
            Log.i("NFC_READ", "Telephone: " + personDetails.getTelephone());
            Log.i("NFC_READ", "Profession: " + personDetails.getProfession());
            Log.i("NFC_READ", "Name of Holder: " + personDetails.getNameOfHolder());
            Log.i("NFC_READ", "Title: " + personDetails.getTitle());
            Log.i("NFC_READ", "Other Names: " + (personDetails.getOtherNames() != null ? String.join(", ", personDetails.getOtherNames()) : "null"));
            Log.i("NFC_READ", "Personal Summary: " + personDetails.getPersonalSummary());
            Log.i("NFC_READ", "Custody Information: " + personDetails.getCustodyInformation());
            Log.i("NFC_READ", "Tag Presence List: " + personDetails.getTagPresenceList());
            Log.i("NFC_READ", "");
            Log.i("NFC_READ", "--- MRZ Content ---");
            Log.i("NFC_READ", documentDetails.getMrzContent());
            Log.i("NFC_READ", "==============================");
            Log.i("NFC_READ", "");

            eDocument.setDocType(docType);
            eDocument.setDocumentDetails(documentDetails);
            eDocument.setPersonDetails(personDetails);

            return new ReadResult(true, "", eDocument);

        } catch (Exception e) {
            Log.i("DocumentReadTask", "error: "+e.getMessage());
            throw e;
        }


    }
}