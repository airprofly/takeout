package com.airprofly.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 员工分页查询 DTO
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class EmployeePageQueryDTO extends PageQueryDTO {

    private static final long serialVersionUID = 100000004156789012L;

    /**
     * 员工姓名
     */
    private String name;

}
