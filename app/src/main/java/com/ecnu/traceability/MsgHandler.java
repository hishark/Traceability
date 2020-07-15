package com.ecnu.traceability;

public interface MsgHandler
{
    /**
     * 消息
     * @param type 消息类型
     * @param data 数据
     */
    void onMessage(String type, Object data);
    /**
     * 事件
     * @param event x
     */
    void onEvent(int event);
}