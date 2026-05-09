package com.aw.taskmanager.ui;

import java.text.SimpleDateFormat;
import java.util.*;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


class UtilsUITest {

    private utilsUI utils = new utilsUI(null, null, null, null);

    // isAfterDeadline()
    @Test
    void shouldReturnFalseWhenDeadlineIsNull() {
        assertFalse(utils.isAfterDeadline(null));
    }
    
    @Test
    void shouldReturnFalseWhenDeadlineNotPassed() throws Exception {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 1); // jutro
        Date tomorrow = cal.getTime();
        
        assertFalse(utils.isAfterDeadline(tomorrow));
    }
    
    @Test
    void shouldReturnTrueWhenDeadlinePassed() throws Exception {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1); // wczoraj
        Date yesterday = cal.getTime();
        
        assertTrue(utils.isAfterDeadline(yesterday));
    }
    
    // formatDate()
    @Test
    void shouldReturnEmptyStringWhenDateIsNull() {
        assertEquals("", utils.formatDate(null));
    }
    
    @Test
    void shouldCorrectlyFormatValidDate() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = sdf.parse("2026-05-09");
        
        assertEquals("2026-05-09", utils.formatDate(date));
    }
}
