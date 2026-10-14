package com.marksheet.model;

import java.util.List;

public final class StudentMarksheet extends Marksheet {
    public StudentMarksheet(String studentName, String registrationNumber, String fatherName,
                            String institutionName, String programme, String semester,
                            List<Subject> subjects, double sgpa) {
        super(studentName, registrationNumber, fatherName, institutionName, programme,
                semester, subjects, sgpa);
    }

    @Override
    public String toTextBlock() {
        StringBuilder rows = new StringBuilder();
        for (Subject subject : subjects()) {
            rows.append(String.format("%-13s %-43s %7d %8s%n",
                    subject.code(), subject.title(), subject.credits(), subject.grade()));
        }
        return """
                ================================================================
                                  UNOFFICIAL GRADE SUMMARY
                ================================================================
                %s
                %s
                %s

                NAME: %-38s REG. NO.: %s
                FATHER/GUARDIAN: %s

                SUBJECT CODE  SUBJECT TITLE                                  CREDIT    GRADE
                ----------------------------------------------------------------
                %s----------------------------------------------------------------
                TOTAL                                                        %7d      ---

                SGPA: %.2f                                      CREDITS EARNED: %d

                       UNOFFICIAL SUMMARY — NOT ISSUED BY THE INSTITUTION
                ================================================================
                """.formatted(institutionName(), programme(), semester(),
                studentName(), registrationNumber(), fatherName(),
                rows, totalCredits(), sgpa(), totalCredits());
    }
}
