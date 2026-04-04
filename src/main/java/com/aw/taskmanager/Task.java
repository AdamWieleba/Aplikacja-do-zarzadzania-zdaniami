package com.aw.taskmanager;

import java.util.List;
import java.util.UUID;
import jakarta.xml.bind.annotation.*;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD) // -> jaxb będzie korzystał bezpośrednio z pól, więc nie wymaga get/set ani @XmlElement
public class Task {
    private String name;
    private String descr;
    private String difficulty;
    private Integer priority;
    private String notes;
    private boolean isArchived;
    
    @XmlElementWrapper(name = "dependencies")
    @XmlElement(name = "dependency")
    private List<Dependency> dependencies;
    
    @XmlID
    private String id;
    
    public Task() {} // wymagane przez jaxb

    public Task(String name, String description, String difficulty, Integer priority, String notes, List<Dependency> dependencies, boolean isArchived) {
        this.name = name;
        this.descr = description;
        this.difficulty = difficulty;
        this.priority = priority;
        this.notes = notes;
        this.dependencies = dependencies;
        this.isArchived = isArchived;
        this.id = UUID.randomUUID().toString();
    }
    
    public String getId() {
        return id;
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
    
    public List<Dependency> getDependencies() {
        return dependencies;
    }
    public void setDependencies(List<Dependency> dependencies) {
        this.dependencies = dependencies;
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
            id.equals(t.id);
    }

}
