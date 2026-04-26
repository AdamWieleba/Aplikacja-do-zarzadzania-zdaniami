package com.aw.taskmanager.ui.dialogs;

import java.awt.*;
import javax.swing.*;
import java.util.List;

import com.aw.taskmanager.model.Dependency;
import com.aw.taskmanager.model.Task;
import com.aw.taskmanager.ui.TaskController;
import com.aw.taskmanager.ui.utilsUI;

public class DependencyDialog {

    private final TaskController controller;
    private final JList<Task> taskList;
    private final JFrame parentFrame;
    private final utilsUI utils;

    public DependencyDialog(TaskController controller, DefaultListModel<Task> listModel, JList<Task> taskList, JFrame parentFrame) {
        this.controller = controller;
        this.taskList = taskList;
        this.parentFrame = parentFrame;
        this.utils = new utilsUI(controller, listModel, taskList, parentFrame);
    }

    public void showAddDependencyDialog(int lastSortOption, boolean showArchived) {
        Task selectedTask = taskList.getSelectedValue();
        if (selectedTask == null) {
            JOptionPane.showMessageDialog(parentFrame, "Wybierz zadanie najpierw.", "Brak wyboru", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(parentFrame, "Dodaj powiązanie", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(parentFrame);

        DefaultComboBoxModel<Task> srcModel = new DefaultComboBoxModel<>();
        DefaultComboBoxModel<Task> dstModel = new DefaultComboBoxModel<>();
        controller.getTasks().forEach(task -> {
            srcModel.addElement(task);
            dstModel.addElement(task);
        });

        JComboBox<Task> srcCombo = new JComboBox<>(srcModel);
        JComboBox<Task> dstCombo = new JComboBox<>(dstModel);
        JTextField descrField = new JTextField();

        srcCombo.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel label = new JLabel();
            if (value != null) {
                label.setText(value.getName());
            }
            return label;
        });

        dstCombo.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel label = new JLabel();
            if (value != null) {
                label.setText(value.getName());
            }
            return label;
        });

        // Domyślnie ustaw wybrane zadanie jako źródłowe
        srcCombo.setSelectedItem(selectedTask);

        JButton swapButton = new JButton("Zamień");
        swapButton.addActionListener(e -> {
            Task temp = (Task) srcCombo.getSelectedItem();
            srcCombo.setSelectedItem(dstCombo.getSelectedItem());
            dstCombo.setSelectedItem(temp);
        });

        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Anuluj");

        okButton.addActionListener(e -> {
            Task src = (Task) srcCombo.getSelectedItem();
            Task dst = (Task) dstCombo.getSelectedItem();
            String descr = descrField.getText().trim();
            if (src != null && dst != null) {
                controller.addDependency(src, dst, descr);
                refreshTasks(lastSortOption, showArchived);
                selectTaskById(selectedTask.getId());
                dialog.dispose();
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Zadanie źródłowe:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        panel.add(srcCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(new JLabel("Zadanie docelowe:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        panel.add(dstCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Opis:"), gbc);
        gbc.gridx = 1;
        panel.add(descrField, gbc);

        gbc.gridx = 1; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.EAST;
        panel.add(swapButton, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.getContentPane().add(panel, BorderLayout.CENTER);
        dialog.getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    public void showRemoveDependencyDialog(int lastSortOption, boolean showArchived) {
        Task selectedTask = taskList.getSelectedValue();
        if (selectedTask == null) {
            JOptionPane.showMessageDialog(parentFrame, "Wybierz zadanie najpierw.", "Nie wybrano zadania", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<Dependency> dependencies = selectedTask.getDependencies();
        if (dependencies.isEmpty()) {
            JOptionPane.showMessageDialog(parentFrame, "Wybrane zadanie nie ma powiązań.", "Brak powiązań", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(parentFrame, "Usuń powiązanie", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(parentFrame);

        DefaultListModel<Dependency> depListModel = new DefaultListModel<>();
        dependencies.forEach(depListModel::addElement);
        JList<Dependency> depList = new JList<>(depListModel);
        depList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        depList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            String srcName = value.getSrc() != null ? value.getSrc().getName() : "";
            String dstName = value.getDst() != null ? value.getDst().getName() : "";
            String descr = value.getName() != null ? value.getName() : "";
            JLabel label = new JLabel(srcName + " -> " + dstName + " (" + descr + ")");
            if (isSelected) {
                label.setOpaque(true);
                label.setBackground(list.getSelectionBackground());
                label.setForeground(list.getSelectionForeground());
            }
            return label;
        });

        JButton removeButton = new JButton("Usuń");
        JButton cancelButton = new JButton("Anuluj");

        removeButton.addActionListener(e -> {
            Dependency selectedDep = depList.getSelectedValue();
            if (selectedDep != null) {
                controller.removeDependency(selectedDep);
                refreshTasks(lastSortOption, showArchived);
                selectTaskById(selectedTask.getId());
                dialog.dispose();
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(depList), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(removeButton);
        buttonPanel.add(cancelButton);

        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.getContentPane().add(panel, BorderLayout.CENTER);
        dialog.getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void refreshTasks(int lastSortOption, boolean showArchived) {
        utils.refreshTasks(lastSortOption, showArchived);
    }

    private void selectTaskById(String taskId) {
        utils.selectTaskById(taskId);
    }
}
