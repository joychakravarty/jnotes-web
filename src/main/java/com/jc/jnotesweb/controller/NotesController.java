package com.jc.jnotesweb.controller;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jc.jnotesweb.model.Notes;
import com.jc.jnotesweb.service.NotesService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class NotesController {
    
    @Autowired
    private NotesService notesService;
    
    @Autowired
    @Qualifier("dbProperties")
    private String dbProperties;
    
    @RequestMapping("/")
    String index(Map<String, Object> model) {
        System.out.println("dbProperties >> "+dbProperties);
        model.put("dbProperties", dbProperties);
      return "index";
    }

    @GetMapping("/getUserNotes")
    public @ResponseBody Notes getUserNotes (
            @RequestParam(value="userId", required=true) String userId,
            @RequestParam(value="notebook", required=false) String notebook) {
        log.info("userId : "+userId);
        log.info("notebook : "+notebook);
        Notes notes;
        if(StringUtils.isBlank(notebook)) {
            notes = notesService.getAllUserNotes(userId);
        }else {
            notes = notesService.getUserNotesForNotebook(userId, notebook);
        }
        return notes;
    }
    
    
    //POST
    
    //setupUser
    
    //DELETE
    
    //CREATE
    
    //EDIT
    
    
    
    
    
    
}
    
