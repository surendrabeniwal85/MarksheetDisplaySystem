package com.marksheet.model;

/** One subject row on a grade card. */
public record Subject(String code, String title, int credits, String grade) {
    public Subject {
        if (code == null || title == null || grade == null) {
            throw new IllegalArgumentException("Subject fields cannot be null.");
        }
        if (credits < 0) {
            throw new IllegalArgumentException("Credits cannot be negative.");
        }
    }
}
