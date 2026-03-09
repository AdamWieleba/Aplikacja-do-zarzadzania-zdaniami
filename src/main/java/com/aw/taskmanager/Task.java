package com.aw.taskmanager;

public class Task {
    private String name;
    private String description;
    private boolean isArchived = false;
    
    public Task(String name, String description) {
        this.name = name;
        this.description = description;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescr() {
        return description;
    }
    
    public void setDescr(String description) {
        this.description = description;
    }
    
    public boolean isArchived() {
        return isArchived;
    }
    
    public void setArchivedState(boolean isArchived) {
        this.isArchived = isArchived;
    }

}
