package com.airprofly.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 数据概览查询 DTO
 * 用于查询指定时间范围内的数据概览
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataOverViewQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 100000001497205899L;

    /**
     * 开始时间
     */
    private LocalDateTime begin;

    /**
     * 结束时间
     */
    private LocalDateTime end;

}
