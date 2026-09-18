package org.example.medflow.vo;

import lombok.Data;

@Data
public class R<T> {
    private Integer code; // 状态码：200成功/500失败/401未授权/403无权限
    private String msg; // 提示信息
    private T data; // 返回数据

    // 成功（无数据）
    public static <T> R<T> success() {
        R<T> r = new R<>();
        r.setCode(200);
        r.setMsg("操作成功");
        return r;
    }

    // 成功（有数据）
    public static <T> R<T> success(T data) {
        R<T> r = new R<>();
        r.setCode(200);
        r.setMsg("操作成功");
        r.setData(data);
        return r;
    }

    // 失败
    public static <T> R<T> error(String msg) {
        R<T> r = new R<>();
        r.setCode(500);
        r.setMsg(msg);
        return r;
    }

    // 自定义状态码
    public static <T> R<T> result(Integer code, String msg, T data) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setMsg(msg);
        r.setData(data);
        return r;
    }

}