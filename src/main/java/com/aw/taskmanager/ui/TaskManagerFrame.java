package com.aw.taskmanager.ui;

import com.aw.taskmanager.model.Dependency;
import com.aw.taskmanager.model.Task;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

public class TaskManagerFrame extends JFrame {

    private final TaskController controller;
    private final DefaultListModel<Task> listModel = new DefaultListModel<>();
    private final JList<Task> taskList = new JList<>(listModel);
    private final JEditorPane detailsArea = new JEditorPane();

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
        setSize(700, 450);
        setLocationRelativeTo(null);

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

        addButton.addActionListener(e -> showAddDialog());
        editButton.addActionListener(e -> showEditDialog());
        deleteButton.addActionListener(e -> deleteSelectedTask());
        archiveButton.addActionListener(e -> archiveSelectedTask(true));
        restoreButton.addActionListener(e -> archiveSelectedTask(false));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(archiveButton);
        buttonPanel.add(restoreButton);

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
        sb.append("<p><strong>Priorytet:</strong> ")
                .append(task.getPriority()).append("</p>");
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
        JTextField priorityField = new JTextField();
        JTextArea notesArea = new JTextArea(3, 20);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        JScrollPane notesScroll = new JScrollPane(notesArea);

        if (editMode) {
            nameField.setText(taskToEdit.getName());
            descrArea.setText(taskToEdit.getDescr());
            difficultyStrField.setText(taskToEdit.getDifficultyStr());
            difficultyDblSpinner.setValue(taskToEdit.getDifficultyDbl() != null ? taskToEdit.getDifficultyDbl() : 0.0);
            priorityField.setText(String.valueOf(taskToEdit.getPriority()));
            notesArea.setText(taskToEdit.getNotes());
        }

        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Anuluj");

        okButton.addActionListener(e -> {
            int priority = parsePriority(priorityField.getText());
            double difficultyDbl = ((Number) difficultyDblSpinner.getValue()).doubleValue();
            if (editMode) {
                controller.updateTask(
                        taskToEdit.getId(),
                        nameField.getText().stripTrailing(),
                        descrArea.getText().stripTrailing(),
                        difficultyStrField.getText().stripTrailing(),
                        difficultyDbl,
                        priority,
                        notesArea.getText().stripTrailing(),
                        taskToEdit.isArchived());
            } else {
                controller.createTask(
                        nameField.getText().stripTrailing(),
                        descrArea.getText().stripTrailing(),
                        difficultyStrField.getText().stripTrailing(),
                        difficultyDbl,
                        priority,
                        notesArea.getText().stripTrailing(),
                        false);
            }
            refreshTasks();
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
        panel.add(new JLabel("Priorytet:"), gbc);
        gbc.gridx = 1;
        panel.add(priorityField, gbc);

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

        // Po zamknięciu dialogu - ponownie wybierz zadanie (zarówno po OK jak i Anuluj)
        if (editMode && taskToEdit != null) {
            String taskId = taskToEdit.getId();
            for (int i = 0; i < listModel.getSize(); i++) {
                if (listModel.getElementAt(i).getId().equals(taskId)) {
                    taskList.setSelectedIndex(i);
                    break;
                }
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

    private int parsePriority(String text) {
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
}