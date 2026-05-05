package com.aw.taskmanager.model;

import java.util.Calendar;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


class TaskTest {
    private Task task;

    @BeforeEach
    void setUp() {
        task = new Task();
    }

    @Test
    void shouldNotEqualNullAfterEmptyConstructor() {
        assertNotEquals(task, null);
    }

    @Test
    void shouldGenerateIdOnEmptyConstructor() {
        task.generateId();
        assertNotNull(task.getId());
        assertEquals(32, task.getId().length()); // UUID bez myślników
    }

    @Test
    void shouldNotRegenerateIdIfAlreadySet() {
        task.generateId();
        String originalId = task.getId();
        task.generateId();
        assertEquals(originalId, task.getId());
    }

    @Test
    void shouldCreateTaskWithAllParameters() {
        Task t = new Task("Zadanie", "Opis", "trudne", 8.5, 10, "Notatka", false, new Date());
        assertEquals("Zadanie", t.getName());
        assertEquals("Opis", t.getDescr());
        assertEquals("trudne", t.getDifficultyStr());
        assertEquals(8.5, t.getDifficultyDbl());
        assertEquals(10, t.getImportance());
        assertEquals("Notatka", t.getNotes());
        assertFalse(t.isArchived());
        assertNotNull(t.getId());
    }

    @Test
    void shouldHandleNullDifficultyDblAsZero() {
        Task t = new Task("Test", "Test", "easy", null, 5, "Notes", false, null);
        assertEquals(0.0, t.getDifficultyDbl());
    }

    @Test
    void shouldHandleNullImportanceAsZero() {
        Task t = new Task("Test", "Test", "easy", 5.0, null, "Notes", false, null);
        assertEquals(0, t.getImportance());
    }

    @Test
    void shouldReturnDefaultDeadlineWhenNull() {
        task.setDeadline(null);
        Date deadline = task.getDeadline();
        assertNotNull(deadline);
        // Sprawdź, że to rok 2999
        Calendar cal = Calendar.getInstance();
        cal.setTime(deadline);
        assertEquals(2999, cal.get(Calendar.YEAR));
    }

    @Test
    void shouldEqualsBasedOnId() {
        Task t1 = new Task("1", "", "", 1.0, 1, "", false, null);
        Task t2 = new Task("1", "", "", 1.0, 1, "", false, null); // te same parametry

        assertNotEquals(t1, t2);
    }

    @Test
    void shouldAddDependencyBidirectionally() {
        Task task1 = new Task("T1", "D1", "e", 1.0, 1, "N", false, null);
        Task task2 = new Task("T2", "D2", "e", 1.0, 1, "N", false, null);

        Dependency dep = new Dependency(task1, task2, "Zależy od");
        task1.addDependency(dep);

        assertTrue(task1.getDependencies().contains(dep));
        assertTrue(task2.getDependencies().contains(dep));
    }

    @Test
    void shouldRemoveDependencyBidirectionally() {
        Task task1 = new Task("T1", "D1", "e", 1.0, 1, "N", false, null);
        task1.generateId();
        Task task2 = new Task("T2", "D2", "e", 1.0, 1, "N", false, null);
        task2.generateId();

        Dependency dep = new Dependency(task1, task2, "Zależy od");
        task1.addDependency(dep);
        task1.removeDependency(dep);

        assertFalse(task1.getDependencies().contains(dep));
        assertFalse(task2.getDependencies().contains(dep));
    }

    @Test
    void shouldInitializeDependenciesAsEmptyList() {
        Task t = new Task();
        assertNotNull(t.getDependencies());
        assertTrue(t.getDependencies().isEmpty());
    }
}