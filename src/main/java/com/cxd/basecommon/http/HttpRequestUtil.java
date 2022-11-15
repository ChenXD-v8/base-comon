package com.cxd.basecommon.http;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * http请求通用类
 * </p>
 *
 * @author xiaodong.chen01@hand-china.com 2022/07/07 11:08
 */
public class HttpRequestUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpRequestUtil.class);

    /**
     * 发送get请求
     * @param url
     * @param bodyParam 参数
     * @param milliseconds 超时时间 毫秒
     * @return success 0 1 ,
     */
    public static HttpResponseData sendGetRequest(String url, String bodyParam, int milliseconds){
        String result = null;
        try {
            LOGGER.debug("请求第三方URL:{}",url);
            HttpRequest httpRequest = HttpUtil.createGet(url)
                    .body(bodyParam)
                    .contentType("application/json")
                    .setConnectionTimeout(milliseconds);
            result = httpRequest.execute().body();
        }catch (Exception e){
            LOGGER.error("发送请求失败:{}",e.getMessage());
            return HttpResponseData.errorData(e.getMessage());
        }
        LOGGER.debug("返回值：{}",result);
        return HttpResponseData.successData(result);
    }
    public static HttpResponseData sendPostRequest(String url, String bodyParam, int milliseconds){
        String result = "";
        try {
            LOGGER.debug("请求第三方URL:{}",url);
            HttpRequest httpRequest = HttpUtil.createPost(url)
                    .body(bodyParam)
                    .contentType("application/json")
                    .setConnectionTimeout(milliseconds);
            result = httpRequest.execute().body();
        }catch (Exception e){
            LOGGER.error("发送请求失败:{}",e.getMessage());
            return HttpResponseData.errorData(e.getMessage());
        }
        LOGGER.debug("返回值：{}",result);
        return HttpResponseData.successData(result);
    }

    /**
     * 发送get请求 获取文件信息
     * @param url
     * @param bodyParam 参数
     * @param milliseconds 超时时间 毫秒
     * @return success 0 1 ,
     */
    public static HttpResponseData getFileBytes(String url,String bodyParam,int milliseconds){
        byte[] bytes = null;
        try {
            LOGGER.debug("请求第三方URL:{}",url);
            HttpRequest httpRequest = HttpUtil.createGet(url)
                    .body(bodyParam)
                    .contentType("application/json")
                    .setConnectionTimeout(milliseconds);
            bytes = httpRequest.execute().bodyBytes();
        }catch (Exception e){
            LOGGER.error("发送请求失败:{}",e.getMessage());
            return HttpResponseData.errorData(e.getMessage());
        }
        return HttpResponseData.successData(bytes);
    }
}
