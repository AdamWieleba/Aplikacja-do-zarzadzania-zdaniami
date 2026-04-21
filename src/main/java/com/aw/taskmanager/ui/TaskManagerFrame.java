package com.aw.taskmanager.ui;

import com.aw.taskmanager.model.Dependency;
import com.aw.taskmanager.model.Task;

import javax.swing.*;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TaskManagerFrame extends JFrame {

    private final TaskController controller;
    private final DefaultListModel<Task> listModel = new DefaultListModel<>();
    private final JList<Task> taskList = new JList<>(listModel);
    private final JEditorPane detailsArea = new JEditorPane();
    private int lastSortOption = 0; // 0 - Nazwa, 1 - Trudność, 2 - Ważność

    public TaskManagerFrame(TaskController controller) {
        super("Task Manager");
        this.controller = controller;
        initUI();
        refreshTasks();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                controller.saveAll();
            }
        });
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(850, 550);
        setLocationRelativeTo(null);

        JComboBox<String> sortCombo = new JComboBox<>(
            new String[]{"Nazwa", "Trudność", "Ważność"}
        );
        sortCombo.setSelectedIndex(lastSortOption);
        sortCombo.addActionListener(e -> {
            sortTaskList(sortCombo.getSelectedIndex());
        });

        taskList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        taskList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel label = new JLabel(value.getName() + (value.isArchived() ? " (archiwalne)" : ""));
            if (isSelected) {
                label.setOpaque(true);
                label.setBackground(list.getSelectionBackground());
                label.setForeground(list.getSelectionForeground());
            }
            return label;
        });
        taskList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showTaskDetails(taskList.getSelectedValue());
                updateButtons();
            }
        });

        detailsArea.setEditable(false);
        detailsArea.setContentType("text/html");
        detailsArea.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);

        JButton addButton = new JButton("Dodaj");
        JButton editButton = new JButton("Edytuj");
        JButton deleteButton = new JButton("Usuń");
        JButton archiveButton = new JButton("Archiwizuj");
        JButton restoreButton = new JButton("Przywróć");
        JButton addDependencyButton = new JButton("Dodaj powiązanie");
        JButton removeDependencyButton = new JButton("Usuń powiązanie");

        addButton.addActionListener(e -> showAddDialog());
        editButton.addActionListener(e -> showEditDialog());
        deleteButton.addActionListener(e -> deleteSelectedTask());
        archiveButton.addActionListener(e -> archiveSelectedTask(true));
        restoreButton.addActionListener(e -> archiveSelectedTask(false));
        addDependencyButton.addActionListener(e -> showAddDependencyDialog());
        removeDependencyButton.addActionListener(e -> showRemoveDependencyDialog());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(new JLabel("Sortuj:"));
        buttonPanel.add(sortCombo);
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(archiveButton);
        buttonPanel.add(restoreButton);
        buttonPanel.add(addDependencyButton);
        buttonPanel.add(removeDependencyButton);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(taskList),
                new JScrollPane(detailsArea));
        splitPane.setResizeWeight(0.4);

        getContentPane().setLayout(new BorderLayout(5, 5));
        getContentPane().add(buttonPanel, BorderLayout.NORTH);
        getContentPane().add(splitPane, BorderLayout.CENTER);

        updateButtons();
    }

    private void refreshTasks() {
        listModel.clear();
        List<Task> tasks = controller.getTasks();
        tasks.forEach(listModel::addElement);
        sortTaskList(lastSortOption); // Ponownie sortuj przy odświeżaniu
        updateButtons();
    }

    private void showTaskDetails(Task task) {
        if (task == null) {
            detailsArea.setText("");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style='font-family:").append(detailsArea.getFont().getFamily())
                .append("; font-size:").append(detailsArea.getFont().getSize()).append("pt;'>");

        sb.append("<h2 style='text-align:center; margin:0; padding:0;'>")
                .append(escapeHtml(task.getName())).append("</h2>");
        sb.append("<p style='margin-top:12px;'> ")
                .append(escapeHtmlWithBreaks(task.getDescr())).append("</p>");
        sb.append("<hr style='margin:18px 0 0 0;'>");
        sb.append("<p><strong>Trudność:</strong>")
                .append(" &nbsp;")  //spacje nieprzerywane
                .append(escapeHtml(task.getDifficultyStr()))
                .append(" &nbsp;(")
                .append(formatDifficultyDbl(task.getDifficultyDbl()))
                .append(" na 5)</p>");
        sb.append("<p><strong>Ważność:</strong> ")
                .append(task.getImportance()).append("</p>");
        sb.append("<p><strong>Notatki:</strong> <br/>")
                .append(escapeHtmlWithBreaks(task.getNotes())).append("</p>");
        sb.append(renderDependenciesSection(task.getDependencies()));
        sb.append("</body></html>");

        detailsArea.setText(sb.toString());
        detailsArea.setCaretPosition(0);
    }

    private String renderDependenciesSection(List<Dependency> dependencies) {
        if (dependencies == null || dependencies.isEmpty()) {
            return "<p><strong>Zależności:</strong> brak</p>";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<p><strong>Powiązania:</strong></p>");
        sb.append("<div style='margin-left:6px;'>");
        for (Dependency dp : dependencies) {
            String srcName = dp.getSrc() != null ? escapeHtml(dp.getSrc().getName()) : "";
            String dstName = dp.getDst() != null ? escapeHtml(dp.getDst().getName()) : "";
            sb.append("<p style='margin:4px 0;'><strong>")
                    .append(srcName).append(" </strong> -> <strong> ").append(dstName)
                    .append("</strong><br/>")
                    .append(escapeHtmlWithBreaks(dp.getName()))
                    .append("</p>");
        }
        sb.append("</div>");
        return sb.toString();
    }

    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private String escapeHtmlWithBreaks(String text) {
        if (text == null) {
            return "";
        }
        return escapeHtml(text).replace("\n", "<br>");
    }

    private String formatDifficultyDbl(Double difficultyDbl) {
        if (difficultyDbl == null) {
            return "";
        }
        if (difficultyDbl % 1 == 0) {
            return String.format("%.0f", difficultyDbl);
        }
        return String.format("%.1f", difficultyDbl);
    }

    private void showAddDialog() {
        showTaskDialog(null);
    }

    private void showEditDialog() {
        Task task = taskList.getSelectedValue();
        if (task == null) {
            return;
        }
        showTaskDialog(task);
    }

    private void showTaskDialog(Task taskToEdit) {
        boolean editMode = taskToEdit != null;
        JDialog dialog = new JDialog(this, editMode ? "Edytuj zadanie" : "Dodaj zadanie", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(400, 500);
        dialog.setLocationRelativeTo(this);

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
            refreshTasks();
            selectTaskById(taskId);
            dialog.dispose();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

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

    private void selectTaskById(String taskId) {
        for (int i = 0; i < listModel.getSize(); i++) {
            if (listModel.getElementAt(i).getId().equals(taskId)) {
                taskList.setSelectedIndex(i);
                break;
            }
        }
    }

    private void deleteSelectedTask() {
        Task task = taskList.getSelectedValue();
        if (task == null) {
            return;
        }

        int option = JOptionPane.showConfirmDialog(this,
                "Usuń zadanie \"" + task.getName() + "\"?",
                "Potwierdź usunięcie",
                JOptionPane.YES_NO_OPTION);

        if (option == JOptionPane.YES_OPTION) {
            controller.deleteTask(task.getId());
            refreshTasks();
        }
    }

    private void archiveSelectedTask(boolean archive) {
        Task task = taskList.getSelectedValue();
        if (task == null) {
            return;
        }
        controller.setArchived(task.getId(), archive);
        refreshTasks();
    }

    private int parseImportance(String text) {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void updateButtons() {
        boolean selected = taskList.getSelectedValue() != null;
        for (Component comp : ((JPanel) getContentPane().getComponent(0)).getComponents()) {
            if (comp instanceof JButton button && !"Dodaj".equals(button.getText())) {
                button.setEnabled(selected);
            }
        }
    }

    private void showAddDependencyDialog() {
        Task selectedTask = taskList.getSelectedValue();
        if (selectedTask == null) {
            JOptionPane.showMessageDialog(this, "Wybierz zadanie najpierw.", "Brak wyboru", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(this, "Dodaj powiązanie", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(this);

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

        // Set default source to selected task
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
                refreshTasks();
                // Reselect the original selected task
                String selectedId = selectedTask.getId();
                for (int i = 0; i < listModel.getSize(); i++) {
                    if (listModel.getElementAt(i).getId().equals(selectedId)) {
                        taskList.setSelectedIndex(i);
                        break;
                    }
                }
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

    private void showRemoveDependencyDialog() {
        Task selectedTask = taskList.getSelectedValue();
        if (selectedTask == null) {
            JOptionPane.showMessageDialog(this, "Wybierz zadanie najpierw.", "Nie wybrano zadania", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<Dependency> dependencies = selectedTask.getDependencies();
        if (dependencies.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Wybrane zadanie nie ma powiązań.", "Brak powiązań", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(this, "Usuń powiązanie", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(this);

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
                refreshTasks();
                // Reselect the original selected task
                String selectedId = selectedTask.getId();
                for (int i = 0; i < listModel.getSize(); i++) {
                    if (listModel.getElementAt(i).getId().equals(selectedId)) {
                        taskList.setSelectedIndex(i);
                        break;
                    }
                }
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

    private void sortTaskList(int sortOption) {
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
        
        lastSortOption = sortOption;
        listModel.clear();
        for (Task task : tasks) {
            listModel.addElement(task);
        }
        
        selectTaskById(taskId);
    }

}