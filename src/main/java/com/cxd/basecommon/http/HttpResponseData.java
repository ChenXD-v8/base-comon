package com.cxd.basecommon.http;

import lombok.Data;

/**
 * <p>
 * description
 * </p>
 *
 * @author xiaodong.chen01@hand-china.com 2022/07/21 14:04
 */
@Data
public class HttpResponseData {
    public static final String DATA_TYPE = "data";
    public static final String BYTE_TYPE = "byte";

    private Boolean success;
    private String type;
    private String data;
    private byte[] bytes;
    private String message;

    private HttpResponseData(Boolean success, String type, String data, byte[] bytes) {
        this.success = success;
        this.type = type;
        this.data = data;
        this.bytes = bytes;
    }

    public HttpResponseData(Boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static HttpResponseData successData(String data){
        return new HttpResponseData(true,DATA_TYPE,data,null);
    }
    public static HttpResponseData successData(byte[] bytes){
        return new HttpResponseData(true,BYTE_TYPE,null,bytes);
    }
    public static HttpResponseData errorData(String message){
        return new HttpResponseData(false,message);
    }

}
