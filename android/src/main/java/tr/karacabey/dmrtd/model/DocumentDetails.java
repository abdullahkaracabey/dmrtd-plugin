package tr.karacabey.dmrtd.model;

import java.util.HashMap;
import java.util.Map;

public class DocumentDetails {

    String name;
    String surname;
    String personalNumber;
    String gender;
    String birthDate;
    String issueDate;
    String expiryDate;
    String serialNumber;
    String nationality;
    String issuerAuthority;
    String faceImageBase64;
    String portraitImageBase64;
    String signatureBase64;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getPersonalNumber() {
        return personalNumber;
    }

    public void setPersonalNumber(String personalNumber) {
        this.personalNumber = personalNumber;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public String getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(String issueDate) {
        this.issueDate = issueDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getIssuerAuthority() {
        return issuerAuthority;
    }

    public void setIssuerAuthority(String issuerAuthority) {
        this.issuerAuthority = issuerAuthority;
    }

    public String getFaceImageBase64() {
        return faceImageBase64;
    }

    public void setFaceImageBase64(String faceImageBase64) {
        this.faceImageBase64 = faceImageBase64;
    }

    public String getPortraitImageBase64() {
        return portraitImageBase64;
    }

    public void setPortraitImageBase64(String portraitImageBase64) {
        this.portraitImageBase64 = portraitImageBase64;
    }

    public String getSignatureBase64() {
        return signatureBase64;
    }

    public void setSignatureBase64(String signatureBase64) {
        this.signatureBase64 = signatureBase64;
    }

    public Map<String,String> toMap(){

        Map<String,String> result = new HashMap<String,String>();
        result.put("name",name);
        result.put("surname",surname);
        result.put("personalNumber",personalNumber);
        result.put("gender",gender);
        result.put("birthDate",birthDate);
        result.put("expiryDate",expiryDate);
        result.put("issueDate",issueDate);
        result.put("serialNumber",serialNumber);
        result.put("nationality",nationality);
        result.put("issuerAuthority",issuerAuthority);
        result.put("faceImageBase64",faceImageBase64);
        result.put("portraitImageBase64",portraitImageBase64);
        result.put("signatureBase64",signatureBase64);

        return result;
    }
}
