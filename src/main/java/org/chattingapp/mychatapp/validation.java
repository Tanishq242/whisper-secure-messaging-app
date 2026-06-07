package org.chattingapp.mychatapp;

import java.util.regex.Pattern;

public class validation {
    static String emptyMsg = "Name should not empty";
    static String numberMsg = "Name should not start with number";
    static String whiteSpaceMsg = "Space is not allowed";
    static String lengthMsg = "one character name is not allowed";
    static String specialCharMsg = "Special character is not allowed except @";
    static String twolengthStringMsg = "This type of name is not allowed";
    static String emojiMsg = "emoji is not allowed in name";
    static String mobileLengthMsg = "Mobile no. should be 10-digit";
    static String passwordLengthMsg = "Password must be 8 characters";
    static String passwordNotSameMsg = "Password is not same";
    static Pattern specialCharPattern = Pattern.compile("[!\"#$%&'()*+,\\\\\\-./:;<=>?\\[\\\\\\]^_`{|}~]");
    static String returnMsg = "";

    public static void nameValidation(String str) {
        boolean hasEmoji = str.codePoints()
                .anyMatch(cp -> (cp >= 0x1F600 && cp <= 0x1F64F) || // Emoticons
                        (cp >= 0x1F300 && cp <= 0x1F5FF) || // Misc Symbols & Pictographs
                        (cp >= 0x1F680 && cp <= 0x1F6FF) || // Transport & Map
                        (cp >= 0x2600 && cp <= 0x26FF) || // Misc symbols
                        (cp >= 0x2700 && cp <= 0x27BF) || // Dingbats
                        (cp >= 0x1F900 && cp <= 0x1F9FF) || // Supplemental Symbols & Pictographs
                        (cp >= 0x1FA70 && cp <= 0x1FAFF) // Symbols & Pictographs Extended-A
                );

        if (str.isEmpty()) {
            returnMsg = returnMsg + emptyMsg + "\n";
        }

        if (str.length() == 1) {
            returnMsg = returnMsg + lengthMsg + "\n";
        }

        if (str.matches("^\\d.*")) {
            returnMsg = returnMsg + numberMsg + "\n";
        }

        if (str.contains(" ")) {
            returnMsg = returnMsg + whiteSpaceMsg + "\n";
        }

        if (specialCharPattern.matcher(str).find()) {
            returnMsg = returnMsg + specialCharMsg + "\n";
        }

        if (str.matches("^*.[0-9].*") && str.length() == 2) {
            returnMsg = returnMsg + twolengthStringMsg + "\n";
        }

        if (hasEmoji) {
            returnMsg = returnMsg + emojiMsg + "\n";
        }
    }

    public static void mobileValidation(String str) {
        if (str.length() < 10) {
            returnMsg = returnMsg + mobileLengthMsg + "\n";
        }
    }

    public static void passwordValidation(String str1, String str2) {
        if (str1.length() < 8) {
            returnMsg = returnMsg + passwordLengthMsg + "\n";
        }

        if (!str1.equals(str2)) {
            returnMsg = returnMsg + passwordNotSameMsg + "\n";
        }
    }

    public static String getValidation(int validateCode, String ...arr) {
        if (validateCode == 1) {
            mobileValidation(arr[0]);
            passwordValidation(arr[1], arr[1]);
        } else {
            nameValidation(arr[0]);
            mobileValidation(arr[1]);
            passwordValidation(arr[2], arr[3]);
        }

        if (!returnMsg.isEmpty()) {
            return returnMsg;
        }

        return null;
    }

    public static String otpValidation(String responseCode) {
        return switch (responseCode) {
            case "200" -> "True";
            case "400" -> "Bad request";
            case "500" -> "Server Error";
            default -> "Error code: " + responseCode;
        };
    }

    public static String verifyOTP(String responseCode) {
        return switch (responseCode) {
            case "200" -> "True";
            case "702" -> "Wrong OTP provided";
            case "705" -> "OTP verification expired, Try again";
            case "400" -> "Bad Request";
            case "500" -> "Server Error";
            default -> "Error code: " + responseCode;
        };
    }
}