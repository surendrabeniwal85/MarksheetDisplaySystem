package com.marksheet.ui;

import com.marksheet.service.MarksheetInputService;

import javax.swing.SwingUtilities;

public final class Main {
    private Main() {}

    public static void main(String[] args) {
        String mode = args.length == 0 ? "gui" : args[0].toLowerCase();
        switch (mode) {
            case "gui", "desktop" -> SwingUtilities.invokeLater(() ->
                    new MarksheetFrame().setVisible(true));
            case "console" -> {
                var record = MarksheetInputService.readFromConsole();
                System.out.println(record.toTextBlock());
            }
            case "help" -> System.out.println("""
                    Marksheet Display System
                    Run desktop GUI: mvn exec:java
                    Run console input: mvn exec:java -Dexec.args=console
                    Enter student details and subject rows, then export the displayed record as PDF.
                    """);
            default -> System.out.println("Unknown mode. Use gui, console, or help.");
        }
    }
}
