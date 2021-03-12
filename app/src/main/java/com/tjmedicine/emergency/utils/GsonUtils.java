package com.tjmedicine.emergency.utils;

import android.util.Log;

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

    /**
     * <PD=0,0,0,0,0,0|CD>  <PD=00,0,0,0,0|CD>   <PD=0,0,0,0,0,|CD>
     * <OK>
     * <Errno=errno|XY>
     * <Blow>
     * <Battery =bat|XY>
     * <FWVer=ver|XY>
     * <HWVer=ver|XY>
     *
     * @param data
     * @return
     */
    public static String returnFormatText2(String data) {
        String TAG = "TAG-RETURN";
        String str = "";
        if (data.startsWith("<") && data.endsWith(">")) {
            String splitter = "<";
            String splitter1 = ">";
            String regex = "^" + splitter + "*|" + splitter1 + "*$";

            str = data.replaceAll(regex, "");
            if (str.startsWith("PD=")) {
                String[] split = str.split("=");
                if (split.length > 1) {
                    //0,0,0,0,0,0|CD
                    if (split[1].contains("|")) {
                        String[] split1 = split[1].split("\\|");
                        Log.e(TAG, "returnFormatText2: PD=" + split1[0]);
                        Log.e(TAG, "returnFormatText2: ABCD=" + split1[1]);
                    }
                }
            }
            if ("Battery=".startsWith(str)) {//电池电量 单位百分比
//                Log.e(TAG, "returnFormatText2: Battery=", );
            }
            if ("FWVer=".startsWith(str)) {//软件版本号
//                Log.e(TAG, "returnFormatText2: FWVer=", );
            }
            if ("HWVer".startsWith(str)) {//硬件版本号
//                Log.e(TAG, "returnFormatText2: HWVer=", );
            }

            if ("Blow".startsWith(str)) {
//                Log.e(TAG, "returnFormatText2: Blow=", );
            }
            if ("Errno=".startsWith(str)) {
//                Log.e(TAG, "returnFormatText2: Errno=", );
            }
            if ("OK=".startsWith(str)) {
//                Log.e(TAG, "returnFormatText2: OK=", );
            }
            return null;
        }
        return null;
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
        if ("Blow".equals(data)) {

            return "吹起成功：" + data;
        }
        if ("PD".startsWith(data)) {
            String[] split = data.split("=");

            return "--按压深度：" + split[1] + "mm";
        }
        return "";

    }
}
