package com.aw.taskmanager.ui.dialogs;

import java.awt.*;
import javax.swing.*;
import com.toedter.calendar.JDateChooser;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Date;
import java.util.List;

import com.aw.taskmanager.model.Schedule;
import com.aw.taskmanager.model.Task;
import com.aw.taskmanager.ui.TaskController;
import com.aw.taskmanager.ui.utilsUI;

public class PlannerDialog {

    private final JList<Task> taskList;
    private final JFrame parentFrame;
    private final utilsUI utils;

    public PlannerDialog(TaskController controller, DefaultListModel<Task> listModel, JList<Task> taskList, JFrame parentFrame) {
        this.taskList = taskList;
        this.parentFrame = parentFrame;
        this.utils = new utilsUI(controller, listModel, taskList, parentFrame);
    }

    public void showPlannerDialog(Task task, int lastSortOption, boolean showArchived) {
        List<Schedule> schedules = task.getSchedules();

        JDialog dialog = new JDialog(parentFrame, "Zaplanuj czas wykonania", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(parentFrame);

        DefaultListModel<Schedule> scheduleListModel = new DefaultListModel<>();
        schedules.forEach(scheduleListModel::addElement);
        JList<Schedule> scheduleList = new JList<>(scheduleListModel);
        scheduleList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scheduleList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            String date = utils.formatDate(value.getDate());
            String startTime = utils.formatTime(value.getStartTime());
            String endTime = utils.formatTime(value.getEndTime());
            JLabel label = new JLabel(date + ",  " + startTime + " - " + endTime);
            if (isSelected) {
                label.setOpaque(true);
                label.setBackground(list.getSelectionBackground());
                label.setForeground(list.getSelectionForeground());
            }
            return label;
        });

        JButton addButton = new JButton("Dodaj");
        JButton removeButton = new JButton("Usuń");
        JButton okButton = new JButton("Ok");

        addButton.addActionListener(e -> {
            showAddScheduleDialog(task);
            scheduleListModel.clear(); // odświeża listę po dodaniu
            schedules.forEach(scheduleListModel::addElement);
        });
        
        removeButton.addActionListener(e -> {
            Schedule selectedSchedule = scheduleList.getSelectedValue();
            if (selectedSchedule != null) {
                task.removeSchedule(selectedSchedule);
                scheduleListModel.removeElement(selectedSchedule);
            }
        });

        okButton.addActionListener(e -> {dialog.dispose();});

        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                utils.refreshTasks(lastSortOption, showArchived);
                utils.selectTaskById(task.getId());
            }
        });

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(scheduleList), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(okButton);

        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.getContentPane().add(panel, BorderLayout.CENTER);
        dialog.getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        dialog.setMinimumSize(new Dimension(dialog.getWidth(), dialog.getHeight()));
        dialog.setVisible(true);
    }

    public void showAddScheduleDialog(Task task) {
        JDialog dialog = new JDialog(parentFrame, "Zaplanuj wykonanie", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(400, 500);
        dialog.setLocationRelativeTo(parentFrame);

        JDateChooser dateChooser = new JDateChooser(new Date());
        dateChooser.setDateFormatString("yyyy-MM-dd");
        JSpinner startTimeSpinner = new JSpinner(new SpinnerDateModel());
        startTimeSpinner.setEditor(new JSpinner.DateEditor(startTimeSpinner, "HH:mm"));
        JSpinner endTimeSpinner = new JSpinner(new SpinnerDateModel());
        endTimeSpinner.setEditor(new JSpinner.DateEditor(endTimeSpinner, "HH:mm"));

        Object[] fields = {
                "Dzień wykonania:", dateChooser,
                "Od godziny:", startTimeSpinner,
                "Do godziny:", endTimeSpinner
        };

        int result = JOptionPane.showConfirmDialog(parentFrame, fields, "Dodaj blok czasowy",
        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            Date date = dateChooser.getDate();
            Date startTime = (Date) startTimeSpinner.getValue();
            Date endTime = (Date) endTimeSpinner.getValue();

            task.addSchedule(new Schedule(date, startTime, endTime));
        }
    }

    public void showPlannerDialog(int lastSortOption, boolean showArchived) {
        Task task = taskList.getSelectedValue();
        if (task == null) {
            return;
        }
        showPlannerDialog(task, lastSortOption, showArchived);
    }
}
