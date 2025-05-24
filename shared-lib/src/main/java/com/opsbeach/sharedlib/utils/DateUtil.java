package com.opsbeach.sharedlib.utils;

import com.opsbeach.sharedlib.security.SecurityUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * <p>
 * Common DateUtil class.
 * </p>
 */
public class DateUtil {
    private static final String UTC = "UTC";
    private static final String MMM_YYYY = "MMM, yyyy";

    public static LocalDateTime convertTimeZoneToDifferentZone(LocalDateTime localDateTime) {
        ZonedDateTime zonedUTC = localDateTime.atZone(ZoneId.of(UTC));
        var timeZone = SecurityUtil.getLoggedInUserDetail().getTimeZone();
        return zonedUTC.withZoneSameInstant(ZoneId.of(timeZone)).toLocalDateTime();
    }

    public static LocalDate currentDate() {
        return ZonedDateTime.now(ZoneId.of(UTC)).toLocalDate();
    }

    public static LocalDateTime currentDateTime() {
        return LocalDateTime.now();
    }

    public static LocalDate currentTimeZoneDate() {
        var zonedUTC = ZonedDateTime.now(ZoneId.of(UTC));
        var timeZone = SecurityUtil.getLoggedInUserDetail().getTimeZone();
        var zonedDate = zonedUTC.withZoneSameInstant(ZoneId.of(timeZone));
        return zonedDate.toLocalDate();
    }

    public static LocalDateTime currentTimeZoneDateTime() {
        var zonedUTC = ZonedDateTime.now(ZoneId.of(UTC));
        var timeZone = SecurityUtil.getLoggedInUserDetail().getTimeZone();
        var zonedDate = zonedUTC.withZoneSameInstant(ZoneId.of(timeZone));
        return zonedDate.toLocalDateTime();
    }
     public static LocalDateTime plusHours(LocalDateTime localDateTime, long value) {
        return localDateTime.plusHours(value);
    }

    public static LocalDate plusMonths(LocalDate localDate, long value) {
        return localDate.plusMonths(value);
    }

    public static LocalDate minusMonths(LocalDate localDate, long value) {
        return localDate.minusMonths(value);
    }

    public static LocalDate plusDays(LocalDate localDate, long value) {
        return localDate.plusDays(value);
    }

    public static LocalDate minusDays(LocalDate localDate, long value) {
        return localDate.minusDays(value);
    }

    public static LocalDateTime plusMonths(LocalDateTime localDateTime, long value) {
        return localDateTime.plusMonths(value);
    }

    public static LocalDateTime minusMonths(LocalDateTime localDateTime, long value) {
        return localDateTime.minusMonths(value);
    }

    public static LocalDateTime plusDays(LocalDateTime localDateTime, long value) {
        return localDateTime.plusDays(value);
    }

    public static LocalDateTime minusDays(LocalDateTime localDateTime, long value) {
        return localDateTime.minusDays(value);
    }

    public static LocalDateTime plusMinutes(LocalDateTime localDateTime, long value) {
        return localDateTime.plusMinutes(value);
    }

    public static LocalDate plusYear(LocalDate localDate, long value) {
        return localDate.plusYears(value);
    }

    public static LocalDate minusYear(LocalDate localDate, long value) {
        return localDate.minusYears(value);
    }

    public static LocalDateTime plusYear(LocalDateTime localDateTime, long value) {
        return localDateTime.plusYears(value);
    }

    public static LocalDateTime minusYear(LocalDateTime localDateTime, long value) {
        return localDateTime.minusYears(value);
    }

    public static LocalDate plusWeeks(LocalDate localDate, long value) {
        return localDate.plusWeeks(value);
    }

    public static LocalDate minusWeeks(LocalDate localDate, long value) {
        return localDate.minusWeeks(value);
    }

    public static LocalDateTime plusWeeks(LocalDateTime localDateTime, long value) {
        return localDateTime.plusWeeks(value);
    }

    public static LocalDateTime minusWeeks(LocalDateTime localDateTime, long value) {
        return localDateTime.minusWeeks(value);
    }

    public static Boolean isBefore(LocalDate localDateA, LocalDate localDateB) {
        return localDateA.isBefore(localDateB);
    }

    public static Boolean isBefore(LocalDateTime localDateTimeA, LocalDateTime localDateTimeB) {
        return localDateTimeA.isBefore(localDateTimeB);
    }

    public static Boolean isAfter(LocalDate localDateA, LocalDate localDateB) {
        return localDateA.isAfter(localDateB);
    }

    public static Boolean isAfter(LocalDateTime localDateTimeA, LocalDateTime localDateTimeB) {
        return localDateTimeA.isAfter(localDateTimeB);
    }

    public static Boolean equals(LocalDate localDateA, LocalDate localDateB) {
        return localDateA.equals(localDateB);
    }

    public static Boolean equals(LocalDateTime localDateTimeA, LocalDateTime localDateTimeB) {
        return localDateTimeA.equals(localDateTimeB);
    }

    public static String format(LocalDate localDate, DateTimeFormatter dateTimeFormatter) {
        return dateTimeFormatter.format(localDate);
    }

    public static String format(LocalDateTime localDateTime, DateTimeFormatter dateTimeFormatter) {
        return dateTimeFormatter.format(localDateTime);
    }

    public static LocalDate convertToLocalDate(LocalDateTime localDateTime) {
        return localDateTime.toLocalDate();
    }

    public static Long daysBetweenTwoDates(LocalDate localDateA, LocalDate localDateB) {
        return (long) Period.between(localDateA, localDateB).getDays();
    }

    public static LocalDate withDayOfMonth(LocalDate localDate, int dayOfMonth) {
        return localDate.withDayOfMonth(dayOfMonth);
    }

    public static LocalDate minusMonthsFromFirstDay(LocalDate localDate, long months) {
        return localDate.withDayOfMonth(1).minusMonths(months);
    }

    public static LocalDate minusMonthsFromLastDay(LocalDate localDate, long months) {
        return localDate.minusMonths(months).withDayOfMonth(localDate.minusMonths(months).lengthOfMonth());
    }

    public static Long durationBetween(LocalDate localDateA, LocalDate localDateB) {
        return Duration.between(localDateA.atStartOfDay(), localDateB.atStartOfDay()).toDays();
    }

    public static long minutesBetweenTime(LocalDateTime localDateTimeA, LocalDateTime localDateTimeB) {
        return Duration.between(localDateTimeA, localDateTimeB).toMinutes();
    }

    public static long hoursBetweenTime(LocalDateTime localDateTimeA, LocalDateTime localDateTimeB) {
        return Duration.between(localDateTimeA, localDateTimeB).toHours();
    }

    public static Long secondsBetweenDate(LocalDateTime localDateTimeA, LocalDateTime localDateTimeB) {
        return ChronoUnit.SECONDS.between(localDateTimeA, localDateTimeB);
    }

    public static String getFormattedYearMonth(int year, int month) {
        YearMonth thisYearMonth = YearMonth.of(year, month);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(MMM_YYYY);
        return thisYearMonth.format(formatter);
    }

    public static DateFormat getDateFormat(String pattern) {
        var timeZone = TimeZone.getTimeZone("UTC");
        DateFormat dateFormat = new SimpleDateFormat(pattern);
        dateFormat.setTimeZone(timeZone);
        return dateFormat;
    }

    public static Calendar getCalendar() {
        var date = new Date();
        var calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }

    public static String formatDate(DateFormat dateFormat, Date date) {
        return dateFormat.format(date);
    }

    public static Boolean validateExpiration(LocalDateTime expirationTime) {
        var valid = Boolean.FALSE;
        if (expirationTime != null && LocalDateTime.now().isAfter(expirationTime)) {
            valid = Boolean.TRUE;
        }
        return valid;
    }

    public static LocalDateTime convertDatetoLocalDateTimeUTC(Date date) {
        return date.toInstant().atZone(ZoneId.of(UTC)).toLocalDateTime();
    }

    public static LocalDateTime convertDatetoLocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static LocalDateTime currentDateTimeUTC() {
        return ZonedDateTime.now(ZoneId.of(UTC)).toLocalDateTime();
    }

    public static Date convertLocalDateTimeToDateUTC(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.of(UTC)).toInstant());
    }

    public static Date convertLocalDateTimeToDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}
