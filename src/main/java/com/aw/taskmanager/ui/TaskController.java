package com.aw.taskmanager.ui;

import com.aw.taskmanager.model.Task;
import com.aw.taskmanager.model.Dependency;
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

    public Task createTask(String name, String descr, String difficultyStr, Double difficultyDbl, Integer priority, String notes, boolean archived) {
        Task task = new TaskBuilder()
            .name(makeTitle(name))
            .descr(descr)
            .difficultyStr(difficultyStr)
            .difficultyDbl(difficultyDbl)
            .priority(priority)
            .notes(notes)
            .isArchived(archived)
            .build();
        tasks.add(task);
        return task;
    }

    public boolean updateTask(String id, String name, String descr, String difficultyStr, Double difficultyDbl, Integer priority, String notes, boolean archived) {
        Optional<Task> existing = findTaskById(id);
        existing.ifPresent(task -> new TaskBuilder(task)
            .name(makeTitle(name))
            .descr(descr)
            .difficultyStr(difficultyStr)
            .difficultyDbl(difficultyDbl)
            .priority(priority)
            .notes(notes)
            .isArchived(archived)
            .build());
        return existing.isPresent();
    }

    private String makeTitle(String name) {
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