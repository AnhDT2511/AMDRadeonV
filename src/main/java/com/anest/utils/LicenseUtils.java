/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.anest.utils;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

/**
 *
 * @author Shado
 */
public class LicenseUtils {
    
    final String STR_CONSTANT = "nguoiquaduong25111211";
    final String STR_IS_CHECK = "2511";
    
    private String getMacAddress() {
        try {
            NetworkInterface network = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
            byte[] mac = network.getHardwareAddress();

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
            }

            return sb.toString();
        } catch (SocketException | java.net.UnknownHostException ex) {
            ex.printStackTrace(System.out);
        }
        return null;
    }
    
    private String encryptMD5(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(text.getBytes());
            BigInteger number = new BigInteger(1, messageDigest);
            String hashtext = number.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public String readFile(String fileName) throws IOException {
        String code = "";
        Path path = Paths.get(fileName);
        try {
            Scanner scanner = new Scanner(path);
            while (scanner.hasNextLine()) {
                code = scanner.nextLine();
            }
            return code.trim();
        } catch (IOException ex) {
            throw new IOException();
        }
    }

    public boolean isCheckLicense(String license) {
        LicenseUtils licenseUtils = new LicenseUtils();
        String macAddress = licenseUtils.getMacAddress();
        String md5 = licenseUtils.encryptMD5(macAddress + STR_CONSTANT);

        if (license.startsWith(STR_IS_CHECK)) {
            return md5.equalsIgnoreCase(license.substring(4));
        }
        return false;
    }

}
