package com.mcinfotech.event.handler.domain;

import java.util.List;

/**
 * date 2022/5/26 15:10
 * @version V1.0
 * @Package com.mcinfotech.event.domain
 */
public class Expression {
    private String conditionColumn;
    private String conditionDataType;
    private String operator;
    private List<String> conditionValue;
    private List<Object> mapping;
    //表名
    private String tableName;
    //列名
    private String columnName;
    //返回单行结果字段
    private String resultColumnValue;
    //返回多行结果字段
    private String result;

    public String getConditionColumn() {
        return conditionColumn;
    }

    public void setConditionColumn(String conditionColumn) {
        this.conditionColumn = conditionColumn;
    }

    public String getConditionDataType() {
        return conditionDataType;
    }

    public void setConditionDataType(String conditionDataType) {
        this.conditionDataType = conditionDataType;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public List<String> getConditionValue() {
        return conditionValue;
    }

    public void setConditionValue(List<String> conditionValue) {
        this.conditionValue = conditionValue;
    }

    public List<Object> getMapping() {
        return mapping;
    }

    public void setMapping(List<Object> mapping) {
        this.mapping = mapping;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getResultColumnValue() {
        return resultColumnValue;
    }

    public void setResultColumnValue(String resultColumnValue) {
        this.resultColumnValue = resultColumnValue;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "Expression{" +
                "conditionColumn='" + conditionColumn + '\'' +
                ", conditionDataType='" + conditionDataType + '\'' +
                ", operator='" + operator + '\'' +
                ", conditionValue=" + conditionValue +
                ", mapping=" + mapping +
                ", tableName='" + tableName + '\'' +
                ", columnName='" + columnName + '\'' +
                ", resultColumnValue='" + resultColumnValue + '\'' +
                ", result='" + result + '\'' +
                '}';
    }
}
