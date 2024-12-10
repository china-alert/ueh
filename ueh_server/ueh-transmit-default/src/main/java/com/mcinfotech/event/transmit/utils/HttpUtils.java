package com.mcinfotech.event.transmit.utils;

import org.apache.http.*;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.SSLContext;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 */
public class HttpUtils {
    private static Logger log = LogManager.getLogger(HttpUtils.class);
    private static final HttpClientBuilder httpClientBuilder = HttpClients.custom();
    static {
        /*
         一、绕过不安全的https请求的证书验证
         */
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.INSTANCE)
                .register("https", trustHttpsCertificates())
                .build();
        /*
          二、创建连接池
         */
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(registry);
        cm.setMaxTotal(50); // 连接池最大有50个连接,<=20
        /*
            123.com域名/ip+port 就是一个路由，
            http://www.123.com/s/spsb/beanvalidation/
            http://www.123.com/s/1.html
            https://www.baidu.com/一个域名，又是一个新的路由
         */
        cm.setDefaultMaxPerRoute(50); // 每个路由默认有多少连接,<=2
        /*
            // 连接池的最大连接数
            System.out.println("cm.getMaxTotal():"+cm.getMaxTotal());
            // 每一个路由的最大连接数
            System.out.println("cm.getDefaultMaxPerRoute():"+cm.getDefaultMaxPerRoute());
            PoolStats totalStats = cm.getTotalStats();
            // 连接池的最大连接数
            System.out.println("totalStats.getMax():"+totalStats.getMax());
            // 连接池里面有多少连接是被占用了
            System.out.println("totalStats.getLeased():"+totalStats.getLeased());
            // 连接池里面有多少连接是可用的
            System.out.println("totalStats.getAvailable():"+totalStats.getAvailable());
         */
        httpClientBuilder.setConnectionManager(cm);
        /*
        三、设置请求默认配置
         */
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(20000)
                .setSocketTimeout(20000)
                .setConnectionRequestTimeout(20000)
                .build();
        httpClientBuilder.setDefaultRequestConfig(requestConfig);
        /*
        四、设置默认的一些header
         */
        List<Header> defaultHeaders = new ArrayList<>();
        BasicHeader userAgentHeader = new BasicHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.141 Safari/537.36");
        defaultHeaders.add(userAgentHeader);
        httpClientBuilder.setDefaultHeaders(defaultHeaders);
    }

    /**
     * 构造安全连接工厂
     * @return SSLConnectionSocketFactory
     */
    private static ConnectionSocketFactory trustHttpsCertificates() {
        SSLContextBuilder sslContextBuilder = new SSLContextBuilder();
        try {
            sslContextBuilder.loadTrustMaterial(null, new TrustStrategy() {
                // 判断是否信任url
                @Override
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return true;
                }
            });
            SSLContext sslContext = sslContextBuilder.build();
            return new SSLConnectionSocketFactory(sslContext,
                    new String[]{"SSLv2Hello","SSLv3","TLSv1","TLSv1.1","TLSv1.2"}
                    ,null, NoopHostnameVerifier.INSTANCE);
        } catch (Exception e) {
            log.error("构造安全连接工厂失败",e);
            throw new RuntimeException("构造安全连接工厂失败");
        }
    }

    /**
     * 发送get请求
     * @param url 请求url,参数需经过URLEncode编码处理
     * @param headers 自定义请求头
     * @return 返回结果
     */
    public static String executeGet(String url, Map<String,String> headers) {
        CloseableHttpClient closeableHttpClient = httpClientBuilder.build();
        // 构造httpGet请求对象
        HttpGet httpGet = new HttpGet(url);
        // 自定义请求头设置
        if (headers != null) {
            Set<Map.Entry<String, String>> entries = headers.entrySet();
            for (Map.Entry<String, String> entry : entries) {
                httpGet.addHeader(new BasicHeader(entry.getKey(),entry.getValue()));
            }
        }
        // 可关闭的响应
        CloseableHttpResponse response = null;
        try {
            log.info("prepare to execute url:{}",url);
            response = closeableHttpClient.execute(httpGet);
            StatusLine statusLine = response.getStatusLine();
            if (HttpStatus.SC_OK == statusLine.getStatusCode()) {
                HttpEntity entity = response.getEntity();
                return EntityUtils.toString(entity, StandardCharsets.UTF_8);
            }else {
                log.error("响应失败,响应码:"+statusLine.getStatusCode());
            }
        }catch (Exception e) {
            log.error("executeGet error,url:{}",url,e);
        } finally {
            // 调用EntityUtils.consume(response.getEntity());来关闭response
            HttpClientUtils.closeQuietly(response);
        }
        return null;
    }

    /**
     * 发送表单类型的post请求
     * @param url 要请求的url
     * @param list 参数列表
     * @param headers 自定义头
     * @return 返回结果
     */
    public static String postForm(String url, List<NameValuePair> list, Map<String,String> headers) {
        CloseableHttpClient closeableHttpClient = httpClientBuilder.build();
        HttpPost httpPost = new HttpPost(url);
        if (headers != null) {
            Set<Map.Entry<String, String>> entries = headers.entrySet();
            for (Map.Entry<String, String> entry : entries) {
                httpPost.addHeader(new BasicHeader(entry.getKey(),entry.getValue()));
            }
        }
        // 确保请求头一定是form类型
        httpPost.addHeader("Content-Type","application/x-www-form-urlencoded; charset=utf-8");
        // 给post对象设置参数
        UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(list,Consts.UTF_8);
        httpPost.setEntity(formEntity);
        // 响应处理
        CloseableHttpResponse response = null;
        try {
            log.info("prepare to execute url:{}",httpPost.getRequestLine());
            response = closeableHttpClient.execute(httpPost);
            StatusLine statusLine = response.getStatusLine();
            if (HttpStatus.SC_OK == statusLine.getStatusCode()) {
                HttpEntity entity = response.getEntity();
                return EntityUtils.toString(entity, StandardCharsets.UTF_8);
            }else {
                log.error("响应失败,响应码:"+statusLine.getStatusCode());
            }
        }catch (Exception e) {
            log.error("executeGet error,url:{}",url,e);
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
        return null;
    }

    /**
     * 发送json类型的post请求
     * @param url 请求url
     * @param body json字符串
     * @param headers 自定义header
     * @return 返回结果
     */
    public static String postJson(String url, String body, Map<String,String> headers) {
        CloseableHttpClient closeableHttpClient = httpClientBuilder.build();
        HttpPost httpPost = new HttpPost(url);
        // 设置请求头
        if (headers != null) {
            Set<Map.Entry<String, String>> entries = headers.entrySet();
            for (Map.Entry<String, String> entry : entries) {
                httpPost.addHeader(new BasicHeader(entry.getKey(),entry.getValue()));
            }
        }
        // 确保请求头是json类型
        httpPost.addHeader("Content-Type","application/json; charset=utf-8");
        /*s
        设置请求体
         */
        StringEntity jsonEntity = new StringEntity(body, Consts.UTF_8);
//        jsonEntity.setContentType("application/json; charset=utf-8");
//        jsonEntity.setContentEncoding(Consts.UTF_8.name());
        httpPost.setEntity(jsonEntity);

        CloseableHttpResponse response = null;
        try {
            response = closeableHttpClient.execute(httpPost);
            StatusLine statusLine = response.getStatusLine();
            if (HttpStatus.SC_OK == statusLine.getStatusCode()) {
                HttpEntity entity = response.getEntity();
                return EntityUtils.toString(entity, StandardCharsets.UTF_8);
            }else {
                log.error("响应失败,响应码:"+statusLine.getStatusCode());
            }
        }catch (Exception e) {
            log.error("executeGet error,url:{}",url,e);
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
        return null;
    }
    public static Header[] getHeaders(String url, String body, Map<String,String> headers, String headerName) {
        CloseableHttpClient closeableHttpClient = httpClientBuilder.build();
        HttpPost httpPost = new HttpPost(url);
        // 设置请求头
        if (headers != null) {
            Set<Map.Entry<String, String>> entries = headers.entrySet();
            for (Map.Entry<String, String> entry : entries) {
                httpPost.addHeader(new BasicHeader(entry.getKey(),entry.getValue()));
            }
        }
        // 确保请求头是json类型
        httpPost.addHeader("Content-Type","application/json; charset=utf-8");
        /*
        设置请求体
         */
        StringEntity jsonEntity = new StringEntity(body, Consts.UTF_8);
        httpPost.setEntity(jsonEntity);

        CloseableHttpResponse response = null;
        try {
            response = closeableHttpClient.execute(httpPost);
            Header[] responseHeaders = response.getHeaders(headerName);
            StatusLine statusLine = response.getStatusLine();
            if (HttpStatus.SC_OK == statusLine.getStatusCode()) {
                HttpEntity entity = response.getEntity();
//                return EntityUtils.toString(entity, StandardCharsets.UTF_8);
                return responseHeaders;
            }else {
                log.error("响应失败,响应码:"+statusLine.getStatusCode());
            }
        }catch (Exception e) {
            log.error("executeGet error,url:{}",url,e);
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
        return null;
    }
}
