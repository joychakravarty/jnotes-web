package com.jc.jnotesweb.repository;

import com.datastax.driver.core.Row;
import com.jc.jnotesweb.model.NoteEntry;

import java.util.List;

public interface NotesRepository {

    void createUserTable(String userId);
    void insertValidationTextForUser(String userId, String encrypt);
    String getValidationTextForUser(String userId);
    void dropUserTable(String userId);

    List<Row> getAllUserNotes(String userId);

    List<Row> getUserNotesForNotebook(String userId, String notebook);

    void insertNote(NoteEntry noteEntry);
    void updateNote(NoteEntry noteEntry);

    void deleteNotes(String userId, List<String> noteIds);
    void deleteNotebook(String userId, String notebookToBeDeleted);
}
