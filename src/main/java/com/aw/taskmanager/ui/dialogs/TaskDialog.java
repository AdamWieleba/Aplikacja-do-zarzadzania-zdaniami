package com.aw.taskmanager.ui.dialogs;

import java.awt.*;
import javax.swing.*;

import com.aw.taskmanager.model.Task;
import com.aw.taskmanager.ui.TaskController;
import com.aw.taskmanager.ui.utilsUI;

public class TaskDialog {

    private final TaskController controller;
    private final JList<Task> taskList;
    private final JFrame parentFrame;
    private final utilsUI utils;
    
    public TaskDialog(TaskController controller, DefaultListModel<Task> listModel, JList<Task> taskList, JFrame parentFrame) {
        this.controller = controller;
        this.taskList = taskList;
        this.parentFrame = parentFrame;
        this.utils = new utilsUI(controller, listModel, taskList, parentFrame);
    }

    public void showTaskDialog(Task taskToEdit, int lastSortOption, boolean showArchived) {
        boolean editMode = taskToEdit != null;
        JDialog dialog = new JDialog(parentFrame, editMode ? "Edytuj zadanie" : "Dodaj zadanie", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(400, 500);
        dialog.setLocationRelativeTo(parentFrame);

        JTextField nameField = new JTextField();
        JTextArea descrArea = new JTextArea(3, 20);
        descrArea.setLineWrap(true);
        descrArea.setWrapStyleWord(true);
        JScrollPane descrScroll = new JScrollPane(descrArea);
        JTextField difficultyStrField = new JTextField();
        JSpinner difficultyDblSpinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 5.0, 0.5));
        JTextField importanceField = new JTextField();
        JTextArea notesArea = new JTextArea(3, 20);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        JScrollPane notesScroll = new JScrollPane(notesArea);

        if (editMode) {
            nameField.setText(taskToEdit.getName());
            descrArea.setText(taskToEdit.getDescr());
            difficultyStrField.setText(taskToEdit.getDifficultyStr());
            difficultyDblSpinner.setValue(taskToEdit.getDifficultyDbl() != null ? taskToEdit.getDifficultyDbl() : 0.0);
            importanceField.setText(String.valueOf(taskToEdit.getImportance()));
            notesArea.setText(taskToEdit.getNotes());
        }

        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Anuluj");

        okButton.addActionListener(e -> {
            int importance = parseImportance(importanceField.getText());
            double difficultyDbl = ((Number) difficultyDblSpinner.getValue()).doubleValue();
            String taskId;
            if (editMode) {
                controller.updateTask(
                        taskToEdit.getId(),
                        nameField.getText().stripTrailing(),
                        descrArea.getText().stripTrailing(),
                        difficultyStrField.getText().stripTrailing(),
                        difficultyDbl,
                        importance,
                        notesArea.getText().stripTrailing(),
                        taskToEdit.isArchived());
                taskId = taskToEdit.getId();
            } else {
                taskId = controller.createTask(
                        nameField.getText().stripTrailing(),
                        descrArea.getText().stripTrailing(),
                        difficultyStrField.getText().stripTrailing(),
                        difficultyDbl,
                        importance,
                        notesArea.getText().stripTrailing(),
                        false)
                        .getId();
            }
            refreshTasks(lastSortOption, showArchived);
            selectTaskById(taskId);
            dialog.dispose();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        // nie można zrobić poniższego formatowania automatycznie poprzez JOptionPane.showConfirmDialog(...), bo wtedy "Enter" zamyka dialog zamiast dodać nową linię w opisie, notatkach itd.
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Nazwa:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        panel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(new JLabel("Opis:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1.0; gbc.weighty = 0.5;
        panel.add(descrScroll, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0; gbc.weighty = 0;
        panel.add(new JLabel("Trudność (tekst):"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        panel.add(difficultyStrField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Trudność (0-5, co 0.5):"), gbc);
        gbc.gridx = 1;
        panel.add(difficultyDblSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Ważność:"), gbc);
        gbc.gridx = 1;
        panel.add(importanceField, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(new JLabel("Notatki:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1.0; gbc.weighty = 0.5;
        panel.add(notesScroll, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.getContentPane().add(panel, BorderLayout.CENTER);
        dialog.getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    public void showAddDialog(int lastSortOption, boolean showArchived) {
        showTaskDialog(null, lastSortOption, showArchived);
    }

    public void showEditDialog(int lastSortOption, boolean showArchived) {
        Task task = taskList.getSelectedValue();
        if (task == null) {
            return;
        }
        showTaskDialog(task, lastSortOption, showArchived);
    }

    private int parseImportance(String text) {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void refreshTasks(int lastSortOption, boolean showArchived) {
        utils.refreshTasks(lastSortOption, showArchived);
    }

    private void selectTaskById(String taskId) {
        utils.selectTaskById(taskId);
    }
}
