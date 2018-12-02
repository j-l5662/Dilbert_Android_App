package com.johannlau.dilbert_app.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

public class DateUtil {

    private final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static String returnRandomDate() {
        LocalDate startDate = LocalDate.of(1996,4,16);
        LocalDate endDate = LocalDate.now();

        long start = startDate.toEpochDay();
        long end = endDate.toEpochDay();
        long randomDate = ThreadLocalRandom.current().nextLong(start,end);

        LocalDate randomStringDate = LocalDate.ofEpochDay(randomDate);
        String randomDateString = randomStringDate.format(formatter);

        return randomDateString;
    }
}
