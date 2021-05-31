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
import static com.jc.jnotesweb.security.AuthStatus.*;
import com.jc.jnotesweb.security.Authenticator;
import com.jc.jnotesweb.service.NotesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Controller
public class NotesAPIController {
    private static final Logger log = LoggerFactory.getLogger(NotesAPIController.class);
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
        log.info("getUserNotes | notebook:" + notebook);
        Notes notes;
        AuthResponse authResponse = authenticator.evaluateAuthHeader(authReqHeader);
        String userId = authResponse.getUserId();
        if (authResponse.isAuthenticationSuccessful()) {
            if (StringUtils.isBlank(notebook)) {
                notes = notesService.getAllUserNotes(userId, authResponse.getEncryptionKey());
            } else {
                notes = notesService.getUserNotesForNotebook(userId, authResponse.getEncryptionKey(), notebook);
            }
            return new ResponseEntity<Notes>(notes, HttpStatus.OK);
        } else {
            switch (authResponse.getAuthStatus()) {
            case INVALID_HEADER:
                return new ResponseEntity<Notes>(EMPTY_NOTES, HttpStatus.BAD_REQUEST);
            case INVALID_USER:
                return new ResponseEntity<Notes>(EMPTY_NOTES, HttpStatus.NOT_FOUND);
            case INVALID_SECRET:
                return new ResponseEntity<Notes>(EMPTY_NOTES, HttpStatus.UNAUTHORIZED);
            default:
                return new ResponseEntity<Notes>(EMPTY_NOTES, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }

    @GetMapping("/authenticateUser")
    public ResponseEntity<String> authenticateUser(@RequestHeader("authorization") String authReqHeader) {
        log.info("authenticateUser authReqHeader:" + authReqHeader);
        AuthResponse authResponse = authenticator.evaluateAuthHeader(authReqHeader);
        switch (authResponse.getAuthStatus()) {
        case INVALID_HEADER:
            return new ResponseEntity<String>(StringUtils.EMPTY, HttpStatus.BAD_REQUEST);
        case INVALID_USER:
            return new ResponseEntity<String>(StringUtils.EMPTY, HttpStatus.NOT_FOUND);
        case INVALID_SECRET:
            return new ResponseEntity<String>(StringUtils.EMPTY, HttpStatus.UNAUTHORIZED);
        case VALID:
            return new ResponseEntity<String>(StringUtils.EMPTY, HttpStatus.OK);
        default:
            return new ResponseEntity<String>(StringUtils.EMPTY, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/setupUser")
    public ResponseEntity<String> setupUser(@RequestBody NewUserRequest newUserRequest) {
        log.info("setupUser | userId:" + newUserRequest.getUserId());
        String userId = newUserRequest.getUserId();
        String encryptionKey = newUserRequest.getUserSecret();
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
        log.info("addNote | noteEntry: " + noteEntry);
        AuthResponse authResponse = authenticator.evaluateAuthHeader(authReqHeader);
        String userId = authResponse.getUserId();
        String encryptionKey = authResponse.getEncryptionKey();
        if (!authResponse.isAuthenticationSuccessful()) {
            log.error("addNote | Authentication Unsuccessful | userId:" + userId);
            return new ResponseEntity<String>(StringUtils.EMPTY, HttpStatus.UNAUTHORIZED);
        }

        notesService.addNoteEntry(userId, encryptionKey, noteEntry);
        return new ResponseEntity<String>(StringUtils.EMPTY, HttpStatus.CREATED);
    }

    @PostMapping("/editNote")
    public ResponseEntity<String> editNote(@RequestHeader("authorization") String authReqHeader, @RequestBody NoteEntry noteEntry) {
        log.info("editNote | noteEntry: " + noteEntry);
        AuthResponse authResponse = authenticator.evaluateAuthHeader(authReqHeader);
        String userId = authResponse.getUserId();
        String encryptionKey = authResponse.getEncryptionKey();
        if (!authResponse.isAuthenticationSuccessful()) {
            log.error("editNote | Authentication Unsuccessful | userId:" + userId);
            return new ResponseEntity<String>(StringUtils.EMPTY, HttpStatus.UNAUTHORIZED);
        }

        notesService.editNoteEntry(userId, encryptionKey, noteEntry);
        return new ResponseEntity<String>(StringUtils.EMPTY, HttpStatus.ACCEPTED);
    }

    @PostMapping("/deleteNotes")
    public ResponseEntity<String> deleteNotes(@RequestHeader("authorization") String authReqHeader, @RequestBody Notes notes) {
        log.info("deleteNotes | number of notes: " + notes.getNoteEntries().size());
        AuthResponse authResponse = authenticator.evaluateAuthHeader(authReqHeader);
        String userId = authResponse.getUserId();
        if (!authResponse.isAuthenticationSuccessful()) {
            log.error("deleteNotes | Authentication Unsuccessful | userId:" + userId);
            return new ResponseEntity<String>(StringUtils.EMPTY, HttpStatus.UNAUTHORIZED);
        }

        notesService.deleteNotes(userId, notes);
        return new ResponseEntity<String>(StringUtils.EMPTY, HttpStatus.ACCEPTED);
    }

    @PostMapping("/backupNotes")
    public ResponseEntity<String> backupNotes(@RequestHeader("authorization") String authReqHeader, @RequestBody Notes notes) {
        log.info("backupNotes | number of notes: " + notes.getNoteEntries().size());
        AuthResponse authResponse = authenticator.evaluateAuthHeader(authReqHeader);
        String userId = authResponse.getUserId();
        String encryptionKey = authResponse.getEncryptionKey();
        if (!authResponse.isAuthenticationSuccessful()) {
            log.error("backupNotes | Authentication Unsuccessful | userId:" + userId);
            return new ResponseEntity<String>(StringUtils.EMPTY, HttpStatus.UNAUTHORIZED);
        }
        String notebook = notes.getNoteEntries().get(0).getNotebook();
        notesService.deleteNotebook(userId, notes.getNoteEntries().get(0).getNotebook());
        log.info("backupNotes | deleteNotebook | notebook: " + notebook);
        notesService.saveNotes(userId, encryptionKey, notes);
        log.info("backupNotes | saveNotes | notebook: " + notebook);
        return new ResponseEntity<String>(StringUtils.EMPTY, HttpStatus.ACCEPTED);
    }

    @PostMapping("/deleteNotebook")
    public ResponseEntity<String> deleteNotebook(@RequestHeader("authorization") String authReqHeader,
            @RequestBody NotebookRequest notebookRequest) {
        log.info("deleteNotebook | notebookRequest: " + notebookRequest);
        AuthResponse authResponse = authenticator.evaluateAuthHeader(authReqHeader);
        String userId = authResponse.getUserId();
        if (!authResponse.isAuthenticationSuccessful()) {
            log.error("deleteNotebook | Authentication Unsuccessful | userId:" + userId);
            return new ResponseEntity<String>(StringUtils.EMPTY, HttpStatus.UNAUTHORIZED);
        }

        notesService.deleteNotebook(userId, notebookRequest.getNotebookToBeDeleted());
        return new ResponseEntity<String>(StringUtils.EMPTY, HttpStatus.ACCEPTED);
    }

    @PostMapping("/renameNotebook")
    public ResponseEntity<String> renameNotebook(@RequestHeader("authorization") String authReqHeader,
            @RequestBody NotebookRequest notebookRequest) {
        log.info("renameNotebook | notebookRequest: " + notebookRequest);
        AuthResponse authResponse = authenticator.evaluateAuthHeader(authReqHeader);
        String userId = authResponse.getUserId();
        String encryptionKey = authResponse.getEncryptionKey();
        if (!authResponse.isAuthenticationSuccessful()) {
            log.error("renameNotebook | Authentication Unsuccessful | userId:" + userId);
            return new ResponseEntity<String>(StringUtils.EMPTY, HttpStatus.UNAUTHORIZED);
        }

        Notes notes = notesService.getUserNotesForNotebook(userId, encryptionKey, notebookRequest.getNotebookToBeRenamed());
        notes.getNoteEntries().forEach((note) -> note.setNotebook(notebookRequest.getNotebookNewName()));
        notesService.saveNotes(userId, encryptionKey, notes);
        notesService.deleteNotebook(userId, notebookRequest.getNotebookToBeRenamed());
        return new ResponseEntity<String>(StringUtils.EMPTY, HttpStatus.ACCEPTED);
    }

}
