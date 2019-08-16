package vip.readm.common.utils;


import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class CookieUtils {


    private HttpServletResponse response;
    private HttpServletRequest request;

    /**
     * 构造函数 为了方便直接传入 response request 所对应的对象
     *
     * @param response
     * @param request
     */
    public CookieUtils(HttpServletResponse response, HttpServletRequest request) {
        this.request = request;
        this.response = response;
    }


    /**
     * 设置cookie的基础方法
     *
     * @param key    cookie的名字
     * @param value  cookie对应的值
     * @param expire cookie的有效期
     * @param path   cookie 有效的路径
     * @param domain cookie 有效的域名
     */
    public void setCookie(String key, String value, Integer expire, String path,
                          String domain) {

        Cookie cookie = new Cookie(key, value);

        if (expire != null) {
            cookie.setMaxAge(expire);
        }
        if (path != null) {
            cookie.setPath(path);
        }
        if (domain != null) {
            cookie.setDomain(domain);
        }
        this.response.addCookie(cookie);
    }

    public void setCookie(String key, String value) {
        setCookie(key, value, null, null, null);
    }

    public void setCookie(String key, String value, Integer expire) {
        setCookie(key, value, expire, null, null);
    }

    public void setCookie(String key, String value, Integer expire, String path) {
        setCookie(key, value, expire, path, null);
    }


    /**
     * 根据cookie名字获取cookie的值
     *
     * @param key cookie名
     * @return 返回对应的cookie值  如果没有返回空字符串
     */
    public String getCookie(String key) {


        Cookie[] cookies = request.getCookies();
        String value = "";

        if (cookies != null) {
            for (Cookie cookie : cookies) {

                if (key.equals(cookie.getName())) {
                    value = cookie.getValue();
                    break;
                }
            }
        }

        return value;
    }


    /**
     * url编码  utf-8格式
     *
     * @param param 需要url编码的数据
     * @return 返回编码后的结果  如果失败返回空字符串
     */
    public String urlEncode(String param) {

        try {

            return URLEncoder.encode(param, "utf-8");

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * url解码
     *
     * @param param 需要进行url编码解码的数据
     * @return 返回url编码 解码后的结果  如果失败返回空字符串
     */
    public String urlDecode(String param) {

        try {
            return URLDecoder.decode(param, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }
    }


    /**
     * 将字符按指定格式编码
     *
     * @param str     原始字符串
     * @param charset 字符集
     * @return 成功返回编码后的结果 失败为空串
     */
    public String charEncode(String str, String charset) {

        String res = "";
        try {
            res = new String(str.getBytes(), charset);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return res;
        }

        return res;
    }

}
