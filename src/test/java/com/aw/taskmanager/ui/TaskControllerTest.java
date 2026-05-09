package com.aw.taskmanager.ui;

import com.aw.taskmanager.model.Task;
import com.aw.taskmanager.dao.XmlDAO;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


public class TaskControllerTest {

    private TaskController taskController;
    
    @BeforeEach
    void setUp() {
        taskController = new TaskController(new XmlDAO());
    }

    // makeTitle()
    @Test
    void shouldReturnDefaultTitleWhenNameIsNull() {
        assertEquals("(Bez tytułu)", taskController.makeTitle(null));
    }
    
    @Test
    void shouldReturnDefaultTitleWhenNameIsEmpty() {
        assertEquals("(Bez tytułu)", taskController.makeTitle(""));
    }
    
    @Test
    void shouldReturnDefaultTitleWhenNameIsOnlyWhitespace() {
        assertEquals("(Bez tytułu)", taskController.makeTitle("   "));
    }
    
    @Test
    void shouldReturnTrimmedNameWhenNameIsValid() {
        assertEquals("Moje zadanie", taskController.makeTitle("Moje zadanie"));
    }
    
    @Test
    void shouldStripTrailingWhitespace() {
        assertEquals("Tekst", taskController.makeTitle("Tekst   "));
    }
    
    // findTaskById()
    @Test
    void shouldReturnEmptyOptionalWhenTaskNotFound() {
        Optional<Task> result = taskController.findTaskById("-1");
        assertFalse(result.isPresent());
    }
    
    @Test
    void shouldReturnTaskWhenIdExists() {
        createTaskThroughController("Test");
        String id = createTaskThroughController("Test2").getId();
        
        Optional<Task> result = taskController.findTaskById(id);

        assertTrue(result.isPresent());
        assertEquals("Test2", result.get().getName());
    }
    
    // deleteTask()
    @Test
    void shouldReturnFalseWhenTaskNotFound() {
        assertFalse(taskController.deleteTask("-1"));
    }
    
    @Test
    void shouldDeleteTaskAndReturnTrueWhenFound() {
        int initialSize = taskController.getTasks().size();
        String id = createTaskThroughController("").getId();
        
        assertTrue(taskController.deleteTask(id));
        assertEquals(initialSize, taskController.getTasks().size());
    }
    
    // setArchived()
    @Test
    void shouldReturnFalseWhenTaskNotFoundForArchive() {
        assertFalse(taskController.setArchived("-1", true));
    }
    
    @Test
    void shouldArchiveTaskAndReturnTrueWhenFound() {
        Task task = createTaskThroughController("");
        
        assertTrue(taskController.setArchived(task.getId(), true));
        assertTrue(task.isArchived());
    }
    
    @Test
    void shouldUnarchiveTaskWhenSetArchivedFalse() {
        Task task = createTaskThroughController("");
        
        taskController.setArchived(task.getId(), false);
        assertFalse(task.isArchived());
    }

    private Task createTaskThroughController(String name) {
        return taskController.createTask(name, null, null, null, null, null, false, null);
    }
}