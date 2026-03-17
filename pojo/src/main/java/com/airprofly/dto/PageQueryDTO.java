package com.airprofly.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 分页查询基类
 * 所有分页查询 DTO 的基类,包含通用分页参数
 */
@Data
public class PageQueryDTO implements Serializable {

    private static final long serialVersionUID = 100000001294868459L;
    /**
     * 页码,从1开始
     */
    private Integer page = 1;

    /**
     * 每页显示记录数
     */
    private Integer pageSize = 10;

}
