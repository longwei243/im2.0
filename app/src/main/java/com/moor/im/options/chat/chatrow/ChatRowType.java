package com.moor.im.options.chat.chatrow;

/**
 * Created by longwei on 2016/3/9.
 * 聊天条目的类型
 */
public enum ChatRowType {

    /**
     * 接收的文本消息类型
     */
    TEXT_ROW_RECEIVED("C200R" , Integer.valueOf(1)),

    /**
     * 发送的文本消息类型
     */
    TEXT_ROW_TRANSMIT("C200T" , Integer.valueOf(2)),
    /**
     * 接收的图片消息类型
     */
    IMAGE_ROW_RECEIVED("C300R" , Integer.valueOf(3)),

    /**
     * 发送的图片消息类型
     */
    IMAGE_ROW_TRANSMIT("C300T" , Integer.valueOf(4)),
    /**
     * 接收的语音消息类型
     */
    VOICE_ROW_RECEIVED("C400R" , Integer.valueOf(5)),

    /**
     * 发送的语音消息类型
     */
    VOICE_ROW_TRANSMIT("C400T" , Integer.valueOf(6));

    private final Integer mId;
    private final Object mDefaultValue;

    private ChatRowType(Object defaultValue , Integer id) {
        this.mId = id;
        this.mDefaultValue = defaultValue;
    }

    /**
     * Method that returns the unique identifier of the setting.
     * @return the mId
     */
    public Integer getId() {
        return this.mId;
    }

    /**
     * Method that returns the default value of the setting.
     *
     * @return Object The default value of the setting
     */
    public Object getDefaultValue() {
        return this.mDefaultValue;
    }

    public static ChatRowType fromValue(String value) {
        ChatRowType[] values = values();
        int cc = values.length;
        for (int i = 0; i < cc; i++) {
            if (values[i].mDefaultValue.equals(value)) {
                return values[i];
            }
        }
        return null;
    }


}
