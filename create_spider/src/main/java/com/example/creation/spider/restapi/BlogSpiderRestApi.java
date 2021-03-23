package com.example.creation.spider.restapi;


import com.example.creation.spider.pipeline.BlogPipeline;
import com.example.creation.spider.processer.BlogProcesser;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.scheduler.QueueScheduler;

import javax.annotation.Resource;

/**
 * 文章爬取RestApi
 *
 */
@RestController
@RequestMapping("/spider")
@Api(value = "文章爬取RestApi", tags = {"文章爬取相关接口"})
@Slf4j
public class BlogSpiderRestApi {

    @Resource
    private BlogProcesser blogProcesser;

    @Resource
    private BlogPipeline blogPipeline;

    private Spider spider;

    /**
     * 爬取csdn文章
     *
     * @return
     */
    @ApiOperation(value = "startSpiderCsdn", notes = "startSpiderCsdn")
    @RequestMapping(value = "/startSpiderCsdn", method = RequestMethod.GET)
    public String startSpiderCsdn() {

        if (spider != null) {
            spider.run();
            return "启动爬取";
        }
        //开启蜘蛛爬取内容
        spider = Spider.create(blogProcesser)
                .addUrl("https://www.csdn.net/")
                .addPipeline(blogPipeline)
                .setScheduler(new QueueScheduler())
                .thread(10);

        spider.start();

        return "开始爬取";
    }

    /**
     * 爬取csdn文章
     *
     * @return
     */
    @ApiOperation(value = "stopSpiderCsdn", notes = "stopSpiderCsdn")
    @RequestMapping(value = "/stopSpiderCsdn", method = RequestMethod.GET)
    public String stopSpiderCsdn() {

        //关闭蜘蛛爬取内容
        spider.stop();

        return "关闭爬虫";
    }
}

