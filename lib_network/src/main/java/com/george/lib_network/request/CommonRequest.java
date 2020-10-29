package com.george.lib_network.request;

import java.io.File;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;

public class CommonRequest {
    /**
     * 文件上传请求
     */
    private static final MediaType FILE_TYPE = MediaType.parse("application/octet-stream");

    public static Request createPostRequest(String url, RequestParams requestParams) {
        return createPostRequest(url, requestParams, null);
    }

    /**
     * 带请求头的Post请求
     *
     * @param url           连接
     * @param requestParams 请求参数
     * @param headers       请求头
     * @return 返回Request
     */
    private static Request createPostRequest(String url, RequestParams requestParams, RequestParams headers) {
        FormBody.Builder mFormBodyBuild = new FormBody.Builder();
        if (null != requestParams) {
            for (Map.Entry<String, String> entry : requestParams.urlParams.entrySet()) {
                mFormBodyBuild.add(entry.getKey(), entry.getValue());
            }
        }
        Headers.Builder mHeaderBuild = new Headers.Builder();
        if (null != headers) {
            for (Map.Entry<String, String> entry : headers.urlParams.entrySet()) {
                mHeaderBuild.add(entry.getKey(), entry.getValue());
            }
        }
        FormBody mFormBody = mFormBodyBuild.build();
        Headers mHeader = mHeaderBuild.build();
        return new Request.Builder().url(url)
                .post(mFormBody)
                .headers(mHeader)
                .build();
    }

    public static Request createGetRequest(String url, RequestParams params) {
        return createGetRequest(url, params, null);
    }

    /**
     * 带请求头的get请求
     *
     * @param url     url地址
     * @param params  参数
     * @param headers header头
     * @return
     */
    private static Request createGetRequest(String url, RequestParams params, RequestParams headers) {
        StringBuilder urlBuilder = new StringBuilder(url).append("?");
        if (null != params) {
            for (Map.Entry<String, String> entry : params.urlParams.entrySet()) {
                urlBuilder.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
        }
        Headers.Builder mHeaderBuilder = new Headers.Builder();
        if (null != headers) {
            for (Map.Entry<String, String> entry : headers.urlParams.entrySet()) {
                mHeaderBuilder.add(entry.getKey(), entry.getValue());
            }
        }
        Headers mHeader = mHeaderBuilder.build();
        return new Request.Builder()
                .url(urlBuilder.substring(0, urlBuilder.toString().length() - 1))
                .get()
                .headers(mHeader)
                .build();
    }

    public static Request createMultiPostRequest(String url, RequestParams params) {
        MultipartBody.Builder requestBody = new MultipartBody.Builder();
        requestBody.setType(MultipartBody.FORM);
        if (null != params) {
            for (Map.Entry<String, Object> entry : params.fileParams.entrySet()) {
                if (entry.getValue() instanceof File) {
                    requestBody.addPart(Headers.of("Content-Disposition", "form0-data;name=\"" + entry.getKey() + "\""),
                            RequestBody.create(FILE_TYPE, (File) entry.getValue()));
                } else if (entry.getValue() instanceof String) {
                    requestBody.addPart(Headers.of("Content-Disposition", "form-data;name=\"" + entry.getKey() + "\""),
                            RequestBody.create(null, (String) entry.getValue()));
                }
            }
        }
        return new Request.Builder().url(url).post(requestBody.build()).build();
    }


}
