//
//  Date.swift
//  dmrtd_plugin
//
//  Created by Abdullah Karacabey on 29.01.2023.
//




extension Date {
    func toString(format: String = "yyMMdd") -> String {
        let formatter = DateFormatter()
        formatter.dateStyle = .short
        formatter.dateFormat = format
        return formatter.string(from: self)
    }
    
    func fromString(format:String="yyMMdd", str:String)->Date? {
        let dateFormatter = DateFormatter()

        dateFormatter.dateFormat = format

       return  dateFormatter.date(from: str)
    }
}
