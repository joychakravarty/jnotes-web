package com.jc.jnotesweb.model;

import java.util.Collections;
import java.util.List;

import lombok.Getter;

@Getter
public final class Notes {

    public Notes(List<NoteEntry> noteEntries) {
        this.noteEntries = Collections.unmodifiableList(noteEntries);
    }

    private List<NoteEntry> noteEntries;

}
