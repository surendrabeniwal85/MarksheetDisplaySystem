package com.marksheet.ui;

import com.marksheet.model.Marksheet;
import com.marksheet.model.StudentMarksheet;
import com.marksheet.model.Subject;
import com.marksheet.pdf.PdfMarksheetExporter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public final class MarksheetFrame extends JFrame {
    private final List<Marksheet> marksheets = new ArrayList<>();
    private final PdfMarksheetExporter pdfExporter = new PdfMarksheetExporter();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "marksheet-timeout");
        t.setDaemon(true);
        return t;
    });
    private int currentIndex;
    private ScheduledFuture<?> timeoutTask;

    private final JLabel institutionLabel = new JLabel("", SwingConstants.CENTER);
    private final JLabel programmeLabel = new JLabel("", SwingConstants.CENTER);
    private final JLabel semesterLabel = new JLabel("", SwingConstants.CENTER);
    private final JLabel studentLabel = new JLabel();
    private final JLabel registrationLabel = new JLabel();
    private final JLabel fatherLabel = new JLabel();
    private final JLabel sgpaLabel = new JLabel();
    private final JLabel creditsLabel = new JLabel();
    private final JLabel timerLabel = new JLabel("Auto-next in 20 seconds", SwingConstants.CENTER);
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"SUBJECT CODE", "SUBJECT TITLE", "CREDIT", "GRADE"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JButton previousButton = new JButton("Previous");
    private final JButton nextButton = new JButton("Next");
    private final JButton addButton = new JButton("Enter New Grade Card");
    private final JButton pdfButton = new JButton("Export PDF");

    public MarksheetFrame() {
        super("Marksheet Display System");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(900, 680));
        setLocationRelativeTo(null);
        buildUi();
        addButton.addActionListener(e -> enterGradeCard());
        previousButton.addActionListener(e -> moveBy(-1));
        nextButton.addActionListener(e -> moveBy(1));
        pdfButton.addActionListener(e -> exportCurrentPdf());
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosed(java.awt.event.WindowEvent e) {
                stopTimeout();
                scheduler.shutdownNow();
            }
        });
        SwingUtilities.invokeLater(this::enterGradeCard);
    }

    private void buildUi() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(18, 22, 16, 22));
        root.setBackground(Color.WHITE);
        setContentPane(root);

        JPanel header = new JPanel(new GridLayout(0, 1, 0, 5));
        header.setBackground(Color.WHITE);
        header.add(new AcademicLogoPanel());
        institutionLabel.setFont(new Font("SansSerif", Font.BOLD, 17));
        programmeLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        semesterLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        JLabel title = new JLabel("GRADE CARD", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        header.add(title);
        header.add(institutionLabel);
        header.add(programmeLabel);
        header.add(semesterLabel);
        JLabel disclaimer = new JLabel("UNOFFICIAL GRADE SUMMARY", SwingConstants.CENTER);
        disclaimer.setForeground(new Color(150, 45, 45));
        disclaimer.setFont(new Font("SansSerif", Font.BOLD, 10));
        header.add(disclaimer);
        root.add(header, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(10, 12));
        body.setBackground(Color.WHITE);
        body.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY),
                new EmptyBorder(16, 14, 16, 14)));

        JPanel details = new JPanel(new GridLayout(0, 2, 12, 10));
        details.setBackground(Color.WHITE);
        for (JLabel label : List.of(studentLabel, registrationLabel, fatherLabel)) {
            label.setFont(new Font("SansSerif", Font.BOLD, 12));
        }
        details.add(studentLabel);
        details.add(registrationLabel);
        details.add(fatherLabel);
        details.add(new JLabel(""));
        body.add(details, BorderLayout.NORTH);

        JTable table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setFont(new Font("SansSerif", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.setFillsViewportHeight(true);
        table.setShowGrid(true);
        table.setGridColor(Color.GRAY);
        body.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel summary = new JPanel(new GridLayout(1, 2));
        summary.setBackground(Color.WHITE);
        sgpaLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        creditsLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        summary.add(sgpaLabel);
        summary.add(creditsLabel);
        body.add(summary, BorderLayout.SOUTH);
        root.add(body, BorderLayout.CENTER);

        JPanel controls = new JPanel(new BorderLayout(0, 8));
        controls.setBackground(Color.WHITE);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        buttons.setBackground(Color.WHITE);
        buttons.add(addButton);
        buttons.add(previousButton);
        buttons.add(nextButton);
        buttons.add(pdfButton);
        controls.add(timerLabel, BorderLayout.NORTH);
        controls.add(buttons, BorderLayout.CENTER);
        root.add(controls, BorderLayout.SOUTH);
    }

    private void enterGradeCard() {
        JTextField name = new JTextField();
        JTextField reg = new JTextField();
        JTextField guardian = new JTextField();
        JTextField institution = new JTextField("Beniwal Engineering College");
        JTextField programme = new JTextField("Bachelor of Technology");
        JTextField semester = new JTextField("SIXTH SEMESTER");
        JTextField sgpa = new JTextField("8.50");
        JTextField subjectCount = new JTextField("6");

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("Student name:")); form.add(name);
        form.add(new JLabel("Registration number:")); form.add(reg);
        form.add(new JLabel("Father/guardian name:")); form.add(guardian);
        form.add(new JLabel("Institution/school:")); form.add(institution);
        form.add(new JLabel("Programme:")); form.add(programme);
        form.add(new JLabel("Semester:")); form.add(semester);
        form.add(new JLabel("SGPA (0–10):")); form.add(sgpa);
        form.add(new JLabel("Number of subjects (1–20):")); form.add(subjectCount);

        int answer = JOptionPane.showConfirmDialog(this, form,
                "Enter Grade Card Details", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (answer != JOptionPane.OK_OPTION) {
            if (marksheets.isEmpty()) dispose();
            return;
        }

        try {
            String studentName = required(name.getText(), "Student name");
            String registration = required(reg.getText(), "Registration number");
            String fatherName = required(guardian.getText(), "Father/guardian name");
            String institutionName = required(institution.getText(), "Institution/school");
            String programmeName = required(programme.getText(), "Programme");
            String semesterName = required(semester.getText(), "Semester");
            double sgpaValue = Double.parseDouble(sgpa.getText().trim());
            int count = Integer.parseInt(subjectCount.getText().trim());
            if (sgpaValue < 0 || sgpaValue > 10 || count < 1 || count > 20) {
                throw new IllegalArgumentException("SGPA must be 0–10 and subjects must be 1–20.");
            }

            List<Subject> subjects = new ArrayList<>();
            for (int i = 1; i <= count; i++) {
                JTextField code = new JTextField();
                JTextField title = new JTextField();
                JTextField credits = new JTextField("3");
                JTextField grade = new JTextField("A");
                JPanel subjectForm = new JPanel(new GridLayout(0, 2, 8, 8));
                subjectForm.add(new JLabel("Subject code:")); subjectForm.add(code);
                subjectForm.add(new JLabel("Subject title:")); subjectForm.add(title);
                subjectForm.add(new JLabel("Credits:")); subjectForm.add(credits);
                subjectForm.add(new JLabel("Grade:")); subjectForm.add(grade);

                int subjectAnswer = JOptionPane.showConfirmDialog(this, subjectForm,
                        "Subject " + i + " of " + count, JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE);
                if (subjectAnswer != JOptionPane.OK_OPTION) return;
                int creditValue = Integer.parseInt(credits.getText().trim());
                if (creditValue < 0 || creditValue > 30) {
                    throw new IllegalArgumentException("Subject credits must be from 0 to 30.");
                }
                subjects.add(new Subject(required(code.getText(), "Subject code"),
                        required(title.getText(), "Subject title"), creditValue,
                        required(grade.getText(), "Grade")));
            }

            StudentMarksheet record = new StudentMarksheet(studentName, registration,
                    fatherName, institutionName, programmeName, semesterName, subjects, sgpaValue);
            marksheets.add(record);
            showMarksheet(marksheets.size() - 1);
            startTimeout();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Enter valid numeric values for SGPA, subject count, and credits.",
                    "Invalid input", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Invalid input", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static String required(String value, String label) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(label + " is required.");
        return value.trim();
    }

    private void showMarksheet(int index) {
        if (marksheets.isEmpty()) return;
        currentIndex = Math.floorMod(index, marksheets.size());
        Marksheet m = marksheets.get(currentIndex);
        institutionLabel.setText(m.institutionName());
        programmeLabel.setText(m.programme());
        semesterLabel.setText(m.semester());
        studentLabel.setText("NAME: " + m.studentName());
        registrationLabel.setText("REG. NO.: " + m.registrationNumber());
        fatherLabel.setText("FATHER/GUARDIAN: " + m.fatherName());
        tableModel.setRowCount(0);
        m.subjects().forEach(s -> tableModel.addRow(
                new Object[]{s.code(), s.title(), s.credits(), s.grade()}));
        tableModel.addRow(new Object[]{"TOTAL", "", m.totalCredits(), "---"});
        sgpaLabel.setText(String.format("SGPA: %.2f", m.sgpa()));
        creditsLabel.setText("CREDITS EARNED: " + m.totalCredits());
        previousButton.setEnabled(marksheets.size() > 1);
        nextButton.setEnabled(marksheets.size() > 1);
        setTitle("Marksheet Display System — " + (currentIndex + 1) + " of " + marksheets.size());
    }

    private void moveBy(int offset) {
        if (marksheets.isEmpty()) return;
        showMarksheet(currentIndex + offset);
        startTimeout();
    }

    private void startTimeout() {
        stopTimeout();
        timerLabel.setText("Auto-next in 20 seconds");
        timeoutTask = scheduler.schedule(() -> SwingUtilities.invokeLater(() -> {
            if (!isDisplayable() || marksheets.size() < 2) return;
            showMarksheet(currentIndex + 1);
            startTimeout();
        }), 20, TimeUnit.SECONDS);
    }

    private void stopTimeout() {
        if (timeoutTask != null) timeoutTask.cancel(false);
    }

    private void exportCurrentPdf() {
        if (marksheets.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a grade card first.");
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save grade summary as PDF");
        chooser.setSelectedFile(new java.io.File("grade-card.pdf"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        Path path = chooser.getSelectedFile().toPath();
        if (!path.toString().toLowerCase().endsWith(".pdf")) path = Path.of(path + ".pdf");
        try {
            pdfExporter.export(marksheets.get(currentIndex), path);
            JOptionPane.showMessageDialog(this, "PDF saved to:\n" + path,
                    "PDF exported", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not export PDF:\n" + ex.getMessage(),
                    "Export error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** A custom academic emblem, deliberately not a copy of an institutional logo. */
    private static final class AcademicLogoPanel extends JPanel {
        AcademicLogoPanel() {
            setPreferredSize(new Dimension(100, 62));
            setBackground(Color.WHITE);
        }
        @Override protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int cx = getWidth() / 2;
            g.setColor(new Color(35, 65, 105));
            g.setStroke(new BasicStroke(2f));
            g.drawOval(cx - 23, 2, 46, 46);
            g.setFont(new Font("Serif", Font.BOLD, 19));
            g.drawString("A", cx - 7, 32);
            g.setFont(new Font("SansSerif", Font.BOLD, 9));
            g.drawString("ACADEMIC GRADE CARD", cx - 67, 60);
            g.dispose();
        }
    }
}
