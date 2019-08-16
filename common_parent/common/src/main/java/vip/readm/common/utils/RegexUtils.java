package vip.readm.common.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author: Readm
 * @Date: 2019/7/11 4:06
 * @Version 1.0
 */

public class RegexUtils {

    public static String extract(String origin, String regex){

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(origin);
        String result="";

        if(matcher.find()){
            result=matcher.group(1).trim();
        }
        return result;
    }

}
