package com.jc.jnotesweb.repository;

import static com.datastax.oss.driver.api.querybuilder.SchemaBuilder.createTable;

import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.servererrors.InvalidQueryException;
import com.datastax.oss.driver.api.core.type.DataTypes;
import com.datastax.oss.driver.api.querybuilder.schema.CreateTable;
import com.jc.jnotesweb.model.NoteEntry;
import com.jc.jnotesweb.model.Notes;
import com.jc.jnotesweb.util.EncryptionUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@Repository
public class CassandraNotesRepository implements NotesRepository {
    
    private static final String GET_ALL_NOTES_FOR_NOTEBOOK = "SELECT * FROM %s.%s WHERE notebook = ?";
    private static final String GET_ALL_NOTES_FOR_USER = "SELECT * FROM %s.%s";
    private static final String ADD_NOTE_ENTRY = "INSERT INTO %s.%s (notebook, noteid, key, value, info, isPassword, lastModifiedTime) VALUES (?,?,?,?,?,?,?)";
    private static final String EDIT_NOTE_ENTRY = "UPDATE %s.%s SET key = ?, value = ?, info = ?, isPassword = ?, lastModifiedTime = ? where notebook = ? and noteid = ?";
    private static final String DELETE_NOTE_ENTRIES = "DELETE FROM %s.%s WHERE notebook = ? and noteid IN ?";

    private static final String DELETE_NOTEBOOK = "DELETE FROM %s.%s WHERE notebook = ?";

    private static final String ADD_USERSCRET_VALIDATION_ROW = "INSERT INTO %s.%s (notebook, encrypted_validation_text) VALUES (?, ?)";
    private static final String GET_ENCRYPTED_VALIDATION_TEXT = "SELECT encrypted_validation_text FROM %s.%s WHERE notebook = ?";

    @Autowired
    private EncryptionUtil encryptionUtil;

    @Autowired
    @Qualifier("keyspace")
    private String keyspace;
    
    @Autowired 
    private CqlSession session;

    @Override
    public void createUserTable(String userId) {
        CreateTable createUserTable = createTable(keyspace, userId).withPartitionKey("notebook", DataTypes.TEXT)
                    .withClusteringColumn("noteid", DataTypes.TEXT).withColumn("key", DataTypes.TEXT).withColumn("value", DataTypes.TEXT)
                    .withColumn("info", DataTypes.TEXT).withColumn("isPassword", DataTypes.BOOLEAN)
                    .withColumn("lastModifiedTime", DataTypes.TIMESTAMP).withStaticColumn("encrypted_validation_text", DataTypes.TEXT);
        session.execute(createUserTable.build());
        log.info("Cassandra: User table created: {}", userId);
    }

    @Override
    public void insertValidationTextForUser(String userId, String encryptedValidationText) {
        session.execute(SimpleStatement.builder(String.format(ADD_USERSCRET_VALIDATION_ROW, keyspace, userId))
                        .addPositionalValues(VALIDATION_NOTEBOOK, encryptedValidationText).build());
        log.info("Cassandra: Validation Text added to User table: {}", userId);
    }

    @Override
    public String getValidationTextForUser(String userId) {
        String encryptedValidationText = null;
        try {
            ResultSet results = session
                    .execute(SimpleStatement.builder(String.format(GET_ENCRYPTED_VALIDATION_TEXT, keyspace, userId))
                            .addPositionalValues(VALIDATION_NOTEBOOK).build());
            Row row = results.one();
            encryptedValidationText = row.getString("encrypted_validation_text");
        } catch (Exception ex) {
            log.error("Exception in getEncryptedValidationText (can't do much here) : ", ex);
            return null;
        }
        return encryptedValidationText;
    }

    @Override
    public void dropUserTable(String userId) {
        session.execute(SimpleStatement.builder(String.format("DROP TABLE IF EXISTS %s.%s", keyspace, userId)).build());
        log.info("Cassandra: User table dropped: {}", userId);
    }

    @Override
    public List<Row> getAllUserNotes(String userId) {
        String cqlStr = String.format(GET_ALL_NOTES_FOR_USER, keyspace, userId);
        ResultSet results;
        try {
            results = session.execute(SimpleStatement.builder(cqlStr).build());
        } catch (InvalidQueryException ex) {
            log.error("getAllUserNotes: Caught InvalidQueryException", ex);
            return null;
        }
        return results.all();
    }

    @Override
    public List<Row> getUserNotesForNotebook(String userId, String notebook) {
        String cqlStr = String.format(GET_ALL_NOTES_FOR_NOTEBOOK, keyspace, userId);
        ResultSet results;
        try {
            results = session.execute(SimpleStatement.builder(cqlStr).addPositionalValues(notebook).build());
        } catch (InvalidQueryException ex) {
            log.error("getUserNotesForNotebook : Caught InvalidQueryException", ex);
            return null;
        }
        return results.all();
    }

    @Override
    public void insertNote(String userId, String encryptionKey, NoteEntry noteEntry) {
        String cqlStr = String.format(ADD_NOTE_ENTRY, keyspace, userId);
        session.execute(SimpleStatement.builder(cqlStr)
                        .addPositionalValues(noteEntry.getNotebook(), noteEntry.getId(), encryptionUtil.encrypt(encryptionKey, noteEntry.getKey()), encryptionUtil.encrypt(encryptionKey, noteEntry.getValue()),
                                encryptionUtil.encrypt(encryptionKey, noteEntry.getInfo()), noteEntry.isPassword(), noteEntry.getLastModifiedTime().toInstant(ZoneOffset.UTC))
                        .build());
    }

    @Override
    public void updateNote(String userId, String encryptionKey, NoteEntry noteEntry) {
        String cqlStr = String.format(EDIT_NOTE_ENTRY, keyspace, userId);
        session.execute(SimpleStatement.builder(cqlStr)
                        .addPositionalValues(encryptionUtil.encrypt(encryptionKey, noteEntry.getKey()), encryptionUtil.encrypt(encryptionKey, noteEntry.getValue()), encryptionUtil.encrypt(encryptionKey, noteEntry.getInfo()), noteEntry.isPassword(),
                                noteEntry.getLastModifiedTime().toInstant(ZoneOffset.UTC), noteEntry.getNotebook(), noteEntry.getId())
                        .build());
        
    }

    @Override
    public void deleteNotes(String userId, String notebook, List<String> noteIds) {
        String cqlStr = String.format(DELETE_NOTE_ENTRIES, keyspace, userId);
        session.execute(SimpleStatement.builder(cqlStr).addPositionalValues(notebook, noteIds).build());
    }

    @Override
    public void deleteNotebook(String userId, String notebookToBeDeleted) {
        String cqlStr = String.format(DELETE_NOTEBOOK, keyspace, userId);
        session.execute(SimpleStatement.builder(cqlStr).addPositionalValues(notebookToBeDeleted).build());
    }

    @Override
    public void saveNotes(String userId, String encryptionKey, Notes notes) {
        List<BoundStatement> statements = notes.getNoteEntries().stream()
                .map((noteEntry) -> getBoundStatementForAddNoteEntry(userId, encryptionKey, noteEntry)).collect(Collectors.toList());
        statements.forEach(statement -> session.execute(statement));        
    }

    private BoundStatement getBoundStatementForAddNoteEntry(String userId, String encryptionKey, NoteEntry noteEntry) {
        String addNoteEntryCQL = String.format(ADD_NOTE_ENTRY, keyspace, userId);
        PreparedStatement preparedAddNoteEntry = session.prepare(addNoteEntryCQL);

        return preparedAddNoteEntry.bind(noteEntry.getNotebook(), noteEntry.getId(), encryptionUtil.encrypt(encryptionKey, noteEntry.getKey()), encryptionUtil.encrypt(encryptionKey, noteEntry.getValue()),
                encryptionUtil.encrypt(encryptionKey, noteEntry.getInfo()), noteEntry.isPassword(), noteEntry.getLastModifiedTime().toInstant(ZoneOffset.UTC)).setIdempotent(true);
    }
    
}
