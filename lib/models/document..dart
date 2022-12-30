import 'package:dmrtd_plugin/models/doc_type.dart';
import 'package:dmrtd_plugin/models/document_details.dart';
import 'package:dmrtd_plugin/models/person_details.dart';
import 'dart:convert';

class Document {
  final DocType docType;
  final DocumentDetails documentDetails;
  final PersonDetails personDetails;

  Document(this.docType, this.documentDetails, this.personDetails);

  Document.fromJson(Map<String, dynamic> data)
      : docType = DocType.values.byName(data["docType"]),
        documentDetails = DocumentDetails.fromJson(
            Map<String, dynamic>.from(data["documentDetails"])),
        personDetails = PersonDetails.fromJson(
            Map<String, dynamic>.from(data["personDetails"]));
}
