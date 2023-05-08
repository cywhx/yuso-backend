package com.yupi.springbootinit;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.yupi.springbootinit.model.entity.Picture;
import com.yupi.springbootinit.model.entity.Post;
import com.yupi.springbootinit.service.PostService;
import org.jsoup.Jsoup;


import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class CrawlerTest {
    @Resource
    private PostService postService;

    @Test
    void  testFetchPicture() throws IOException {
        int current = 1 ;
        String url = String.format("https://cn.bing.com/images/search?q=小黑子&form=HDRSC2&first=1&cw=1177&ch=%s",current);
        Document doc = Jsoup.connect(url).get();
        Elements elements = doc.select(".iuscp.isv");
        List<Picture> pictures = new ArrayList<>();
        for (Element element : elements){
            // 取图片地址(murl)
            String m = element.select(".iusc").get(0).attr("m");
            Map<String, Object> map = JSONUtil.toBean(m, Map.class);
            String murl = (String) map.get("murl");
            //取标签
            String title = element.select(".inflnk").get(0).attr("aria-label");

            Picture picture = new Picture();
            picture.setTitle(title);
            picture.setUrl(murl);
            pictures.add(picture);
//            System.out.println(murl);
//            System.out.println(title );
        }
    }
    @Test
    void testFetchPassage() {
        // 1. 获取数据
//        post清求json参数
        String json = "{\"sortField\":\"createTime\",\"sortOrder\":\"descend\",\"reviewStatus\":1,\"current\":1}";

//        url地址
        String url = "https://www.code-nav.cn/api/post/list/page/vo";

        String result2 = HttpRequest.post(url)
                .body(json)
                .execute()
                .body();
        System.out.println(result2);
        //2. 数据转换
        Map<String, Object> map = JSONUtil.toBean(result2, Map.class);
        JSONObject data = (JSONObject) map.get("data");
        JSONArray records = (JSONArray) data.get("records");
        List<Post> postLIst = new ArrayList<>();
        for (Object record : records ){
            JSONObject tempRecord = (JSONObject) record;
            Post post = new Post();
            //标题
            post.setTitle(tempRecord.getStr("title"));
            //内容
            post.setContent(tempRecord.getStr("content"));
            // 标签
            JSONArray tags = (JSONArray) tempRecord.get("tags");
            List<String > tagList = tags.toList(String.class);
            post.setTags(JSONUtil.toJsonStr(tagList));
            // 用户id
            post.setUserId(1L);
            postLIst.add(post);

        }
        //3.数据入库
        boolean b = postService.saveBatch(postLIst);
        Assertions.assertTrue(b);
    }
}
