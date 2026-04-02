package com.aw.taskmanager;

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
    public void saveAll(List<Task> list) throws Exception {
        TaskList wrapper = new TaskList();
        wrapper.setTasks(list);

        JAXBContext context = JAXBContext.newInstance(TaskList.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        marshaller.marshal(wrapper, new File(file));
    }

    @Override
    public List<Task> loadAll() throws Exception {
        JAXBContext context = JAXBContext.newInstance(TaskList.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();

        TaskList wrapper =
                (TaskList) unmarshaller.unmarshal(new File(file));

        return wrapper.getTasks();
    }

    @Override
    public void delete(Task task) throws Exception {
        List<Task> list = loadAll();
        list.remove(task);
        saveAll(list);
    }
}
