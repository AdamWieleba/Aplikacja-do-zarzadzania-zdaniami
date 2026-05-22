package com.aw.taskmanager.ui;

import javax.swing.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.awt.*;
import java.text.SimpleDateFormat;

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
            if (comp instanceof JButton button && !List.of("Dodaj", "Pokaż archiwalne", "Pokaż zwykłe", "Pokaż kalendarz").contains(button.getText())) {
                button.setEnabled(selected);
            }
        }
    }

    public void refreshTasks(int lastSortOption, boolean showArchived) {
        listModel.clear();
        List<Task> tasks = controller.getTasks();
        tasks.stream()
            .filter(task -> task.isArchived() == showArchived)
            .forEach(listModel::addElement);
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
            case 0: // Nazwa alfabetycznie
                tasks.sort(Comparator.comparing(Task::getName));
                break;
            case 1: // Nazwa odwrotnie
                tasks.sort(Comparator.comparing(Task::getName).reversed());
                break;
            case 2: // Trudność od najłatiwejszych
                tasks.sort(Comparator.comparingDouble(Task::getDifficultyDbl)
                        .thenComparing(Task::getName)); // thenComparing na wypadek równych wartości
                break;
            case 3: // Trudność od najtrudniejszych
                tasks.sort(Comparator.comparingDouble(Task::getDifficultyDbl).reversed()
                        .thenComparing(Task::getName));
                break;
            case 4: // Ważność od najważniejszych
                tasks.sort(Comparator.comparingInt(Task::getImportance).reversed()
                        .thenComparing(Task::getName));
                break;
            case 5: // Ważność od najmniej ważnych
                tasks.sort(Comparator.comparingInt(Task::getImportance)
                        .thenComparing(Task::getName));
                break;
            case 6: // Termin
                tasks.sort(Comparator.comparing(Task::getDeadlineOrDefault)
                        .thenComparing(Task::getName));
                break;
        }
        
        listModel.clear();
        for (Task task : tasks) {
            listModel.addElement(task);
        }

        selectTaskById(taskId);
        return sortOption;
    }

    public boolean isAfterDeadline(Date deadline) {
        if (deadline == null) {
            return false;
        }
        Calendar deadlineCal = Calendar.getInstance();
        deadlineCal.setTime(deadline);
        deadlineCal.set(Calendar.HOUR_OF_DAY, 23);
        deadlineCal.set(Calendar.MINUTE, 59);
        deadlineCal.set(Calendar.SECOND, 59);
        
        return new Date().after(deadlineCal.getTime());
    }

    public String formatDate(Date date) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }
    
}
