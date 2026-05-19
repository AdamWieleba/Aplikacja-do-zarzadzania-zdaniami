package com.aw.taskmanager.ui;

import com.aw.taskmanager.dao.XmlDao;
import javax.swing.SwingUtilities;

public class TaskManagerUI {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            XmlDao dao = new XmlDao();
            TaskController controller = new TaskController(dao);
            TaskManagerFrame frame = new TaskManagerFrame(controller);
            frame.setVisible(true);
        });
    }
}