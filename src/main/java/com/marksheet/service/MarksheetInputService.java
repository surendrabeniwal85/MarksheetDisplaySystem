package com.marksheet.service;

import com.marksheet.model.StudentMarksheet;
import com.marksheet.model.Subject;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/** Reads marksheet information from the console. */
public final class MarksheetInputService {
    private MarksheetInputService() {}

    public static StudentMarksheet readFromConsole() {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("=== Marksheet Display System ===");
            System.out.print("Student name: ");
            String name = required(scanner.nextLine(), "Student name");
            System.out.print("Registration number: ");
            String reg = required(scanner.nextLine(), "Registration number");
            System.out.print("Father/guardian name: ");
            String father = required(scanner.nextLine(), "Father/guardian name");
            System.out.print("Institution/school name (Beniwal Engineering College): ");
            String school = required(scanner.nextLine(), "Institution/school name");
            System.out.print("Programme: ");
            String programme = required(scanner.nextLine(), "Programme");
            System.out.print("Semester: ");
            String semester = required(scanner.nextLine(), "Semester");

            int count = readInt(scanner, "Number of subjects: ", 1, 20);
            List<Subject> subjects = new ArrayList<>();
            for (int i = 1; i <= count; i++) {
                System.out.printf("%nSubject %d%n", i);
                System.out.print("  Subject code: ");
                String code = required(scanner.nextLine(), "Subject code");
                System.out.print("  Subject title: ");
                String title = required(scanner.nextLine(), "Subject title");
                int credits = readInt(scanner, "  Credits: ", 0, 30);
                System.out.print("  Grade (e.g. O, A+, A, B+): ");
                String grade = required(scanner.nextLine(), "Grade");
                subjects.add(new Subject(code, title, credits, grade));
            }

            double sgpa = readDouble(scanner, "SGPA (0.00–10.00): ", 0, 10);
            return new StudentMarksheet(name, reg, father, school, programme,
                    semester, subjects, sgpa);
        }
    }

    private static String required(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + " cannot be blank.");
        }
        return value.trim();
    }

    private static int readInt(Scanner scanner, String prompt, int min, int max) {
        while (true) {
            try {
                System.out.print(prompt);
                int value = Integer.parseInt(scanner.nextLine().trim());
                if (value < min || value > max) throw new NumberFormatException();
                return value;
            } catch (NumberFormatException ex) {
                System.out.printf("Enter a whole number from %d to %d.%n", min, max);
            }
        }
    }

    private static double readDouble(Scanner scanner, String prompt, double min, double max) {
        while (true) {
            try {
                System.out.print(prompt);
                double value = Double.parseDouble(scanner.nextLine().trim());
                if (!Double.isFinite(value) || value < min || value > max) {
                    throw new NumberFormatException();
                }
                return value;
            } catch (NumberFormatException ex) {
                System.out.printf("Enter a number from %.2f to %.2f.%n", min, max);
            }
        }
    }
}
