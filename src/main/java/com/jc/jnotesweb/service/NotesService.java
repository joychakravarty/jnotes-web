package com.jc.jnotesweb.service;

import com.jc.jnotesweb.model.NoteEntry;
import com.jc.jnotesweb.model.Notes;

public interface NotesService {
    
    boolean setupUser(String userId, String encryptedValidationText);
    
    Notes getAllUserNotes(String userId);
    
    Notes getUserNotesForNotebook(String userId, String notebook);

    void addNoteEntry(String userId, NoteEntry noteEntry);

    void editNoteEntry(String userId, NoteEntry noteEntry);

    void deleteNotes(String userId, Notes notes);

    boolean saveNotes(String userId, Notes notes);
    
    void deleteNotebook(String userId, String notebookToBeDeleted);

    String getEncryptedValidationText(String userId);

}
