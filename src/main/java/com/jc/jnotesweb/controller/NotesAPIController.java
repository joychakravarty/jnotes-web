package com.jc.jnotesweb.controller;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.jc.jnotesweb.model.NewUserRequest;
import com.jc.jnotesweb.model.NoteEntry;
import com.jc.jnotesweb.model.NotebookRequest;
import com.jc.jnotesweb.model.Notes;
import com.jc.jnotesweb.security.AuthResponse;
import com.jc.jnotesweb.security.Authenticator;
import com.jc.jnotesweb.service.NotesService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class NotesAPIController {
    private static final Notes EMPTY_NOTES = new Notes(Collections.emptyList());

    @Autowired
    private NotesService notesService;

    @Autowired
    private Authenticator authenticator;

    @Autowired
    @Qualifier("dbProperties")
    private String dbProperties;

    @RequestMapping("/")
    String index(Map<String, Object> model) {
        System.out.println("dbProperties >> " + dbProperties);
        model.put("dbProperties", dbProperties);
        return "index";
    }

    @GetMapping("/getUserNotes")
    public ResponseEntity<Notes> getUserNotes(@RequestHeader("authorization") String authReqHeader,
            @RequestParam(value = "notebook", required = false) String notebook) {
        Notes notes;
        AuthResponse authResponse = authenticator.evaluateAuthHeader(authReqHeader);
        if (!authResponse.isAuthenticationSuccessful()) {
            return new ResponseEntity<Notes>(EMPTY_NOTES, HttpStatus.UNAUTHORIZED);
        }

        String userId = authResponse.getUserId();
        log.info("userId : " + userId);
        log.info("notebook : " + notebook);

        if (StringUtils.isBlank(notebook)) {
            notes = notesService.getAllUserNotes(userId, authResponse.getEncryptionKey());
        } else {
            notes = notesService.getUserNotesForNotebook(userId, authResponse.getEncryptionKey(), notebook);
        }
        return new ResponseEntity<Notes>(notes, HttpStatus.OK);
    }
    
    @GetMapping("/authenticateUser")
    public ResponseEntity<String> authenticateUser(@RequestHeader("authorization") String authReqHeader) {
        AuthResponse authResponse = authenticator.evaluateAuthHeader(authReqHeader);
        switch(authResponse.getAuthStatus()) {
            case INVALID_HEADER: return new ResponseEntity<String>(StringUtils.EMPTY, HttpStatus.BAD_REQUEST);
            case INVALID_USER: return new ResponseEntity<String>(StringUtils.EMPTY, HttpStatus.NOT_FOUND);
            case INVALID_SECRET: return new ResponseEntity<String>(StringUtils.EMPTY, HttpStatus.UNAUTHORIZED);
            case VALID: return new ResponseEntity<String>(StringUtils.EMPTY, HttpStatus.OK);
            default: return new ResponseEntity<String>(StringUtils.EMPTY, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/setupUser")
    public ResponseEntity<String> setupUser(@RequestBody NewUserRequest newUserRequest) {
        String userId = newUserRequest.getUserId();
        String encryptionKey = newUserRequest.getUserSecret();
        log.info("userId : " + userId);
        int returnStatus = notesService.setupUser(userId, encryptionKey);
        if (returnStatus == 0) {
            return new ResponseEntity<String>(StringUtils.EMPTY, HttpStatus.CREATED);
        } else if (returnStatus == 1) {
            return new ResponseEntity<String>(String.format("%s already exists", userId), HttpStatus.CONFLICT);
        } else {
            return new ResponseEntity<String>(String.format("Failed to setup new user: %s. Please contact Joy", userId),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/addNote")
    public ResponseEntity<String> addNote(@RequestHeader("authorization") String authReqHeader, @RequestBody NoteEntry noteEntry) {
        AuthResponse authResponse = authenticator.evaluateAuthHeader(authReqHeader);
        if (!authResponse.isAuthenticationSuccessful()) {
            return new ResponseEntity<String>(StringUtils.EMPTY, HttpStatus.UNAUTHORIZED);
        }
        String userId = authResponse.getUserId();
        String encryptionKey = authResponse.getEncryptionKey();
        log.info("userId : " + userId);
        
        notesService.addNoteEntry(userId, encryptionKey, noteEntry);
        return new ResponseEntity<String>(StringUtils.EMPTY, HttpStatus.CREATED);
    }
    
    @PostMapping("/editNote")
    public ResponseEntity<String> editNote(@RequestHeader("authorization") String authReqHeader, @RequestBody NoteEntry noteEntry) {
        AuthResponse authResponse = authenticator.evaluateAuthHeader(authReqHeader);
        if (!authResponse.isAuthenticationSuccessful()) {
            return new ResponseEntity<String>(StringUtils.EMPTY, HttpStatus.UNAUTHORIZED);
        }
        String userId = authResponse.getUserId();
        String encryptionKey = authResponse.getEncryptionKey();
        log.info("userId : " + userId);
        
        notesService.editNoteEntry(userId, encryptionKey, noteEntry);
        return new ResponseEntity<String>(StringUtils.EMPTY, HttpStatus.ACCEPTED);
    }
    
    @PostMapping("/deleteNotes")
    public ResponseEntity<String> deleteNotes(@RequestHeader("authorization") String authReqHeader, @RequestBody Notes notes) {
        AuthResponse authResponse = authenticator.evaluateAuthHeader(authReqHeader);
        if (!authResponse.isAuthenticationSuccessful()) {
            return new ResponseEntity<String>(StringUtils.EMPTY, HttpStatus.UNAUTHORIZED);
        }
        String userId = authResponse.getUserId();
        log.info("userId : " + userId);
        
        notesService.deleteNotes(userId, notes);
        return new ResponseEntity<String>(StringUtils.EMPTY, HttpStatus.ACCEPTED);
    }
    
    @PostMapping("/backupNotes")
    public ResponseEntity<String> backupNotes(@RequestHeader("authorization") String authReqHeader, @RequestBody Notes notes) {
        AuthResponse authResponse = authenticator.evaluateAuthHeader(authReqHeader);
        if (!authResponse.isAuthenticationSuccessful()) {
            return new ResponseEntity<String>(StringUtils.EMPTY, HttpStatus.UNAUTHORIZED);
        }
        String userId = authResponse.getUserId();
        String encryptionKey = authResponse.getEncryptionKey();
        log.info("userId : " + userId);
        
        notesService.saveNotes(userId, encryptionKey, notes);
        return new ResponseEntity<String>(StringUtils.EMPTY, HttpStatus.ACCEPTED);
    }
    
    @PostMapping("/deleteNotebook")
    public ResponseEntity<String> deleteNotebook(@RequestHeader("authorization") String authReqHeader, @RequestBody NotebookRequest notebookRequest) {
        AuthResponse authResponse = authenticator.evaluateAuthHeader(authReqHeader);
        if (!authResponse.isAuthenticationSuccessful()) {
            return new ResponseEntity<String>(StringUtils.EMPTY, HttpStatus.UNAUTHORIZED);
        }
        String userId = authResponse.getUserId();
        log.info("userId : " + userId);
        
        notesService.deleteNotebook(userId, notebookRequest.getNotebookToBeDeleted());
        return new ResponseEntity<String>(StringUtils.EMPTY, HttpStatus.ACCEPTED);
    }
    
    @PostMapping("/renameNotebook")
    public ResponseEntity<String> renameNotebook(@RequestHeader("authorization") String authReqHeader, @RequestBody NotebookRequest notebookRequest) {
        AuthResponse authResponse = authenticator.evaluateAuthHeader(authReqHeader);
        if (!authResponse.isAuthenticationSuccessful()) {
            return new ResponseEntity<String>(StringUtils.EMPTY, HttpStatus.UNAUTHORIZED);
        }
        String userId = authResponse.getUserId();
        String encryptionKey = authResponse.getEncryptionKey();
        log.info("userId : " + userId);
        
        Notes notes = notesService.getUserNotesForNotebook(userId, encryptionKey, notebookRequest.getNotebookToBeRenamed());
        notes.getNoteEntries().forEach((note)->note.setNotebook(notebookRequest.getNotebookNewName()));
        notesService.saveNotes(userId, encryptionKey, notes);
        notesService.deleteNotebook(userId, notebookRequest.getNotebookToBeRenamed());
        return new ResponseEntity<String>(StringUtils.EMPTY, HttpStatus.ACCEPTED);
    }


}
