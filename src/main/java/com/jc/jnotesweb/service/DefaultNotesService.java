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
package com.jc.jnotesweb.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.servererrors.AlreadyExistsException;
import com.jc.jnotesweb.model.NoteEntry;
import com.jc.jnotesweb.model.Notes;
import com.jc.jnotesweb.repository.NotesRepository;
import com.jc.jnotesweb.util.EncryptionUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
public class DefaultNotesService implements NotesService {
    @Autowired
    private NotesRepository repository;

    @Autowired
    private EncryptionUtil encryptionUtil;

    public static final String VALIDATION_NOTEBOOK = "VALIDATION_NOTEBOOK";

    /**
     * 1. Create userId table which will store the notes for this user.<br>
     * 2. Store the secret-validation-text in an encrypted form - this is to
     * validate the user later. Note: secret itself is not stored (so user data is
     * protected)!<br>
     * 
     * What can go wrong?<br>
     * if Step 1 fails -> no worries, user will not be connected, no table is
     * created, so they can retry as new user<br>
     * if Step 2 fails -> table will be created but the validation row will not be
     * present -> rollback by dropping the table from step 1<br>
     * 
     */
    @Override
    public int setupUser(String userId, String encryptionKey) {
        String progress = null;
        try {
            repository.createUserTable(userId);
            log.info("Created user table " + userId);
            TimeUnit.SECONDS.sleep(3);
            progress = "TableCreated";
            repository.insertValidationTextForUser(userId, encryptionUtil.encrypt(encryptionKey, VALIDATION_TEXT));
            progress = "Completed";
            log.info("inserted validation text");
        } catch (AlreadyExistsException alreadyExistsException) {
            log.error(String.format("%s already exists", userId));
            return 1;
        } catch (Exception ex) {
            log.error(String.format("Failed to setup new user: %s.", userId), ex);
            if ("TableCreated".equals(progress)) {
                repository.dropUserTable(userId);
            }
            return 2;
        }
        return 0;
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
        NoteEntry noteEntry = new NoteEntry(row.getString("notebook"), row.getString("noteid"),  encryptionUtil.decrypt(encryptionKey, row.getString("key")),
                encryptionUtil.decrypt(encryptionKey, row.getString("value")), encryptionUtil.decrypt(encryptionKey, row.getString("info")), row.getBoolean("isPassword"),
                LocalDateTime.ofInstant(row.getInstant("lastModifiedTime"), ZoneOffset.UTC));
        return noteEntry;
    }

    @Override
    public Notes getAllUserNotes(String userId, String encryptionKey) {
        List<NoteEntry> noteEntries;
        List<Row> rows = repository.getAllUserNotes(userId);
        if (rows != null && !rows.isEmpty()) {
            noteEntries = rows.stream().filter(row -> isValidationNotebook(row))
                    .map(row -> toNoteEntry(row, encryptionKey)).collect(Collectors.toList());
        } else {
            noteEntries = Collections.emptyList();
        }
        return new Notes(noteEntries);
    }

    @Override
    public Notes getUserNotesForNotebook(String userId, String encryptionKey, String notebook) {
        List<NoteEntry> noteEntries;
        List<Row> rows = repository.getUserNotesForNotebook(userId, notebook);
        if (rows != null && !rows.isEmpty()) {
            noteEntries = rows.stream().filter(row -> isValidationNotebook(row))
                    .map(row -> toNoteEntry(row, encryptionKey)).collect(Collectors.toList());
        } else {
            noteEntries = Collections.emptyList();
        }
        return new Notes(noteEntries);
    }

    @Override
    public void addNoteEntry(String userId, String encryptionKey, NoteEntry noteEntry) {
        repository.insertNote(userId, encryptionKey, noteEntry);
    }

    @Override
    public void editNoteEntry(String userId, String encryptionKey, NoteEntry noteEntry) {
        repository.updateNote(userId, encryptionKey, noteEntry);
    }

    @Override
    public void deleteNotes(String userId, Notes notes) {
        String notebook = notes.getNoteEntries().get(0).getNotebook();
        List<String> noteIds = notes.getNoteEntries().stream().map((noteEntry) -> noteEntry.getId())
                .collect(Collectors.toList());
        repository.deleteNotes(userId, notebook, noteIds);
    }

    @Override
    public void saveNotes(String userId, String encryptionKey, Notes notes) {
        repository.saveNotes(userId, encryptionKey, notes);
    }

    @Override
    public void deleteNotebook(String userId, String notebookToBeDeleted) {
        repository.deleteNotebook(userId, notebookToBeDeleted);
    }

    @Override
    public String getEncryptedValidationText(String userId) {
        String encryptedValidationText = "";
        try {
            encryptedValidationText = repository.getValidationTextForUser(userId);
        } catch (Exception ex) {
            log.error("Exception in getEncryptedValidationText (can't do much here) : ", ex);
        }
        return encryptedValidationText;
    }

}