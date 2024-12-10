package com.mcinfotech.event.utils;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class DateTimeToNumberUtil {
    /**
     * 判断给出的定时器设置是否在有效范围内
     *
     * @param execType      执行类型：生效类型，O单次，R重复
     * @param intervalType  适用于重复，重复类型，day每天，week每周，month月，year年
     * @param dayOfWeekAt   适用于重复，从周几开始,适用于重复执行,1(周一)、2(周二)、3(周三)、4(周四)、5(周五)、6(周六)、7(周日)
     * @param dayOfWeekUtil 适用于重复，到周几结束，适用于重复执行,1(周一)、2(周二)、3(周三)、4(周四)、5(周五)、6(周六)、7(周日)
     * @param executeAt     从什么时间开始，适用于单次和重复，时间格式为2021-05-04 04:00:00
     * @param executeUtil   从什么时间结束，适用于单次和重复，时间格式为2021-05-04 04:00:00
     * @return true有效，false无效
     */
    public static Integer isValid(String execType, String intervalType, String dayOfWeekAt, String dayOfWeekUtil, String executeAt, String executeUtil) {
        //0 未执行 //1 执行中  //2 执行完
        Integer isValid = 0;
        DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        DateTime executeAtOnce = null;
        DateTime executeUntilOnce = null;
        long currentTimestamp = System.currentTimeMillis();
        try {
            if (execType.equalsIgnoreCase("O")) {//单次

                if (StringUtils.isBlank(executeAt) && StringUtils.isNotBlank(executeUtil)) {//
                    executeUntilOnce = DateTime.parse(executeUtil, format);
                    if (currentTimestamp < executeUntilOnce.getMillis()) {//current time less than execute until time
                        isValid = 1;
                    } else {
                        isValid = 2;
                    }
                } else if (StringUtils.isNotBlank(executeAt) && StringUtils.isBlank(executeUtil)) {
                    executeAtOnce = DateTime.parse(executeAt, format);
                    if (currentTimestamp >= executeAtOnce.getMillis()) {//current time great than execute until time
                        isValid = 1;
                    }
                } else if (StringUtils.isNotBlank(executeAt) && StringUtils.isNotBlank(executeUtil)) {
                    executeAtOnce = DateTime.parse(executeAt, format);
                    executeUntilOnce = DateTime.parse(executeUtil, format);
                    if (currentTimestamp >= executeAtOnce.getMillis() && currentTimestamp < executeUntilOnce.getMillis()) {
                        isValid = 1;
                    } else if (currentTimestamp >= executeUntilOnce.getMillis()) {
                        isValid = 2;
                    }
                }

            } else if (execType.equalsIgnoreCase("R")) {
                if (intervalType.equalsIgnoreCase("day")) {
                    SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd ");
                    String currentDate = dayFormat.format(new Timestamp(currentTimestamp));
                    if (StringUtils.isBlank(executeAt) && StringUtils.isNotBlank(executeUtil)) {//
                        executeUntilOnce = DateTime.parse(currentDate + executeUtil, format);
                        if (currentTimestamp < executeUntilOnce.getMillis()) {//current time less than execute until time
                            isValid = 1;
                        } else {
                            isValid = 2;
                        }
                    } else if (StringUtils.isNotBlank(executeAt) && StringUtils.isBlank(executeUtil)) {
                        executeAtOnce = DateTime.parse(currentDate + executeAt, format);
                        if (currentTimestamp >= executeAtOnce.getMillis()) {//current time great than execute until time
                            isValid = 1;
                        }
                    } else if (StringUtils.isNotBlank(executeAt) && StringUtils.isNotBlank(executeUtil)) {
                        executeAtOnce = DateTime.parse(currentDate + executeAt, format);
                        executeUntilOnce = DateTime.parse(currentDate + executeUtil, format);
                        if (executeUntilOnce.getMillis() <= executeAtOnce.getMillis()) {
                            executeUntilOnce = executeUntilOnce.plusDays(1);
                        }
                        if (currentTimestamp >= executeAtOnce.getMillis() && currentTimestamp < executeUntilOnce.getMillis()) {
                            isValid = 1;
                        } else if (currentTimestamp >= executeUntilOnce.getMillis()) {
                            isValid = 2;
                        }
                    }
                } else if (intervalType.equalsIgnoreCase("week")) {
                    SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd");
                    String currentDate = dayFormat.format(new Timestamp(currentTimestamp));
                    DateTimeFormatter zeroFormat = DateTimeFormat.forPattern("yyyy-MM-dd");
                    DateTime currentDateTime = new DateTime(currentTimestamp);
                    DateTime executeAtTime = null;
                    DateTime executeAtZero = null;
                    DateTime executeUntilTime = null;
                    DateTime executeUntilZero = null;
                    if (StringUtils.isNotBlank(dayOfWeekAt) && StringUtils.isBlank(dayOfWeekUtil)) {
                        if (StringUtils.isBlank(executeAt)) {
                            if (currentDateTime.getDayOfWeek() == Integer.valueOf(dayOfWeekAt)) {//current time less than execute until time
                                isValid = 1;
                            }
                        } else {
                            currentDate = dayFormat.format(new Timestamp(currentDateTime.withDayOfWeek(Integer.valueOf(dayOfWeekAt)).getMillis()));
                            executeAtTime = DateTime.parse(currentDate + " " + executeAt, format);
                            executeAtZero = DateTime.parse(dayFormat.format(executeAtTime.plusDays(1).toDate()), zeroFormat);
                            if (currentTimestamp >= executeAtTime.getMillis() && currentTimestamp < executeAtZero.getMillis()) {//current time great than execute until time
                                isValid = 1;
                            } else if (currentTimestamp >= executeAtZero.getMillis()) {
                                isValid = 2;
                            }
                        }
                    } else if (StringUtils.isBlank(dayOfWeekAt) && StringUtils.isNotBlank(dayOfWeekUtil)) {
                        if (executeUtil == null) {
                            if (currentDateTime.getDayOfWeek() == Integer.valueOf(dayOfWeekUtil)) {//current time less than execute until time
                                isValid = 1;
                            }
                        } else {
                            currentDate = dayFormat.format(new Timestamp(currentDateTime.withDayOfWeek(Integer.valueOf(dayOfWeekAt)).getMillis()));
                            executeUntilTime = DateTime.parse(currentDate + " " + executeUtil, format);
                            executeUntilZero = DateTime.parse(dayFormat.format(executeUntilTime.minusDays(1).toDate()), zeroFormat);
                            if (currentTimestamp >= executeUntilZero.getMillis() && currentTimestamp < executeUntilTime.getMillis()) {//current time great than execute until time
                                isValid = 1;
                            } else if (currentTimestamp >= executeUntilTime.getMillis()) {
                                isValid = 2;
                            }
                        }
                    } else if (StringUtils.isNotBlank(dayOfWeekAt) && StringUtils.isNotBlank(dayOfWeekUtil)) {
                        int dayOfWeekAtIndex = Integer.valueOf(dayOfWeekAt);
                        int dayOfWeekUntilIndex = Integer.valueOf(dayOfWeekUtil);
                        if (dayOfWeekAtIndex < dayOfWeekUntilIndex) {
                            if (StringUtils.isNotBlank(executeAt) && StringUtils.isNotBlank(executeUtil)) {

                                executeAtTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekAtIndex).toDate()) + " " + executeAt, format);
                                executeUntilTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekUntilIndex).toDate()) + " " + executeUtil, format);

                            } else if (StringUtils.isNotBlank(executeAt) && StringUtils.isBlank(executeUtil)) {
                                executeAtTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekAtIndex).toDate()) + " " + executeAt, format);
                                executeUntilTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekUntilIndex).toDate()) + " 00:00", format);

                            } else if (StringUtils.isBlank(executeAt) && StringUtils.isNotBlank(executeUtil)) {
                                executeAtTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekAtIndex).toDate()) + " 00:00", format);
                                executeUntilTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekUntilIndex).toDate()) + " " + executeUtil, format);

                            } else {
                                executeAtTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekAtIndex).toDate()) + " 00:00", format);
                                executeUntilTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekUntilIndex).toDate()) + " 00:00", format);

                            }
                        } else if (dayOfWeekAtIndex == dayOfWeekUntilIndex) {
                            if (StringUtils.isNotBlank(executeAt) && StringUtils.isNotBlank(executeUtil)) {

                                executeAtTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekAtIndex).toDate()) + " " + executeAt, format);
                                executeUntilTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekUntilIndex).toDate()) + " " + executeUtil, format);

                            } else if (StringUtils.isNotBlank(executeAt) && StringUtils.isBlank(executeUtil)) {
                                executeAtTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekAtIndex).toDate()) + " " + executeAt, format);
                                executeUntilTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekUntilIndex).toDate()) + " 23:59", format);

                            } else if (StringUtils.isBlank(executeAt) && StringUtils.isNotBlank(executeUtil)) {
                                executeAtTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekAtIndex).toDate()) + " 00:00", format);
                                executeUntilTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekUntilIndex).toDate()) + " " + executeUtil, format);

                            } else {
                                executeAtTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekAtIndex).toDate()) + " 00:00", format);
                                executeUntilTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekUntilIndex).plusDays(1).toDate()) + " 00:00", format);

                            }
                        } else {
                            if (StringUtils.isNotBlank(executeAt) && StringUtils.isNotBlank(executeUtil)) {

                                executeAtTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekAtIndex).toDate()) + " " + executeAt, format);
                                executeUntilTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekUntilIndex).plusWeeks(1).toDate()) + " " + executeUtil, format);

                            } else if (StringUtils.isNotBlank(executeAt) && StringUtils.isBlank(executeUtil)) {
                                executeAtTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekAtIndex).toDate()) + " " + executeAt, format);
                                executeUntilTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekUntilIndex).plusWeeks(1).toDate()) + " 00:00", format);

                            } else if (StringUtils.isBlank(executeAt) && StringUtils.isNotBlank(executeUtil)) {
                                executeAtTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekAtIndex).toDate()) + " 00:00", format);
                                executeUntilTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekUntilIndex).plusWeeks(1).toDate()) + " " + executeUtil, format);

                            } else {
                                executeAtTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekAtIndex).toDate()) + " 00:00", format);
                                executeUntilTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekUntilIndex).plusWeeks(1).plusDays(1).toDate()) + " 00:00", format);

                            }
                        }
                        if (currentTimestamp >= executeAtTime.getMillis() && currentTimestamp < executeUntilTime.getMillis()) {//current time less than execute until time
                            isValid = 1;
                        } else if (currentTimestamp >= executeUntilTime.getMillis()) {
                            isValid = 2;
                        }
                    }
                } else if (intervalType.equalsIgnoreCase("month")) {
                    SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-");
                    String currentDate = dayFormat.format(new Timestamp(currentTimestamp));
                    if (StringUtils.isBlank(executeAt) && StringUtils.isNotBlank(executeUtil)) {//
                        executeUntilOnce = DateTime.parse(currentDate + executeUtil, format);
                        if (currentTimestamp < executeUntilOnce.getMillis()) {//current time less than execute until time
                            isValid = 1;
                        } else {
                            isValid = 2;
                        }
                    } else if (StringUtils.isNotBlank(executeAt) && StringUtils.isBlank(executeUtil)) {
                        executeAtOnce = DateTime.parse(currentDate + executeAt, format);
                        if (currentTimestamp >= executeAtOnce.getMillis()) {//current time great than execute until time
                            isValid = 1;
                        }
                    } else if (StringUtils.isNotBlank(executeAt) && StringUtils.isNotBlank(executeUtil)) {
                        executeAtOnce = DateTime.parse(currentDate + executeAt, format);
                        executeUntilOnce = DateTime.parse(currentDate + executeUtil, format);

                        if (executeUntilOnce.getMillis() <= executeAtOnce.getMillis()) {
                            executeUntilOnce = executeUntilOnce.plusMonths(1);
                        }
                        if (currentTimestamp >= executeAtOnce.getMillis() && currentTimestamp < executeUntilOnce.getMillis()) {
                            isValid = 1;
                        } else if (currentTimestamp >= executeUntilOnce.getMillis()) {
                            isValid = 2;
                        }
                    }
                } else if (intervalType.equalsIgnoreCase("year")) {
                    SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-");
                    String currentDate = dayFormat.format(new Timestamp(currentTimestamp));
                    if ((StringUtils.isBlank(executeAt)) && (StringUtils.isNotBlank(executeUtil))) {//
                        executeUntilOnce = DateTime.parse(currentDate + executeUtil, format);
                        if (currentTimestamp < executeUntilOnce.getMillis()) {//current time less than execute until time
                            isValid = 1;
                        } else {
                            isValid = 2;
                        }
                    } else if (StringUtils.isNotBlank(executeAt) && StringUtils.isBlank(executeUtil)) {
                        executeAtOnce = DateTime.parse(currentDate + executeAt, format);
                        if (currentTimestamp >= executeAtOnce.getMillis()) {//current time great than execute until time
                            isValid = 1;
                        }
                    } else if (StringUtils.isNotBlank(executeAt) && StringUtils.isNotBlank(executeUtil)) {
                        executeAtOnce = DateTime.parse(currentDate + executeAt, format);
                        executeUntilOnce = DateTime.parse(currentDate + executeUtil, format);
                        if (executeUntilOnce.getMillis() <= executeAtOnce.getMillis()) {
                            executeUntilOnce = executeUntilOnce.plusYears(1);
                        }
                        if (currentTimestamp >= executeAtOnce.getMillis() && currentTimestamp < executeUntilOnce.getMillis()) {
                            isValid = 1;
                        } else if (currentTimestamp >= executeUntilOnce.getMillis()) {
                            isValid = 2;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isValid;
    }
}
