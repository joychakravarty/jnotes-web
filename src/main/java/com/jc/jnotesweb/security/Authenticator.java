package com.jc.jnotesweb.security;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jc.jnotesweb.service.NotesService;
import com.jc.jnotesweb.util.EncryptionUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class Authenticator {

    private static final String AUTH_TYPE = "JNOTES";

    @Autowired
    private NotesService service;

    @Autowired
    private EncryptionUtil encryptionUtil;

    private final ConcurrentMap<String, String> userCache = new ConcurrentHashMap<>();

    public AuthResponse evaluateAuthHeader(String authReqHeader) {
        if (StringUtils.isBlank(authReqHeader)) {
            return AuthResponse.INVALID_HEADER_AUTH_RESPONSE;
        } else {
            String[] authParts = authReqHeader.split("\\s+");
            if (!AUTH_TYPE.equals(authParts[0])) {
                return AuthResponse.INVALID_HEADER_AUTH_RESPONSE;
            }
            String authInfo = authParts[1];
            if (StringUtils.isBlank(authInfo)) {
                return AuthResponse.INVALID_HEADER_AUTH_RESPONSE;
            }
            String credentials = new String(Base64.decodeBase64(authInfo));
            String[] credArr = credentials.split(":");
            if (credArr.length != 2) {
                return AuthResponse.INVALID_HEADER_AUTH_RESPONSE;
            }
            String userId = credArr[0];
            String userSecret = credArr[1];
            userCache.computeIfAbsent(userId, (k) -> service.getEncryptedValidationText(k));
            String encryptedValidationText = userCache.get(userId);
            if (encryptedValidationText == null) {
                return AuthResponse.INVALID_USER_AUTH_RESPONSE;
            } else {
                try {
                    String decryptedValidationText = encryptionUtil.decrypt(userSecret, encryptedValidationText);
                    if (NotesService.VALIDATION_TEXT.equals(decryptedValidationText)) {
                        log.info("User Authentication successful | userId:" + userId);
                        return new AuthResponse(userId, userSecret);
                    } else {
                        return AuthResponse.INVALID_SECRET_AUTH_RESPONSE;
                    }
                } catch (Exception ex) {//In case secret is incorrect we get something like BadPaddingException
                    return AuthResponse.INVALID_SECRET_AUTH_RESPONSE;
                }
            }
        }
    }

}
