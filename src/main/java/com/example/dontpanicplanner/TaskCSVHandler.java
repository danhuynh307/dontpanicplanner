package com.example.dontpanicplanner;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

// handles reading and writing Task objects to CSV files
public class TaskCSVHandler {

    // writes a list of tasks to a CSV file (overwrites if file exists)
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

    // reads tasks from a CSV file and skips header/empty lines
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

    // converts tasks into CSV format as a byte array (useful for downloads)
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

    // reads tasks from CSV byte data (useful for uploads)
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
