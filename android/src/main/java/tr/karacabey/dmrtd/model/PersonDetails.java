package tr.karacabey.dmrtd.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PersonDetails {

    String custodyInformation;
    String fullDateOfBirth;
    String nameOfHolder;
    List<String> otherNames;
    List<String> otherValidTDNumbers;
    List<String> permanentAddress;
    String personalNumber;
    String personalSummary;
    List<String> placeOfBirth;
    String profession;
    byte[] proofOfCitizenship;
    int tag;
    List<Integer> tagPresenceList;
    String telephone;
    String title;

    public String getCustodyInformation() {
        return custodyInformation;
    }

    public void setCustodyInformation(String custodyInformation) {
        this.custodyInformation = custodyInformation;
    }

    public String getFullDateOfBirth() {
        return fullDateOfBirth;
    }

    public void setFullDateOfBirth(String fullDateOfBirth) {
        this.fullDateOfBirth = fullDateOfBirth;
    }

    public String getNameOfHolder() {
        return nameOfHolder;
    }

    public void setNameOfHolder(String nameOfHolder) {
        this.nameOfHolder = nameOfHolder;
    }

    public List<String> getOtherNames() {
        return otherNames;
    }

    public void setOtherNames(List<String> otherNames) {
        this.otherNames = otherNames;
    }

    public List<String> getOtherValidTDNumbers() {
        return otherValidTDNumbers;
    }

    public void setOtherValidTDNumbers(List<String> otherValidTDNumbers) {
        this.otherValidTDNumbers = otherValidTDNumbers;
    }

    public List<String> getPermanentAddress() {
        return permanentAddress;
    }

    public void setPermanentAddress(List<String> permanentAddress) {
        this.permanentAddress = permanentAddress;
    }

    public String getPersonalNumber() {
        return personalNumber;
    }

    public void setPersonalNumber(String personalNumber) {
        this.personalNumber = personalNumber;
    }

    public String getPersonalSummary() {
        return personalSummary;
    }

    public void setPersonalSummary(String personalSummary) {
        this.personalSummary = personalSummary;
    }

    public List<String> getPlaceOfBirth() {
        return placeOfBirth;
    }

    public void setPlaceOfBirth(List<String> placeOfBirth) {
        this.placeOfBirth = placeOfBirth;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public byte[] getProofOfCitizenship() {
        return proofOfCitizenship;
    }

    public void setProofOfCitizenship(byte[] proofOfCitizenship) {
        this.proofOfCitizenship = proofOfCitizenship;
    }

    public int getTag() {
        return tag;
    }

    public void setTag(int tag) {
        this.tag = tag;
    }

    public List<Integer> getTagPresenceList() {
        return tagPresenceList;
    }

    public void setTagPresenceList(List<Integer> tagPresenceList) {
        this.tagPresenceList = tagPresenceList;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Map<String, Object> toMap() {

        Map<String, Object> result = new HashMap();
        result.put("custodyInformation", custodyInformation);
        result.put("fullDateOfBirth", fullDateOfBirth);
        result.put("nameOfHolder", nameOfHolder);
        result.put("otherNames", otherNames);
        result.put("otherValidTDNumbers", otherValidTDNumbers);
        result.put("permanentAddress", permanentAddress);
        result.put("personalNumber", personalNumber);
        result.put("personalSummary", personalSummary);
        result.put("placeOfBirth", placeOfBirth);
        result.put("profession", profession);
        result.put("proofOfCitizenship", proofOfCitizenship);
        result.put("tag", tag);
        result.put("tagPresenceList", tagPresenceList);
        result.put("telephone", telephone);
        result.put("title", title);

        return result;
    }
}
