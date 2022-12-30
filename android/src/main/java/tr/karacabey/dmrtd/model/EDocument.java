package tr.karacabey.dmrtd.model;

import java.util.HashMap;
import java.util.Map;

public class EDocument {

    private DocType docType;
    DocumentDetails documentDetails;
    PersonDetails personDetails;
    //private PublicKey docPublicKey;

    public DocType getDocType() {
        return docType;
    }

    public void setDocType(DocType docType) {
        this.docType = docType;
    }

    public DocumentDetails getDocumentDetails() {
        return documentDetails;
    }

    public void setDocumentDetails(DocumentDetails documentDetails) {
        this.documentDetails = documentDetails;
    }

    public PersonDetails getPersonDetails() {
        return personDetails;
    }

    public void setPersonDetails(PersonDetails personDetails) {
        this.personDetails = personDetails;
    }

   /* public PublicKey getDocPublicKey() {
        return docPublicKey;
    }

    public void setDocPublicKey(PublicKey docPublicKey) {
        this.docPublicKey = docPublicKey;
    }*/

    public Map toMap(){

        Map<String,Object> result = new HashMap();
        result.put("docType",docType.name());
        result.put("documentDetails", documentDetails.toMap());
        result.put("personDetails", personDetails.toMap());

        return result;
    }
}
