package com.jc.jnotesweb.model;

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

    @Override
    public String toString() {
        return "NotebookRequest{" +
                "notebookToBeDeleted='" + notebookToBeDeleted + '\'' +
                ", notebookToBeRenamed='" + notebookToBeRenamed + '\'' +
                ", notebookNewName='" + notebookNewName + '\'' +
                '}';
    }

    public String getNotebookToBeDeleted() {
        return notebookToBeDeleted;
    }

    public void setNotebookToBeDeleted(String notebookToBeDeleted) {
        this.notebookToBeDeleted = notebookToBeDeleted;
    }

    public String getNotebookToBeRenamed() {
        return notebookToBeRenamed;
    }

    public void setNotebookToBeRenamed(String notebookToBeRenamed) {
        this.notebookToBeRenamed = notebookToBeRenamed;
    }

    public String getNotebookNewName() {
        return notebookNewName;
    }

    public void setNotebookNewName(String notebookNewName) {
        this.notebookNewName = notebookNewName;
    }

    private String notebookNewName;

}
