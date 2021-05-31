package com.jc.jnotesweb.model;

import java.time.LocalDateTime;
import java.util.Objects;

public final class NoteEntry {
    
    public NoteEntry() {
    }

    public String getNotebook() {
        return notebook;
    }

    public void setNotebook(String notebook) {
        this.notebook = notebook;
    }

    @Override
    public String toString() {
        return "NoteEntry{" +
                "notebook='" + notebook + '\'' +
                ", id='" + id + '\'' +
                ", key='" + key + '\'' +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public boolean isPassword() {
        return isPassword;
    }

    public void setPassword(boolean password) {
        isPassword = password;
    }

    public LocalDateTime getLastModifiedTime() {
        return lastModifiedTime;
    }

    public void setLastModifiedTime(LocalDateTime lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
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

    private String notebook;
    private String id;
    private String key;
    private String value;
    private String info;
    private boolean isPassword;
    private LocalDateTime lastModifiedTime;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NoteEntry noteEntry = (NoteEntry) o;
        return notebook.equals(noteEntry.notebook) && Objects.equals(id, noteEntry.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(notebook, id);
    }
}
