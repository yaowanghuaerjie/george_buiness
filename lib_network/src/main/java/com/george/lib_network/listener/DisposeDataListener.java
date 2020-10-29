package com.george.lib_network.listener;

public interface DisposeDataListener {
    /**
     * 请求成功回调事件处理
     *
     * @param responseObj
     */
    void onSuccess(Object responseObj);

    /**
     * 请求失败回调事件处理
     */
    void onFailure(Object responseObj);
}
