package com.example.dontpanicplanner;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles reading and writing lists of Tasks to/from CSV files.
 *
 * Export: TaskCSVHandler.exportToFile(tasks, "tasks.csv")
 * Import: List<Task> tasks = TaskCSVHandler.importFromFile("tasks.csv")
 */
public class TaskCSVHandler {

    /**
     * Writes tasks to a CSV file on disk.
     * Creates the file if it doesn't exist; overwrites if it does.
     */
    public static void exportToFile(List<Task> tasks, String filePath) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(
                Paths.get(filePath), StandardCharsets.UTF_8)) {

            writer.write(Task.CSV_HEADER);
            writer.newLine();

            for (Task task : tasks) {
                writer.write(task.toCSV());
                writer.newLine();
            }
        }
    }

    /**
     * Reads tasks from a CSV file on disk.
     * Skips the header row and any blank lines.
     */
    public static List<Task> importFromFile(String filePath) throws IOException {
        List<Task> tasks = new ArrayList<>();
        List<String> lines = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);

        for (int i = 1; i < lines.size(); i++) {   // i = 1 skips header
            String line = lines.get(i).trim();
            if (!line.isEmpty()) {
                tasks.add(Task.fromCSV(line));
            }
        }
        return tasks;
    }

    /**
     * Returns the CSV content as a byte array.
     * Use this for HTTP download responses (no temp file needed).
     *
     * Example in a REST controller:
     *   byte[] csv = TaskCSVHandler.exportToBytes(tasks);
     *   return ResponseEntity.ok()
     *       .header("Content-Disposition", "attachment; filename=tasks.csv")
     *       .contentType(MediaType.parseMediaType("text/csv"))
     *       .body(csv);
     */
    public static byte[] exportToBytes(List<Task> tasks) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(out, StandardCharsets.UTF_8))) {

            writer.write(Task.CSV_HEADER);
            writer.newLine();

            for (Task task : tasks) {
                writer.write(task.toCSV());
                writer.newLine();
            }
        }
        return out.toByteArray();
    }

    /**
     * Parses CSV content from a byte array (e.g. from a multipart file upload).
     * Use this for HTTP upload endpoints.
     */
    public static List<Task> importFromBytes(byte[] csvBytes) throws IOException {
        List<Task> tasks = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new ByteArrayInputStream(csvBytes), StandardCharsets.UTF_8))) {

            reader.readLine();  // skip header

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    tasks.add(Task.fromCSV(line));
                }
            }
        }
        return tasks;
    }
}
