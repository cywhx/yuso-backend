package com.yupi.springbootinit.job.once;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.yupi.springbootinit.esdao.PostEsDao;
import com.yupi.springbootinit.model.dto.post.PostEsDTO;
import com.yupi.springbootinit.model.entity.Post;
import com.yupi.springbootinit.service.PostService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.elasticsearch.Assertions;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 获取初始化的帖子
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
// 取消注释后，每次启动 springboot 项目时会执行一次run方法
@Component
@Slf4j
public class FetchlnitPostList implements CommandLineRunner {

    @Resource
    private PostService postService;



    @Override
    public void run(String... args) {
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
        for (Object record : records) {
            JSONObject tempRecord = (JSONObject) record;
            Post post = new Post();
            //标题
            post.setTitle(tempRecord.getStr("title"));
            //内容
            post.setContent(tempRecord.getStr("content"));
            // 标签
            JSONArray tags = (JSONArray) tempRecord.get("tags");
            List<String> tagList = tags.toList(String.class);
            post.setTags(JSONUtil.toJsonStr(tagList));
            // 用户id
            post.setUserId(1L);
            postLIst.add(post);

        }
        //3.数据入库
        boolean b = postService.saveBatch(postLIst);
       if (b){
           log.info("获取初始化帖子成功，获取到条数+{}",postLIst.size());
       }else {
           log.info("获取初始化帖子失败");
       }

    }
}
