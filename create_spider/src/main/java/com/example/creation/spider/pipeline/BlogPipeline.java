package com.example.creation.spider.pipeline;

import com.example.creation.spider.entity.BlogSpider;
import com.example.creation.spider.mapper.BlogSpiderMapper;
import com.example.creation.spider.util.IdWorker;
import com.example.creation.base.enums.EPublish;
import com.example.creation.base.enums.EStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import javax.annotation.Resource;

/**
 * 文章传输管道
 *
 */
@Component
public class BlogPipeline implements Pipeline {


    private final String SAVE_PATH = "C:/Users/King/Desktop/tensquare/webmgicFile/piantuUrl/youxi/";
    @Resource
    private IdWorker idWorker;
    @Resource
    private BlogSpiderMapper blogDao;

    @Override
    public void process(ResultItems res, Task task) {
        //获取title和content
        String title = res.get("title");
        String content = res.get("content");
        System.out.println("title: " + title);
        System.out.println("content: " + content);
        if (!StringUtils.isEmpty(title) && !StringUtils.isEmpty(content)) {

            try {
                BlogSpider blog = new BlogSpider();
                blog.setUid(idWorker.nextId() + "");
                blog.setTitle(title);
                blog.setSummary(title);
                blog.setContent(content);
                blog.setTagUid("5c4c541e600ff422ccb371ee788f59d6");
                blog.setClickCount(0);
                blog.setCollectCount(0);
                blog.setStatus(EStatus.ENABLE);
                blog.setAdminUid("1f01cd1d2f474743b241d74008b12333");
                blog.setAuthor("xmy");
                blog.setArticlesPart("创作平台");
                blog.setBlogSortUid("6a1c7a50c0e7b8e8657949bf02d5d0ca");
                blog.setLevel(0);
                blog.setIsPublish(EPublish.PUBLISH);
                blog.setSort(0);
                blog.insert();

                //下载到本地
                //DownloadUtil.download("http://pic.netbian.com"+fileUrl,fileName,SAVE_PATH);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }


}
