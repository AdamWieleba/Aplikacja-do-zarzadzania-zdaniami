package com.aw.taskmanager.dao;

import com.aw.taskmanager.model.Task;
import java.util.List;

public interface TaskDAO {

    public void saveAll(List<Task> list);

    public List<Task> loadAll();

    public void delete(Task task);
}
