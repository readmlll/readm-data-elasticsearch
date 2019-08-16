package vip.readm.common.utils;

import okhttp3.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;

/**
 * @Author: Readm
 * @Date: 2019/5/30 17:24
 * @Version 1.0
 */


class OKHttpsClientBuilder {

   public static OkHttpClient.Builder oKHttpsClientBuilder() {
       try {
           TrustManager[] trustAllCerts = buildTrustManagers();
           final SSLContext sslContext = SSLContext.getInstance("SSL");
           sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

           final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
           OkHttpClient.Builder builder = new OkHttpClient.Builder();
           builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
           builder.hostnameVerifier((hostname, session) -> true);
           return builder;
       } catch (Exception e) {
           e.printStackTrace();
           return new OkHttpClient.Builder();
       }
   }

   private static TrustManager[] buildTrustManagers() {
       return new TrustManager[]{
               new X509TrustManager() {
                   @Override
                   public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                   }

                   @Override
                   public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                   }

                   @Override
                   public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                       return new java.security.cert.X509Certificate[]{};
                   }
               }
       };
   }
}

/**
 * 利用okhttp库 发送get或者post请求
 */
public class OkHttpUtils {


   static  Log log= LogFactory.getLog(OkHttpUtils.class);

    private OkHttpUtils(){}

    private static OkHttpClient okHttpClient;
    private static OkHttpClient okHttpsClient;
    static {
        getHttpInstance();
        getHttpsInstance();
    }



    /**
     * 得到单例的OkHttpClient对象
     * @return
     */
    public static OkHttpClient getHttpInstance(){
        //单例
        if(okHttpClient==null){

            synchronized (OkHttpUtils.class){

                if(okHttpClient==null){
                    okHttpClient=new OkHttpClient();
                }
            }
        }
        return okHttpClient;
    }

    /**
     * 得到单例的OkHttpsClient对象 忽略了ssl证书用于对https的访问
     * @return
     */
    public static OkHttpClient getHttpsInstance(){
        //单例
        if(okHttpsClient==null){

            synchronized (OkHttpUtils.class){

                if(okHttpsClient==null){
                    okHttpsClient=OKHttpsClientBuilder.oKHttpsClientBuilder().build();
                }
            }
        }
        return okHttpClient;
    }


    /**
     * 对于get的请求参数生成
     * @param param 传入参数的键值对
     * @return 生成参数url 示例?q=123&m=456 如果出错返回null
     */
    public static String getParamParse(Map<String, String> param){

        StringBuilder sb=new StringBuilder();
        sb.append("?");
        Set<Map.Entry<String, String>> entrySet=param.entrySet();
        for(Map.Entry<String, String> entry:entrySet){
            try {
                String key= URLEncoder.encode(entry.getKey(), "utf8");
                String value= URLEncoder.encode(entry.getValue(), "utf8");
                sb.append(key);
                sb.append("=");
                sb.append(value);
                sb.append("&");

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return  null;
            }
        }

        String urlParam=sb.toString().substring(0, sb.toString().length()-1);


        return urlParam;
    }




    /**
     * 简单的get请求  传入url   如有需要可以传入请求头的键值对
     * @param strUrl
     * @param head
     * @return
     */
    public static String get(String strUrl, Map<String, String> head, boolean ssl){

        String res="";
        Request.Builder requestBuilder = new Request.Builder().url(strUrl);

        //增加请求头信息
        if(head!=null&&!head.isEmpty()){

            Set<Map.Entry<String, String>> entry=head.entrySet();
            for (Map.Entry<String, String> en:entry){
                try {
                    requestBuilder.addHeader(en.getKey(), URLEncoder.encode(en.getValue(), "utf-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }

        Request request=requestBuilder.build();
        Response response=null;
        try {
            if(ssl){
                response=getHttpsInstance().newCall(request).execute();
            }
            else
                response=getHttpInstance().newCall(request).execute();
        } catch (IOException e) {
            log.error("newCall(request).execute(); 错误 "+e.getMessage());
            e.printStackTrace();
        }
        //如果访问失败 则返回空串
        if(!response.isSuccessful()){
            return res;
        }

        try {
            res=response.body().string();
        } catch (IOException e) {
            log.error("response.body() 错误 "+e.getMessage());
            e.printStackTrace();
        }
        return res;
    }


    /**
     * 传入post请求的键值对 返回RequestBody对象
     * @param postData
     * @return
     */
    private static RequestBody CreatePostRequestBody(Map<String, String> postData){

        RequestBody requestBody=null;
        FormBody.Builder requestBodyBuilder= new FormBody.Builder();


        if(postData!=null&&!postData.isEmpty()){

            Set<Map.Entry<String, String>> entrySet=postData.entrySet();
            for(Map.Entry<String, String> entry:entrySet){
                requestBodyBuilder.add(entry.getKey(), entry.getValue());
            }
        }
        requestBody=requestBodyBuilder.build();

        return requestBody;
    }


    /**
     * 简单的post请求，传入url，post参数键值对，如有需要可传入请求头键值对
     * @param strUrl
     * @param postData
     * @param head
     * @return
     */
    public static ResponseBody postGetBody(String strUrl, Map<String, String> postData , Map<String, String> head, boolean ssl){
        String res="";
        Request.Builder requestBuilder = new Request.Builder().url(strUrl);
        requestBuilder.method("POST", CreatePostRequestBody(postData));

        //添加头信息
        if(head!=null && head.size()>0){
            Set<Map.Entry<String, String>> entrySet = head.entrySet();
            for(Map.Entry<String, String> entry:entrySet){

                requestBuilder.addHeader(entry.getKey(),entry.getValue());
            }
        }

        Request request=requestBuilder.build();
        Response response=null;

        try {
            if(ssl)
                response= getHttpsInstance().newCall(request).execute();
            else
                response= getHttpInstance().newCall(request).execute();
        } catch (IOException e) {
            System.out.println("执行post请求时 newCall(request).execute() 失败");
            e.printStackTrace();
        }

        if (response==null)
            return null;

        try {
            return response.body();
        } catch (Exception e) {
            System.out.println("执行post请求时 body().string()失败");
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 简单的post请求，传入url，post参数键值对，如有需要可传入请求头键值对
     * @param strUrl
     * @param postData
     * @param head
     * @return
     */
    public static String post(String strUrl, Map<String, String> postData , Map<String, String> head, boolean ssl){
        String res="";
        Request.Builder requestBuilder = new Request.Builder().url(strUrl);
        requestBuilder.method("POST", CreatePostRequestBody(postData));

        //添加头信息
        if(head!=null && head.size()>0){
            Set<Map.Entry<String, String>> entrySet = head.entrySet();
            for(Map.Entry<String, String> entry:entrySet){

                requestBuilder.addHeader(entry.getKey(),entry.getValue());
            }
        }

        Request request=requestBuilder.build();
        Response response=null;

        try {
            if(ssl){
                response= getHttpsInstance().newCall(request).execute();
            }
            else{
                response= getHttpInstance().newCall(request).execute();
            }

        } catch (IOException e) {
            System.out.println("执行post请求时 newCall(request).execute() 失败");
            e.printStackTrace();
        }

        if (response==null)
            return res;

        try {
            res=response.body().string();
        } catch (IOException e) {
            System.out.println("执行post请求时 body().string()失败");
            e.printStackTrace();
        }

        return res;
    }



    /**
     * 简单的post请求，传入url，json字串，如有需要可传入请求头键值对
     * @param strUrl
     * @param json
     * @param head
     * @return
     */
    public static String post(String strUrl, String json , Map<String, String> head, boolean ssl){
        String res="";
        Request.Builder requestBuilder = new Request.Builder().url(strUrl);
        RequestBody requestBody=FormBody.create(MediaType.parse("application/json; charset=utf-8"), json);
        requestBuilder.method("POST", requestBody);

        //添加头信息
        if(head!=null && head.size()>0){
            Set<Map.Entry<String, String>> entrySet = head.entrySet();
            for(Map.Entry<String, String> entry:entrySet){

                requestBuilder.addHeader(entry.getKey(),entry.getValue());
            }
        }

        Request request=requestBuilder.build();
        Response response=null;

        try {
            if(ssl)
                response= getHttpsInstance().newCall(request).execute();
            else
                response= getHttpInstance().newCall(request).execute();
        } catch (IOException e) {
            System.out.println("执行post请求时 newCall(request).execute() 失败");
            e.printStackTrace();
        }

        if (response==null)
            return res;

        try {
            res=response.body().string();
        } catch (IOException e) {
            System.out.println("执行post请求时 body().string()失败");
            e.printStackTrace();
        }

        return res;
    }



}
