package com.snapmint.checkoutsdk.utils;

import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Helper1 {

    static String TAG = "Helper.java";

    public static String validateChecksum(String status, String message) {

        String checkSum = "";
        String checkSumString = "";
//        Log.v(TAG, "Message Received : " + message);
//        message = "b56e0b7dbc40608e9821a9f11aa5a47e78381afde37f61870451620";//For Testing Invalid
//        merchant_key|order_id|order_value|full_name|email|token
        if (status.equalsIgnoreCase("success"))
            checkSumString = "VFmRKCgU|success|1|2000|GIRIDHAR m MAMIDIPALLY|rahul@snapmint.com|y9A2reJs";
        else
            checkSumString = "VFmRKCgU|failure|1|20000|GIRIDHAR m MAMIDIPALLY|rahul@snapmint.com|y9A2reJs";

        String generatedCheckSum = generateCheckSum(checkSumString);

        if (generatedCheckSum.equals(message)) {
            checkSum = "Valid Checksum";
        } else {
            checkSum = "Invalid Checksum";
        }
        return checkSum;
    }

    public static String generateCheckSum(String checkSumString) {

        Log.d("STR",checkSumString);

        String generatedCheckSum = "";
        MessageDigest messageDigest = null;

        try {
            messageDigest = MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        byte[] digest = messageDigest.digest(checkSumString.getBytes());
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < digest.length; i++) {
            stringBuilder.append(Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1));
        }

//        Log.v(TAG, "Created Checksum : " + stringBuilder);
        return stringBuilder.toString();
    }
}
