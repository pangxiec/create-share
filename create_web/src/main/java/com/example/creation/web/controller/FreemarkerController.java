package com.example.creation.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.creation.commons.entity.Article;
import com.example.creation.commons.entity.ArticleSort;
import com.example.creation.commons.entity.SystemConfig;
import com.example.creation.commons.entity.Tag;
import com.example.creation.commons.feign.PictureFeignClient;
import com.example.creation.utils.ResultUtil;
import com.example.creation.utils.StringUtils;
import com.example.creation.web.global.SQLConf;
import com.example.creation.web.global.SysConf;
import com.example.creation.xo.service.*;
import com.example.creation.xo.utils.WebUtil;
import com.example.creation.base.enums.ELevel;
import com.example.creation.base.enums.EPublish;
import com.example.creation.base.enums.EStatus;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * FreemarkController
 *
 */
@RequestMapping("freemarker")
@RefreshScope
@Controller
public class FreemarkerController {

    @Resource
    WebUtil webUtil;

    @Resource
    WebConfigService webConfigService;

    @Resource
    SystemConfigService systemConfigService;

    @Resource
    ArticleService articleService;

    @Resource
    TagService tagService;

    @Resource
    ArticleSortService articleSortService;

    @Resource
    LinkService linkService;

    @Resource
    private PictureFeignClient pictureFeignClient;

    @Value(value = "${file.upload.path}")
    private String fileUploadPath;

    @Value(value = "${data.webSite.url}")
    private String webSiteUrl;

    @Value(value = "${data.web.url}")
    private String webUrl;

    @RequestMapping("/info/{uid}")
    public String index(Map<String, Object> map, @PathVariable("uid") String uid) {
        // fc98d2ae7756d2587390ae441b82f52d
        List<Article> sameBlog = articleService.getSameBlogByBlogUid(uid);
        sameBlog = setBlog(sameBlog);

        List<Article> thirdBlog = articleService.getBlogListByLevel(ELevel.THIRD);
        thirdBlog = setBlog(thirdBlog);

        List<Article> fourthBlog = articleService.getBlogListByLevel(ELevel.FOURTH);
        fourthBlog = setBlog(fourthBlog);

        SystemConfig systemConfig = systemConfigService.getConfig();
        if (systemConfig == null) {
            return ResultUtil.result(SysConf.ERROR, "??????????????????");
        }

        map.put("vueWebBasePath", webSiteUrl);
        map.put("webBasePath", webUrl);
        map.put("staticBasePath", systemConfig.getLocalPictureBaseUrl());
        map.put("webConfig", webConfigService.getWebConfig());
        map.put("blog", articleService.getBlogByUid(uid));
        map.put("sameBlog", sameBlog);
        map.put("thirdBlogList", thirdBlog);
        map.put("fourthBlogList", fourthBlog);
        map.put("fourthBlogList", fourthBlog);
        map.put("hotBlogList", articleService.getBlogListByTop(SysConf.FIVE));
        return "info";
    }

    /**
     * ??????????????????
     *
     * @throws IOException
     * @throws TemplateException
     */
    public void generateHtml(String uid) {
        try {
            //???????????????
            Configuration configuration = new Configuration(Configuration.getVersion());
            String classpath = this.getClass().getResource("/").getPath();
            //??????????????????
            configuration.setDirectoryForTemplateLoading(new File(classpath + "/templates/"));
            //???????????????
            configuration.setDefaultEncoding("utf-8");
            //????????????
            Template template = configuration.getTemplate("info.ftl");
            //????????????
            Map map = new HashMap();

            List<Article> sameBlog = articleService.getSameBlogByBlogUid(uid);
            sameBlog = setBlog(sameBlog);

            List<Article> thirdBlog = articleService.getBlogListByLevel(ELevel.THIRD);
            thirdBlog = setBlog(thirdBlog);

            List<Article> fourthBlog = articleService.getBlogListByLevel(ELevel.FOURTH);
            fourthBlog = setBlog(fourthBlog);

            map.put("vueWebBasePath", "http://localhost:9527/#/");
            map.put("webBasePath", "http://localhost:8603");
            map.put("staticBasePath", "http://localhost:8600");
            map.put("webConfig", webConfigService.getWebConfig());
            map.put("blog", articleService.getBlogByUid(uid));
            map.put("sameBlog", sameBlog);
            map.put("hotBlogList", articleService.getBlogListByTop(SysConf.FIVE));

            //?????????
            String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);

            InputStream inputStream = IOUtils.toInputStream(content);
            //????????????
            String savePath = fileUploadPath + "/blog/page/" + uid + ".html";
            FileOutputStream fileOutputStream = new FileOutputStream(new File(savePath));
            int copy = IOUtils.copy(inputStream, fileOutputStream);
        } catch (Exception e) {
            e.getMessage();
        }

    }

    /**
     * ?????????????????????????????????
     */
    @RequestMapping("/getAllHtml")
    @ResponseBody
    public String getAllHtml() throws IOException {
        FileOutputStream fileOutputStream = null;
        InputStream inputStream = null;
        try {
            //???????????????
            Configuration configuration = new Configuration(Configuration.getVersion());
            String classpath = this.getClass().getResource("/").getPath();
            //??????????????????
            configuration.setDirectoryForTemplateLoading(new File(classpath + "/templates/"));
            //???????????????
            configuration.setDefaultEncoding("utf-8");
            //????????????
            Template template = configuration.getTemplate("info.ftl");

            QueryWrapper<Article> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq(SQLConf.STATUS, EStatus.ENABLE);
            queryWrapper.eq(SQLConf.IS_PUBLISH, EPublish.PUBLISH);
            List<Article> blogList = articleService.list(queryWrapper);
            blogList = setBlog(blogList);
            Map<String, List<Article>> blogMap = new HashMap<>();
            List<Article> thirdBlog = new ArrayList<>();
            List<Article> fourthBlog = new ArrayList<>();
            List<Article> hotBlogList = articleService.getBlogListByTop(SysConf.FIVE);
            blogList.forEach(item -> {

                if (item.getLevel() == ELevel.THIRD) {
                    thirdBlog.add(item);
                } else if (item.getLevel() == ELevel.FOURTH) {
                    fourthBlog.add(item);
                }

                List<Article> tempList = blogMap.get(item.getArticleSortUid());
                if (tempList != null && tempList.size() > 0) {
                    tempList.add(item);
                    blogMap.put(item.getArticleSortUid(), tempList);
                } else {
                    List<Article> temp = new ArrayList<>();
                    temp.add(item);
                    blogMap.put(item.getArticleSortUid(), temp);
                }
            });

            SystemConfig systemConfig = systemConfigService.getConfig();
            if (systemConfig == null) {
                return ResultUtil.result(SysConf.ERROR, "??????????????????");
            }

            for (int a = 0; a < blogList.size(); a++) {
                //????????????
                Map map = new HashMap();
                List<Article> sameBlog = blogMap.get(blogList.get(a).getArticleSortUid());
                map.put("vueWebBasePath", webSiteUrl);
                map.put("webBasePath", webUrl);
                map.put("staticBasePath", systemConfig.getLocalPictureBaseUrl());
                map.put("webConfig", webConfigService.getWebConfig());
                map.put("blog", blogList.get(a));
                map.put("sameBlog", sameBlog);
                map.put("thirdBlogList", thirdBlog);
                map.put("fourthBlogList", fourthBlog);
                map.put("hotBlogList", hotBlogList);
                //?????????
                String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);

                inputStream = IOUtils.toInputStream(content);

                //????????????
                String savePath = fileUploadPath + "/blog/page/" + blogList.get(a).getUid() + ".html";
                fileOutputStream = new FileOutputStream(new File(savePath));
                IOUtils.copy(inputStream, fileOutputStream);
            }
            return ResultUtil.result(SysConf.SUCCESS, "????????????");
        } catch (Exception e) {
            e.getMessage();
        } finally {
            inputStream.close();
            fileOutputStream.close();
        }
        return ResultUtil.result(SysConf.SUCCESS, "????????????");
    }

    /**
     * ????????????????????????????????????
     *
     * @param list
     * @return
     */
    private List<Article> setBlog(List<Article> list) {
        final StringBuffer fileUids = new StringBuffer();
        List<String> sortUids = new ArrayList<>();
        List<String> tagUids = new ArrayList<>();

        list.forEach(item -> {
            if (StringUtils.isNotEmpty(item.getFileUid())) {
                fileUids.append(item.getFileUid() + SysConf.FILE_SEGMENTATION);
            }
            if (StringUtils.isNotEmpty(item.getArticleSortUid())) {
                sortUids.add(item.getArticleSortUid());
            }
            if (StringUtils.isNotEmpty(item.getTagUid())) {
                tagUids.add(item.getTagUid());
            }
        });
        String pictureList = null;

        if (fileUids != null) {
            pictureList = this.pictureFeignClient.getPicture(fileUids.toString(), SysConf.FILE_SEGMENTATION);
        }
        List<Map<String, Object>> picList = webUtil.getPictureMap(pictureList);
        Collection<ArticleSort> sortList = new ArrayList<>();
        Collection<Tag> tagList = new ArrayList<>();
        if (sortUids.size() > 0) {
            sortList = articleSortService.listByIds(sortUids);
        }
        if (tagUids.size() > 0) {
            tagList = tagService.listByIds(tagUids);
        }


        Map<String, ArticleSort> sortMap = new HashMap<>();
        Map<String, Tag> tagMap = new HashMap<>();
        Map<String, String> pictureMap = new HashMap<>();

        sortList.forEach(item -> {
            sortMap.put(item.getUid(), item);
        });

        tagList.forEach(item -> {
            tagMap.put(item.getUid(), item);
        });

        picList.forEach(item -> {
            pictureMap.put(item.get(SQLConf.UID).toString(), item.get(SQLConf.URL).toString());
        });


        for (Article item : list) {

            //????????????
            if (StringUtils.isNotEmpty(item.getArticleSortUid())) {
                item.setArticleSort(sortMap.get(item.getArticleSortUid()));
            }

            //????????????
            if (StringUtils.isNotEmpty(item.getTagUid())) {
                List<String> tagUidsTemp = StringUtils.changeStringToString(item.getTagUid(), SysConf.FILE_SEGMENTATION);
                List<Tag> tagListTemp = new ArrayList<Tag>();

                tagUidsTemp.forEach(tag -> {
                    tagListTemp.add(tagMap.get(tag));
                });
                item.setTagList(tagListTemp);
            }

            //????????????
            if (StringUtils.isNotEmpty(item.getFileUid())) {
                List<String> pictureUidsTemp = StringUtils.changeStringToString(item.getFileUid(), SysConf.FILE_SEGMENTATION);
                List<String> pictureListTemp = new ArrayList<>();

                pictureUidsTemp.forEach(picture -> {
                    pictureListTemp.add(pictureMap.get(picture));
                });
                item.setPhotoList(pictureListTemp);
            }
        }
        return list;
    }
}
