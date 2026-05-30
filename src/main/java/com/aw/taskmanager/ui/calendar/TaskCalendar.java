package com.aw.taskmanager.ui.calendar;

import com.aw.taskmanager.ui.TaskController;
import com.aw.taskmanager.model.Task;

import javax.swing.*;
import java.awt.*;
import javax.swing.border.EmptyBorder;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TaskCalendar extends JFrame {
    
    public static void openTaskCalendar(JFrame parentFrame, TaskController controller) {
        JDialog calendarDialog = new JDialog(parentFrame, "Kalendarz zadań", true);
        calendarDialog.setLayout(new BorderLayout());

        // ===== GÓRNY PANEL =====
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JComboBox<Integer> weeksCombo = new JComboBox<>(new Integer[]{2, 3, 4, 5});
        weeksCombo.setSelectedItem(3);

        JLabel monthLabel = new JLabel();

        topPanel.add(new JLabel("Liczba tygodni:"));
        topPanel.add(weeksCombo);
        topPanel.add(Box.createHorizontalStrut(20));
        topPanel.add(monthLabel);

        calendarDialog.add(topPanel, BorderLayout.NORTH);

        // ===== PANEL GŁÓWNY =====
        JPanel contentPanel = new JPanel(new BorderLayout());

        // ===== NAGŁÓWKI DNI =====
        JPanel headerPanel = new JPanel(new GridLayout(1, 7, 5, 5));

        String[] dayNames = {"Poniedziałek", "Wtorek", "Środa", "Czwartek", "Piątek", "Sobota", "Niedziela"};
        for (String day : dayNames) {
            JLabel label = new JLabel(day, SwingConstants.CENTER);
            label.setFont(new Font(null, Font.BOLD, 12));
            headerPanel.add(label);
        }

        contentPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel calendarPanel = new JPanel();
        calendarPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane calendarScrollPane = new JScrollPane(calendarPanel);
        contentPanel.add(calendarScrollPane, BorderLayout.CENTER);

        calendarDialog.add(contentPanel, BorderLayout.CENTER);

        Runnable rebuildCalendar = () -> {

            calendarPanel.removeAll();

            int weeks = (Integer) weeksCombo.getSelectedItem();
            calendarPanel.setLayout(new GridLayout(weeks, 7, 5, 5));

            Calendar cal = Calendar.getInstance();
            Calendar today = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            int daysToGoBack = (dayOfWeek - Calendar.MONDAY + 7) % 7;
            cal.add(Calendar.DAY_OF_MONTH, -daysToGoBack);

            monthLabel.setText("Aktualny miesiąc: " + new SimpleDateFormat("LLLL yyyy").format(cal.getTime()));

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat firstDayFormat = new SimpleDateFormat("d MMMM");
            int dayHeight = 450 / weeks;

            for (int i = 0; i < weeks * 7; i++) {

                JPanel dayPanel = new JPanel(new BorderLayout(5, 5));

                boolean isToday = sdf.format(cal.getTime())
                    .equals(sdf.format(today.getTime()));

                if (isToday) {
                    dayPanel.setBorder(BorderFactory.createLineBorder(Color.GREEN, 3));
                } else {
                    dayPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                }

                dayPanel.setBackground(Color.WHITE);
                dayPanel.setOpaque(true);

                String dateText;

                if (cal.get(Calendar.DAY_OF_MONTH) == 1) {
                    dateText = firstDayFormat.format(cal.getTime());
                } else {
                    dateText = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
                }

                JLabel dateLabel = new JLabel(dateText);
                dateLabel.setFont(new Font(null, Font.BOLD, 14));

                dayPanel.add(dateLabel, BorderLayout.NORTH);

                JPanel tasksPanel = new JPanel();
                tasksPanel.setLayout(new BoxLayout(tasksPanel, BoxLayout.Y_AXIS));
                tasksPanel.setBackground(Color.WHITE);

                for (Task task : controller.getTasks()) {
                    if (!task.isArchived() && sdf.format(task.getDeadlineOrDefault()).equals(sdf.format(cal.getTime()))) {
                        String taskName = task.getName();

                        // Zawijanie do max 2 linii
                        int lineLength = 14;
                        int firstLineLength = lineLength - 2; // miejsce na "• "
                        int maxLength = firstLineLength + lineLength;
                        if (taskName.length() > maxLength) {
                            taskName = taskName.substring(0, maxLength - 3) + "...";
                        }

                        StringBuilder wrappedName = new StringBuilder();
                        // pierwsza linia
                        int endFirst = Math.min(firstLineLength, taskName.length());
                        wrappedName.append(taskName.substring(0, endFirst));
                        // druga linia
                        if (taskName.length() > firstLineLength) {
                            wrappedName.append("<br>").append(taskName.substring(
                                firstLineLength, Math.min(firstLineLength + lineLength, taskName.length())));
                        }
                        
                        JLabel taskLabel = new JLabel("<html>• " + wrappedName + "</html>");
                        taskLabel.setFont(new Font("Monospaced", Font.PLAIN, 13));
                        taskLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                        tasksPanel.add(taskLabel);
                    }
                }

                tasksPanel.add(Box.createVerticalGlue());

                JScrollPane taskScrollPane = new JScrollPane(tasksPanel);
                taskScrollPane.setBorder(null);
                taskScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

                dayPanel.add(taskScrollPane, BorderLayout.CENTER);
                dayPanel.setPreferredSize(new Dimension(100, dayHeight));
                calendarPanel.add(dayPanel);

                cal.add(Calendar.DAY_OF_MONTH, 1);
            }

            calendarPanel.revalidate();
            calendarPanel.repaint();
        };

        weeksCombo.addActionListener(e -> rebuildCalendar.run());

        rebuildCalendar.run();

        calendarDialog.setSize(1000, 650);
        calendarDialog.setMinimumSize(new Dimension(calendarDialog.getWidth(), calendarDialog.getHeight()));
        calendarDialog.setLocationRelativeTo(parentFrame);
        calendarDialog.setVisible(true);
    }
}
