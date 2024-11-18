package com.squirrel.project.service.impl;

import com.squirrel.project.service.UrlTitleService;
import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * URL 标题接口实现层
 */
@Service
public class UrlTitleServiceImpl implements UrlTitleService {

    /**
     * 根据 URL 获取对应网站的标题
     * @param url 网站url
     * @return 网站标题
     */
    @SneakyThrows
    @Override
    public String getTitleByUrl(String url) {
        // 创建 URL 对象
        URL targetUrl = new URL(url);
        // 建立 HTTP 连接
        HttpURLConnection connection = (HttpURLConnection) targetUrl.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            // 使用 Jsoup 发起请求并获取目标网页的内容
            Document document = Jsoup.connect(url).get();
            // title() 方法会去解析 HTML，获取 <title> 标签的内容并返回
            return document.title();
        }
        return "Error while fetching title";
    }
}
