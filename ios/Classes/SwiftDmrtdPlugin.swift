import Flutter
import UIKit

import QKMRZParser

public class SwiftDmrtdPlugin: NSObject, FlutterPlugin {
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "dmrtd_plugin", binaryMessenger: registrar.messenger())
    let instance = SwiftDmrtdPlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {

    if (call.method == "getPlatformVersion") {
      result("iOS " + UIDevice.current.systemVersion)
    }else if( call.method=="read"){
        
        let mrzData = call.arguments as! String
        
        Task{
          await readPassPort(mrzData:mrzData, result: result)
        }
         
        
    
    }
      else{
          result("not-implemented")
      }


  }

    private func handleProgress(percentualProgress: Int) -> String {
        let p = (percentualProgress/20)
        let full = String(repeating: "🟢 ", count: p)
        let empty = String(repeating: "⚪️ ", count: 5-p)
        return "\(full)\(empty)"
    }
    
    
    private func readPassPort(mrzData : String, result: @escaping FlutterResult) async{
        
        do{
            
            var mrzLines=[String]()
            let chars = Array(mrzData)
            if(mrzData.starts(with: "P")){
                let firstLine =  mrzData.prefix(mrzData.count/2)
                let secondLine =  mrzData.suffix(mrzData.count/2)
                mrzLines.append(String(firstLine))
                mrzLines.append(String(secondLine))
            }
            
            let mrzParser = QKMRZParser(ocrCorrection: true)
            let mrzResult = mrzParser.parse(mrzLines: mrzLines)
            
        
            let passportUtil = PassportUtils()
          
            let mrzKey = passportUtil.getMRZKey(passportNumber: mrzResult!.documentNumber, dateOfBirth: mrzResult!.birthdate!.toString(), dateOfExpiry: mrzResult!.expiryDate!.toString())
            
            let passportReader = PassportReader()
            
            let response = try await  passportReader.readPassport(mrzKey: mrzKey, customDisplayMessage: { (displayMessage) in
                switch displayMessage {
                    case .requestPresentPassport:
                        return "Cihazı belgeye yakınlıaştırın"
                   case .successfulRead:
                    return "Okuma tammalandı"
                case .error(let error):
                    if(error.value ==  NFCPassportReaderError.ConnectionError.value){
                        return "Bağlantı hatası"
                    }
                       return "Bilinmeyen bir hata oluştu"
                   case .readingDataGroupProgress(let dataGroup, let progress):
                   let progressString = self.handleProgress(percentualProgress: progress)
                       return "Belge okunuyor...\n\(progressString)"
                   default:
                       return nil
               }
           })
            let image = response.passportImage?.jpegData(compressionQuality: 1)?.base64EncodedString()
            let documentDetails:[String : Any?]  = [
                "name":response.firstName,
                "surname":response.lastName,
                "mrzContent":response.passportMRZ,
                "personalNumber":response.personalNumber,
                "gender":response.gender,
                "birthDate": Date().fromString(str:response.dateOfBirth)?.toString(format: "yyyyMMdd"),
                "issueDate":response.dateOfIssue,
                "expiryDate":Date().fromString(str:response.documentExpiryDate)?.toString(format: "yyyyMMdd"),
                "serialNumber": response.documentSigningCertificate?.getSerialNumber(),
                "nationality":response.nationality,
                "issuerAuthority":response.issuerAuthority,
                "faceImageBase64": image
              //"portraitImageBase64":"portraitImageBase64",
              //"signatureBase64":"signatureBase64"
            ]
            let document:[String : Any?] = [
                "docType": mrzResult!.documentType == "P" ? "passport" : (mrzResult!.documentType == "I" ? "idCard" : "other"),
                "documentDetails":documentDetails,
                "personDetails":[
                    //"custodyInformation":,
                    "fullDateOfBirth":Date().fromString(str:response.dateOfBirth)?.toString(format: "yyyyMMdd"),
                    "nameOfHolder":response.firstName+"<<"+response.lastName,
                  //"otherNames":"otherNames",
                  //"otherValidTDNumbers":"otherValidTDNumbers",
                    "permanentAddress":response.residenceAddress != nil ? [response.residenceAddress] : [],
                    "personalNumber":response.personalNumber,
                  //"personalSummary":"personalSummary",
                    "placeOfBirth": response.placeOfBirth != nil ? [response.placeOfBirth] : [],
                    //"profession":,
                    //"proofOfCitizenship":,
                  //"tag":"tag",
                  //"tagPresenceList":"tagPresenceList",
                  //"telephone":"telephone",
                    //"title":
                ],

                "error": mrzResult!.documentType
                
            ]
           
            DispatchQueue.main.async {  result(document) }
        }
        catch{
            
            DispatchQueue.main.async {  result(["error":  "\(error)"]) }
        }
        
       
        
       
    }
}
