package com.aw.taskmanager.model;

import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import jakarta.xml.bind.annotation.*;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD) // -> jaxb będzie korzystał bezpośrednio z pól, więc nie wymaga get/set ani @XmlElement
public class Task {
    private String name;
    private String descr;
    private String difficultyStr;
    private Double difficultyInt;
    private Integer priority;
    private String notes;
    private boolean isArchived;
    
    @XmlElementWrapper(name = "dependencies")
    @XmlElement(name = "dependency")
    private List<Dependency> dependencies = new ArrayList<>();  //gwarantuje inicjalizację
    
    @XmlID
    private String id;
    
    public Task() {} // wymagane przez jaxb

    public Task(String name, String description, String difficultyStr, Double difficultyInt, Integer priority, String notes, boolean isArchived) {
        this.name = name;
        this.descr = description;
        this.difficultyStr = difficultyStr;
        this.difficultyInt = difficultyInt;
        this.priority = priority;
        this.notes = notes;
        this.isArchived = isArchived;
        this.id = UUID.randomUUID().toString().replace("-", "");  
    }
    
    public String getId() {
        return id;
    }
    public void generateId() { //powód: TaskBuilder używa pustego konstruktora, ale nie można dać w nim id, bo jaxb będzie je nadpisywał
        if(id == null) { 
            this.id = UUID.randomUUID().toString().replace("-", "");
        }
    }
    
    public List<Dependency> getDependencies() {
        return dependencies;
    }
    
    public void addDependency(Dependency dp) {
        dp.getSrc().dependencies.add(dp);
        dp.getDst().dependencies.add(dp);
    }
    
    public void removeDependency(Dependency dp) {
        dp.getSrc().dependencies.remove(dp);
        dp.getDst().dependencies.remove(dp);
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
    
    public String getDifficultyStr() {
        return difficultyStr;
    }
    public void setDifficultyStr(String difficultyStr) {
        this.difficultyStr = difficultyStr;
    }
    
    public Double getDifficultyInt() {
        return difficultyInt;
    }
    public void setDifficultyInt(Double difficultyInt) {
        this.difficultyInt = difficultyInt;
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
    public void setArchived(boolean isArchived) {
        this.isArchived = isArchived;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Task t &&
            id.equals(t.id);
    }

}
