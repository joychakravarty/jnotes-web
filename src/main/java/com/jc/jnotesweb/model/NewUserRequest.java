package com.jc.jnotesweb.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewUserRequest {
    
    public NewUserRequest() {
        
    }
    
    public NewUserRequest(String userId, String userSecret) {
        this.userId = userId;
        this.userSecret = userSecret;
    }

    private String userId;
    private String userSecret;

}
