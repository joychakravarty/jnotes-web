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

import static com.jc.jnotesweb.service.cassandra.CassandraNotesService.VALIDATION_NOTEBOOK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.jc.jnotesweb.model.NoteEntry;
import com.jc.jnotesweb.model.Notes;
import com.jc.jnotesweb.service.NotesService;
import com.jc.jnotesweb.util.EncryptionUtil;

/**
 * 
 * @author Joy C
 *
 */
@SpringBootTest
@TestInstance(Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
public class CassandraNotesServiceTest {

    private static final String TEST_USER_ID = "jnotes_testuser";
    private static final String TEST_USER_SECRET = "jnotes_testsecret";
    
    @Autowired
    private NotesService service;
    
    @Autowired
    private CqlSession session;

    @BeforeAll
    public void beforeAll() {
        ResultSet results = null;
        try {
            results = session.execute(SimpleStatement.builder("SELECT * from " + TEST_USER_ID).build());
        } catch (Exception ex) {
            System.out.println("Test table doest seem to exist");
        }
        if (results != null) {
            session.execute(SimpleStatement.builder("DROP TABLE IF EXISTS jnotes_testuser").build());
        }
    }
    
    @Test
    @Order(1)
    public void testGetEncryptedValidationText_UserDoesNotExist() {
        String encryptedValidtionText = service.getEncryptedValidationText(TEST_USER_ID);
        assertNull(encryptedValidtionText, "There shouldnt be any encryptedValidtionText");
    }

    @Test
    @Order(2)
    public void testSetupUser_NewUser() {
        int returnStatus = service.setupUser(TEST_USER_ID, TEST_USER_SECRET);

        assertEquals(0, returnStatus, "Setup new user should have been successful");

        ResultSet results = session.execute(SimpleStatement.builder("SELECT * from " + TEST_USER_ID).build());

        assertNotNull(results, TEST_USER_ID + " table should have been created");
        Row row = results.one();
        String notebook = row.getString("notebook");
        assertEquals(VALIDATION_NOTEBOOK, notebook);
    }

    @Test
    @Order(3)
    public void testSetupUser_ExistingUser() {
        int returnStatus = service.setupUser(TEST_USER_ID, TEST_USER_SECRET);
        assertEquals(1, returnStatus, "Setup should already be created");
    }
    
    @Test
    @Order(4)
    public void testGetEncryptedValidationText_UserExists() {
        String encryptedValidtionText = service.getEncryptedValidationText(TEST_USER_ID);
        String validationText = EncryptionUtil.decrypt(TEST_USER_SECRET, encryptedValidtionText);
        assertEquals(validationText, NotesService.VALIDATION_TEXT, "ValidationText should match");
    }

    @Test
    @Order(5)
    public void testAddNoteEntry() throws IOException {
        String testNotebook = "nb1";
        NoteEntry noteEntry = new NoteEntry(testNotebook, "ididid", "kkk", "vvv", "iii", false, LocalDateTime.now());
        service.addNoteEntry(TEST_USER_ID, TEST_USER_SECRET, noteEntry);

        Notes notes = service.getUserNotesForNotebook(TEST_USER_ID, TEST_USER_SECRET, testNotebook);

        assertEquals(1, notes.getNoteEntries().size());
        assertEquals("kkk", notes.getNoteEntries().get(0).getKey());
    }

    @Test
    @Order(6)
    public void testEditNoteEntry() throws IOException {
        String testNotebook = "nb1";
        NoteEntry noteEntry = new NoteEntry(testNotebook, "ididid", "kkk222", "vvv222", "iii222", true, LocalDateTime.now());
        service.editNoteEntry(TEST_USER_ID, TEST_USER_SECRET, noteEntry);

        Notes notes = service.getUserNotesForNotebook(TEST_USER_ID, TEST_USER_SECRET, testNotebook);

        assertEquals(1, notes.getNoteEntries().size());
        assertEquals("kkk222", notes.getNoteEntries().get(0).getKey());
        assertEquals("vvv222", notes.getNoteEntries().get(0).getValue());
        assertEquals("iii222", notes.getNoteEntries().get(0).getInfo());
        assertEquals(true, notes.getNoteEntries().get(0).isPassword());
    }

    @Test
    @Order(7)
    public void testSaveNotes() throws IOException {
        String testNotebook1 = "nbX";
        String testNotebook2 = "nbY";
        NoteEntry noteEntry1 = new NoteEntry(testNotebook1, "id1", "kkk111", "vvv", "iii", false, LocalDateTime.now());
        NoteEntry noteEntry2 = new NoteEntry(testNotebook2, "id2", "kkk222", "vvv", "iii", true, LocalDateTime.now());
        List<NoteEntry> list1 = new ArrayList<>();
        list1.add(noteEntry1);
        list1.add(noteEntry2);
        
        Notes notes = new Notes(list1);
        
        service.saveNotes(TEST_USER_ID, TEST_USER_SECRET, notes);
        
        NoteEntry noteEntry3 = new NoteEntry(testNotebook1, "id3", "kkk333", "vvv", "iii", true, LocalDateTime.now());
        List<NoteEntry> list2 = new ArrayList<>();
        list2.add(noteEntry3);
        notes = new Notes(list2);
        
        service.saveNotes(TEST_USER_ID, TEST_USER_SECRET, notes);

        Notes retNotes = service.getUserNotesForNotebook(TEST_USER_ID, TEST_USER_SECRET, testNotebook1);
        assertEquals(2, retNotes.getNoteEntries().size());

    }

    @Test
    @Order(8)
    public void testGetAllUserNotes() throws IOException {
        Notes retNotes = service.getAllUserNotes(TEST_USER_ID, TEST_USER_SECRET);
        assertEquals(4, retNotes.getNoteEntries().size());
    }
    
    @Test
    @Order(9)
    public void testDeleteNotebook() throws IOException {
        String testNotebook2 = "nbY";
        Notes retNotes = service.getUserNotesForNotebook(TEST_USER_ID, TEST_USER_SECRET, testNotebook2);
        assertEquals(1, retNotes.getNoteEntries().size());
        service.deleteNotebook(TEST_USER_ID, testNotebook2);
        retNotes = service.getUserNotesForNotebook(TEST_USER_ID, TEST_USER_SECRET, testNotebook2);
        assertEquals(0, retNotes.getNoteEntries().size());
    }
    
    @Test
    @Order(10)
    public void testDeleteNotes() throws IOException {
        String testNotebook1 = "nbX";
        Notes retNotes = service.getUserNotesForNotebook(TEST_USER_ID, TEST_USER_SECRET, testNotebook1);
        assertEquals(2, retNotes.getNoteEntries().size());
        
        NoteEntry noteEntry1 = new NoteEntry(testNotebook1, "id1", "kkk111", "vvv", "iii", false, LocalDateTime.now());
        NoteEntry noteEntry3 = new NoteEntry(testNotebook1, "id3", "kkk333", "vvv", "iii", true, LocalDateTime.now());
        
        List<NoteEntry> list = new ArrayList<>();
        list.add(noteEntry1);
        list.add(noteEntry3);
        Notes notes = new Notes(list);
        
        service.deleteNotes(TEST_USER_ID, notes);
        retNotes = service.getUserNotesForNotebook(TEST_USER_ID, TEST_USER_SECRET, testNotebook1);
        assertEquals(0, retNotes.getNoteEntries().size());
    }

}
