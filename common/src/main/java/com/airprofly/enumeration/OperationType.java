package com.airprofly.enumeration;

public enum OperationType {
    INSERT("新增"),
    UPDATE("修改"),
    DELETE("删除"),
    QUERY("查询");

    private final String description;

    OperationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
