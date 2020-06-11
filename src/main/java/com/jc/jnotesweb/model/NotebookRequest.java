package com.jc.jnotesweb.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotebookRequest {
    
    public NotebookRequest() {
        
    }
    
    public NotebookRequest(String notebookToBeDeleted) {
        this.notebookToBeDeleted = notebookToBeDeleted;
    }
    
    public NotebookRequest(String notebookToBeRenamed, String notebookNewName) {
        this.notebookToBeRenamed = notebookToBeRenamed;
        this.notebookNewName = notebookNewName;
    }

    private String notebookToBeDeleted;
    
    private String notebookToBeRenamed;
    private String notebookNewName;

}
