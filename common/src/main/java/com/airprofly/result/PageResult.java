package com.airprofly.result;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;


/**
 * 分页结果封装类
 *
 * <p>用于封装分页查询的返回结果，包含总记录数和当前页的数据列表。
 * 适用于 REST API 响应，配合 {@link org.springframework.data.domain.Page} 使用。
 *
 * @param <T> 列表数据的类型
 * @author airprofly
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 总记录数
     */
    private long total;

    /**
     * 当前页的数据列表
     */
    private List<T> records;
}
