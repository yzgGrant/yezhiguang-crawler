package com.github.hcsp;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {

    public static void main(String[] args) throws IOException {
        List<String> linkPool = new ArrayList<>();
        Set<String> processedLinks = new HashSet<>();
        linkPool.add("https://sina.cn");

        while (true) {
            if (linkPool.isEmpty()) {
                break;
            }

            String link = linkPool.remove(linkPool.size() - 1);
            //从尾部删除更有效率

            if (processedLinks.contains(link)) {
                continue;
            }
            if (isInterestedLink(link)) {
                //这是我们感兴趣的，我们只处理新浪站内链接
                Document doc = httpGetAndParseHtml(link);

//                ArrayList<Element> links = doc.select("a");
                doc.select("a").stream().map(aTag -> aTag.attr("href")).forEach(linkPool::add);

//                for (Element aTag : links) {
//                    linkPool.add(aTag.attr("href"));
//                }

                storeInroDatabaseIfItIsNewsPage(doc);


                processedLinks.add(link);
            }

        }
    }

    private static void storeInroDatabaseIfItIsNewsPage(Document doc) {
        //假如这是个新闻的详情页面，就存入数据库，否则，就什么也不做
        ArrayList<Element> articleTags = doc.select("article");
        if (!articleTags.isEmpty()) {
            for (Element article : articleTags) {
                System.out.println(article.child(0).text());
            }
        }


    }

    private static Document httpGetAndParseHtml(String link) throws IOException {

        CloseableHttpClient httpclient = HttpClients.createDefault();
        if (link.startsWith("://")) {
            link = "https" + link;
        }
        HttpGet httpGet = new HttpGet(link);
        httpGet.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.88 Safari/537.36");
        try (CloseableHttpResponse response1 = httpclient.execute(httpGet)) {
            System.out.println(response1.getStatusLine());
            HttpEntity entity1 = response1.getEntity();
            String html = EntityUtils.toString(entity1);
            return Jsoup.parse(html);
        }

    }

    private static boolean isInterestedLink(String link) {
        return isNewsPage(link) || isIndexPage(link) && isNotLoginPage(link);

    }

    private static boolean isNotLoginPage(String link) {
        return !link.contains("passport.sina.cn");
    }

    private static boolean isNewsPage(String link) {
        return link.contains("news.sina.cn");
    }

    private static boolean isIndexPage(String link) {
        return "https://sina.cn".equals(link);
    }

}

