package com.marksheet.service;

import com.marksheet.model.Marksheet;
import java.util.ArrayList;
import java.util.List;

/** Generic, type-safe storage for marksheets. */
public final class MarksheetRepository<T extends Marksheet> {
    private final List<T> records = new ArrayList<>();

    public void add(T record) {
        if (record == null) {
            throw new IllegalArgumentException("Marksheet cannot be null.");
        }
        records.add(record);
    }

    public List<T> findAll() {
        return List.copyOf(records);
    }

    public int size() {
        return records.size();
    }
}
