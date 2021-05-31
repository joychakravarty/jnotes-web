package com.jc.jnotesweb.repository;


import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.jc.jnotesweb.model.NoteEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CassandraNotesRepository implements NotesRepository {
    private static final Logger log = LoggerFactory.getLogger(CassandraNotesRepository.class);

    @Autowired
    @Qualifier("keyspace")
    private String keyspace;

    @Autowired
    private Session session;

    private static final String GET_ALL_NOTES_FOR_NOTEBOOK = "SELECT * FROM %s.%s WHERE notebook = ?";
    private static final String GET_ALL_NOTES_FOR_USER = "SELECT * FROM %s.%s";
    private static final String ADD_NOTE_ENTRY = "INSERT INTO %s.%s (notebook, noteid, key, value, info, isPassword, lastModifiedTime) VALUES (?,?,?,?,?,?,?)";
    private static final String EDIT_NOTE_ENTRY = "UPDATE %s.%s SET key = ?, value = ?, info = ?, isPassword = ?, lastModifiedTime = ? where notebook = ? and noteid = ?";
    private static final String DELETE_NOTE_ENTRIES = "DELETE FROM %s.%s WHERE notebook = ? and noteid IN ?";

    private static final String DELETE_NOTEBOOK = "DELETE FROM %s.%s WHERE notebook = ?";

    private static final String ADD_USERSCRET_VALIDATION_ROW = "INSERT INTO %s.%s (notebook, encrypted_validation_text) VALUES (?, ?)";
    private static final String GET_ENCRYPTED_VALIDATION_TEXT = "SELECT encrypted_validation_text FROM %s.%s WHERE notebook = ?";


    @Override
    public void createUserTable(String userId) {
        final String query = "CREATE TABLE jnotes.%s (notebook text, noteid text, key text, value text, info text, isPassword boolean, lastModifiedTime timestamp, encrypted_validation_text text STATIC, PRIMARY KEY (notebook, noteid))";
        session.execute(String.format(query, userId));
    }

    @Override
    public void insertValidationTextForUser(String userId, String encrypt) {

    }

    @Override
    public String getValidationTextForUser(String userId) {
        return null;
    }

    @Override
    public void dropUserTable(String userId) {

    }

    @Override
    public List<Row> getAllUserNotes(String userId) {
        return null;
    }

    @Override
    public List<Row> getUserNotesForNotebook(String userId, String notebook) {
        return null;
    }

    @Override
    public void insertNote(NoteEntry noteEntry) {

    }

    @Override
    public void updateNote(NoteEntry noteEntry) {

    }

    @Override
    public void deleteNotes(String userId, List<String> noteIds) {

    }

    @Override
    public void deleteNotebook(String userId, String notebookToBeDeleted) {

    }
}
