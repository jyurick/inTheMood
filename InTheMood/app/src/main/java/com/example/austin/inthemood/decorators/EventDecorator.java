package com.example.austin.inthemood.decorators;


import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import java.util.Collection;
import java.util.HashSet;

/**
 * Decorate several days with a dot
 * Based on
 * https://github.com/prolificinteractive/material-calendarview/tree/master/sample/src/main/java/com/prolificinteractive/materialcalendarview/sample
 * and is Copyright (c) 2016 Prolific Interactive.
 */
public class EventDecorator implements DayViewDecorator {

    private int color;
    private HashSet<CalendarDay> dates;

    /**
     * Instantiates a new Event decorator.
     *
     * @param color the color
     * @param dates the dates
     */
    public EventDecorator(int color, Collection<CalendarDay> dates) {
        this.color = color;
        this.dates = new HashSet<>(dates);
    }

    /**
     * determain if a CalendarDay should be decorated
     * @param day the CalendarDay to be checked
     * @return True if day is in the HashSet of dates
     */
    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return dates.contains(day);
    }

    /**
     * Add a DotSpan below the view
     * @param view
     */
    @Override
    public void decorate(DayViewFacade view) {
        view.addSpan(new DotSpan(10, color));
    }
}
