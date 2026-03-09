package com.airprofly.mapper;

import com.airprofly.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 订单明细 Mapper 接口
 */
@Mapper
public interface OrderDetailMapper {

    /**
     * 批量插入订单明细
     * @param orderDetailList 订单明细列表
     */
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertBatch(List<OrderDetail> orderDetailList);

    /**
     * 根据订单ID查询订单明细
     * @param orderId 订单ID
     * @return 订单明细列表
     */
    @Select("select * from order_detail where order_id = #{orderId}")
    List<OrderDetail> getByOrderId(Long orderId);
}
