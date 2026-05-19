package com.aw.taskmanager.ui;

import com.aw.taskmanager.model.Task;
import com.aw.taskmanager.model.Dependency;
import com.aw.taskmanager.builder.TaskBuilder;
import com.aw.taskmanager.dao.TaskDao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class TaskController {

    private final TaskDao dao;
    private final List<Task> tasks = new ArrayList<>();

    public TaskController(TaskDao dao) {
        this.dao = dao;
        tasks.addAll(dao.loadAll());
    }

    public List<Task> getTasks() {
        return Collections.unmodifiableList(tasks);
    }

    public Task createTask(String name, String descr, String difficultyStr, Double difficultyDbl, Integer importance, String notes, boolean archived, Date deadline) {
        Task task = new Task(makeTitle(name), descr, difficultyStr, difficultyDbl, importance, notes, archived, deadline);
        tasks.add(task);
        return task;
    }

    public boolean updateTask(String id, String name, String descr, String difficultyStr, Double difficultyDbl, Integer importance, String notes, boolean archived, Date deadline) {
        Optional<Task> existing = findTaskById(id);
        existing.ifPresent(task -> new TaskBuilder(task)
            .name(makeTitle(name))
            .descr(descr)
            .difficultyStr(difficultyStr)
            .difficultyDbl(difficultyDbl)
            .importance(importance)
            .notes(notes)
            .isArchived(archived)
            .deadline(deadline)
            .build());
        return existing.isPresent();
    }

    public String makeTitle(String name) {
        String trimmed = (name == null) ? "" : name.stripTrailing();
        return trimmed.isEmpty() ? "(Bez tytułu)" : trimmed;
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

    public void addDependency(Task src, Task dst, String description) {
        Dependency dep = new Dependency(src, dst, description);
        src.addDependency(dep);
    }

    public void removeDependency(Dependency dep) {
        dep.getSrc().removeDependency(dep);
    }
}