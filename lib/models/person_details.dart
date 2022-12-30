import 'dart:core';
import 'dart:typed_data';

class PersonDetails {
  final String? custodyInformation;
  final String? fullDateOfBirth;
  final String? nameOfHolder;
  final List<String>? otherNames;
  final List<String>? otherValidTDNumbers;
  final List<String>? permanentAddress;
  final String? personalNumber;
  final String? personalSummary;
  final List<String>? placeOfBirth;
  final String? profession;
  final Uint8List? proofOfCitizenship;
  final int? tag;
  final List<int>? tagPresenceList;
  final String? telephone;
  final String? title;

  PersonDetails(
      this.custodyInformation,
      this.fullDateOfBirth,
      this.nameOfHolder,
      this.otherNames,
      this.otherValidTDNumbers,
      this.permanentAddress,
      this.personalNumber,
      this.personalSummary,
      this.placeOfBirth,
      this.profession,
      this.proofOfCitizenship,
      this.tag,
      this.tagPresenceList,
      this.telephone,
      this.title);

  PersonDetails.fromJson(Map<String, dynamic> data)
      : custodyInformation = data["custodyInformation"],
        fullDateOfBirth = data["fullDateOfBirth"],
        nameOfHolder = data["nameOfHolder"],
        otherNames = 
            (data["otherNames"] as List?)?.map((e) => e as String).toList(),
        otherValidTDNumbers = (data["otherValidTDNumbers"] as List?)
            ?.map((e) => e as String)
            .toList(),
        permanentAddress = (data["permanentAddress"] as List?)
            ?.map((e) => e as String)
            .toList(),
        placeOfBirth =
            (data["placeOfBirth"] as List?)?.map((e) => e as String).toList(),
        personalNumber = data["personalNumber"],
        personalSummary = data["personalSummary"],
        profession = data["profession"],
        proofOfCitizenship = data["proofOfCitizenship"],
        tag = data["tag"],
        tagPresenceList =(data["tagPresenceList"] as List?)
            ?.map((e) => e as int)
            .toList(),
        telephone = data["telephone"],
        title = data["title"];
}
