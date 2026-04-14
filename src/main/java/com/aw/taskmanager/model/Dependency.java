package com.aw.taskmanager.model;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class Dependency {
    private String name;
    
    @XmlIDREF  // zapisze w xml tylko referencję do tego obiektu
    private Task src;
    @XmlIDREF
    private Task dst;

    public Dependency() {}

    public Dependency(Task src, Task dst, String name) {
        this.src = src;
        this.dst = dst;
        this.name = name;
    }

    public Task getSrc() {
        return src;
    }
    public void setSrc(Task src) {
        this.src = src;
    }

    public Task getDst() {
        return dst;
    }
    public void setDst(Task dst) {
        this.dst = dst;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
