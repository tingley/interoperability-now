package com.globalsight.tip;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

class DateUtil {

    private static final String FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    
    static Date parseTIPDate(String dateString) {
        try {
            SimpleDateFormat df = new SimpleDateFormat(FORMAT);
            return df.parse(dateString);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid date string: " + 
                                               dateString);
        }
    }
    
    static String writeTIPDate(Date date) {
        SimpleDateFormat df = new SimpleDateFormat(FORMAT);
        return df.format(date);
    }
}
