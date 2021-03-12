package com.tjmedicine.emergency.ui.bean;

/**
 * 血压基本，包含时间和数值
 */
public class PDBean {
    String time;
    float value;

    public PDBean(String time, int value) {
        this.time = time;
        this.value = value;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return
                "time='" + time + '\'' +
                        ", value=" + value;
    }
}