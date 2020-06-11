package com.jc.jnotesweb.model;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public final class NoteEntry {
    
    public NoteEntry() {
    }

    public NoteEntry(String notebook, String noteId, String key, String value, String info, boolean isPassword,
            LocalDateTime lastModifiedTime) {
        super();
        this.notebook = notebook;
        this.noteId = noteId;
        this.key = key;
        this.value = value;
        this.info = info;
        this.isPassword = isPassword;
        this.lastModifiedTime = lastModifiedTime;
    }

    private String notebook;
    private String noteId;
    private String key;
    private String value;
    private String info;
    private boolean isPassword;
    private LocalDateTime lastModifiedTime;

}
