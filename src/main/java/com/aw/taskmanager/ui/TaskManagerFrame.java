package com.aw.taskmanager.ui;

import com.aw.taskmanager.model.Dependency;
import com.aw.taskmanager.model.Schedule;
import com.aw.taskmanager.model.Task;
import com.aw.taskmanager.ui.dialogs.DependencyDialog;
import com.aw.taskmanager.ui.dialogs.PlannerDialog;
import com.aw.taskmanager.ui.dialogs.TaskDialog;
import com.aw.taskmanager.ui.dialogs.NotesDialog;
import com.aw.taskmanager.ui.calendar.TaskCalendar;

import javax.swing.*;

import java.awt.*;
import java.awt.event.WindowAdapter; //te importy muszą być osobno
import java.awt.event.WindowEvent;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TaskManagerFrame extends JFrame {

    private final TaskController controller;
    private final DefaultListModel<Task> listModel = new DefaultListModel<>();
    private final JList<Task> taskList = new JList<>(listModel);
    private final JTextPane detailsArea = new JTextPane();
    private final DependencyDialog depDialog;
    private final TaskDialog taskDialog;
    private final NotesDialog notesDialog;
    private final PlannerDialog plannerDialog;
    private final utilsUI utils;
    private int lastSortOption = 0; //Domyślnie alfabetycznie
    private boolean showArchived = false;

    private JButton archiveButton; // używane przez metody prywatne 
    private JButton restoreButton;
    private JButton toggleArchiveButton;

    public TaskManagerFrame(TaskController controller) {
        super("Task Manager");
        this.controller = controller;

        this.utils = new utilsUI(controller, listModel, taskList, this);
        this.depDialog = new DependencyDialog(controller, listModel, taskList, this);
        this.taskDialog = new TaskDialog(controller, listModel, taskList, this);
        this.notesDialog = new NotesDialog(controller, listModel, taskList, this);
        this.plannerDialog = new PlannerDialog(controller, listModel, taskList, this);

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

        JComboBox<String> sortCombo = new JComboBox<>(
            new String[]{"Nazwa", "Nazwa  (odwrotnie)", 
                "Trudność  (od najłatwiejszych)", "Trudność  (od najtrudniejszych)", 
                "Ważność  (od najważniejszych)", "Ważność  (od najmniej ważnych)", 
                "Termin"}
        );
        sortCombo.setSelectedIndex(lastSortOption);
        sortCombo.addActionListener(e -> {
            sortTaskList(sortCombo.getSelectedIndex());
        });

        taskList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        taskList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel label = new JLabel(makeTitle(value));
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

        JButton addButton = new JButton("Dodaj");
        JButton editButton = new JButton("Edytuj");
        JButton deleteButton = new JButton("Usuń");
        archiveButton = new JButton("Archiwizuj");
        restoreButton = new JButton("Przywróć");
        toggleArchiveButton = new JButton("Pokaż archiwalne");
        JButton manageDependenciesButton = new JButton("Zarządzaj powiązaniami");
        JButton showNotesButton = new JButton("Dopisz notatkę");
        JButton planExecutionButton = new JButton("Zaplanuj wykonanie");
        JButton showCalendarButton = new JButton("Pokaż kalendarz");

        addButton.addActionListener(e -> taskDialog.showAddDialog(lastSortOption, showArchived));
        editButton.addActionListener(e -> taskDialog.showEditDialog(lastSortOption, showArchived));
        deleteButton.addActionListener(e -> deleteSelectedTask());
        archiveButton.addActionListener(e -> archiveSelectedTask(true));
        restoreButton.addActionListener(e -> archiveSelectedTask(false));
        toggleArchiveButton.addActionListener(e -> toggleArchiveView());
        manageDependenciesButton.addActionListener(e -> depDialog.showDependencyDialog(lastSortOption, showArchived));
        showNotesButton.addActionListener(e -> notesDialog.showNotesDialog(lastSortOption, showArchived));
        planExecutionButton.addActionListener(e -> plannerDialog.showPlannerDialog(lastSortOption, showArchived));
        showCalendarButton.addActionListener(e -> TaskCalendar.openTaskCalendar(this, controller));

        JPanel buttonPanel = new JPanel(new BorderLayout());

        JPanel topButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topButtons.add(new JLabel("Sortuj:"));
        topButtons.add(sortCombo);
        topButtons.add(addButton);
        topButtons.add(editButton);
        topButtons.add(deleteButton);
        topButtons.add(archiveButton);
        topButtons.add(restoreButton);
        topButtons.add(toggleArchiveButton);

        JPanel bottomButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomButtons.add(manageDependenciesButton);
        bottomButtons.add(showNotesButton);
        bottomButtons.add(planExecutionButton);
        bottomButtons.add(showCalendarButton);

        buttonPanel.add(topButtons, BorderLayout.NORTH);
        buttonPanel.add(bottomButtons, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(taskList),
                new JScrollPane(detailsArea));
        splitPane.setDividerLocation(370);

        getContentPane().setLayout(new BorderLayout(5, 5));
        getContentPane().add(buttonPanel, BorderLayout.NORTH);
        getContentPane().add(splitPane, BorderLayout.CENTER);

        pack(); //automatyczny rozmiar żeby wszystkie przyciski były widoczne
        setSize(getWidth(), 550);
        setMinimumSize(new Dimension(getWidth(), getHeight()));
        setLocationRelativeTo(null);
        updateButtonsVisibility();
    }

    private void toggleArchiveView() {
        showArchived = !showArchived;
        toggleArchiveButton.setText(showArchived ? "Pokaż zwykłe" : "Pokaż archiwalne");
        refreshTasks();
        updateButtonsVisibility();
    }

    private void updateButtonsVisibility() {
        archiveButton.setVisible(!showArchived);
        restoreButton.setVisible(showArchived);
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
                .append(escapeHtml(task.getDescr())).append("</p>");
        sb.append("<hr style='margin:18px 0 0 0;'>");
        if (formatDate(task.getDeadlineOrDefault()).compareTo("2998-01-01") < 0) {
            sb.append("<p><strong>Ostateczny termin:</strong> ")
                .append(escapeHtml(formatDate(task.getDeadlineOrDefault())))
                .append(isAfterDeadline(task) && !task.isArchived() ? "<strong> (po terminie)</strong>" : "")
                .append("</p>");
        }
        sb.append(renderDifficultySection(task.getDifficultyStr(), task.getDifficultyDbl()));
        sb.append("<p><strong>Ważność:</strong> ")
                .append(task.getImportance()).append("</p>");
        if (task.getNotes() != null && !task.getNotes().trim().isEmpty()) {
            sb.append("<p><strong>Notatki:</strong> <br/>")
                .append(escapeHtml(task.getNotes())).append("</p>");
        }
        sb.append(renderDependenciesSection(task.getDependencies()));
        sb.append(renderScheduleSection(task.getSchedules()));
        sb.append("</body></html>");

        detailsArea.setText(sb.toString());
        detailsArea.setCaretPosition(0);
    }

    private String renderDifficultySection(String difficultyStr, Double difficultyDbl) {
        StringBuilder sb = new StringBuilder("<p><strong>Trudność:</strong> &nbsp;");

        if (difficultyStr == null || difficultyStr.isEmpty()) {
            sb.append(formatDifficultyDbl(difficultyDbl))
                .append(" / 5</p>");
        } else {
            sb.append(escapeHtml(difficultyStr))
                .append(" &nbsp;(")
                .append(formatDifficultyDbl(difficultyDbl))
                .append(" / 5)</p>");
        }
        return sb.toString();
    }

    private String renderDependenciesSection(List<Dependency> dependencies) {
        if (dependencies == null || dependencies.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<p><strong>Powiązania:</strong></p>");
        sb.append("<div style='margin-left:6px;'>");
        for (Dependency dep : dependencies) {
            String srcName = dep.getSrc() != null ? escapeHtml(dep.getSrc().getName()) : "";
            String dstName = dep.getDst() != null ? escapeHtml(dep.getDst().getName()) : "";
            sb.append("<p style='margin:4px 0;'><strong>")
                    .append(srcName).append("</strong> -> <strong>").append(dstName)
                    .append("</strong><br/>")
                    .append(escapeHtml(dep.getName()))
                    .append("</p>");
        }
        sb.append("</div>");
        return sb.toString();
    }

    private String renderScheduleSection(List<Schedule> schedules) {
        if (schedules == null || schedules.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<p><strong>Planowany czas wykonania:</strong></p>");
        sb.append("<div style='margin-left:6px;'>");
        for (Schedule schedule : schedules) {
            String date = utils.formatDate(schedule.getDate());
            String startTime = utils.formatTime(schedule.getStartTime());
            String endTime = utils.formatTime(schedule.getEndTime());
            sb.append("<p style='margin:4px 0;'><strong>")
                    .append(date + ",  " + startTime + " - " + endTime + "</p>");
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
                .replace("\"", "&quot;")
                .replace("\n", "<br>");
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

        String[] options = {"Tak", "Nie"};
        int option = JOptionPane.showOptionDialog(this,
                "Usuń zadanie \"" + task.getName() + "\"?",
                "Potwierdź usunięcie",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[1]); // domyślny przycisk
        
        if (option == 0) {
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

    private String makeTitle(Task task) {
        StringBuilder sb = new StringBuilder(" ");

        switch(lastSortOption) {
            //case 0, 1 nic nie dodaje bo to nazwa
            case 2:
            case 3:
                sb.append(task.getDifficultyDbl()).append("      ");
                break;
            case 4:
            case 5:
                sb.append(task.getImportance()).append("      ");
                break;
            case 6:
                Calendar cal = Calendar.getInstance();
                cal.set(2998, Calendar.JANUARY, 1);
                if(task.getDeadlineOrDefault().before(cal.getTime())) //nie pokazuj daty dla zadań "bez terminu"
                    sb.append(formatDate(task.getDeadlineOrDefault())).append("      ");
                break;
        }

        if(task.isArchived()) {
            sb.append(task.getName())
            .append("   (archiwalne)");
        }
        else if(isAfterDeadline(task)) {
            sb.append("[PO TERMINIE]    ")
            .append(task.getName());
        }
        else {
            sb.append(task.getName());
        }

        return sb.toString();
    }

    private void updateButtons() {
        utils.updateButtons();
    }

    private void refreshTasks() {
        utils.refreshTasks(lastSortOption, showArchived);
    }

    private void sortTaskList(int sortOption) {
        this.lastSortOption = sortOption;
        utils.sortTaskList(sortOption);
    }

    private boolean isAfterDeadline(Task task) {
        return utils.isAfterDeadline(task.getDeadlineOrDefault());
    }

    private String formatDate(Date date) {
        return utils.formatDate(date);
    }
}