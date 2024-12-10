package com.mcinfotech.event.handler.domain;

/**
 * date 2023/1/12 16:50
 * @version V1.0
 * @Package com.mcinfotech.event.domain
 * 丰富来源字段
 */
public class RichResultColumn {
    /**
     * 列描述
     */
    private String columnName;
    /**
     * 列名
     */
    private String columnValue;
    /**
     * 与其他丰富来源字段拼接时的操作符：append cover
     */
    private String operate;

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnValue() {
        return columnValue;
    }

    public void setColumnValue(String columnValue) {
        this.columnValue = columnValue;
    }

    public String getOperate() {
        return operate;
    }

    public void setOperate(String operate) {
        this.operate = operate;
    }
}
