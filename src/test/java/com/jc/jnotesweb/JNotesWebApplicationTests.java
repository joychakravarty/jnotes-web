package com.jc.jnotesweb;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.tinkerpop.gremlin.structure.T;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.jc.jnotesweb.model.NewUserRequest;
import com.jc.jnotesweb.model.NoteEntry;
import com.jc.jnotesweb.model.NotebookRequest;
import com.jc.jnotesweb.model.Notes;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestInstance(Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
class JNotesWebApplicationTests {

    // @LocalServerPort
    // private int port;

    // @Autowired
    // private TestRestTemplate restTemplate;
    
    // private static final String TEST_USER_ID = "jnotes_testuser_1";
    // private static final String TEST_USER_SECRET = "jnotes_testsecret_1";
    
    // @Autowired
    // private CqlSession session;

    // @BeforeAll
    // public void beforeAll() {
    //     ResultSet results = null;
    //     try {
    //         results = session.execute(SimpleStatement.builder("SELECT * from " + TEST_USER_ID).build());
    //     } catch (Exception ex) {
    //         System.out.println("Test table doest seem to exist");
    //     }
    //     if (results != null) {
    //         session.execute(SimpleStatement.builder("DROP TABLE IF EXISTS jnotes_testuser").setTimeout(Duration.ofMinutes(1)).build());
    //     }
    // }
    
    // @Test
    // @Order(1)
    // public void testAuthenticateUser_UserDoesNotExist() {        
    //     ResponseEntity<String> result = restTemplate.exchange
    //             ("http://localhost:" + port + "/authenticateUser", HttpMethod.GET, new HttpEntity<T>(createHeaders()), String.class);
    //     assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode(), "User shouldnt have existed");
    // }
    
    // @Test
    // @Order(2)
    // public void testSetupUser() {       
    //     NewUserRequest newUserRequest = new NewUserRequest(TEST_USER_ID, TEST_USER_SECRET);
    //     ResponseEntity<String> result = restTemplate.exchange
    //             ("http://localhost:" + port + "/setupUser", HttpMethod.POST, new HttpEntity<NewUserRequest>(newUserRequest), String.class);
    //     assertEquals(HttpStatus.CREATED, result.getStatusCode(), "User should have been created");
    // }
    
    // @Test
    // @Order(3)
    // public void testSetupUser_ConflictFailure() {       
    //     NewUserRequest newUserRequest = new NewUserRequest(TEST_USER_ID, TEST_USER_SECRET);
    //     ResponseEntity<String> result = restTemplate.exchange
    //             ("http://localhost:" + port + "/setupUser", HttpMethod.POST, new HttpEntity<NewUserRequest>(newUserRequest), String.class);
    //     assertEquals(HttpStatus.CONFLICT, result.getStatusCode(), "User should have been created");
    // }
    
    // @Test
    // @Order(4)
    // public void testAuthenticateUser_UserExists() {        
    //     ResponseEntity<String> result = restTemplate.exchange
    //             ("http://localhost:" + port + "/authenticateUser", HttpMethod.GET, new HttpEntity<T>(createHeaders()), String.class);
    //     assertEquals(HttpStatus.OK, result.getStatusCode(), "User should have existed");
    // }
    
    // @Test
    // @Order(5)
    // public void testAddNote() {  
    //     String testNotebook1 = "nbX";
    //     NoteEntry noteEntry1 = new NoteEntry(testNotebook1, "id1", "kkk111", "vvv", "iii", false, LocalDateTime.now());
    //     ResponseEntity<String> result = restTemplate.exchange
    //             ("http://localhost:" + port + "/addNote", HttpMethod.POST, new HttpEntity<NoteEntry>(noteEntry1, createHeaders()), String.class);
    //     assertEquals(HttpStatus.CREATED, result.getStatusCode(), "Note should have been added");
    // }
    
    // @Test
    // @Order(6)
    // public void testEditNote() {  
    //     String testNotebook1 = "nbX";
    //     NoteEntry noteEntry1 = new NoteEntry(testNotebook1, "id1", "kkk222", "vvv222", "iii222", true, LocalDateTime.now());
    //     ResponseEntity<String> result = restTemplate.exchange
    //             ("http://localhost:" + port + "/editNote", HttpMethod.POST, new HttpEntity<NoteEntry>(noteEntry1, createHeaders()), String.class);
    //     assertEquals(HttpStatus.ACCEPTED, result.getStatusCode(), "Note should have been edited");
    // }
    
    // @Test
    // @Order(7)
    // public void testRenameNotebook() { 
    //     NotebookRequest notebookRequest = new NotebookRequest("nbX", "nbY"); 
    //     ResponseEntity<String> result = restTemplate.exchange
    //             ("http://localhost:" + port + "/renameNotebook", HttpMethod.POST, new HttpEntity<NotebookRequest>(notebookRequest, createHeaders()), String.class);
    //     assertEquals(HttpStatus.ACCEPTED, result.getStatusCode(), "Note should be renamed");
    // }
    
    // @Test
    // @Order(8)
    // public void testGetUserNotes() { 
    //     ResponseEntity<Notes> result = restTemplate.exchange
    //             ("http://localhost:" + port + "/getUserNotes?notebook=nbY", HttpMethod.GET, new HttpEntity<T>(createHeaders()), Notes.class);
    //     assertEquals(HttpStatus.OK, result.getStatusCode());
    //     assertEquals("kkk222", result.getBody().getNoteEntries().get(0).getKey());
    //     assertEquals("vvv222", result.getBody().getNoteEntries().get(0).getValue());
    //     assertEquals("iii222", result.getBody().getNoteEntries().get(0).getInfo());
    // }
    
    // @Test
    // @Order(9)
    // public void testBackupNotes() {
    //     String testNotebook1 = "nbX";
    //     NoteEntry noteEntry1 = new NoteEntry(testNotebook1, "testid1", "kkk222", "vvv222", "iii222", true, LocalDateTime.now());
    //     NoteEntry noteEntry2 = new NoteEntry(testNotebook1, "testid2", "kkk222", "vvv222", "iii222", true, LocalDateTime.now());
    //     List<NoteEntry> list = new ArrayList<>();
    //     list.add(noteEntry1);
    //     list.add(noteEntry2);
    //     Notes notes = new Notes(list);
    //     ResponseEntity<String> result = restTemplate.exchange
    //             ("http://localhost:" + port + "/backupNotes", HttpMethod.POST, new HttpEntity<>(notes, createHeaders()), String.class);
    //     assertEquals(HttpStatus.ACCEPTED, result.getStatusCode());
    // }
    
    // @Test
    // @Order(10)
    // public void testGetAllUserNotes() { 
    //     ResponseEntity<Notes> result = restTemplate.exchange
    //             ("http://localhost:" + port + "/getUserNotes", HttpMethod.GET, new HttpEntity<T>(createHeaders()), Notes.class);
    //     assertEquals(HttpStatus.OK, result.getStatusCode());
    //     assertEquals(3, result.getBody().getNoteEntries().size());
        
    // }


    // private HttpHeaders createHeaders() {
    //     String userId = TEST_USER_ID;
    //     String userSecret = TEST_USER_SECRET;
    //     return new HttpHeaders() {
    //         private static final long serialVersionUID = 1L;
    //         {
    //             String auth = userId + ":" + userSecret;
    //             byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.UTF_8));
    //             String authHeader = "JNOTES " + new String(encodedAuth);
    //             set("Authorization", authHeader);
    //         }
    //     };
    // }

}
