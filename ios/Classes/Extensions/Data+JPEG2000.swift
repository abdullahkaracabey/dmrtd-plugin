//
//  Data+JPEG2000.swift
//  dmrtd_plugin
//
//  Convenience extension for JPEG 2000 decoding
//

import Foundation
import UIKit

extension Data {
    
    /// Checks if data is a JPEG 2000 codestream (starts with FF 4F FF 51)
    var isJPEG2000Codestream: Bool {
        return OpenJPEGDecoder.isJPEG2000Codestream(self)
    }
    
    /// Decodes JPEG 2000 codestream to UIImage using OpenJPEG
    /// - Returns: Decoded UIImage or nil if decoding fails
    func toJPEG2000Image() -> UIImage? {
        return OpenJPEGDecoder.decodeJPEG2000Codestream(self)
    }
    
}
