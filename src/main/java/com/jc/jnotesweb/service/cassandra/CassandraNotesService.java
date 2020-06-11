/*
 * This file is part of JNotes. Copyright (C) 2020  Joy Chakravarty
 * 
 * JNotes is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JNotes is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JNotes.  If not, see <https://www.gnu.org/licenses/>.
 * 
 * 
 */
package com.jc.jnotesweb.service.cassandra;

import static com.datastax.oss.driver.api.querybuilder.SchemaBuilder.createTable;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BatchStatement;
import com.datastax.oss.driver.api.core.cql.BatchStatementBuilder;
import com.datastax.oss.driver.api.core.cql.BatchType;
import com.datastax.oss.driver.api.core.cql.BatchableStatement;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.servererrors.AlreadyExistsException;
import com.datastax.oss.driver.api.core.servererrors.InvalidQueryException;
import com.datastax.oss.driver.api.core.type.DataTypes;
import com.datastax.oss.driver.api.querybuilder.schema.CreateTable;
import com.jc.jnotesweb.model.NoteEntry;
import com.jc.jnotesweb.model.Notes;
import com.jc.jnotesweb.service.NotesService;
import com.jc.jnotesweb.util.EncryptionUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * This class uses Cassandra DB as the Remote DataStore<br>
 * 
 * 
 * @author Joy C
 *
 */
@Slf4j
@Service
public class CassandraNotesService implements NotesService {
    
    @Autowired
    @Qualifier("keyspace")
    private String keyspace;
    
    @Autowired 
    private CqlSession session;

    private static final String GET_ALL_NOTES_FOR_NOTEBOOK = "SELECT * FROM %s.%s WHERE notebook = ?";
    private static final String GET_ALL_NOTES_FOR_USER = "SELECT * FROM %s.%s";
    private static final String ADD_NOTE_ENTRY = "INSERT INTO %s.%s (notebook, noteid, key, value, info, isPassword, lastModifiedTime) VALUES (?,?,?,?,?,?,?)";
    private static final String EDIT_NOTE_ENTRY = "UPDATE %s.%s SET key = ?, value = ?, info = ?, isPassword = ?, lastModifiedTime = ? where notebook = ? and noteid = ?";
    private static final String DELETE_NOTE_ENTRIES = "DELETE FROM %s.%s WHERE notebook = ? and noteid IN ?";

    private static final String DELETE_NOTEBOOK = "DELETE FROM %s.%s WHERE notebook = ?";

    private static final String ADD_USERSCRET_VALIDATION_ROW = "INSERT INTO %s.%s (notebook, encrypted_validation_text) VALUES (?, ?)";
    private static final String GET_ENCRYPTED_VALIDATION_TEXT = "SELECT encrypted_validation_text FROM %s.%s WHERE notebook = ?";

    public static final String VALIDATION_NOTEBOOK = "VALIDATION_NOTEBOOK";

    /**
     * 1. Create userId table which will store the notes for this user.<br>
     * 2. Store the secret-validation-text in an encrypted form - this is to validate the user later. Note: secret itself is not stored (so user data is protected)!<br>
     * 
     * What can go wrong?<br>
     * if Step 1 fails -> no worries, user will not be connected, no table is created, so they can retry as new user<br>
     * if Step 2 fails -> table will be created but the validation row will not be present -> rollback by dropping the table from step 1<br>
     * 
     */
    @Override
    public int setupUser(String userId, String encryptionKey) {
        String progress = null;
        try {
            CreateTable createUserTable = createTable(keyspace, userId).withPartitionKey("notebook", DataTypes.TEXT)
                    .withClusteringColumn("noteid", DataTypes.TEXT).withColumn("key", DataTypes.TEXT).withColumn("value", DataTypes.TEXT)
                    .withColumn("info", DataTypes.TEXT).withColumn("isPassword", DataTypes.BOOLEAN)
                    .withColumn("lastModifiedTime", DataTypes.TIMESTAMP).withStaticColumn("encrypted_validation_text", DataTypes.TEXT);

            session.execute(createUserTable.build());
            log.info("Cassandra: User table created");
            progress = "TableCreated";
            insertValidationText(userId,  EncryptionUtil.encrypt(encryptionKey, VALIDATION_TEXT));
            progress = "Completed";
        } catch (AlreadyExistsException alreadyExistsException) {
            log.error(String.format("%s already exists", userId));
            return 1; 
        } catch (Exception ex) {
            log.error(String.format("Failed to setup new user: %s.", userId), ex);
            if("TableCreated".equals(progress)) {
                session.execute(SimpleStatement.builder(String.format("DROP TABLE IF EXISTS %s.%s", keyspace, userId)).build());
            }
            return 2;
        }
        return 0;
    }

    private void insertValidationText(String userId, String encryptedValidationText) {
        session.execute(SimpleStatement.builder(String.format(ADD_USERSCRET_VALIDATION_ROW, keyspace, userId))
                        .addPositionalValues(VALIDATION_NOTEBOOK, encryptedValidationText).build());
        log.info("Cassandra: Validation Text added to User table");
    }

    /**
     * Used for filtering out the Validation Row
     * 
     * @param row
     * @return
     */
    private boolean isValidationNotebook(Row row) {
        String notebook = row.getString("notebook");
        if (VALIDATION_NOTEBOOK.equals(notebook)) {
            return false;
        } else {
            return true;
        }
    }

    private NoteEntry toNoteEntry(Row row, String encryptionKey) {
        NoteEntry noteEntry = new NoteEntry(row.getString("notebook"), row.getString("noteid"),  EncryptionUtil.decrypt(encryptionKey, row.getString("key")),
                EncryptionUtil.decrypt(encryptionKey, row.getString("value")), EncryptionUtil.decrypt(encryptionKey, row.getString("info")), row.getBoolean("isPassword"),
                LocalDateTime.ofInstant(row.getInstant("lastModifiedTime"), ZoneOffset.UTC));
        return noteEntry;
    }

    private BoundStatement getBoundStatementForAddNoteEntry(String userId, String encryptionKey, NoteEntry noteEntry) {
        String addNoteEntryCQL = String.format(ADD_NOTE_ENTRY, keyspace, userId);
        PreparedStatement preparedAddNoteEntry = session.prepare(addNoteEntryCQL);

        return preparedAddNoteEntry.bind(noteEntry.getNotebook(), noteEntry.getNoteId(), EncryptionUtil.encrypt(encryptionKey, noteEntry.getKey()), EncryptionUtil.encrypt(encryptionKey, noteEntry.getValue()),
                EncryptionUtil.encrypt(encryptionKey, noteEntry.getInfo()), noteEntry.isPassword(), noteEntry.getLastModifiedTime().toInstant(ZoneOffset.UTC));
    }

    @Override
    public Notes getAllUserNotes(String userId, String encryptionKey) {
        List<NoteEntry> noteEntries;
        String cqlStr = String.format(GET_ALL_NOTES_FOR_USER, keyspace, userId);
        ResultSet results;
        try {
            results = session.execute(SimpleStatement.builder(cqlStr).build());
        } catch (InvalidQueryException ex) {
            log.error("Caught InvalidQueryException");
            return null;
        }
        List<Row> rows = results.all();
        if (rows != null && !rows.isEmpty()) {
            noteEntries = rows.stream().filter(row -> isValidationNotebook(row)).map(row -> toNoteEntry(row, encryptionKey)).collect(Collectors.toList());
        } else {
            noteEntries = Collections.emptyList();
        }
        return new Notes(noteEntries);
    }

    @Override
    public Notes getUserNotesForNotebook(String userId, String encryptionKey, String notebook) {
        List<NoteEntry> noteEntries;
        String cqlStr = String.format(GET_ALL_NOTES_FOR_NOTEBOOK, keyspace, userId);
        ResultSet results;
        try {
            results = session.execute(SimpleStatement.builder(cqlStr).addPositionalValues(notebook).build());
        } catch (InvalidQueryException ex) {
            log.error("Caught InvalidQueryException");
            return null;
        }
        List<Row> rows = results.all();
        if (rows != null && !rows.isEmpty()) {
            noteEntries = rows.stream().filter(row -> isValidationNotebook(row)).map(row -> toNoteEntry(row, encryptionKey)).collect(Collectors.toList());
        } else {
            noteEntries = Collections.emptyList();
        }
        return new Notes(noteEntries);
    }

    @Override
    public void addNoteEntry(String userId, String encryptionKey, NoteEntry noteEntry) {
        String cqlStr = String.format(ADD_NOTE_ENTRY, keyspace, userId);

        session
                .execute(SimpleStatement.builder(cqlStr)
                        .addPositionalValues(noteEntry.getNotebook(), noteEntry.getNoteId(), EncryptionUtil.encrypt(encryptionKey, noteEntry.getKey()), EncryptionUtil.encrypt(encryptionKey, noteEntry.getValue()),
                                EncryptionUtil.encrypt(encryptionKey, noteEntry.getInfo()), noteEntry.isPassword(), noteEntry.getLastModifiedTime().toInstant(ZoneOffset.UTC))
                        .build());

    }

    @Override
    public void editNoteEntry(String userId, String encryptionKey, NoteEntry noteEntry) {
        String cqlStr = String.format(EDIT_NOTE_ENTRY, keyspace, userId);

        session
                .execute(SimpleStatement.builder(cqlStr)
                        .addPositionalValues(EncryptionUtil.encrypt(encryptionKey, noteEntry.getKey()), EncryptionUtil.encrypt(encryptionKey, noteEntry.getValue()), EncryptionUtil.encrypt(encryptionKey, noteEntry.getInfo()), noteEntry.isPassword(),
                                noteEntry.getLastModifiedTime().toInstant(ZoneOffset.UTC), noteEntry.getNotebook(), noteEntry.getNoteId())
                        .build());

    }

    @Override
    public void deleteNotes(String userId, Notes notes) {
        String notebook = notes.getNoteEntries().get(0).getNotebook();
        List<String> noteIds = notes.getNoteEntries().stream().map((noteEntry) -> noteEntry.getNoteId()).collect(Collectors.toList());
        String cqlStr = String.format(DELETE_NOTE_ENTRIES, keyspace, userId);
        session.execute(SimpleStatement.builder(cqlStr).addPositionalValues(notebook, noteIds).build());
    }

    @Override
    public void saveNotes(String userId, String encryptionKey, Notes notes) {
        Iterable<BatchableStatement<?>> statements = notes.getNoteEntries().stream()
                .map((noteEntry) -> getBoundStatementForAddNoteEntry(userId, encryptionKey, noteEntry)).collect(Collectors.toList());
        BatchStatement batch = new BatchStatementBuilder(BatchType.LOGGED).setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM)
                .addStatements(statements).build();
        session.execute(batch);
    }

    @Override
    public void deleteNotebook(String userId, String notebookToBeDeleted) {
        String cqlStr = String.format(DELETE_NOTEBOOK, keyspace, userId);
        session.execute(SimpleStatement.builder(cqlStr).addPositionalValues(notebookToBeDeleted).build());
    }

    @Override
    public String getEncryptedValidationText(String userId) {
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

}
