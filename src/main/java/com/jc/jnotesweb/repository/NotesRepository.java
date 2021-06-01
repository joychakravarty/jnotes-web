package com.jc.jnotesweb.repository;

import java.util.List;

import com.datastax.oss.driver.api.core.cql.Row;
import com.jc.jnotesweb.model.NoteEntry;
import com.jc.jnotesweb.model.Notes;

public interface NotesRepository {

    public static final String VALIDATION_NOTEBOOK = "VALIDATION_NOTEBOOK";

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