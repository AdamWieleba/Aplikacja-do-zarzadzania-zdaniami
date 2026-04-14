package com.aw.taskmanager.dao;

import com.aw.taskmanager.model.Task;
import com.aw.taskmanager.model.TaskList;

import jakarta.xml.bind.*;
import java.io.File;
import java.util.List;

public class XmlDAO implements TaskDAO {
    private String file;

    public XmlDAO() {
        file = "Tasks.xml";
    }

    public XmlDAO(String file) {
        this.file = file;
    }

    @Override
    public void saveAll(List<Task> list) {
        TaskList wrapper = new TaskList();
        wrapper.setTasks(list);

        try {
            JAXBContext context = JAXBContext.newInstance(TaskList.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(wrapper, new File(file));       
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Task> loadAll() {
        try {
            JAXBContext context = JAXBContext.newInstance(TaskList.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            TaskList wrapper = (TaskList) unmarshaller.unmarshal(new File(file));
            return wrapper.getTasks();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    @Override
    public void delete(Task task) {
        List<Task> list = loadAll();
        list.remove(task);
        saveAll(list);
    }
}
