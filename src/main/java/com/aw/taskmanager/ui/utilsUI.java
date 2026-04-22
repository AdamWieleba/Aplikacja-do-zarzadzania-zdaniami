package com.aw.taskmanager.ui;

import javax.swing.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.awt.*;

import com.aw.taskmanager.model.Task;

public class utilsUI {
    
    private final TaskController controller;
    private final DefaultListModel<Task> listModel;
    private final JList<Task> taskList;
    private final JFrame parentFrame;

    public utilsUI(TaskController controller, DefaultListModel<Task> listModel, JList<Task> taskList, JFrame parentFrame) {
        this.controller = controller;
        this.listModel = listModel;
        this.taskList = taskList;
        this.parentFrame = parentFrame;
    }

    public void updateButtons() {
        boolean selected = taskList.getSelectedValue() != null;
        for (Component comp : ((JPanel) parentFrame.getContentPane().getComponent(0)).getComponents()) {
            if (comp instanceof JButton button && !"Dodaj".equals(button.getText())) {
                button.setEnabled(selected);
            }
        }
    }

    public void refreshTasks(int lastSortOption) {
        listModel.clear();
        List<Task> tasks = controller.getTasks();
        tasks.forEach(listModel::addElement);
        sortTaskList(lastSortOption); // Ponownie sortuj przy odświeżaniu
        updateButtons();
    }

    public void selectTaskById(String taskId) {
        for (int i = 0; i < listModel.getSize(); i++) {
            if (listModel.getElementAt(i).getId().equals(taskId)) {
                taskList.setSelectedIndex(i);
                break;
            }
        }
    }

    public int sortTaskList(int sortOption) {
        List<Task> tasks = new ArrayList<>();

        int index = taskList.getSelectedIndex();
        String taskId = (index >= 0) ? listModel.get(index).getId() : null;
        
        for (int i = 0; i < listModel.size(); i++) {
            tasks.add(listModel.get(i));
        }
        
        switch (sortOption) {
            case 0: // Nazwa
                tasks.sort(Comparator.comparing(Task::getName));
                break;
            case 1: // Trudność
                tasks.sort(Comparator.comparingDouble(Task::getDifficultyDbl)
                        .thenComparing(Task::getName)); // thenComparing na wypadek równych wartości
                break;
            case 2: // Ważność
                tasks.sort(Comparator.comparingInt(Task::getImportance)
                        .thenComparing(Task::getName).reversed()); // reversed żeby ważniejsze były wyżej
                break;
        }
        
        listModel.clear();
        for (Task task : tasks) {
            listModel.addElement(task);
        }

        selectTaskById(taskId);
        return sortOption;
    }
    
}
