class DocumentReadException implements Exception {
  String code;
  String message;

  DocumentReadException({required this.code, required this.message});
}
