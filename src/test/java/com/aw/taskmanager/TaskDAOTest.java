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
    public void testSaveAll_LoadAll(Class<? extends TaskDAO> DAOclass, String file) {
        // given
        TaskDAO taskDAO = createDAO(DAOclass, file);
        List<Task> orgTasks = new ArrayList<>();
        orgTasks.add(new Task("name1", "des...\ndes"));
        orgTasks.add(new Task("name2", "des\n...des"));
        orgTasks.add(new Task("name3", "des.\n..des"));

        // when
        taskDAO.saveAll(orgTasks);
        List<Task> loadedTasks = taskDAO.loadAll();

        // then
        assertEquals(orgTasks, loadedTasks);
    }

    private static Stream<Arguments> daoProvider() {
        return Stream.of(
            Arguments.of(TxtDAO.class, "./target/DAOTest.txt")
            //,Arguments.of(.class, "")
        );
    }/*Próbowałem wykorzystać klasy dziedziczące + super(.class, "-"), ale cały czas były błędy "JUnit nie wie 
    jak przekazać parametry", "flexible constructors są niedostępne" itp. Więc musiałem zrobić tak jak jest. */
    
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
