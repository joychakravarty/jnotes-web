package com.jc.jnotesweb.model;

import java.time.LocalDateTime;

import javax.annotation.concurrent.Immutable;

import lombok.Getter;

@Getter
@Immutable
public final class NoteEntry {

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

    private final String notebook;
    private final String id;
    private final String key;
    private final String value;
    private final String info;
    private final boolean isPassword;
    private final LocalDateTime lastModifiedTime;

}
