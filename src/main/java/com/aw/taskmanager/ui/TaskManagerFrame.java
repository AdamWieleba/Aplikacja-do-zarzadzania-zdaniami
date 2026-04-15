package com.aw.taskmanager.ui;

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
    private final JTextArea detailsArea = new JTextArea();

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
        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);

        JButton addButton = new JButton("Dodaj");
        JButton deleteButton = new JButton("Usuń");
        JButton archiveButton = new JButton("Archiwizuj");
        JButton restoreButton = new JButton("Przywróć");

        addButton.addActionListener(e -> showAddDialog());
        deleteButton.addActionListener(e -> deleteSelectedTask());
        archiveButton.addActionListener(e -> archiveSelectedTask(true));
        restoreButton.addActionListener(e -> archiveSelectedTask(false));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(addButton);
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
        sb.append("Nazwa: ").append(task.getName()).append("\n");
        sb.append("Opis: ").append(task.getDescr()).append("\n");
        sb.append("Trudność: ").append(task.getDifficulty()).append("\n");
        sb.append("Priorytet: ").append(task.getPriority()).append("\n");
        sb.append("Notatki: ").append(task.getNotes()).append("\n");

        detailsArea.setText(sb.toString());
    }

    private void showAddDialog() {
        JTextField nameField = new JTextField();
        JTextField descrField = new JTextField();
        JTextField difficultyField = new JTextField();
        JTextField priorityField = new JTextField();
        JTextField notesField = new JTextField();

        Object[] fields = {
                "Nazwa:", nameField,
                "Opis:", descrField,
                "Trudność:", difficultyField,
                "Priorytet:", priorityField,
                "Notatki:", notesField
        };

        int result = JOptionPane.showConfirmDialog(this, fields, "Dodaj zadanie",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            int priority = parsePriority(priorityField.getText());
            controller.createTask(
                    nameField.getText().trim(),
                    descrField.getText().trim(),
                    difficultyField.getText().trim(),
                    priority,
                    notesField.getText().trim(),
                    false);
            refreshTasks();
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