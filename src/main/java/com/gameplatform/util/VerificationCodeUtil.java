package com.gameplatform.util;

import org.springframework.stereotype.Component;
import java.security.SecureRandom;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2025/01/06 20:46
 * @description TODO
 */
@Component
public class VerificationCodeUtil {
    private static final String CHARACTERS = "0123456789";
    private static final int CODE_LENGTH = 6;
    private final SecureRandom random = new SecureRandom();

    public String generateCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return code.toString();
    }

    public boolean verifyCode(String storedCode, String inputCode) {
        if (storedCode == null || inputCode == null) {
            return false;
        }
        return storedCode.equals(inputCode);
    }
}