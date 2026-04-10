package com.example.dontpanicplanner;

import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TaskSplitter.
 * Run by right-clicking the test file and choosing Run All Tests.
 *
 * Sprint 2 - Abdur Rahman
 */
class TaskSplitterTest {

    private final PriorityScoreService scoreService = new PriorityScoreService();

    private Task makeTask(String name, double estimatedHours) {
        return new Task(name, "Assignment", estimatedHours,
                LocalDate.now().plusDays(5), 20, 85.0);
    }

    // ── canSplit() tests ──────────────────────────────────────

    @Test
    void canSplit_underThirtyMin_returnsFalse() {
        Task task = makeTask("Quick Task", 0.25);
        assertFalse(TaskSplitter.canSplit(task),
                "Task under 30 min should NOT be splittable");
    }

    @Test
    void canSplit_exactlyThirtyMin_returnsTrue() {
        Task task = makeTask("Half Hour Task", 0.5);
        assertTrue(TaskSplitter.canSplit(task),
                "Task of exactly 30 min should be splittable");
    }

    @Test
    void canSplit_nullEstimatedTime_returnsFalse() {
        Task task = new Task("No Time", LocalDate.now().plusDays(3));
        assertFalse(TaskSplitter.canSplit(task));
    }

    // ── split() tests — no split needed ──────────────────────

    @Test
    void split_tooShort_returnsOriginal() {
        Task task = makeTask("Short Task", 0.25);
        List<Task> result = TaskSplitter.split(task, 2, scoreService);
        assertEquals(1, result.size());
        assertSame(task, result.get(0));
    }

    @Test
    void split_onlyOneSession_returnsOriginal() {
        Task task = makeTask("Essay", 2.0);
        List<Task> result = TaskSplitter.split(task, 1, scoreService);
        assertEquals(1, result.size());
        assertSame(task, result.get(0));
    }

    @Test
    void split_sessionWouldBeTooShort_returnsOriginal() {
        // 0.5 hours / 2 sessions = 0.25 hours per session — under minimum
        Task task = makeTask("Half Hour", 0.5);
        List<Task> result = TaskSplitter.split(task, 2, scoreService);
        assertEquals(1, result.size());
    }

    // ── split() tests — split needed ─────────────────────────

    @Test
    void split_twoHourTaskIntoFourSessions_returnsFourSessions() {
        Task task = makeTask("Research Essay", 2.0);
        List<Task> sessions = TaskSplitter.split(task, 4, scoreService);
        assertEquals(4, sessions.size());
    }

    @Test
    void split_eachSessionIsThirtyMin() {
        Task task = makeTask("Research Essay", 2.0);
        List<Task> sessions = TaskSplitter.split(task, 4, scoreService);
        for (Task session : sessions) {
            assertEquals(0.5, session.getEstimatedTime(), 0.001,
                    "Each session should be 30 minutes");
        }
    }

    @Test
    void split_sessionNamesContainPartNumbers() {
        Task task = makeTask("Group Project", 2.0);
        List<Task> sessions = TaskSplitter.split(task, 4, scoreService);
        assertTrue(sessions.get(0).getName().contains("Part 1/4"));
        assertTrue(sessions.get(3).getName().contains("Part 4/4"));
    }

    @Test
    void split_sessionsInheritOriginalFields() {
        Task task = makeTask("Final Prep", 2.0);
        List<Task> sessions = TaskSplitter.split(task, 4, scoreService);
        for (Task session : sessions) {
            assertEquals(task.getDueDate(),     session.getDueDate());
            assertEquals(task.getGradeWeight(), session.getGradeWeight());
            assertEquals(task.getTaskType(),    session.getTaskType());
            assertEquals(task.getCurrentGrade(),session.getCurrentGrade());
        }
    }

    @Test
    void split_priorityScoreIsRecomputed() {
        Task task = makeTask("Essay", 2.0);
        List<Task> sessions = TaskSplitter.split(task, 4, scoreService);
        for (Task session : sessions) {
            assertNotNull(session.getPriorityScore(),
                    "Priority score should be recomputed after splitting");
        }
    }

    // ── splitInto30MinSessions() tests ────────────────────────

    @Test
    void splitInto30Min_twoHourTask_returnsFourSessions() {
        Task task = makeTask("Long Essay", 2.0);
        List<Task> sessions = TaskSplitter.splitInto30MinSessions(task, scoreService);
        assertEquals(4, sessions.size());
    }

    @Test
    void splitInto30Min_shortTask_returnsOriginal() {
        Task task = makeTask("Quick Read", 0.25);
        List<Task> sessions = TaskSplitter.splitInto30MinSessions(task, scoreService);
        assertEquals(1, sessions.size());
        assertSame(task, sessions.get(0));
    }

    @Test
    void splitInto30Min_priorityRecomputed() {
        Task task = makeTask("Study Session", 1.5);
        List<Task> sessions = TaskSplitter.splitInto30MinSessions(task, scoreService);
        for (Task session : sessions) {
            assertNotNull(session.getPriorityScore());
        }
    }
    @Test
void getUnscheduledTasks_returnsTasksNotInSchedule() {
    // Create two tasks
    Task task1 = makeTask("Essay", 1.0);
    Task task2 = makeTask("Quiz", 0.5);

    TaskDataStructure<Task> tasks = new TaskDataStructure<>();
    tasks.add(task1);
    tasks.add(task2);

    // Only schedule task1
    List<Task> scheduledList = new ArrayList<>();
    scheduledList.add(task1);

    AvailabilityBlock block = new AvailabilityBlock("Monday", "09:00", "10:00");
    ScheduledTaskGroup group = new ScheduledTaskGroup(List.of(block), scheduledList);

    List<ScheduledTaskGroup> scheduledGroups = new ArrayList<>();
    scheduledGroups.add(group);

    // task2 should be unscheduled
    ScheduleGenerator generator = new ScheduleGenerator();
    List<Task> unscheduled = generator.getUnscheduledTasks(tasks, scheduledGroups);

    assertEquals(1, unscheduled.size());
    assertEquals("Quiz", unscheduled.get(0).getName());
}
}
