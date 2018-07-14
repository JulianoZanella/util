package br.com.julianozanella.util;

import java.sql.Date;
import java.time.LocalDate;
import java.time.Period;
import java.util.Calendar;

public final class DateUtil {

    public static Date getSQLDate(LocalDate date){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, date.getYear());
        calendar.set(Calendar.MONTH, date.getMonthValue() - 1);
        calendar.set(Calendar.DAY_OF_MONTH, date.getDayOfMonth());
        return new Date(calendar.getTimeInMillis());
    }

    public static int getAge(LocalDate birthDate){
        return Period.between(birthDate, LocalDate.now()).getYears();
    }


}
