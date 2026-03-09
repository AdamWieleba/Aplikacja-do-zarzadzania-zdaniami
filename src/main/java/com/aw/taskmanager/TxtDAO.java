package com.aw.taskmanager;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class TxtDAO implements TaskDAO {
    private String file;

    public TxtDAO() {
        file = "Tasks.txt";
    }
    
    public TxtDAO(String file) {
        this.file = file;
    }

    @Override
    public void saveAll(List<Task> list) {
        StringBuilder sb = new StringBuilder();

        for (Task task : list) {
            sb.append(buildTask(task));
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {
            writer.write(sb.toString());
        } catch (IOException e) {
            System.err.println("Błąd podczas zapisu do pliku: " + e.getMessage());
        }
    }

    @Override
    public List<Task> loadAll() {
        //uzupełnić
        return null;
    }

    @Override
    public void delete(Task task) {
        List<Task> list = loadAll();
        list.remove(task);
        saveAll(list);
    }
    
    public StringBuilder buildTask(Task task) {
        StringBuilder sb = new StringBuilder(task.isArchived() ? "[Archived]\n" : "\n");
        sb.append(task.getName())
            .append("\n")
            .append(task.getDescr().replace("\n", "`")) //pozwoli zapisać des w 1 linii
            .append("\n\n");

        return sb;
    }
}
