package com.aw.taskmanager.builder;

import com.aw.taskmanager.model.Dependency;
import com.aw.taskmanager.model.Task;
import java.util.Date;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


class TaskBuilderTest {
    @Test
    void shouldCreateNewTaskWithGeneratedId() {
        Task task = new TaskBuilder().build();
        
        assertNotNull(task);
        assertNotNull(task.getId());
    }

    @Test
    void shouldChainMethodCalls() {
        Task task = new TaskBuilder()
                .name("Moje zadanie")
                .descr("Opis")
                .difficultyStr("trudne")
                .difficultyDbl(7.5)
                .importance(8)
                .notes("Ważne!")
                .isArchived(false)
                .build();

        assertEquals("Moje zadanie", task.getName());
        assertEquals("Opis", task.getDescr());
        assertEquals("trudne", task.getDifficultyStr());
        assertEquals(7.5, task.getDifficultyDbl());
        assertEquals(8, task.getImportance());
        assertEquals("Ważne!", task.getNotes());
        assertFalse(task.isArchived());
    }

    @Test
    void shouldModifyExistingTask() {
        Task existing = new Task("Stare", "Stary opis", "easy", 2.0, 1, "Old", false, null);

        Task modified = new TaskBuilder(existing)
                .name("Nowe")
                .importance(10)
                .build();

        assertEquals("Nowe", modified.getName());
        assertEquals(10, modified.getImportance());
        assertEquals("Stary opis", modified.getDescr()); // Bez zmian
        assertEquals(existing, modified); // Ten sam obiekt
    }

    @Test
    void shouldAddAndRemoveDependencies() {
        Task src = new Task("Source task", "", "", 1.0, 1, "", false, null);
        Task dst = new Task("Destination task", "", "", 1.0, 1, "", false, null);
        TaskBuilder tb = new TaskBuilder(src);

        Dependency dep = new Dependency(src, dst, "Finish-to-start");
        
        tb.addDependency(dep);

        assertTrue(src.getDependencies().contains(dep));
        assertTrue(dst.getDependencies().contains(dep));

        tb.removeDependency(dep);
        
        assertFalse(src.getDependencies().contains(dep));
        assertFalse(dst.getDependencies().contains(dep));
    }

    @Test
    void shouldSetDeadline() {
        Date deadline = new Date();
        Task task = new TaskBuilder()
                .name("Task")
                .deadline(deadline)
                .build();

        assertEquals(deadline, task.getDeadline());
    }
}