package com.aw.taskmanager.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DependencyTest {
    @Test
    void shouldCreateDependencyWithParameters() {
        Task src = new Task("Source", "Src", "e", 1.0, 1, "N", false, null);
        src.generateId();
        Task dst = new Task("Dest", "Dst", "e", 1.0, 1, "N", false, null);
        dst.generateId();

        Dependency dep = new Dependency(src, dst, "blokuje");

        assertEquals(src, dep.getSrc());
        assertEquals(dst, dep.getDst());
        assertEquals("blokuje", dep.getName());
    }

    @Test
    void shouldSetAndGetProperties() {
        Dependency dep = new Dependency();
        Task src = new Task();
        src.generateId();
        Task dst = new Task();
        dst.generateId();

        dep.setSrc(src);
        dep.setDst(dst);
        dep.setName("zależy od");

        assertEquals(src, dep.getSrc());
        assertEquals(dst, dep.getDst());
        assertEquals("zależy od", dep.getName());
    }
}