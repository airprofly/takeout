package com.airprofly.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GoodsSalesDTO implements Serializable {
    //商品名称
    @Serial
    private static final long serialVersionUID = 100000003357295959L;
    private String name;

    //销量
    private Integer number;
}
