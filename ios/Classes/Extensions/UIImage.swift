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
    
    func scalePreservingAspectRatio(targetSize: CGSize) -> UIImage {
        // Determine the scale factor that preserves aspect ratio
        let widthRatio = targetSize.width / size.width
        let heightRatio = targetSize.height / size.height
        
        let scaleFactor = min(widthRatio, heightRatio)
        
        // Compute the new image size that preserves aspect ratio
        let scaledImageSize = CGSize(
            width: size.width * scaleFactor,
            height: size.height * scaleFactor
        )

        // Draw and return the resized UIImage
        let renderer = UIGraphicsImageRenderer(
            size: scaledImageSize
        )

        let scaledImage = renderer.image { _ in
            self.draw(in: CGRect(
                origin: .zero,
                size: scaledImageSize
            ))
        }
        
        return scaledImage
    }
    
    func scale(targetSize: CGSize) -> UIImage {
        
        // Draw and return the resized UIImage
        let renderer = UIGraphicsImageRenderer(
            size: targetSize
        )

        let scaledImage = renderer.image { _ in
            self.draw(in: CGRect(
                origin: .zero,
                size: targetSize
            ))
        }
        
        return scaledImage
    }
}

