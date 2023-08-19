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
                CardSecurityFile cardSecurityFile = new CardSecurityFile(service.getInputStream(PassportService.EF_CARD_SECURITY));
                Collection<SecurityInfo> securityInfoCollection = cardSecurityFile.getSecurityInfos();
                for (SecurityInfo securityInfo : securityInfoCollection) {
                    if (securityInfo instanceof PACEInfo) {
                        PACEInfo paceInfo = (PACEInfo) securityInfo;
                        service.doPACE(bacKey, paceInfo.getObjectIdentifier(), PACEInfo.toParameterSpec(paceInfo.getParameterId()), null);
                        paceSucceeded = true;
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, e);


                if(e instanceof CardServiceException){

                    if(e.getMessage().equals("Tag was lost")){


                        throw new DocumentReadException("tag-lost");
                    }
                }
            }

            service.sendSelectApplet(paceSucceeded);

            if (!paceSucceeded) {
                try {
                    service.getInputStream(PassportService.EF_COM).read();
                } catch (Exception e) {
                    service.doBAC(bacKey);
                }
            }

            statusCallback.onChange("readDocumentDetails");
            // -- Personal Details -- //
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