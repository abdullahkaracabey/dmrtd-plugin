import 'package:dmrtd_plugin/models/document..dart';

class Result {
  final bool isSuccess;
  final String? code;
  final String? message;
  final Document? document;

  Result({required this.isSuccess, this.code, this.message, this.document});
}
