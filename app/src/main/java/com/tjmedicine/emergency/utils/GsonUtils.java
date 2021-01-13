package com.tjmedicine.emergency.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Json字符串转换类
 *
 * @author QiZai
 * @date 2018/4/11
 */

public class GsonUtils {
    private static Gson gson = new Gson();

    /**
     * 将json数据转换成对应的类型数据
     */
    public static <T> T parseJsonWithClass(String jsonData, Type type) {
        return gson.fromJson(jsonData, type);
    }

    /**
     * 将其他类型数据转换成json数据
     */
    public static String parseToJson(Object jsonData) {
        return gson.toJson(jsonData);
    }

    /**
     * 将json数据转换成list集合
     */
    public static <T> List<T> parseJsonArrayWithGson(String jsonData, Class<T> type) {
        return gson.fromJson(jsonData, new TypeToken<List<T>>() {
        }.getType());
    }


    public static String returnFormatText(String data) {
        String splitter = "<M>";
        String splitter1 = "</M>";
        String regex = "^" + splitter + "*|" + splitter1 + "*$";
        return data.replaceAll(regex, "");
    }

//    手机->设备	Start
//    设备->手机	OK
//    Failed
//2.2停止测试
//    手机->设备	Stop
//    设备->手机	OK
//    Failed
//2.3上报按压深度
//    设备->手机	PD=XXX，单位mm
//2.4上报一次吹气
//    设备->手机	Blow

    public static String returnStatus(String data) {
        //手机->设备	Start
        if ("Blow".equals(data)){

            return "吹起成功："+data;
        }
        if ("PD".startsWith(data)){
            String[] split = data.split("=");

            return  "--按压深度："+split[1]+"mm";
        }
        return "";

    }
}
