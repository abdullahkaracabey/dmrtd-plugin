class DocumentDetails {
  final String? name;
  final String? surname;
  final String? personalNumber;
  final String? gender;
  final String? birthDate;
  final String? issueDate;
  final String? expiryDate;
  final String? serialNumber;
  final String? nationality;
  final String? issuerAuthority;
  final String? faceImageBase64;
  final String? portraitImageBase64;
  final String? signatureBase64;

  DocumentDetails(
      this.name,
      this.surname,
      this.personalNumber,
      this.gender,
      this.birthDate,
      this.issueDate,
      this.expiryDate,
      this.serialNumber,
      this.nationality,
      this.issuerAuthority,
      this.faceImageBase64,
      this.portraitImageBase64,
      this.signatureBase64);

  DocumentDetails.fromJson(Map<String, dynamic> data)
      : name = data["name"],
        surname = data["surname"],
        personalNumber = data["personalNumber"],
        gender = data["gender"],
        birthDate = data["birthDate"],
        issueDate = data["issueDate"],
        expiryDate = data["expiryDate"],
        serialNumber = data["serialNumber"],
        nationality = data["nationality"],
        issuerAuthority = data["issuerAuthority"],
        faceImageBase64 = data["faceImageBase64"] != null
            ? data["faceImageBase64"].replaceAll(RegExp(r'\s+'), '')
            : null,
        portraitImageBase64 = data["portraitImageBase64"] != null
            ? data["portraitImageBase64"].replaceAll(RegExp(r'\s+'), '')
            : null,
        signatureBase64 = data["signatureBase64"] != null
            ? data["signatureBase64"].replaceAll(RegExp(r'\s+'), '')
            : null;
}
