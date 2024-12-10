package com.mcinfotech.event.utils;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class DateTimeUtils {
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
    public static boolean isValid2(String execType, String intervalType, String dayOfWeekAt, String dayOfWeekUtil, String executeAt, String executeUtil) {

        boolean isValid = false;
        DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        DateTime executeAtOnce = null;
        DateTime executeUntilOnce = null;
        long currentTimestamp = System.currentTimeMillis();
        try {
            if (execType.equalsIgnoreCase("O")) {

                if (StringUtils.isBlank(executeAt) && StringUtils.isNotBlank(executeUtil)) {//
                    executeUntilOnce = DateTime.parse(executeUtil, format);
                    if (currentTimestamp < executeUntilOnce.getMillis()) {//current time less than execute until time
                        isValid = true;
                    }
                } else if (StringUtils.isNotBlank(executeAt) && StringUtils.isBlank(executeUtil)) {
                    executeAtOnce = DateTime.parse(executeAt, format);
                    if (currentTimestamp > executeAtOnce.getMillis()) {//current time great than execute until time
                        isValid = true;
                    }
                } else if (StringUtils.isNotBlank(executeAt) && StringUtils.isNotBlank(executeUtil)) {
                    executeAtOnce = DateTime.parse(executeAt, format);
                    executeUntilOnce = DateTime.parse(executeUtil, format);
                    if (currentTimestamp > executeAtOnce.getMillis() && currentTimestamp < executeUntilOnce.getMillis()) {
                        isValid = true;
                    }
                }
            } else if (execType.equalsIgnoreCase("R")) {
                if (intervalType.equalsIgnoreCase("day")) {
                    SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd ");
                    String currentDate = dayFormat.format(new Timestamp(currentTimestamp));
                    if (StringUtils.isBlank(executeAt) && StringUtils.isNotBlank(executeUtil)) {//
                        executeUntilOnce = DateTime.parse(currentDate + executeUtil, format);
                        if (currentTimestamp < executeUntilOnce.getMillis()) {//current time less than execute until time
                            isValid = true;
                        }
                    } else if (StringUtils.isNotBlank(executeAt) && StringUtils.isBlank(executeUtil)) {
                        executeAtOnce = DateTime.parse(currentDate + executeAt, format);
                        if (currentTimestamp > executeAtOnce.getMillis()) {//current time great than execute until time
                            isValid = true;
                        }
                    } else if (StringUtils.isNotBlank(executeAt) && StringUtils.isNotBlank(executeUtil)) {
                        executeAtOnce = DateTime.parse(currentDate + executeAt, format);
                        executeUntilOnce = DateTime.parse(currentDate + executeUtil, format);
                        if (executeUntilOnce.getMillis() <= executeAtOnce.getMillis()) {
                            if (currentTimestamp > executeAtOnce.getMillis() && currentTimestamp < executeUntilOnce.plusDays(1).getMillis()) {
                                isValid = true;
                            }
                        } else {
                            if (currentTimestamp > executeAtOnce.getMillis() && currentTimestamp < executeUntilOnce.getMillis()) {
                                isValid = true;
                            }
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
                                isValid = true;
                            }
                        } else {
                            executeAtTime = DateTime.parse(currentDate + " " + executeAt, format);
                            executeAtZero = DateTime.parse(dayFormat.format(executeAtTime.plusDays(1).toDate()), zeroFormat);
                            if (currentTimestamp > executeAtTime.getMillis() && currentTimestamp < executeAtZero.getMillis()) {//current time great than execute until time
                                isValid = true;
                            }
                        }
                    } else if (StringUtils.isBlank(dayOfWeekAt) && StringUtils.isNotBlank(dayOfWeekUtil)) {
                        if (executeUtil == null) {
                            if (currentDateTime.getDayOfWeek() == Integer.valueOf(dayOfWeekUtil)) {//current time less than execute until time
                                isValid = true;
                            }
                        } else {
                            executeUntilTime = DateTime.parse(currentDate + " " + executeUtil, format);
                            executeUntilZero = DateTime.parse(dayFormat.format(executeUntilTime.minusDays(1).toDate()), zeroFormat);
                            if (currentTimestamp > executeUntilZero.getMillis() && currentTimestamp < executeUntilTime.getMillis()) {//current time great than execute until time
                                isValid = true;
                            }
                        }
                    } else if (StringUtils.isNotBlank(dayOfWeekAt) && StringUtils.isNotBlank(dayOfWeekUtil)) {
                        int dayOfWeekAtIndex = Integer.valueOf(dayOfWeekAt);
                        int dayOfWeekUntilIndex = Integer.valueOf(dayOfWeekUtil);
                        if (dayOfWeekAtIndex < dayOfWeekUntilIndex) {
                            if (StringUtils.isNotBlank(executeAt) && StringUtils.isNotBlank(executeUtil)) {

                                executeAtTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekAtIndex).toDate()) + " " + executeAt, format);
                                executeUntilTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekUntilIndex).toDate()) + " " + executeUtil, format);

                                if (currentTimestamp >= executeAtTime.getMillis() && currentTimestamp <= executeUntilTime.getMillis()) {//current time less than execute until time
                                    isValid = true;
                                }
                            } else if (StringUtils.isNotBlank(executeAt) && StringUtils.isBlank(executeUtil)) {
                                executeAtTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekAtIndex).toDate()) + " " + executeAt, format);
                                executeUntilTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekUntilIndex).toDate()) + " 00:00", format);

                                if (currentTimestamp >= executeAtTime.getMillis() && currentTimestamp <= executeUntilTime.getMillis()) {//current time less than execute until time
                                    isValid = true;
                                }
                            } else if (StringUtils.isBlank(executeAt) && StringUtils.isNotBlank(executeUtil)) {
                                executeAtTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekAtIndex).toDate()) + " 00:00", format);
                                executeUntilTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekUntilIndex).toDate()) + " " + executeUtil, format);

                                if (currentTimestamp >= executeAtTime.getMillis() && currentTimestamp <= executeUntilTime.getMillis()) {//current time less than execute until time
                                    isValid = true;
                                }
                            } else {
                                executeAtTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekAtIndex).toDate()) + " 00:00", format);
                                executeUntilTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekUntilIndex).toDate()) + " 00:00", format);

                                if (currentTimestamp >= executeAtTime.getMillis() && currentTimestamp <= executeUntilTime.getMillis()) {//current time less than execute until time
                                    isValid = true;
                                }
                            }
                        } else if (dayOfWeekAtIndex == dayOfWeekUntilIndex) {
                            if (StringUtils.isNotBlank(executeAt) && StringUtils.isNotBlank(executeUtil)) {

                                executeAtTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekAtIndex).toDate()) + " " + executeAt, format);
                                executeUntilTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekUntilIndex).toDate()) + " " + executeUtil, format);

                                if (currentTimestamp >= executeAtTime.getMillis() && currentTimestamp <= executeUntilTime.getMillis()) {//current time less than execute until time
                                    isValid = true;
                                }
                            } else if (StringUtils.isNotBlank(executeAt) && StringUtils.isBlank(executeUtil)) {
                                executeAtTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekAtIndex).toDate()) + " " + executeAt, format);
                                executeUntilTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekUntilIndex).toDate()) + " 00:00", format);

                                if (currentTimestamp >= executeAtTime.getMillis() && currentTimestamp <= executeUntilTime.getMillis()) {//current time less than execute until time
                                    isValid = true;
                                }
                            } else if (StringUtils.isBlank(executeAt) && StringUtils.isNotBlank(executeUtil)) {
                                executeAtTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekAtIndex).toDate()) + " 00:00", format);
                                executeUntilTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekUntilIndex).toDate()) + " " + executeUtil, format);

                                if (currentTimestamp >= executeAtTime.getMillis() && currentTimestamp <= executeUntilTime.getMillis()) {//current time less than execute until time
                                    isValid = true;
                                }
                            } else {
                                executeAtTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekAtIndex).toDate()) + " 00:00", format);
                                executeUntilTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekUntilIndex).plusDays(1).toDate()) + " 00:00", format);

                                if (currentTimestamp >= executeAtTime.getMillis() && currentTimestamp <= executeUntilTime.getMillis()) {//current time less than execute until time
                                    isValid = true;
                                }
                            }
                        } else {
                            if (StringUtils.isNotBlank(executeAt) && StringUtils.isNotBlank(executeUtil)) {

                                executeAtTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekAtIndex).toDate()) + " " + executeAt, format);
                                executeUntilTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekUntilIndex).plusWeeks(1).toDate()) + " " + executeUtil, format);

                                if (currentTimestamp >= executeAtTime.getMillis() && currentTimestamp <= executeUntilTime.getMillis()) {//current time less than execute until time
                                    isValid = true;
                                }
                            } else if (StringUtils.isNotBlank(executeAt) && StringUtils.isBlank(executeUtil)) {
                                executeAtTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekAtIndex).toDate()) + " " + executeAt, format);
                                executeUntilTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekUntilIndex).plusWeeks(1).toDate()) + " 00:00", format);

                                if (currentTimestamp >= executeAtTime.getMillis() && currentTimestamp <= executeUntilTime.getMillis()) {//current time less than execute until time
                                    isValid = true;
                                }
                            } else if (StringUtils.isBlank(executeAt) && StringUtils.isNotBlank(executeUtil)) {
                                executeAtTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekAtIndex).toDate()) + " 00:00", format);
                                executeUntilTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekUntilIndex).plusWeeks(1).toDate()) + " " + executeUtil, format);

                                if (currentTimestamp >= executeAtTime.getMillis() && currentTimestamp <= executeUntilTime.getMillis()) {//current time less than execute until time
                                    isValid = true;
                                }
                            } else {
                                executeAtTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekAtIndex).toDate()) + " 00:00", format);
                                executeUntilTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekUntilIndex).plusWeeks(1).plusDays(1).toDate()) + " 00:00", format);

                                if (currentTimestamp >= executeAtTime.getMillis() && currentTimestamp <= executeUntilTime.getMillis()) {//current time less than execute until time
                                    isValid = true;
                                }
                            }
                        }
                    }
                } else if (intervalType.equalsIgnoreCase("month")) {
                    SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-");
                    String currentDate = dayFormat.format(new Timestamp(currentTimestamp));
                    if (StringUtils.isBlank(executeAt) && StringUtils.isNotBlank(executeUtil)) {//
                        executeUntilOnce = DateTime.parse(currentDate + executeUtil, format);
                        if (currentTimestamp < executeUntilOnce.getMillis()) {//current time less than execute until time
                            isValid = true;
                        }
                    } else if (StringUtils.isNotBlank(executeAt) && StringUtils.isBlank(executeUtil)) {
                        executeAtOnce = DateTime.parse(currentDate + executeAt, format);
                        if (currentTimestamp > executeAtOnce.getMillis()) {//current time great than execute until time
                            isValid = true;
                        }
                    } else if (StringUtils.isNotBlank(executeAt) && StringUtils.isNotBlank(executeUtil)) {
                        executeAtOnce = DateTime.parse(currentDate + executeAt, format);
                        executeUntilOnce = DateTime.parse(currentDate + executeUtil, format);
                        if (executeUntilOnce.getMillis() <= executeAtOnce.getMillis()) {
                            if (currentTimestamp > executeAtOnce.getMillis() && currentTimestamp < executeUntilOnce.plusMonths(1).getMillis()) {
                                isValid = true;
                            }
                        } else {
                            if (currentTimestamp > executeAtOnce.getMillis() && currentTimestamp < executeUntilOnce.getMillis()) {
                                isValid = true;
                            }
                        }
                    }
                } else if (intervalType.equalsIgnoreCase("year")) {
                    SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-");
                    String currentDate = dayFormat.format(new Timestamp(currentTimestamp));
                    if ((StringUtils.isBlank(executeAt)) && (StringUtils.isNotBlank(executeUtil))) {//
                        executeUntilOnce = DateTime.parse(currentDate + executeUtil, format);
                        if (currentTimestamp < executeUntilOnce.getMillis()) {//current time less than execute until time
                            isValid = true;
                        }
                    } else if (StringUtils.isNotBlank(executeAt) && StringUtils.isBlank(executeUtil)) {
                        executeAtOnce = DateTime.parse(currentDate + executeAt, format);
                        if (currentTimestamp > executeAtOnce.getMillis()) {//current time great than execute until time
                            isValid = true;
                        }
                    } else if (StringUtils.isNotBlank(executeAt) && StringUtils.isNotBlank(executeUtil)) {
                        executeAtOnce = DateTime.parse(currentDate + executeAt, format);
                        executeUntilOnce = DateTime.parse(currentDate + executeUtil, format);
                        if (executeUntilOnce.getMillis() <= executeAtOnce.getMillis()) {
                            if (currentTimestamp > executeAtOnce.getMillis() && currentTimestamp < executeUntilOnce.plusYears(1).getMillis()) {
                                isValid = true;
                            }
                        } else {
                            if (currentTimestamp > executeAtOnce.getMillis() && currentTimestamp < executeUntilOnce.getMillis()) {
                                isValid = true;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isValid;
    }

    /**
     * 判断策略结束时间
     * 单次策略：结束时间为空，则设置为0l
     * 重复策略：以第一个周期的结束时间为准
     *
     * @param execType      执行类型：生效类型，O单次，R重复
     * @param intervalType  适用于重复，重复类型，day每天，week每周，month月，year年
     * @param dayOfWeekAt   适用于重复，从周几开始,适用于重复执行,1(周一)、2(周二)、3(周三)、4(周四)、5(周五)、6(周六)、7(周日)
     * @param dayOfWeekUtil 适用于重复，到周几结束，适用于重复执行,1(周一)、2(周二)、3(周三)、4(周四)、5(周五)、6(周六)、7(周日)
     * @param executeAt     从什么时间开始，适用于单次和重复，时间格式为2021-05-04 04:00:00
     * @param executeUtil   从什么时间结束，适用于单次和重复，时间格式为2021-05-04 04:00:00
     * @return 结束时间 单位：毫秒时间戳
     */
    public static Long getEndTime(String execType, String intervalType, String dayOfWeekAt, String dayOfWeekUtil, String executeAt, String executeUtil) {

        Long isValid = 0l;
        DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        DateTime executeAtOnce = null;
        DateTime executeUntilOnce = null;
        long currentTimestamp = System.currentTimeMillis();
        try {
            if (execType.equalsIgnoreCase("O")) {//单次
                if (StringUtils.isNotBlank(executeUtil)) {
                    executeUntilOnce = DateTime.parse(executeUtil, format);
                    isValid = executeUntilOnce.getMillis();
                }
            } else if (execType.equalsIgnoreCase("R")) {//重复
                if (intervalType.equalsIgnoreCase("day")) {
                    SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd ");
                    String currentDate = dayFormat.format(new Timestamp(currentTimestamp));
                    if (StringUtils.isBlank(executeAt) && StringUtils.isNotBlank(executeUtil)) {//
                        executeUntilOnce = DateTime.parse(currentDate + executeUtil, format);
                        isValid = executeUntilOnce.getMillis();
                    } else if (StringUtils.isNotBlank(executeAt) && StringUtils.isBlank(executeUtil)) {
                        isValid = 0l;
                    } else if (StringUtils.isNotBlank(executeAt) && StringUtils.isNotBlank(executeUtil)) {
                        executeAtOnce = DateTime.parse(currentDate + executeAt, format);
                        executeUntilOnce = DateTime.parse(currentDate + executeUtil, format);
                        if (executeUntilOnce.getMillis() <= executeAtOnce.getMillis()) {
                            isValid = executeUntilOnce.plusDays(1).getMillis();
                        } else {
                            isValid = executeUntilOnce.getMillis();
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
                    if (StringUtils.isNotBlank(dayOfWeekAt) && StringUtils.isBlank(dayOfWeekUtil)) {
                        executeAtTime = DateTime.parse(currentDate + " " + executeAt, format);
                        executeAtZero = DateTime.parse(dayFormat.format(executeAtTime.plusDays(1).toDate()), zeroFormat);
                        isValid = executeAtZero.getMillis();
                    } else if (StringUtils.isBlank(dayOfWeekAt) && StringUtils.isNotBlank(dayOfWeekUtil)) {
                        executeUntilTime = DateTime.parse(currentDate + " " + executeUtil, format);
                        isValid = executeUntilTime.getMillis();
                    } else if (StringUtils.isNotBlank(dayOfWeekAt) && StringUtils.isNotBlank(dayOfWeekUtil)) {
                        int dayOfWeekAtIndex = Integer.valueOf(dayOfWeekAt);
                        int dayOfWeekUntilIndex = Integer.valueOf(dayOfWeekUtil);
                        if (dayOfWeekAtIndex < dayOfWeekUntilIndex) {
                            if (StringUtils.isNotBlank(executeAt) && StringUtils.isNotBlank(executeUtil)) {
                                executeUntilTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekUntilIndex).toDate()) + " " + executeUtil, format);
                            } else if (StringUtils.isNotBlank(executeAt) && StringUtils.isBlank(executeUtil)) {
                                executeUntilTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekUntilIndex).toDate()) + " 00:00", format);
                            } else if (StringUtils.isBlank(executeAt) && StringUtils.isNotBlank(executeUtil)) {
                                executeUntilTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekUntilIndex).toDate()) + " " + executeUtil, format);
                            } else {
                                executeUntilTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekUntilIndex).toDate()) + " 00:00", format);
                            }
                            isValid = executeUntilTime.getMillis();
                        } else if (dayOfWeekAtIndex == dayOfWeekUntilIndex) {
                            if (StringUtils.isNotBlank(executeAt) && StringUtils.isNotBlank(executeUtil)) {
                                executeUntilTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekUntilIndex).toDate()) + " " + executeUtil, format);
                                isValid = executeUntilTime.getMillis();
                            } else if (StringUtils.isNotBlank(executeAt) && StringUtils.isBlank(executeUtil)) {
                                executeUntilTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekUntilIndex).plusDays(1).toDate()) + " 00:00", format);
                                isValid = executeUntilTime.getMillis();
                            } else if (StringUtils.isBlank(executeAt) && StringUtils.isNotBlank(executeUtil)) {
                                executeUntilTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekUntilIndex).toDate()) + " " + executeUtil, format);
                                isValid = executeUntilTime.getMillis();
                            } else {
                                executeUntilTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekUntilIndex).plusDays(1).toDate()) + " 00:00", format);
                                isValid = executeUntilTime.getMillis();
                            }
                        } else {
                            if (StringUtils.isNotBlank(executeAt) && StringUtils.isNotBlank(executeUtil)) {
                                executeUntilTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekUntilIndex).plusWeeks(1).toDate()) + " " + executeUtil, format);
                                isValid = executeUntilTime.getMillis();
                            } else if (StringUtils.isNotBlank(executeAt) && StringUtils.isBlank(executeUtil)) {
                                executeUntilTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekUntilIndex).plusWeeks(1).toDate()) + " 00:00", format);
                                isValid = executeUntilTime.getMillis();
                            } else if (StringUtils.isBlank(executeAt) && StringUtils.isNotBlank(executeUtil)) {
                                executeUntilTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekUntilIndex).plusWeeks(1).toDate()) + " " + executeUtil, format);
                                isValid = executeUntilTime.getMillis();
                            } else {
                                executeUntilTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekUntilIndex).plusWeeks(1).plusDays(1).toDate()) + " 00:00", format);
                                isValid = executeUntilTime.getMillis();
                            }
                        }
                    }
                } else if (intervalType.equalsIgnoreCase("month")) {
                    SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-");
                    String currentDate = dayFormat.format(new Timestamp(currentTimestamp));
                    if (StringUtils.isBlank(executeAt) && StringUtils.isNotBlank(executeUtil)) {//
                        executeUntilOnce = DateTime.parse(currentDate + executeUtil, format);
                        isValid = executeUntilOnce.getMillis();
                    } else if (StringUtils.isNotBlank(executeAt) && StringUtils.isBlank(executeUtil)) {
                        isValid = 0l;
                    } else if (StringUtils.isNotBlank(executeAt) && StringUtils.isNotBlank(executeUtil)) {
                        executeAtOnce = DateTime.parse(currentDate + executeAt, format);
                        executeUntilOnce = DateTime.parse(currentDate + executeUtil, format);
                        if (executeUntilOnce.getMillis() <= executeAtOnce.getMillis()) {
                            isValid = executeUntilOnce.plusMonths(1).getMillis();
                        } else {
                            isValid = executeUntilOnce.getMillis();
                        }
                    }
                } else if (intervalType.equalsIgnoreCase("year")) {
                    SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-");
                    String currentDate = dayFormat.format(new Timestamp(currentTimestamp));
                    if ((StringUtils.isBlank(executeAt)) && (StringUtils.isNotBlank(executeUtil))) {//
                        executeUntilOnce = DateTime.parse(currentDate + executeUtil, format);
                        isValid = executeUntilOnce.getMillis();
                    } else if (StringUtils.isNotBlank(executeAt) && StringUtils.isBlank(executeUtil)) {
                        isValid = 0l;
                    } else if (StringUtils.isNotBlank(executeAt) && StringUtils.isNotBlank(executeUtil)) {
                        executeAtOnce = DateTime.parse(currentDate + executeAt, format);
                        executeUntilOnce = DateTime.parse(currentDate + executeUtil, format);
                        if (executeUntilOnce.getMillis() <= executeAtOnce.getMillis()) {
                            isValid = executeUntilOnce.plusYears(1).getMillis();
                        } else {
                            isValid = executeUntilOnce.getMillis();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isValid;
    }

    public static boolean isValid(String execType, String intervalType, String dayOfWeekAt, String dayOfWeekUtil, String executeAt, String executeUtil) {

        boolean isValid = false;
        DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        DateTime executeAtOnce = null;
        DateTime executeUntilOnce = null;
        long currentTimestamp = System.currentTimeMillis();
        //当前为本周的第几天
        int currentDayOfWeek = LocalDateTime.now().getDayOfWeek().getValue();
        //当前为本月的第几天
        int currentDayOfMonth = LocalDateTime.now().getDayOfMonth();
        //当前为本年的第几天
        int currentDayOfYear = LocalDateTime.now().getDayOfYear();
        try {
            if (execType.equalsIgnoreCase("O")) {

                if (StringUtils.isBlank(executeAt) && StringUtils.isNotBlank(executeUtil)) {//
                    executeUntilOnce = DateTime.parse(executeUtil, format);
                    if (currentTimestamp < executeUntilOnce.getMillis()) {//current time less than execute until time
                        isValid = true;
                    }
                } else if (StringUtils.isNotBlank(executeAt) && StringUtils.isBlank(executeUtil)) {
                    executeAtOnce = DateTime.parse(executeAt, format);
                    if (currentTimestamp > executeAtOnce.getMillis()) {//current time great than execute until time
                        isValid = true;
                    }
                } else if (StringUtils.isNotBlank(executeAt) && StringUtils.isNotBlank(executeUtil)) {
                    executeAtOnce = DateTime.parse(executeAt, format);
                    executeUntilOnce = DateTime.parse(executeUtil, format);
                    if (currentTimestamp > executeAtOnce.getMillis() && currentTimestamp < executeUntilOnce.getMillis()) {
                        isValid = true;
                    }
                }
            } else if (execType.equalsIgnoreCase("R")) {
                if (intervalType.equalsIgnoreCase("day")) {
                    SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd ");
                    String currentDate = dayFormat.format(new Timestamp(currentTimestamp));
                    if (StringUtils.isBlank(executeAt) && StringUtils.isNotBlank(executeUtil)) {//
                        executeUntilOnce = DateTime.parse(currentDate + executeUtil, format);
                        if (currentTimestamp < executeUntilOnce.getMillis()) {//current time less than execute until time
                            isValid = true;
                        }
                    } else if (StringUtils.isNotBlank(executeAt) && StringUtils.isBlank(executeUtil)) {
                        executeAtOnce = DateTime.parse(currentDate + executeAt, format);
                        if (currentTimestamp > executeAtOnce.getMillis()) {//current time great than execute until time
                            isValid = true;
                        }
                    } else if (StringUtils.isNotBlank(executeAt) && StringUtils.isNotBlank(executeUtil)) {
                        executeAtOnce = DateTime.parse(currentDate + executeAt, format);
                        executeUntilOnce = DateTime.parse(currentDate + executeUtil, format);
                        if (executeUntilOnce.getMillis() <= executeAtOnce.getMillis()) {
                            if (currentTimestamp > executeAtOnce.getMillis() && currentTimestamp < executeUntilOnce.plusDays(1).getMillis()) {
                                isValid = true;
                            }
                        } else {
                            if (currentTimestamp > executeAtOnce.getMillis() && currentTimestamp < executeUntilOnce.getMillis()) {
                                isValid = true;
                            }
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
                                isValid = true;
                            }
                        } else {
                            executeAtTime = DateTime.parse(currentDate + " " + executeAt, format);
                            executeAtZero = DateTime.parse(dayFormat.format(executeAtTime.plusDays(1).toDate()), zeroFormat);
                            if (currentTimestamp > executeAtTime.getMillis() && currentTimestamp < executeAtZero.getMillis()) {//current time great than execute until time
                                isValid = true;
                            }
                        }
                    } else if (StringUtils.isBlank(dayOfWeekAt) && StringUtils.isNotBlank(dayOfWeekUtil)) {
                        if (executeUtil == null) {
                            if (currentDateTime.getDayOfWeek() == Integer.valueOf(dayOfWeekUtil)) {//current time less than execute until time
                                isValid = true;
                            }
                        } else {
                            executeUntilTime = DateTime.parse(currentDate + " " + executeUtil, format);
                            executeUntilZero = DateTime.parse(dayFormat.format(executeUntilTime.minusDays(1).toDate()), zeroFormat);
                            if (currentTimestamp > executeUntilZero.getMillis() && currentTimestamp < executeUntilTime.getMillis()) {//current time great than execute until time
                                isValid = true;
                            }
                        }
                    } else if (StringUtils.isNotBlank(dayOfWeekAt) && StringUtils.isNotBlank(dayOfWeekUtil)) {
                        int dayOfWeekAtIndex = Integer.valueOf(dayOfWeekAt);
                        int dayOfWeekUntilIndex = Integer.valueOf(dayOfWeekUtil);
                        if (dayOfWeekAtIndex < dayOfWeekUntilIndex && dayOfWeekAtIndex <= currentDayOfWeek && currentDayOfWeek <= dayOfWeekUntilIndex) {
                            if (StringUtils.isNotBlank(executeAt) && StringUtils.isNotBlank(executeUtil)) {

                                executeAtTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(currentDayOfWeek).toDate()) + " " + executeAt, format);
                                executeUntilTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(currentDayOfWeek).toDate()) + " " + executeUtil, format);

                                if (currentTimestamp >= executeAtTime.getMillis() && currentTimestamp <= executeUntilTime.getMillis()) {//current time less than execute until time
                                    isValid = true;
                                }
                            } else if (StringUtils.isNotBlank(executeAt) && StringUtils.isBlank(executeUtil)) {
                                executeAtTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(currentDayOfWeek).toDate()) + " " + executeAt, format);
                                executeUntilTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(currentDayOfWeek).toDate()) + " 00:00", format);

                                if (currentTimestamp >= executeAtTime.getMillis() && currentTimestamp <= executeUntilTime.getMillis()) {//current time less than execute until time
                                    isValid = true;
                                }
                            } else if (StringUtils.isBlank(executeAt) && StringUtils.isNotBlank(executeUtil)) {
                                executeAtTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(currentDayOfWeek).toDate()) + " 00:00", format);
                                executeUntilTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(currentDayOfWeek).toDate()) + " " + executeUtil, format);

                                if (currentTimestamp >= executeAtTime.getMillis() && currentTimestamp <= executeUntilTime.getMillis()) {//current time less than execute until time
                                    isValid = true;
                                }
                            } else {
                                executeAtTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(currentDayOfWeek).toDate()) + " 00:00", format);
                                executeUntilTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(currentDayOfWeek).toDate()) + " 00:00", format);

                                if (currentTimestamp >= executeAtTime.getMillis() && currentTimestamp <= executeUntilTime.getMillis()) {//current time less than execute until time
                                    isValid = true;
                                }
                            }
                        } else if (dayOfWeekAtIndex == dayOfWeekUntilIndex && dayOfWeekUntilIndex == currentDayOfWeek) {
                            if (StringUtils.isNotBlank(executeAt) && StringUtils.isNotBlank(executeUtil)) {

                                executeAtTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekAtIndex).toDate()) + " " + executeAt, format);
                                executeUntilTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekUntilIndex).toDate()) + " " + executeUtil, format);

                                if (currentTimestamp >= executeAtTime.getMillis() && currentTimestamp <= executeUntilTime.getMillis()) {//current time less than execute until time
                                    isValid = true;
                                }
                            } else if (StringUtils.isNotBlank(executeAt) && StringUtils.isBlank(executeUtil)) {
                                executeAtTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekAtIndex).toDate()) + " " + executeAt, format);
                                executeUntilTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekUntilIndex).plusDays(1).toDate()) + " 00:00", format);

                                if (currentTimestamp >= executeAtTime.getMillis() && currentTimestamp <= executeUntilTime.getMillis()) {//current time less than execute until time
                                    isValid = true;
                                }
                            } else if (StringUtils.isBlank(executeAt) && StringUtils.isNotBlank(executeUtil)) {
                                executeAtTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekAtIndex).toDate()) + " 00:00", format);
                                executeUntilTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekUntilIndex).toDate()) + " " + executeUtil, format);

                                if (currentTimestamp >= executeAtTime.getMillis() && currentTimestamp <= executeUntilTime.getMillis()) {//current time less than execute until time
                                    isValid = true;
                                }
                            } else {
                                executeAtTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekAtIndex).toDate()) + " 00:00", format);
                                executeUntilTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekUntilIndex).plusDays(1).toDate()) + " 00:00", format);

                                if (currentTimestamp >= executeAtTime.getMillis() && currentTimestamp <= executeUntilTime.getMillis()) {//current time less than execute until time
                                    isValid = true;
                                }
                            }
                        } else if(dayOfWeekAtIndex > dayOfWeekUntilIndex){
                            if (StringUtils.isNotBlank(executeAt) && StringUtils.isNotBlank(executeUtil)) {

                                executeAtTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekAtIndex).toDate()) + " " + executeAt, format);
                                executeUntilTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekUntilIndex).plusWeeks(1).toDate()) + " " + executeUtil, format);
                                int startDayOfMonth = executeAtTime.getDayOfMonth();
                                int endDayOfMonth = executeUntilTime.getDayOfMonth();
                                if (startDayOfMonth <= currentDayOfMonth && currentDayOfMonth <= endDayOfMonth) {
                                    executeAtTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(currentDayOfWeek).toDate()) + " " + executeAt, format);
                                    executeUntilTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(currentDayOfWeek).toDate()) + " " + executeUtil, format);
                                    if (currentTimestamp >= executeAtTime.getMillis() && currentTimestamp <= executeUntilTime.getMillis()) {//current time less than execute until time
                                        isValid = true;
                                    }
                                }
                            } else if (StringUtils.isNotBlank(executeAt) && StringUtils.isBlank(executeUtil)) {
                                executeAtTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekAtIndex).toDate()) + " " + executeAt, format);
                                executeUntilTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekUntilIndex).plusWeeks(1).toDate()) + " 00:00", format);
                                int startDayOfMonth = executeAtTime.getDayOfMonth();
                                int endDayOfMonth = executeUntilTime.getDayOfMonth();
                                if (startDayOfMonth <= currentDayOfMonth && currentDayOfMonth <= endDayOfMonth) {
                                    executeAtTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(currentDayOfWeek).toDate()) + " " + executeAt, format);
                                    executeUntilTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(currentDayOfWeek).toDate()) + " 23:59", format);
                                    if (currentTimestamp >= executeAtTime.getMillis() && currentTimestamp <= executeUntilTime.getMillis()) {//current time less than execute until time
                                        isValid = true;
                                    }
                                }
                            } else if (StringUtils.isBlank(executeAt) && StringUtils.isNotBlank(executeUtil)) {
                                executeAtTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekAtIndex).toDate()) + " 00:00", format);
                                executeUntilTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekUntilIndex).plusWeeks(1).toDate()) + " " + executeUtil, format);
                                int startDayOfMonth = executeAtTime.getDayOfMonth();
                                int endDayOfMonth = executeUntilTime.getDayOfMonth();
                                if (startDayOfMonth <= currentDayOfMonth && currentDayOfMonth <= endDayOfMonth) {
                                    executeAtTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(currentDayOfWeek).toDate()) + " 00:00", format);
                                    executeUntilTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(currentDayOfWeek).toDate()) + " " + executeUtil, format);
                                    if (currentTimestamp >= executeAtTime.getMillis() && currentTimestamp <= executeUntilTime.getMillis()) {//current time less than execute until time
                                        isValid = true;
                                    }
                                }

                            } else {
                                executeAtTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekAtIndex).toDate()) + " 00:00", format);
                                executeUntilTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(dayOfWeekUntilIndex).plusWeeks(1).plusDays(1).toDate()) + " 00:00", format);
                                int startDayOfMonth = executeAtTime.getDayOfMonth();
                                int endDayOfMonth = executeUntilTime.getDayOfMonth();
                                if (startDayOfMonth <= currentDayOfMonth && currentDayOfMonth <= endDayOfMonth) {
                                    executeAtTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(currentDayOfWeek).toDate()) + " 00:00", format);
                                    executeUntilTime = DateTime.parse(dayFormat.format(currentDateTime.withDayOfWeek(currentDayOfWeek).toDate()) + " 23:59", format);
                                    if (currentTimestamp >= executeAtTime.getMillis() && currentTimestamp <= executeUntilTime.getMillis()) {//current time less than execute until time
                                        isValid = true;
                                    }
                                }
                            }
                        }
                    }
                } else if (intervalType.equalsIgnoreCase("month")) {
                    SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-");
                    String currentDate = dayFormat.format(new Timestamp(currentTimestamp));
                    if (StringUtils.isBlank(executeAt) && StringUtils.isNotBlank(executeUtil)) {//
                        executeUntilOnce = DateTime.parse(currentDate + executeUtil, format);
                        if (currentTimestamp < executeUntilOnce.getMillis()) {//current time less than execute until time
                            isValid = true;
                        }
                    } else if (StringUtils.isNotBlank(executeAt) && StringUtils.isBlank(executeUtil)) {
                        executeAtOnce = DateTime.parse(currentDate + executeAt, format);
                        if (currentTimestamp > executeAtOnce.getMillis()) {//current time great than execute until time
                            isValid = true;
                        }
                    } else if (StringUtils.isNotBlank(executeAt) && StringUtils.isNotBlank(executeUtil)) {
                        executeAtOnce = DateTime.parse(currentDate + executeAt, format);
                        executeUntilOnce = DateTime.parse(currentDate + executeUtil, format);
                        int startDayOfYear = executeAtOnce.getDayOfYear();
                        int endDayOfYear = executeUntilOnce.getDayOfYear();
                        long executeAtOnceMill = LocalDateTime.now().withHour(executeAtOnce.getHourOfDay()).withMinute(executeAtOnce.getMinuteOfHour()).withSecond(executeAtOnce.getSecondOfMinute()).toInstant(ZoneOffset.ofHours(8)).toEpochMilli();
                        long executeUntilOnceMill = LocalDateTime.now().withHour(executeUntilOnce.getHourOfDay()).withMinute(executeUntilOnce.getMinuteOfHour()).withSecond(executeUntilOnce.getSecondOfMinute()).toInstant(ZoneOffset.ofHours(8)).toEpochMilli();

                        if (executeUntilOnce.getMillis() < executeAtOnce.getMillis()) {
                            endDayOfYear = executeUntilOnce.plusMonths(1).getDayOfYear();
                            if (startDayOfYear <= currentDayOfYear && currentDayOfYear <= endDayOfYear) {
                                if (currentTimestamp >= executeAtOnceMill && currentTimestamp <= executeUntilOnceMill) {
                                    isValid = true;
                                }
                            }
                        } else {
                            if (startDayOfYear <= currentDayOfYear && currentDayOfYear <= endDayOfYear) {
                                if (currentTimestamp >= executeAtOnceMill && currentTimestamp <= executeUntilOnceMill) {
                                    isValid = true;
                                }
                            }
                        }
                    }
                } else if (intervalType.equalsIgnoreCase("year")) {
                    SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-");
                    String currentDate = dayFormat.format(new Timestamp(currentTimestamp));
                    if ((StringUtils.isBlank(executeAt)) && (StringUtils.isNotBlank(executeUtil))) {//
                        executeUntilOnce = DateTime.parse(currentDate + executeUtil, format);
                        if (currentTimestamp < executeUntilOnce.getMillis()) {//current time less than execute until time
                            isValid = true;
                        }
                    } else if (StringUtils.isNotBlank(executeAt) && StringUtils.isBlank(executeUtil)) {
                        executeAtOnce = DateTime.parse(currentDate + executeAt, format);
                        if (currentTimestamp > executeAtOnce.getMillis()) {//current time great than execute until time
                            isValid = true;
                        }
                    } else if (StringUtils.isNotBlank(executeAt) && StringUtils.isNotBlank(executeUtil)) {
                        executeAtOnce = DateTime.parse(currentDate + executeAt, format);
                        executeUntilOnce = DateTime.parse(currentDate + executeUtil, format);

                        int startDayOfYear = executeAtOnce.getDayOfYear();
                        int endDayOfYear = executeUntilOnce.getDayOfYear();
                        long executeAtOnceMill = LocalDateTime.now().withHour(executeAtOnce.getHourOfDay()).withMinute(executeAtOnce.getMinuteOfHour()).withSecond(executeAtOnce.getSecondOfMinute()).toInstant(ZoneOffset.ofHours(8)).toEpochMilli();
                        long executeUntilOnceMill = LocalDateTime.now().withHour(executeUntilOnce.getHourOfDay()).withMinute(executeUntilOnce.getMinuteOfHour()).withSecond(executeUntilOnce.getSecondOfMinute()).toInstant(ZoneOffset.ofHours(8)).toEpochMilli();


                        if (executeUntilOnce.getMillis() < executeAtOnce.getMillis()) {
                            if (currentTimestamp > executeAtOnce.getMillis() && currentTimestamp < executeUntilOnce.plusYears(1).getMillis()) {
                                isValid = true;
                            }
                        } else {
                            if (startDayOfYear <= currentDayOfYear && currentDayOfYear <= endDayOfYear) {
                                if (currentTimestamp >= executeAtOnceMill && currentTimestamp <= executeUntilOnceMill) {
                                    isValid = true;
                                }
                            }
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
