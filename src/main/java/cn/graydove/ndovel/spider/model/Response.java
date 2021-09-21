package cn.graydove.ndovel.spider.model;

import lombok.Data;

/**
 * @author graydove
 */
@Data
public class Response<T> {

    private T data;

    private int code;

    private String message;

    private Response(T data, int code, String message) {
        this.data = data;
        this.code = code;
        this.message = message;
    }

    public static <T> Response<T> ok(T data) {
        return new Response<>(data, 200, "OK");
    }


    public static <T> Response<T> error(String error) {
        return new Response<>(null, 500, error);
    }
}
