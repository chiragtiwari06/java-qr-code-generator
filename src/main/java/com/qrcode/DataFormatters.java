package com.qrcode;

public class DataFormatters {

    // Formats Wi-Fi credentials into standard QR auto-connect syntax
    public static String formatWifi(String ssid, String password, String encryption) {
        return String.format("WIFI:S:%s;T:%s;P:%s;;", ssid, encryption, password);
    }

    // Formats contact info into standard vCard 3.0 syntax
    public static String formatVCard(String name, String phone, String email, String company) {
        return "BEGIN:VCARD\n" +
               "VERSION:3.0\n" +
               "N:" + name + "\n" +
               "TEL:" + phone + "\n" +
               "EMAIL:" + email + "\n" +
               "ORG:" + company + "\n" +
               "END:VCARD";
    }
}