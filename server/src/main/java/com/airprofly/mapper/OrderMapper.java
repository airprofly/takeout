package com.airprofly.mapper;

import com.airprofly.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 订单 Mapper 接口
 */
@Mapper
public interface OrderMapper {

    /**
     * 新增订单
     * @param orders 订单对象
     */
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Orders orders);

    /**
     * 根据订单号查询订单
     * @param number 订单号
     * @return 订单对象
     */
    Orders getByNumber(String number);

    /**
     * 更新订单
     * @param orders 订单对象
     */
    void update(Orders orders);

    /**
     * 分页查询订单
     * @param userId 用户ID
     * @param number 订单号
     * @param phone 手机号
     * @param status 状态
     * @param beginTime 开始时间
     * @param endTime 结束时间
     * @return 订单列表
     */
    List<Orders> pageQuery(@Param("userId") Long userId,
                           @Param("number") String number,
                           @Param("phone") String phone,
                           @Param("status") Integer status,
                           @Param("beginTime") LocalDateTime beginTime,
                           @Param("endTime") LocalDateTime endTime);

    /**
     * 根据ID查询订单
     * @param id 订单ID
     * @return 订单对象
     */
    Orders getById(Long id);

    /**
     * 统计指定状态的订单数量
     * @param status 订单状态
     * @return 订单数量
     */
    Integer countStatus(Integer status);

    /**
     * 查询指定状态且下单时间早于指定时间的订单
     * @param status 订单状态
     * @param orderTimeLT 下单时间
     * @return 订单列表
     */
    List<Orders> getByStatusAndOrderTimeLT(@Param("status") Integer status,
                                           @Param("orderTimeLT") LocalDateTime orderTimeLT);

    /**
     * 动态条件统计营业额
     * @param map 查询条件
     * @return 营业额
     */
    Double sumByMap(Map<String, Object> map);

    /**
     * 动态条件统计订单数量
     * @param map 查询条件
     * @return 订单数量
     */
    Integer countByMap(Map<String, Object> map);

    /**
     * 查询商品销量排名TOP10
     * @param begin 开始时间
     * @param end 结束时间
     * @return 销量排名
     */
    List<Map<String, Object>> getSalesTop10(@Param("begin") LocalDateTime begin,
                                             @Param("end") LocalDateTime end);
}
