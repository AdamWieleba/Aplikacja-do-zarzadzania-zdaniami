package com.aw.taskmanager;

import java.util.ArrayList;
import java.util.List;

public class TaskManager {

    public static void main(String[] args) {
        TaskDAO taskDAO = new TxtDAO();

        List<Task> list = new ArrayList<>();
        list.add(new Task("Task1", "...\n."));
        list.add(new Task("Task2", ".\n..."));
        taskDAO.saveAll(list);
    }
}
