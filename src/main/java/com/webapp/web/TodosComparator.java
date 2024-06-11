package com.webapp.web;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;

public class TodosComparator implements Comparator<Todos>{
    //Date format: yyyy-mm-dd
    @Override
    public int compare(Todos todo1, Todos todo2) {
        LocalDate date1 = LocalDate.parse(todo1.getDate());
        LocalDate date2 = LocalDate.parse(todo2.getDate());
        LocalDate now = LocalDate.now();

        // Compare how many days in the past or future the dates are
        long daysFromNow1 = ChronoUnit.DAYS.between(date1, now);
        long daysFromNow2 = ChronoUnit.DAYS.between(date2, now);

        // This will sort the dates so that dates in the past come first,
        // and among those, more recent dates come first.
        // Dates in the future will come after all dates in the past,
        // and among those, earlier dates come first.
        return Long.compare(daysFromNow2, daysFromNow1);
    }
}
