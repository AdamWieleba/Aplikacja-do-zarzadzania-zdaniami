package com.aw.taskmanager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
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
        List<Task> list = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            while(line != null) {
                Task task = new Task();
                task.setArchivedState(line.equals("[Archived]\n"));
                line = reader.readLine();

                task.setName(line);
                line = reader.readLine();

                task.setDescr(line.replace("`", "\n"));
                line = reader.readLine();

                task.setDifficulty(line);
                line = reader.readLine();

                task.setPriority(Integer.parseInt(line));
                line = reader.readLine();

                task.setNotes(line.replace("`", "\n"));
                line = reader.readLine();
                
                line = reader.readLine();
                list.add(task);
            }
        } catch (IOException e) {
            System.err.println("Błąd podczas zapisu do pliku: " + e.getMessage());
        }
        return list;
    }

    @Override
    public void delete(Task task) {
        List<Task> list = loadAll();
        list.remove(task);
        saveAll(list);
    }
    
    public StringBuilder buildTask(Task task) {
        StringBuilder sb = new StringBuilder();
        appendLine(sb, task.isArchived() ? "[Archived]" : "");

        appendLine(sb, task.getName());
        appendLine(sb, task.getDescr().replace("\n", "`")); //zapisuje w 1 linii
        appendLine(sb, task.getDifficulty());
        appendLine(sb, task.getPriority());
        appendLine(sb, task.getNotes().replace("\n", "`"));

        sb.append("\n");
        return sb;
    }

    private StringBuilder appendLine(StringBuilder sb, String line) {
        return sb.append(line).append("\n");
    }

    private StringBuilder appendLine(StringBuilder sb, Object o) {
        return sb.append(o.toString()).append("\n");
    }
}
