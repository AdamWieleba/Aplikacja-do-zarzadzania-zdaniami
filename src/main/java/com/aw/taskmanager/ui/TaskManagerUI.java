package com.aw.taskmanager.ui;

import com.aw.taskmanager.dao.XmlDAO;
import javax.swing.SwingUtilities;

public class TaskManagerUI {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            XmlDAO dao = new XmlDAO();
            TaskController controller = new TaskController(dao);
            TaskManagerFrame frame = new TaskManagerFrame(controller);
            frame.setVisible(true);
        });
    }
}