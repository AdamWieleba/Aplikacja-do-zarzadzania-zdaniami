package com.aw.taskmanager;

public class Task {
    private String name;
    private String descr;
    private String difficulty;
    private Integer priority;
    private String notes;
    private boolean isArchived = false;
    
    public Task() {}

    public Task(String name, String description, String difficulty, Integer priority, String notes, boolean isArchived) {
        this.name = name;
        this.descr = description;
        this.difficulty = difficulty;
        this.priority = priority;
        this.notes = notes;
        this.isArchived = isArchived;
    }
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescr() {
        return descr;
    }
    public void setDescr(String description) {
        this.descr = description;
    }
    
    public String getDifficulty() {
        return difficulty;
    }
    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }
    
    public Integer getPriority() {
        return priority;
    }
    public void setPriority(Integer priority) {
        this.priority = priority;
    }
    
    public String getNotes() {
        return notes;
    }
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public boolean isArchived() {
        return isArchived;
    }
    public void setArchivedState(boolean isArchived) {
        this.isArchived = isArchived;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Task t &&
            name.equals(t.name) &&
            descr.equals(t.descr);
    }

}
