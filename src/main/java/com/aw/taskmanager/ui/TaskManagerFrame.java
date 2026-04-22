package com.aw.taskmanager.ui;

import com.aw.taskmanager.model.Dependency;
import com.aw.taskmanager.model.Task;
import com.aw.taskmanager.ui.dialogs.DependencyDialog;
import com.aw.taskmanager.ui.dialogs.TaskDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter; //te dwa importy muszą być osobno
import java.awt.event.WindowEvent;
import java.util.List;

public class TaskManagerFrame extends JFrame {

    private final TaskController controller;
    private final DefaultListModel<Task> listModel = new DefaultListModel<>();
    private final JList<Task> taskList = new JList<>(listModel);
    private final JEditorPane detailsArea = new JEditorPane();
    private final DependencyDialog depDialog;
    private final TaskDialog taskDialog;
    private final utilsUI utils;
    private int lastSortOption = 0; // 0 - Nazwa, 1 - Trudność, 2 - Ważność

    public TaskManagerFrame(TaskController controller) {
        super("Task Manager");
        this.controller = controller;

        this.utils = new utilsUI(controller, listModel, taskList, this);
        this.depDialog = new DependencyDialog(controller, listModel, taskList, this);
        this.taskDialog = new TaskDialog(controller, listModel, taskList, this);

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

        addButton.addActionListener(e -> taskDialog.showAddDialog(lastSortOption));
        editButton.addActionListener(e -> taskDialog.showEditDialog(lastSortOption));
        deleteButton.addActionListener(e -> deleteSelectedTask());
        archiveButton.addActionListener(e -> archiveSelectedTask(true));
        restoreButton.addActionListener(e -> archiveSelectedTask(false));
        addDependencyButton.addActionListener(e -> depDialog.showAddDependencyDialog(lastSortOption));
        removeDependencyButton.addActionListener(e -> depDialog.showRemoveDependencyDialog(lastSortOption));

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

    private void updateButtons() {
        utils.updateButtons();
    }

    private void refreshTasks() {
        utils.refreshTasks(lastSortOption);
    }

    private void sortTaskList(int sortOption) {
        this.lastSortOption = sortOption;
        utils.sortTaskList(sortOption);
    }
}