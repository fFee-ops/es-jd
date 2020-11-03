package com.sl.es.utils;

import com.sl.es.entity.Content;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class HtmlParseUtil {
    public static void main(String[] args) throws Exception {

    new HtmlParseUtil().parseJD("心理学").forEach(System.out::println);

    }

    public  List<Content> parseJD(String keyword) throws Exception {
        //        JD的请求：https://search.jd.com/Search?keyword=java
        String URL="https://search.jd.com/Search?keyword="+keyword;

//    解析网页(Jsoup会饭就一个Document对象，就是浏览器的Document对象)
//        java.net.SocketTimeoutException: Read timeout  延长这里的timeoutMillis即可
        Document document = Jsoup.parse(new URL(URL),30000);
//现在所有在JS中可以使用的方法这里也能用
        Element element = document.getElementById("J_goodsList");
//        获取所有的li 元素
        Elements elements = document.getElementsByTag("li");
//        System.out.println(element.html());

        ArrayList<Content> goodsList = new ArrayList<>();





//    获取元素中的内容，遍历一下就是每一个li标签
        for (Element el:elements) {
//            爬虫那一块，现在京东多了个li标签，导致有些数据是空的，要在for循环里加个判断,判断条件可能会变化。不是固定的

            if (el.attr("class").equalsIgnoreCase("gl-item")) {//过滤空标签
                //            图片很多的网站都是采用懒加载 所以要拿到真实图片要拿 data-lazy-img
                String img = el.getElementsByTag("img").eq(0).attr("data-lazy-img");
                String price = el.getElementsByClass("p-price").eq(0).text();
                String title = el.getElementsByClass("p-name").eq(0).text();

                Content content = new Content();
                content.setImg(img);
                content.setPrice(price);
                content.setTitle(title);

                goodsList.add(content);
            }

        }

        return goodsList;
    }
}
