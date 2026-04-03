package com.example.dontpanicplanner;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    @Test
    void testConstructorAllFields() {
        LocalDate dueDate = LocalDate.of(2026, 3, 25);

        Task task = new Task(
                "Study for midterm",
                "Exam",
                3.5,
                dueDate,
                20,
                88.5,
                10
        );

        assertEquals("Study for midterm", task.getName());
        assertEquals("Exam", task.getTaskType());
        assertEquals(3.5, task.getEstimatedTime());
        assertEquals(dueDate, task.getDueDate());
        assertEquals(20, task.getGradeWeight());
        assertEquals(88.5, task.getCurrentGrade());
        assertEquals(10, task.getPriorityScore());
    }

    @Test
    void nonPriorityTaskTest() {
        Task task = new Task(
                "Homework 3",
                "Assignment",
                2.0,
                LocalDate.of(2026, 3, 30),
                15,
                92.0
        );

        assertEquals("Homework 3", task.getName());
        assertNull(task.getPriorityScore());
    }

    @Test
    void constructorNoFieldsTest() {
        Task task = new Task("Read chapter 4", LocalDate.of(2026, 4, 1));

        assertEquals("Read chapter 4", task.getName());
        assertEquals(LocalDate.of(2026, 4, 1), task.getDueDate());
        assertNull(task.getTaskType());
        assertNull(task.getEstimatedTime());
        assertNull(task.getGradeWeight());
        assertNull(task.getCurrentGrade());
        assertNull(task.getPriorityScore());
    }

    @Test
    void testSetter() {
        Task task = new Task();

        task.setId(99L);
        task.setName("Lab report");
        task.setTaskType("Lab");
        task.setEstimatedTime(2.5);
        task.setDueDate(LocalDate.of(2026, 4, 12));
        task.setGradeWeight(12);
        task.setCurrentGrade(91.0);
        task.setPriorityScore(8);

        assertEquals(99L, task.getId());
        assertEquals("Lab report", task.getName());
        assertEquals("Lab", task.getTaskType());
        assertEquals(2.5, task.getEstimatedTime());
        assertEquals(LocalDate.of(2026, 4, 12), task.getDueDate());
        assertEquals(12, task.getGradeWeight());
        assertEquals(91.0, task.getCurrentGrade());
        assertEquals(8, task.getPriorityScore());
    }

    @Test
    void CSVexportTest() {
        Task task = new Task(
                "Project",
                "Essay",
                4.0,
                LocalDate.of(2026, 4, 5),
                25,
                90.0,
                7
        );

        assertEquals("Project,Essay,4.0,2026-04-05,25,90.0,7", task.toCSV());
    }

    @Test
    void CSVwithNullFields() {
        Task task = new Task("Quick task", LocalDate.of(2026, 4, 2));

        assertEquals("Quick task,,,2026-04-02,,,", task.toCSV());
    }

    @Test
    void CSVformattingTest() {
        Task task = new Task(
                "Read \"important\", notes",
                "Study",
                1.5,
                LocalDate.of(2026, 4, 3),
                10,
                95.0,
                3
        );

        assertEquals("\"Read \"\"important\"\", notes\",Study,1.5,2026-04-03,10,95.0,3", task.toCSV());
    }

    @Test
    void CSVparsingTest() {
        String line = "Project,Essay,4.0,2026-04-05,25,90.0,7";

        Task task = Task.fromCSV(line);

        assertEquals("Project", task.getName());
        assertEquals("Essay", task.getTaskType());
        assertEquals(4.0, task.getEstimatedTime());
        assertEquals(LocalDate.of(2026, 4, 5), task.getDueDate());
        assertEquals(25, task.getGradeWeight());
        assertEquals(90.0, task.getCurrentGrade());
        assertEquals(7, task.getPriorityScore());
    }

    @Test
    void CSVblankInputsTest() {
        String line = "Temp task,,,2026-04-02,,,";

        Task task = Task.fromCSV(line);

        assertEquals("Temp task", task.getName());
        assertEquals("", task.getTaskType());
        assertNull(task.getEstimatedTime());
        assertEquals(LocalDate.of(2026, 4, 2), task.getDueDate());
        assertNull(task.getGradeWeight());
        assertNull(task.getCurrentGrade());
        assertNull(task.getPriorityScore());
    }

    @Test
    void fromCSVwithSpecialChars() {
        String line = "\"Read \"\"important\"\", notes\",Study,1.5,2026-04-03,10,95.0,3";

        Task task = Task.fromCSV(line);

        assertEquals("Read \"important\", notes", task.getName());
        assertEquals("Study", task.getTaskType());
        assertEquals(1.5, task.getEstimatedTime());
    }

    @Test
    void CSVerrorTest() {
        String badLine = "OnlyName,OnlyType";

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Task.fromCSV(badLine)
        );

        assertTrue(ex.getMessage().contains("CSV row needs 7 columns"));
    }

    @Test
    void toAndFromCSV() {
        Task original = new Task(
                "Capstone draft",
                "Paper",
                6.0,
                LocalDate.of(2026, 4, 10),
                30,
                87.5,
                9
        );

        String csv = original.toCSV();
        Task parsed = Task.fromCSV(csv);

        assertEquals(original.getName(), parsed.getName());
        assertEquals(original.getTaskType(), parsed.getTaskType());
        assertEquals(original.getEstimatedTime(), parsed.getEstimatedTime());
        assertEquals(original.getDueDate(), parsed.getDueDate());
        assertEquals(original.getGradeWeight(), parsed.getGradeWeight());
        assertEquals(original.getCurrentGrade(), parsed.getCurrentGrade());
        assertEquals(original.getPriorityScore(), parsed.getPriorityScore());
    }
}