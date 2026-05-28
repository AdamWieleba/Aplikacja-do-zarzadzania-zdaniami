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
        JPanel calendarPanel = new JPanel(new GridLayout(4, 7, 5, 5));
        calendarPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        Calendar cal = Calendar.getInstance();
        Calendar today = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        
        // Przesunięcie do poniedziałku bieżącego tygodnia
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int daysToGoBack = (dayOfWeek - Calendar.MONDAY + 7) % 7;
        cal.add(Calendar.DAY_OF_MONTH, -daysToGoBack);

        String[] dayNames = {"Poniedziałek", "Wtorek", "Środa", "Czwartek", "Piątek", "Sobota", "Niedziela"};
        for (String day : dayNames) {
            JLabel label = new JLabel(day);
            label.setFont(new Font(null, Font.BOLD, 12));
            calendarPanel.add(label);
        }
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        for (int i = 0; i < 21; i++) {
            JPanel dayPanel = new JPanel(new BorderLayout(5, 5));
            
            boolean isToday = sdf.format(cal.getTime()).equals(sdf.format(today.getTime()));
            if (isToday) {
                dayPanel.setBorder(BorderFactory.createLineBorder(Color.GREEN, 3));
            } else {
                dayPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            }
            dayPanel.setBackground(Color.WHITE);
            dayPanel.setOpaque(true);
            
            JLabel dateLabel = new JLabel(String.valueOf(cal.get(Calendar.DAY_OF_MONTH)));
            dateLabel.setFont(new Font(null, Font.BOLD, 14));
            dayPanel.add(dateLabel, BorderLayout.NORTH);
            
            // Panel z listą zadań i scrollem wewnętrznym
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

            JScrollPane scrollPane = new JScrollPane(tasksPanel);
            scrollPane.setPreferredSize(new Dimension(150, 80));
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setBorder(null);
            dayPanel.add(scrollPane, BorderLayout.CENTER);
            
            // Maksymalna wysokość dla dayPanel
            dayPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
            dayPanel.setPreferredSize(new Dimension(100, 120));
            
            calendarPanel.add(dayPanel);
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
        
        JScrollPane mainScrollPane = new JScrollPane(calendarPanel);
        calendarDialog.add(mainScrollPane);
        calendarDialog.setSize(900, 600);
        calendarDialog.setMinimumSize(new Dimension(900, 600));
        calendarDialog.setLocationRelativeTo(parentFrame);
        calendarDialog.setVisible(true);
    }
}
