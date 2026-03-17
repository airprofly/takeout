package com.airprofly.util;

import com.airprofly.dto.PageQueryDTO;
import com.airprofly.result.PageResult;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import java.util.List;

/**
 * PageHelper 分页工具类
 * <p>
 * 提供 PageHelper 分页操作的便捷方法，简化 Service 层的分页处理逻辑
 *
 * @author airprofly
 * @since 1.0.0
 */
public final class PageHelperUtil {

    private PageHelperUtil() {
        // 私有构造方法，防止实例化
    }

    /**
     * 开始分页
     * <p>
     * 在执行查询前调用此方法，PageHelper 会自动对下一条查询语句进行分页
     *
     * @param pageQueryDTO 分页查询参数
     */
    public static void startPage(PageQueryDTO pageQueryDTO) {
        if (pageQueryDTO == null) {
            PageHelper.startPage(1, 10);
        } else {
            PageHelper.startPage(pageQueryDTO.getPage(), pageQueryDTO.getPageSize());
        }
    }

    /**
     * 开始分页（自定义参数）
     *
     * @param pageNum  页码，从1开始
     * @param pageSize 每页显示记录数
     */
    public static void startPage(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
    }

    /**
     * 构建分页结果
     * <p>
     * 将 PageHelper 返回的分页数据转换为统一的 PageResult 格式
     *
     * @param list 查询结果列表
     * @param <T>  列表数据类型
     * @return 分页结果
     */
    public static <T> PageResult<T> of(List<T> list) {
        if (list instanceof Page) {
            Page<?> page = (Page<?>) list;
            return PageResult.<T>builder()
                    .total(page.getTotal())
                    .records(list)
                    .build();
        }
        // 如果不是 Page 类型，说明没有使用 PageHelper 分页
        return PageResult.<T>builder()
                .total(list.size())
                .records(list)
                .build();
    }

    /**
     * 构建分页结果（支持类型转换）
     * <p>
     * 用于处理 Entity 转 VO 的场景，从原始列表中提取分页信息，返回转换后的列表
     *
     * @param originalList 原始查询结果列表（用于提取分页信息）
     * @param convertedList 转换后的列表（实际返回的数据）
     * @param <E> 原始数据类型
     * @param <T> 转换后的数据类型
     * @return 分页结果
     */
    public static <E, T> PageResult<T> of(List<E> originalList, List<T> convertedList) {
        if (originalList instanceof Page) {
            Page<?> page = (Page<?>) originalList;
            return PageResult.<T>builder()
                    .total(page.getTotal())
                    .records(convertedList)
                    .build();
        }
        return PageResult.<T>builder()
                .total(originalList.size())
                .records(convertedList)
                .build();
    }

    /**
     * 执行分页查询（便捷方法）
     * <p>
     * 集成了 startPage 和构建分页结果的完整流程
     *
     * @param pageQueryDTO 分页查询参数
     * @param supplier     查询执行函数
     * @param <T>          列表数据类型
     * @return 分页结果
     */
    public static <T> PageResult<T> query(PageQueryDTO pageQueryDTO, java.util.function.Supplier<List<T>> supplier) {
        startPage(pageQueryDTO);
        List<T> list = supplier.get();
        return of(list);
    }
}
