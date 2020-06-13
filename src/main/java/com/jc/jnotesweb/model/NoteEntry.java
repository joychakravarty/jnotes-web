package com.jc.jnotesweb.model;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
public final class NoteEntry {
    
    public NoteEntry() {
    }

    public NoteEntry(String notebook, String id, String key, String value, String info, boolean isPassword,
            LocalDateTime lastModifiedTime) {
        super();
        this.notebook = notebook;
        this.id = id;
        this.key = key;
        this.value = value;
        this.info = info;
        this.isPassword = isPassword;
        this.lastModifiedTime = lastModifiedTime;
    }
    
    @ToString.Include
    private String notebook;
    @ToString.Include
    private String id;
    @ToString.Include
    private String key;
    private String value;
    private String info;
    private boolean isPassword;
    private LocalDateTime lastModifiedTime;

}
