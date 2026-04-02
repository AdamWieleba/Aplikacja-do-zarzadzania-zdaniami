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
        List<Task> orgTasks = new ArrayList<>();
        orgTasks.add(new Task("name1", descr, difficulty, priority, notes, true));
        orgTasks.add(new Task("name2", descr, difficulty, priority, notes, false));
        orgTasks.add(new Task("name3", descr, difficulty, priority, notes, false));

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

        Task toDelete = new Task("name3", descr, difficulty, priority, notes, false);
        List<Task> expectedTasks = new ArrayList<>();
        expectedTasks.add(new Task("name1", descr, difficulty, priority, notes, false));
        expectedTasks.add(new Task("name2", descr, difficulty, priority, notes, false));
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
