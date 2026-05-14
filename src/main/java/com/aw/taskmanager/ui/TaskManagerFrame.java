package com.aw.taskmanager.ui;

import com.aw.taskmanager.model.Dependency;
import com.aw.taskmanager.model.Task;
import com.aw.taskmanager.ui.dialogs.DependencyDialog;
import com.aw.taskmanager.ui.dialogs.TaskDialog;

import javax.swing.*;

import java.awt.*;
import java.awt.event.WindowAdapter; //te importy muszą być osobno
import java.awt.event.WindowEvent;
import javax.swing.border.EmptyBorder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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
    private boolean showArchived = false;
    private JButton archiveButton;
    private JButton restoreButton;
    private JButton toggleArchiveButton;

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

        JComboBox<String> sortCombo = new JComboBox<>(
            new String[]{"Nazwa", "Trudność", "Ważność", "Termin"}
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
        detailsArea.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);

        JButton addButton = new JButton("Dodaj");
        JButton editButton = new JButton("Edytuj");
        JButton deleteButton = new JButton("Usuń");
        archiveButton = new JButton("Archiwizuj");
        restoreButton = new JButton("Przywróć");
        toggleArchiveButton = new JButton("Pokaż archiwalne");
        JButton addDependencyButton = new JButton("Dodaj powiązanie");
        JButton removeDependencyButton = new JButton("Usuń powiązanie");
        JButton showCalendarButton = new JButton("Pokaż kalendarz");

        addButton.addActionListener(e -> taskDialog.showAddDialog(lastSortOption, showArchived));
        editButton.addActionListener(e -> taskDialog.showEditDialog(lastSortOption, showArchived));
        deleteButton.addActionListener(e -> deleteSelectedTask());
        archiveButton.addActionListener(e -> archiveSelectedTask(true));
        restoreButton.addActionListener(e -> archiveSelectedTask(false));
        toggleArchiveButton.addActionListener(e -> toggleArchiveView());
        addDependencyButton.addActionListener(e -> depDialog.showAddDependencyDialog(lastSortOption, showArchived));
        removeDependencyButton.addActionListener(e -> depDialog.showRemoveDependencyDialog(lastSortOption, showArchived));
        showCalendarButton.addActionListener(e -> openTaskCalendar());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(new JLabel("Sortuj:"));
        buttonPanel.add(sortCombo);
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(archiveButton);
        buttonPanel.add(restoreButton);
        buttonPanel.add(toggleArchiveButton);
        buttonPanel.add(addDependencyButton);
        buttonPanel.add(removeDependencyButton);
        buttonPanel.add(showCalendarButton);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(taskList),
                new JScrollPane(detailsArea));
        splitPane.setResizeWeight(0.4);

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
                .append(escapeHtmlWithBreaks(task.getDescr())).append("</p>");
        sb.append("<hr style='margin:18px 0 0 0;'>");
        if (formatDate(task.getDeadlineOrDefault()).compareTo("2998-01-01") < 0) {
            sb.append("<p><strong>Ostateczny termin:</strong> ")
                .append(escapeHtml(formatDate(task.getDeadlineOrDefault())))
                .append(isAfterDeadline(task) && !task.isArchived() ? "<strong> (po terminie)</strong>" : "")
                .append("</p>");
        }
        sb.append("<p><strong>Trudność:</strong>")
                .append(" &nbsp;")  //spacje nieprzerywane
                .append(escapeHtml(task.getDifficultyStr()))
                .append(" &nbsp;(")
                .append(formatDifficultyDbl(task.getDifficultyDbl()))
                .append(" na 5)</p>");
        sb.append("<p><strong>Ważność:</strong> ")
                .append(task.getImportance()).append("</p>");
        if (task.getNotes() != null && !task.getNotes().trim().isEmpty()) {
            sb.append("<p><strong>Notatki:</strong> <br/>")
                .append(escapeHtmlWithBreaks(task.getNotes())).append("</p>");
        }
        sb.append(renderDependenciesSection(task.getDependencies()));
        sb.append("</body></html>");

        detailsArea.setText(sb.toString());
        detailsArea.setCaretPosition(0);
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
                    .append(escapeHtmlWithBreaks(dep.getName()))
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

    private void openTaskCalendar() {
        JDialog calendarDialog = new JDialog(this, "Kalendarz zadań", true);
        JPanel calendarPanel = new JPanel(new GridLayout(0, 7, 5, 5));
        calendarPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        
        // Przesunięcie do ostatniego poniedziałku
        while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            cal.add(Calendar.DAY_OF_MONTH, -1);
        }

        String[] dayNames = {"Poniedziałek", "Wtorek", "Środa", "Czwartek", "Piątek", "Sobota", "Niedziela"};
        for (String day : dayNames) {
            JLabel label = new JLabel(day);
            label.setFont(new Font(null, Font.BOLD, 12));
            calendarPanel.add(label);
        }
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        for (int i = 0; i < 30; i++) {
            JPanel dayPanel = new JPanel(new BorderLayout());
            dayPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

            JLabel dateLabel = new JLabel(String.valueOf(cal.get(Calendar.DAY_OF_MONTH)));
            dateLabel.setFont(new Font(null, Font.BOLD, 14));
            dayPanel.add(dateLabel, BorderLayout.NORTH);
            
            StringBuilder tasksText = new StringBuilder("<html>");
            for (Task task : controller.getTasks()) {
                if (!task.isArchived() && sdf.format(task.getDeadlineOrDefault()).equals(sdf.format(cal.getTime()))) {
                    tasksText.append("• ").append(task.getName()).append("<br>");
                }
            }
            tasksText.append("</html>");

            dayPanel.add(new JLabel(tasksText.toString()), BorderLayout.CENTER);
            
            calendarPanel.add(dayPanel);
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
        
        calendarDialog.add(new JScrollPane(calendarPanel));
        calendarDialog.setSize(800, 600);
        calendarDialog.setMinimumSize(new Dimension(calendarDialog.getWidth(), calendarDialog.getHeight()));
        calendarDialog.setLocationRelativeTo(this);
        calendarDialog.setVisible(true);
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
        StringBuilder sb = new StringBuilder();

        switch(lastSortOption) {
            //case 0 nic nie dodaje bo to nazwa
            case 1:
                sb.append("(").append(task.getDifficultyDbl()).append(")   ");
                break;
            case 2:
                sb.append("(").append(task.getImportance()).append(")   ");
                break;
            case 3:
                Calendar cal = Calendar.getInstance();
                cal.set(2998, Calendar.JANUARY, 1);
                if(task.getDeadlineOrDefault().before(cal.getTime())) //nie pokazuj daty dla zadań "bez terminu"
                    sb.append("(").append(formatDate(task.getDeadlineOrDefault())).append(")   ");
                break;
        }

        if(task.isArchived()) {
            sb.append(task.getName())
            .append("   (archiwalne)");
        }
        else if(isAfterDeadline(task)) {
            sb.append(" !   ")
            .append(task.getName())
            .append("    PO TERMINIE");
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