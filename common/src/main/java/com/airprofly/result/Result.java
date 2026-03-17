package com.airprofly.result;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 统一响应结果封装类
 *
 * @param <T> 响应数据类型
 */
@Data
@Accessors(chain = true)
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 响应码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String msg;

    /**
     * 响应数据
     */
    private T data;

    public Result() {
    }

    public Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    // ============ 成功响应 ============

    /**
     * 成功响应(无数据)
     */
    public static <T> Result<T> success() {
        return new Result<>(1, "success", null);
    }

    /**
     * 成功响应(带数据)
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(1, "success", data);
    }

    /**
     * 成功响应(自定义消息)
     */
    public static <T> Result<T> success(String msg) {
        return new Result<>(1, msg, null);
    }

    /**
     * 成功响应(自定义消息和数据)
     */
    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(1, msg, data);
    }

    // ============ 失败响应 ============

    /**
     * 失败响应
     */
    public static <T> Result<T> error(String msg) {
        return new Result<>(0, msg, null);
    }

    /**
     * 失败响应(自定义响应码)
     */
    public static <T> Result<T> error(Integer code, String msg) {
        return new Result<>(code, msg, null);
    }

}
