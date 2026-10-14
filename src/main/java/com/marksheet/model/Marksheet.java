package com.marksheet.model;

import java.util.List;

/** Sealed base class for generated grade summaries. */
public sealed abstract class Marksheet permits StudentMarksheet {
    private final String studentName;
    private final String registrationNumber;
    private final String fatherName;
    private final String institutionName;
    private final String programme;
    private final String semester;
    private final List<Subject> subjects;
    private final double sgpa;

    protected Marksheet(String studentName, String registrationNumber, String fatherName,
                        String institutionName, String programme, String semester,
                        List<Subject> subjects, double sgpa) {
        this.studentName = requireText(studentName, "Student name");
        this.registrationNumber = requireText(registrationNumber, "Registration number");
        this.fatherName = requireText(fatherName, "Father/guardian name");
        this.institutionName = requireText(institutionName, "Institution name");
        this.programme = requireText(programme, "Programme");
        this.semester = requireText(semester, "Semester");
        if (subjects == null || subjects.isEmpty()) {
            throw new IllegalArgumentException("Enter at least one subject.");
        }
        this.subjects = List.copyOf(subjects);
        if (!Double.isFinite(sgpa) || sgpa < 0 || sgpa > 10) {
            throw new IllegalArgumentException("SGPA must be between 0 and 10.");
        }
        this.sgpa = sgpa;
    }

    private static String requireText(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + " cannot be blank.");
        }
        return value.trim();
    }

    public String studentName() { return studentName; }
    public String registrationNumber() { return registrationNumber; }
    public String fatherName() { return fatherName; }
    public String institutionName() { return institutionName; }
    public String programme() { return programme; }
    public String semester() { return semester; }
    public List<Subject> subjects() { return subjects; }
    public double sgpa() { return sgpa; }
    public int totalCredits() {
        return subjects.stream().mapToInt(Subject::credits).sum();
    }

    public abstract String toTextBlock();
}
