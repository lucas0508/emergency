package com.tjmedicine.emergency.utils;

import java.util.regex.Pattern;

/**
 * 验证类
 *
 * @author QiZai
 * @date 2018/5/2 10:07
 */

public class ValidateUtils {


    /**
     * 手机号验证
     *
     * @param phone
     * @return
     */
    public static Boolean validatePhone(String phone) {
        return phone != null && Pattern.compile("^1[345678]\\d{9}$").matcher(phone).matches();
    }

    /**
     * 邮箱验证
     *
     * @param email
     * @return
     */
    public static Boolean validateEmail(String email) {
        return email != null && Pattern.compile("^[a-zA-Z0-9][\\w\\.-]*[a-zA-Z0-9]@[a-zA-Z0-9][\\w\\.-]*[a-zA-Z0-9]\\.[a-zA-Z][a-zA-Z\\.]*[a-zA-Z]$")
                .matcher(email).matches();
    }


    /**
     * 密码验证
     *
     * @param password
     * @return
     */
    public static Boolean validatePassword(String password) {
        return password != null && Pattern.compile("(?![0-9]+)([A-Za-z0-9]{8,16})").matcher(password).matches();
    }


    /**
     * 支付密码验证
     *
     * @param password
     * @return
     */
    public static Boolean validatePaymentPassword(String password) {
        return password != null && Pattern.compile("^\\d{6}$").matcher(password).matches();
    }


    /**
     * 验证码验证
     *
     * @param code
     * @return
     */
    public static Boolean validateCode(String code) {
        return code != null && Pattern.compile("^\\d{6}$").matcher(code).matches();
    }


    /**
     * 验证银行卡
     *
     * @param bankCard
     * @return
     */
    public static Boolean validateBankCard(String bankCard) {
        return bankCard != null && Pattern.compile("^([1-9]{1})(\\d{14,18})$").matcher(bankCard).matches();
    }


    /**
     * 验证身份证
     *
     * @param IDCard
     * @return
     */
    public static Boolean validateIDCard(String IDCard) {
        return IDCard != null && Pattern.compile("^[1-9]\\d{7}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}$|^[1-9]\\d{5}[1-9]\\d{3}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}([0-9]|X)$").matcher(IDCard).matches();

    }


}
