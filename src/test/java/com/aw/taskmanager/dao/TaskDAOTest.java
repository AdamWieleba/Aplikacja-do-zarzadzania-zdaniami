package com.aw.taskmanager.dao;

import com.aw.taskmanager.model.Task;
import com.aw.taskmanager.model.Dependency;
import com.aw.taskmanager.builder.TaskBuilder;

import java.util.List;
import java.util.stream.Stream;
import java.util.ArrayList;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;

public class TaskDAOTest {

    @ParameterizedTest
    @MethodSource("daoProvider")
    public void testSaveAll_LoadAll(Class<? extends TaskDAO> DAOclass, String file) throws Exception {
        // given
        TaskDAO taskDAO = createDAO(DAOclass, file);
        
        Task task1 = new TaskBuilder().name("name1").isArchived(true).build();
        Task task2 = createDefaultTestTask("name2");
        Task task3 = createDefaultTestTask("name3");
        task3.addDependency(new Dependency(task3, task2, "finish -> start"));
        
        List<Task> originalTasks = List.of(task1, task2, task3);

        // when
        taskDAO.saveAll(originalTasks);
        List<Task> loadedTasks = taskDAO.loadAll();

        // then
        assertEquals(originalTasks, loadedTasks);
    }

    @ParameterizedTest
    @MethodSource("daoProvider")
    public void testDelete(Class<? extends TaskDAO> DAOclass, String file) throws Exception {
        // given
        TaskDAO taskDAO = createDAO(DAOclass, file);

        Task task1 = createDefaultTestTask("name1");
        Task task2 = createDefaultTestTask("name2");
        Task toDelete = createDefaultTestTask("toDelete");

        List<Task> expectedTasks = new ArrayList<>(List.of(task1, task2, toDelete)); //samo List.of() tworzy niemodyfikowalną listę
        taskDAO.saveAll(expectedTasks);

        // when
        taskDAO.delete(toDelete);
        List<Task> loadedTasks = taskDAO.loadAll();
        expectedTasks.remove(toDelete);

        // then
        assertEquals(expectedTasks, loadedTasks);
    }

    private Task createDefaultTestTask(String name) {
        String descr = "..\n        .";
        String difficultyStr = "medium";
        Double difficultyInt = 5.0;
        Integer priority = 1;
        String notes = descr;
        boolean isArchived = false;
        return new Task(name, descr, difficultyStr, difficultyInt, priority, notes, isArchived);
    }

    private static Stream<Arguments> daoProvider() {
        return Stream.of(
            //Arguments.of(TxtDAO.class, "./target/DAOTest.txt")
            Arguments.of(XmlDAO.class, "./target/DAOTest.txt")
            //,Arguments.of(.class, "")
        );
    }
    
    private TaskDAO createDAO(Class<? extends TaskDAO> DAOclass, String file) {
        TaskDAO instance;
        try {
            instance = DAOclass.getDeclaredConstructor(String.class).newInstance(file);
        } catch (Exception e) {
            throw new RuntimeException("Błąd przy tworzeniu instancji klasy implementującej interfejs TaskDAO.", e);
        }
        return instance;
    }
    
}
