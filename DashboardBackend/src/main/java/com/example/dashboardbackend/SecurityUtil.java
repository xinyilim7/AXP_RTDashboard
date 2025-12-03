package com.example.dashboardbackend;

import org.springframework.stereotype.Component;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Component
public class SecurityUtil {
    public boolean isValidSignature(String payload, String signatureHeader, String secret){
        if(signatureHeader == null || signatureHeader.isEmpty()){
            return false;
        }
        try {
            // 1. Create HMAC SHA-256
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secret_key);

            // 2. Hash payload
            byte[] hashBytes = sha256_HMAC.doFinal(payload.getBytes(StandardCharsets.UTF_8));

            // 3. Convert to hex string
            StringBuilder hexString = new StringBuilder();
            for(byte b : hashBytes){
                String hex = Integer.toHexString(0xff & b);
                if(hex.length()==1) hexString.append('0');
                hexString.append(hex);
            }
            String expectedSignature = hexString.toString();
            return expectedSignature.equals(signatureHeader);
        }catch (NoSuchAlgorithmException | InvalidKeyException e){
            e.printStackTrace();
            return false;
        }
    }
}
