package com.aw.taskmanager;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class Dependency {

    public enum DependencyType {
        FS, SS, FF;  //finish-to-start, ...
    }

    private DependencyType type;
    
    @XmlIDREF  // zapisze w xml tylko referencję do tego obiektu
    private Task dst;

    public Dependency() {}

    public Dependency(Task dst, DependencyType type) {
        this.dst = dst;
        this.type = type;
    }

    public Task getDst() {
        return dst;
    }
    public void setDst(Task dst) {
        this.dst = dst;
    }

    public DependencyType getType() {
        return type;
    }
    public void setType(DependencyType type) {
        this.type = type;
    }
}
