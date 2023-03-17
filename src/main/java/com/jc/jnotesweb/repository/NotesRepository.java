package com.jc.jnotesweb.repository;

import com.datastax.oss.driver.api.core.cql.Row;
import com.jc.jnotesweb.model.NoteEntry;
import com.jc.jnotesweb.model.Notes;

import java.util.List;

public interface NotesRepository {

    String VALIDATION_NOTEBOOK = "VALIDATION_NOTEBOOK";

    String COLUMN_NOTEBOOK = "notebook";
    String COLUMN_KEY = "key";
    String COLUMN_VALUE = "value";
    String COLUMN_INFO = "info";
    String COLUMN_IS_PASSWORD = "isPassword";
    String COLUMN_LAST_MODIFIED_TIME = "lastModifiedTime";
    String COLUMN_ENC_VALIDATION_TXT = "encrypted_validation_text";
    String COLUMN_NOTEID = "noteid";

    String EXECUTION_PROFILE_NAME = "jnotes";

    void createUserTable(String userId);

    void insertValidationTextForUser(String userId, String encryptedValidationText);

    String getValidationTextForUser(String userId);

    void dropUserTable(String userId);

    List<Row> getAllUserNotes(String userId);

    List<Row> getUserNotesForNotebook(String userId, String notebook);

    void insertNote(String userId, String encryptionKey, NoteEntry noteEntry);

    void updateNote(String userId, String encryptionKey, NoteEntry noteEntry);

    void deleteNotes(String userId, String notebook, List<String> noteIds);

    void deleteNotebook(String userId, String notebookToBeDeleted);

    void saveNotes(String userId, String encryptionKey, Notes notes);
}