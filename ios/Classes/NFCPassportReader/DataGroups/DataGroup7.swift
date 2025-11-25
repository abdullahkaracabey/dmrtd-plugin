//
//  DataGroup7.swift
//
//  Created by Andy Qua on 01/02/2021.
//

import Foundation

#if !os(macOS)
import UIKit
#endif

@available(iOS 13, macOS 10.15, *)
public class DataGroup7 : DataGroup {
    
    public private(set) var imageData : [UInt8] = []
    
    required init( _ data : [UInt8] ) throws {
        try super.init(data)
        datagroupType = .DG7
    }
    
#if !os(macOS)
    func getImage() -> UIImage? {
        if imageData.count == 0 {
            return nil
        }
        
        let data = Data(imageData)
        
        // Check if it's JPEG 2000 raw codestream (FF 4F FF 51)
        if data.isJPEG2000Codestream {
            // Use OpenJPEG decoder for raw codestream
            if let image = data.toJPEG2000Image() {
                return image
            }
            // If OpenJPEG fails, fall through to try native decoder
            print("[DataGroup7] OpenJPEG decode failed, trying native decoder")
        }
        
        // Try native decoder (works for JP2 container and standard JPEG)
        let image = UIImage(data: data)
        return image
    }
#endif
    
    
    override func parse(_ data: [UInt8]) throws {
        var tag = try getNextTag()
        if tag != 0x02 {
            throw NFCPassportReaderError.InvalidResponse
        }
        _ = try getNextValue()
        
        tag = try getNextTag()
        if tag != 0x5F43 {
            throw NFCPassportReaderError.InvalidResponse
        }
        
        imageData = try getNextValue()
    }
}
