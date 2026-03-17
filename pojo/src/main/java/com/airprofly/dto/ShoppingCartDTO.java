package com.airprofly.dto;

import lombok.Data;
import java.io.Serial;
import java.io.Serializable;

@Data
public class ShoppingCartDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 100000001251988966L;
    private Long dishId;
    private Long setmealId;
    private String dishFlavor;

}
