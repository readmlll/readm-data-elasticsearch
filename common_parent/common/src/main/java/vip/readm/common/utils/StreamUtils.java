package vip.readm.common.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/**
 * @Author: Readm
 * @Date: 2019/8/14 11:47
 * @Version 1.0
 */

public class StreamUtils {

    static private Log log= LogFactory.getLog(StreamUtils.class);

    /**
     * 读取输入流中所有字节
     * @param inputStream
     * @param close
     * @return
     */
    static public byte[] readAllBytes(InputStream inputStream,boolean close){

        byte[] bytes=new byte[1];
        bytes[0]=0;

        if(inputStream==null){
            log.error("输入流为null");
            return bytes;
        }


        try {
            int size=  inputStream.available();
            bytes=new byte[size];
            inputStream.read(bytes);

        } catch (Exception e) {
            log.error("发生错误");
            e.printStackTrace();
        }finally {
            if(close){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bytes;
    }

    /**
     * 读取输入流中所有字节转化到string
     * @param inputStream
     * @param close
     * @return
     */
    static public  String readToString(InputStream inputStream,boolean close){
        String res="";
        byte[]  bytes=readAllBytes(inputStream,close);
        try {
            res=new String(bytes,"utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return res;
    }

}
