package com.aw.taskmanager.builder;

import com.aw.taskmanager.model.Task;
import com.aw.taskmanager.model.Dependency;

public class TaskBuilder {
    private final Task task;

    public TaskBuilder() {  // do tworzenia nowych zadań
        this.task = new Task();
        task.generateId();
    }

    public TaskBuilder(Task task) {  // do modyfikowania istniejących 
        this.task = task;
    }

    public TaskBuilder name(String name) {
        task.setName(name);
        return this;
    }

    public TaskBuilder descr(String descr) {
        task.setDescr(descr);
        return this;
    }

    public TaskBuilder difficulty(String difficulty) {
        task.setDifficulty(difficulty);
        return this;
    }

    public TaskBuilder priority(Integer priority) {
        task.setPriority(priority);
        return this;
    }

    public TaskBuilder notes(String notes) {
        task.setNotes(notes);
        return this;
    }
    
    public TaskBuilder addDependency(Dependency dependency) {
        task.addDependency(dependency);
        return this;
    }
    
    public TaskBuilder removeDependency(Dependency dependency) {
        task.removeDependency(dependency);
        return this;
    }

    public TaskBuilder isArchived(boolean isArchived) {
        task.setArchived(isArchived);
        return this;
    }

    public Task build() {
        return task;
    }
}

