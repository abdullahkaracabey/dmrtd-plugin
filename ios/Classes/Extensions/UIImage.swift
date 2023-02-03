//
//  UIImage.swift
//  dmrtd_plugin
//
//  Created by Abdullah Karacabey on 29.01.2023.
//

extension UIImage {
    
    func toBase64()->String?{
        return self.jpegData(compressionQuality: 1)?.base64EncodedString()
    }
    
    var base64: String? {
        self.jpegData(compressionQuality: 1)?.base64EncodedString()
    }
}
