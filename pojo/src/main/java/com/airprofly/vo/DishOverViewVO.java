package com.airprofly.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 菜品总览
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DishOverViewVO implements Serializable {
    // 已启售数量
    @Serial
    private static final long serialVersionUID = 100000002058776671L;
    private Integer sold;

    // 已停售数量
    private Integer discontinued;
}
