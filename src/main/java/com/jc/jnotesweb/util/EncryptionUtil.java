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

    public EncryptionUtil() {
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
        String encryptedVal = encryptionUtil.locallyEncrypt("9L2I3EPsO9fcv7tXL2gBPopcCJE8yz2tBvZ3dfIqUqAAskmwODw8oYcrCc1QkPAPk3I4X9uLBSTdJCzC6rXb4w==");
        System.out.println("encryptedVal " + encryptedVal);

        String decryptedVal = encryptionUtil.locallyDecrypt("184190791a019bd743c7dfaf7c7fb52e5174cbe178caee4f013f74c2dda2efcdb33aeaec1d444fdf5afc801750c63bb02abc83d11a40e059e987d15cae1134e3e62dc83885d8ca600d61e92300821a8c1ac061585dafddb1032ca6da3a4e054de94fd49d4448eda2294a0391a9fb0599");
        System.out.println("decryptedVal " + decryptedVal);
        
    }

}
