package com.item.vo;

import com.item.framework.constant.GlobalStatusCode;
import com.item.framework.constant.IGlobalStatusCode;
import lombok.Data;

@Data
public class Result<T> {

    private int code;
    private String msg;
    private boolean success;
    private T data;

    public Result() {
        this.code = GlobalStatusCode.SUCCESS;
        this.msg = "success";
        this.success = true;
    }

    public Result(T data) {
        this();
        this.data = data;
    }

    public Result(int code, String msg, T result) {
        this.code = code;
        this.msg = msg;
        this.data = result;
        this.success = code == GlobalStatusCode.SUCCESS;
    }

    public static <T> Result<T> success(T result) {
        Result<T> res = new Result<>();
        res.success = true;
        res.setCode(GlobalStatusCode.SUCCESS);
        res.setMsg("success");
        res.setData(result);
        return res;
    }

    public static <T> Result<T> success() {
        Result<T> res = new Result<>();
        res.success = true;
        res.setCode(GlobalStatusCode.SUCCESS);
        res.setMsg("success");
        res.setData(null);
        return res;
    }

    public static <T> Result<T> fail(int code, String msg) {
        Result<T> res = new Result<>();
        res.setCode(code);
        res.success = false;
        res.setMsg(msg);
        res.setData(null);
        return res;
    }

    public static <T> Result<T> fail(IGlobalStatusCode code) {
        Result<T> res = new Result<>();
        res.setCode(code.getCode());
        res.success = false;
        res.setMsg(code.getMsg());
        res.setData(null);
        return res;
    }

    public static <T> Result<T> fail(int code, String msg, T data) {
        Result<T> res = new Result<>();
        res.setCode(code);
        res.success = false;
        res.setMsg(msg);
        res.setData(data);
        return res;
    }

}
