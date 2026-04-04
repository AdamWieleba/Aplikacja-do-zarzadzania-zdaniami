package com.aw.taskmanager;

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
        String descr = "..\n..";
        String difficulty = "hard";
        Integer priority = 1;
        String notes = ".\n..\n.";
        Task task1 = new Task("name1", descr, difficulty, priority, notes, null, true);
        Task task2 = new Task("name2", descr, difficulty, priority, notes, null, false);
        List<Dependency> dependencies = new ArrayList<>();
        dependencies.add(new Dependency(task2, Dependency.DependencyType.FS));
        Task task3 = new Task("name3", descr, difficulty, priority, notes, dependencies, false);
        
        List<Task> orgTasks = new ArrayList<>();
        orgTasks.add(task1);
        orgTasks.add(task2);
        orgTasks.add(task3);

        // when
        taskDAO.saveAll(orgTasks);
        List<Task> loadedTasks = taskDAO.loadAll();

        // then
        assertEquals(orgTasks, loadedTasks);
    }

    @ParameterizedTest
    @MethodSource("daoProvider")
    public void testDelete(Class<? extends TaskDAO> DAOclass, String file) throws Exception {
        // given
        TaskDAO taskDAO = createDAO(DAOclass, file);
        String descr = "..\n..";
        String difficulty = "hard";
        Integer priority = 1;
        String notes = ".\n..\n.";
        Task task1 = new Task("name1", descr, difficulty, priority, notes, null, true);
        List<Dependency> dependencies = new ArrayList<>();
        dependencies.add(new Dependency(task1, Dependency.DependencyType.FS));
        Task task2 = new Task("name2", descr, difficulty, priority, notes, dependencies, false);

        Task toDelete = new Task("name3", descr, difficulty, priority, notes, null, false);
        List<Task> expectedTasks = new ArrayList<>();
        expectedTasks.add(task1);
        expectedTasks.add(task2);
        expectedTasks.add(toDelete);
        taskDAO.saveAll(expectedTasks);

        // when
        taskDAO.delete(toDelete);
        List<Task> loadedTasks = taskDAO.loadAll();
        expectedTasks.remove(toDelete);

        // then
        assertEquals(expectedTasks, loadedTasks);
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
