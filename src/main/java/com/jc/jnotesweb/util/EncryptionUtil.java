/*
 * This file is part of JNotes. Copyright (C) 2020  Joy Chakravarty
 * 
 * JNotes is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JNotes is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JNotes.  If not, see <https://www.gnu.org/licenses/>.
 * 
 * 
 */
package com.jc.jnotesweb.util;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/*
 * This class has encrypt/decrypt methods using Spring security. 
 * 
 */
@Slf4j
@Component
public class EncryptionUtil {
    
    public static final String ENCRYPTION_SALT = "5c0744940b5c369b";
    private static final String ENCRYPTION_SECRET = "ENCRYPTION_SECRET";
    
    private String encryptionSecret = null;

    @PostConstruct
    public void readEncyptionSeret(){
        this.encryptionSecret = System.getProperty(ENCRYPTION_SECRET);
        if(this.encryptionSecret == null) {
            this.encryptionSecret = System.getenv(ENCRYPTION_SECRET);
            log.info("trying encryptionSecret from env: "+encryptionSecret);
        }
        log.info("encryptionSecret : "+encryptionSecret);
    }

    public String locallyEncrypt(String textToEncrypt) {
        return encrypt(encryptionSecret, textToEncrypt);
    }

    public String locallyDecrypt(String textToDecrypt) {
        return decrypt(encryptionSecret, textToDecrypt);
    }

    public String encrypt(String encryptionKey, String textToEncrypt) {
        if (StringUtils.isBlank(textToEncrypt)) {
            return null;
        }
        if(StringUtils.isBlank(encryptionKey)) {
            return textToEncrypt;
        }
        TextEncryptor encryptor = Encryptors.text(encryptionKey, ENCRYPTION_SALT);
        return encryptor.encrypt(textToEncrypt);
    }

    public String decrypt(String encryptionKey, String textToDecrypt) {
        if (StringUtils.isBlank(textToDecrypt)) {
            return null;
        }
        if(StringUtils.isBlank(encryptionKey)) {
            return textToDecrypt;
        }
        TextEncryptor encryptor = Encryptors.text(encryptionKey, ENCRYPTION_SALT);
        return encryptor.decrypt(textToDecrypt);
    }

    public static void main(String[] args) {
        EncryptionUtil encryptionUtil = new EncryptionUtil();
        String encryptedVal = encryptionUtil.locallyEncrypt("Testing");
        System.out.println("encryptedVal " + encryptedVal);

        String decryptedVal = encryptionUtil.locallyDecrypt("6ad8866fb76c2b1285a1393ae5f44582879cd23f397afb0b69f78b788c3638ad");
        System.out.println("decryptedVal " + decryptedVal);
        
    }

}
