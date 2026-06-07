package com.aw.taskmanager.ui.dialogs;

import java.awt.*;
import javax.swing.*;

import com.aw.taskmanager.model.Task;
import com.aw.taskmanager.ui.TaskController;
import com.aw.taskmanager.ui.utilsUI;

public class NotesDialog {

    private final JList<Task> taskList;
    private final JFrame parentFrame;
    private final utilsUI utils;
    
    public NotesDialog(TaskController controller, DefaultListModel<Task> listModel, JList<Task> taskList, JFrame parentFrame) {
        this.taskList = taskList;
        this.parentFrame = parentFrame;
        this.utils = new utilsUI(controller, listModel, taskList, parentFrame);
    }

    public void showNotesDialog(Task taskToEdit, int lastSortOption, boolean showArchived) {
        JDialog dialog = new JDialog(parentFrame, "Zmień notatki", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(parentFrame);

        JTextArea notesArea = new JTextArea(3, 20);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        JScrollPane notesScroll = new JScrollPane(notesArea);

        notesArea.setText(taskToEdit.getNotes());

        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Anuluj");

        okButton.addActionListener(e -> {
            String taskId;
            taskToEdit.setNotes(notesArea.getText().stripTrailing());
            taskId = taskToEdit.getId();
            refreshTasks(lastSortOption, showArchived);
            selectTaskById(taskId);
            dialog.dispose();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        // nie można zrobić formatowania automatycznie poprzez JOptionPane.showConfirmDialog(...), bo wtedy "Enter" zamyka dialog zamiast dodać nową linię
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(new JLabel("Notatki:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1.0; gbc.weighty = 0.5;
        panel.add(notesScroll, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.getContentPane().add(panel, BorderLayout.CENTER);
        dialog.getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        dialog.setMinimumSize(new Dimension(dialog.getWidth(), dialog.getHeight()));
        dialog.setVisible(true);
    }

    public void showNotesDialog(int lastSortOption, boolean showArchived) {
        Task task = taskList.getSelectedValue();
        if (task == null) {
            return;
        }
        showNotesDialog(task, lastSortOption, showArchived);
    }

    private void refreshTasks(int lastSortOption, boolean showArchived) {
        utils.refreshTasks(lastSortOption, showArchived);
    }

    private void selectTaskById(String taskId) {
        utils.selectTaskById(taskId);
    }
}
