package cc.wulian.smarthomev6.support.core.mqtt;

import android.support.annotation.NonNull;

/**
 * Created by zbl on 2017/3/20.
 * MQTT订阅的主题，包括主题字符串和Qos
 */

public class Topic {
    public String topic;
    public int qos = 1;

    public Topic(@NonNull String topic, int qos) {
        this.topic = topic;
        this.qos = qos;
    }
}
