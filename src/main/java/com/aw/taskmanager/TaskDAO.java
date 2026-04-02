package com.aw.taskmanager;

import java.util.List;

public interface TaskDAO {

    public void saveAll(List<Task> list) throws Exception;

    public List<Task> loadAll() throws Exception;

    public void delete(Task task) throws Exception;
}
