package com.jc.jnotesweb.service;

import com.jc.jnotesweb.model.NoteEntry;
import com.jc.jnotesweb.model.Notes;

public interface NotesService {
    
    public static final String VALIDATION_TEXT = "CheckSecret123";
    
    /**
     * 
     * @param userId
     * @param encryptedValidationText
     * @return 0 for success<br>1 for userId already exists<br>2 for failure
     *       
     */
    int setupUser(String userId, String encryptionKey);
    
    String getEncryptedValidationText(String userId);
    
    Notes getAllUserNotes(String userId, String encryptionKey);
    
    Notes getUserNotesForNotebook(String userId, String encryptionKey, String notebook);

    void addNoteEntry(String userId, String encryptionKey, NoteEntry noteEntry);

    void editNoteEntry(String userId, String encryptionKey, NoteEntry noteEntry);

    void deleteNotes(String userId, Notes notes);

    void saveNotes(String userId, String encryptionKey, Notes notes);
    
    void deleteNotebook(String userId, String notebookToBeDeleted);

}
