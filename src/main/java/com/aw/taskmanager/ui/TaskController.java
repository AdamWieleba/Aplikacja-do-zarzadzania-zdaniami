package com.aw.taskmanager.ui;

import com.aw.taskmanager.model.Task;
import com.aw.taskmanager.builder.TaskBuilder;
import com.aw.taskmanager.dao.TaskDAO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class TaskController {

    private final TaskDAO dao;
    private final List<Task> tasks = new ArrayList<>();

    public TaskController(TaskDAO dao) {
        this.dao = dao;
        tasks.addAll(dao.loadAll());
    }

    public List<Task> getTasks() {
        return Collections.unmodifiableList(tasks);
    }

    public Task createTask(String name, String descr, String difficultyStr, Double difficultyInt, Integer priority, String notes, boolean archived) {
        Task task = new TaskBuilder()
            .name(name)
            .descr(descr)
            .difficultyStr(difficultyStr)
            .difficultyInt(difficultyInt)
            .priority(priority)
            .notes(notes)
            .isArchived(archived)
            .build();
        tasks.add(task);
        return task;
    }

    public Optional<Task> findTaskById(String id) {
        return tasks.stream()
            .filter(task -> task.getId().equals(id))
            .findFirst();
    }

    public boolean deleteTask(String id) {
        Optional<Task> task = findTaskById(id);
        task.ifPresent(tasks::remove);
        return task.isPresent();
    }

    public boolean setArchived(String id, boolean archived) {
        Optional<Task> task = findTaskById(id);
        task.ifPresent(t -> t.setArchived(archived));
        return task.isPresent();
    }

    public void saveAll() {
        dao.saveAll(tasks);
    }
}