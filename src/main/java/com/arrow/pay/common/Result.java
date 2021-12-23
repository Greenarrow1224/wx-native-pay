package com.arrow.pay.common;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * 返回结果
 *
 * @author Greenarrow
 * @date 2021-12-17 13:35
 **/
@Data
@Accessors(chain = true)
public class Result {

    private Integer code;
    private String message;
    private Map<String, Object> data = new HashMap<>();

    public static Result ok(){
        Result r = new Result();
        r.setCode(HttpStatus.OK.value());
        r.setMessage("成功");
        return r;
    }
    public static Result error(){
        Result r = new Result();
        r.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        r.setMessage("失败");
        return r;
    }
    public static Result error(Integer code, String message){
        Result r = new Result();
        r.setCode(code);
        r.setMessage(message);
        return r;
    }
    public Result data(String key, Object value){
        this.data.put(key, value);
        return this;
    }
}
