package com.example.creation.xo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.creation.base.enums.*;
import com.example.creation.base.exception.exceptionType.InsertException;
import com.example.creation.base.exception.exceptionType.UpdateException;
import com.example.creation.base.global.ErrorCode;
import com.example.creation.xo.mapper.SensitiveWordMapper;
import com.example.creation.xo.utils.SensitiveWordInit;
import com.example.creation.xo.utils.SensitiveWordUtil;
import com.example.creation.commons.entity.*;
import com.example.creation.commons.feign.PictureFeignClient;
import com.example.creation.utils.*;
import com.example.creation.xo.global.MessageConf;
import com.example.creation.xo.global.RedisConf;
import com.example.creation.xo.global.SQLConf;
import com.example.creation.xo.global.SysConf;
import com.example.creation.xo.mapper.BlogMapper;
import com.example.creation.xo.mapper.BlogSortMapper;
import com.example.creation.xo.mapper.TagMapper;
import com.example.creation.xo.service.*;
import com.example.creation.xo.utils.WebUtil;
import com.example.creation.xo.vo.ArticleVO;
import com.example.creation.base.global.BaseSQLConf;
import com.example.creation.base.global.BaseSysConf;
import com.example.creation.base.global.Constants;
import com.example.creation.base.holder.RequestHolder;
import com.example.creation.base.serviceImpl.SuperServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author xmy
 * @date 2021/3/15 14:31
 */
@Service
@Slf4j
public class ArticleServiceImpl extends SuperServiceImpl<BlogMapper, Article> implements ArticleService {

    @Resource
    private WebUtil webUtil;

    @Resource
    private CommentService commentService;

    @Resource
    private WebVisitService webVisitService;

    @Resource
    private TagService tagService;

    @Resource
    private PictureService pictureService;

    @Resource
    private ArticleSortService articleSortService;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private TagMapper tagMapper;

    @Resource
    private BlogSortMapper blogSortMapper;

    @Resource
    private BlogMapper blogMapper;

    @Resource
    private AdminService adminService;

    @Resource
    private SystemConfigService systemConfigService;

    @Resource
    private SysParamsService sysParamsService;

    @Resource
    private ArticleService articleService;

    @Resource
    private SubjectItemService subjectItemService;

    @Resource
    private PictureFeignClient pictureFeignClient;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private SensitiveWordMapper sensitiveWordMapper;

    @Override
    public List<Article> setTagByArticleList(List<Article> list) {
        for (Article item : list) {
            if (item != null) {
                setTagByArticle(item);
            }
        }
        return list;
    }

    @Override
    public List<Article> setTagAndSortByArticleList(List<Article> list) {
        List<String> sortUids = new ArrayList<>();
        List<String> tagUids = new ArrayList<>();
        list.forEach(item -> {
            if (StringUtils.isNotEmpty(item.getArticleSortUid())) {
                sortUids.add(item.getArticleSortUid());
            }
            if (StringUtils.isNotEmpty(item.getTagUid())) {
                List<String> tagUidsTemp = StringUtils.changeStringToString(item.getTagUid(), BaseSysConf.FILE_SEGMENTATION);
                for (String itemTagUid : tagUidsTemp) {
                    tagUids.add(itemTagUid);
                }
            }
        });
        Collection<ArticleSort> sortList = new ArrayList<>();
        Collection<Tag> tagList = new ArrayList<>();
        if (sortUids.size() > 0) {
            sortList = blogSortMapper.selectBatchIds(sortUids);
        }
        if (tagUids.size() > 0) {
            tagList = tagMapper.selectBatchIds(tagUids);
        }
        Map<String, ArticleSort> sortMap = new HashMap<>();
        Map<String, Tag> tagMap = new HashMap<>();
        sortList.forEach(item -> {
            sortMap.put(item.getUid(), item);
        });
        tagList.forEach(item -> {
            tagMap.put(item.getUid(), item);
        });
        for (Article item : list) {

            //设置分类
            if (StringUtils.isNotEmpty(item.getArticleSortUid())) {
                item.setArticleSort(sortMap.get(item.getArticleSortUid()));
            }
            //获取标签
            if (StringUtils.isNotEmpty(item.getTagUid())) {
                List<String> tagUidsTemp = StringUtils.changeStringToString(item.getTagUid(), BaseSysConf.FILE_SEGMENTATION);
                List<Tag> tagListTemp = new ArrayList<Tag>();
                tagUidsTemp.forEach(tag -> {
                    tagListTemp.add(tagMap.get(tag));
                });
                item.setTagList(tagListTemp);
            }
        }

        return list;
    }

    @Override
    public List<Article> setTagAndSortAndPictureByBlogList(List<Article> list) {

        List<String> sortUids = new ArrayList<>();
        List<String> tagUids = new ArrayList<>();
        Set<String> fileUidSet = new HashSet<>();

        list.forEach(item -> {
            if (StringUtils.isNotEmpty(item.getFileUid())) {
                fileUidSet.add(item.getFileUid());
            }
            if (StringUtils.isNotEmpty(item.getArticleSortUid())) {
                sortUids.add(item.getArticleSortUid());
            }
            if (StringUtils.isNotEmpty(item.getTagUid())) {
                // tagUid有多个，还需要切分
                if (StringUtils.isNotEmpty(item.getTagUid())) {
                    List<String> tagUidsTemp = StringUtils.changeStringToString(item.getTagUid(), BaseSysConf.FILE_SEGMENTATION);
                    for (String itemTagUid : tagUidsTemp) {
                        tagUids.add(itemTagUid);
                    }
                }
            }
        });

        String pictureList = null;
        StringBuffer fileUids = new StringBuffer();
        List<Map<String, Object>> picList = new ArrayList<>();
        // feign分页查询图片数据
        if(fileUidSet.size() > 0) {
            int count = 1;
            for(String fileUid: fileUidSet) {
                fileUids.append(fileUid + ",");
                System.out.println(count%10);
                if(count%10 == 0) {
                    pictureList = this.pictureFeignClient.getPicture(fileUids.toString(), ",");
                    List<Map<String, Object>> tempPicList = webUtil.getPictureMap(pictureList);
                    picList.addAll(tempPicList);
                    fileUids = new StringBuffer();
                }
                count ++;
            }
            // 判断是否存在图片需要获取
            if(fileUids.length() >= Constants.NUM_32) {
                pictureList = this.pictureFeignClient.getPicture(fileUids.toString(), Constants.SYMBOL_COMMA);
                List<Map<String, Object>> tempPicList = webUtil.getPictureMap(pictureList);
                picList.addAll(tempPicList);
            }
        }

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
            pictureMap.put(item.get(SysConf.UID).toString(), item.get(SysConf.URL).toString());
        });

        for (Article item : list) {
            //设置分类
            if (StringUtils.isNotEmpty(item.getArticleSortUid())) {

                item.setArticleSort(sortMap.get(item.getArticleSortUid()));
                if (sortMap.get(item.getArticleSortUid()) != null) {
                    item.setBlogSortName(sortMap.get(item.getArticleSortUid()).getSortName());
                }
            }

            //获取标签
            if (StringUtils.isNotEmpty(item.getTagUid())) {
                List<String> tagUidsTemp = StringUtils.changeStringToString(item.getTagUid(), ",");
                List<Tag> tagListTemp = new ArrayList<Tag>();

                tagUidsTemp.forEach(tag -> {
                    tagListTemp.add(tagMap.get(tag));
                });
                item.setTagList(tagListTemp);
            }

            //获取图片
            if (StringUtils.isNotEmpty(item.getFileUid())) {
                List<String> pictureUidsTemp = StringUtils.changeStringToString(item.getFileUid(), Constants.SYMBOL_COMMA);
                List<String> pictureListTemp = new ArrayList<String>();

                pictureUidsTemp.forEach(picture -> {
                    pictureListTemp.add(pictureMap.get(picture));
                });
                item.setPhotoList(pictureListTemp);
                // 只设置一张标题图
                if (pictureListTemp.size() > 0) {
                    item.setPhotoUrl(pictureListTemp.get(0));
                } else {
                    item.setPhotoUrl("");
                }
            }
        }
        return list;
    }

    @Override
    public Article setTagByArticle(Article article) {
        String tagUid = article.getTagUid();
        if (!StringUtils.isEmpty(tagUid)) {
            String[] uids = tagUid.split(SysConf.FILE_SEGMENTATION);
            List<Tag> tagList = new ArrayList<>();
            for (String uid : uids) {
                Tag tag = tagMapper.selectById(uid);
                if (tag != null && tag.getStatus() != EStatus.DISABLED) {
                    tagList.add(tag);
                }
            }
            article.setTagList(tagList);
        }
        return article;
    }

    @Override
    public Article setSortByArticle(Article article) {
        if (article != null && !StringUtils.isEmpty(article.getArticleSortUid())) {
            ArticleSort articleSort = blogSortMapper.selectById(article.getArticleSortUid());
            article.setArticleSort(articleSort);
        }
        return article;
    }

    @Override
    public List<Article> getBlogListByLevel(Integer level) {
        QueryWrapper<Article> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(BaseSQLConf.LEVEL, level);
        queryWrapper.eq(BaseSQLConf.IS_PUBLISH, EPublish.PUBLISH);

        List<Article> list = blogMapper.selectList(queryWrapper);
        return list;
    }

    @Override
    public IPage<Article> getBlogPageByLevel(Page<Article> page, Integer level, Integer useSort) {
        QueryWrapper<Article> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(BaseSQLConf.LEVEL, level);
        queryWrapper.eq(BaseSQLConf.STATUS, EStatus.ENABLE);
        queryWrapper.eq(BaseSQLConf.IS_PUBLISH, EPublish.PUBLISH);

        if (useSort == 0) {
            queryWrapper.orderByDesc(BaseSQLConf.CREATE_TIME);
        } else {
            queryWrapper.orderByDesc(BaseSQLConf.SORT);
        }

        //因为首页并不需要显示内容，所以需要排除掉内容字段
        queryWrapper.select(Article.class, i -> !i.getProperty().equals(SysConf.CONTENT));

        return blogMapper.selectPage(page, queryWrapper);
    }

    @Override
    public Integer getArticleCount(Integer status) {
        return blogMapper.selectCount(new QueryWrapper<Article>()
                                          .eq(BaseSQLConf.STATUS, EStatus.ENABLE)
                                          .eq(BaseSQLConf.IS_PUBLISH, EPublish.PUBLISH));
    }

    @Override
    public List<Map<String, Object>> getArticleCountByTag() {
        // 从Redis中获取标签下包含的文章数量
        String jsonArrayList = redisUtil.get(RedisConf.DASHBOARD + Constants.SYMBOL_COLON + RedisConf.BLOG_COUNT_BY_TAG);
        if (StringUtils.isNotEmpty(jsonArrayList)) {
            ArrayList jsonList = JsonUtils.jsonArrayToArrayList(jsonArrayList);
            return jsonList;
        }

        List<Map<String, Object>> blogCoutByTagMap = blogMapper.getBlogCountByTag();
        Map<String, Integer> tagMap = new HashMap<>();
        for (Map<String, Object> item : blogCoutByTagMap) {
            String tagUid = String.valueOf(item.get(SQLConf.TAG_UID));
            // java.lang.Number是Integer,Long的父类
            Number num = (Number) item.get(SysConf.COUNT);
            Integer count = num.intValue();
            //如果只有一个UID的情况
            if (tagUid.length() == 32) {
                //如果没有这个内容的话，就设置
                if (tagMap.get(tagUid) == null) {
                    tagMap.put(tagUid, count);
                } else {
                    Integer tempCount = tagMap.get(tagUid) + count;
                    tagMap.put(tagUid, tempCount);
                }
            } else {
                //如果长度大于32，说明含有多个UID
                if (StringUtils.isNotEmpty(tagUid)) {
                    List<String> strList = StringUtils.changeStringToString(tagUid, ",");
                    for (String strItem : strList) {
                        if (tagMap.get(strItem) == null) {
                            tagMap.put(strItem, count);
                        } else {
                            Integer tempCount = tagMap.get(strItem) + count;
                            tagMap.put(strItem, tempCount);
                        }
                    }
                }
            }
        }

        //把查询到的Tag放到Map中
        Set<String> tagUids = tagMap.keySet();
        Collection<Tag> tagCollection = new ArrayList<>();
        if (tagUids.size() > 0) {
            tagCollection = tagMapper.selectBatchIds(tagUids);
        }

        Map<String, String> tagEntityMap = new HashMap<>();
        for (Tag tag : tagCollection) {
            if (StringUtils.isNotEmpty(tag.getContent())) {
                tagEntityMap.put(tag.getUid(), tag.getContent());
            }
        }

        List<Map<String, Object>> resultList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : tagMap.entrySet()) {
            String tagUid = entry.getKey();
            if (tagEntityMap.get(tagUid) != null) {
                String tagName = tagEntityMap.get(tagUid);
                Integer count = entry.getValue();
                Map<String, Object> itemResultMap = new HashMap<>();
                itemResultMap.put(SysConf.TAG_UID, tagUid);
                itemResultMap.put(SysConf.NAME, tagName);
                itemResultMap.put(SysConf.VALUE, count);
                resultList.add(itemResultMap);
            }
        }
        // 将 每个标签下文章数目 存入到Redis【过期时间2小时】
        if (resultList.size() > 0) {
            redisUtil.setEx(RedisConf.DASHBOARD + Constants.SYMBOL_COLON + RedisConf.BLOG_COUNT_BY_TAG, JsonUtils.objectToJson(resultList), 2, TimeUnit.HOURS);
        }
        return resultList;
    }

    @Override
    public List<Map<String, Object>> getArticleCountByBlogSort() {
        // 从Redis中获取文章分类下包含的文章数量
        String jsonArrayList = redisUtil.get(RedisConf.DASHBOARD + Constants.SYMBOL_COLON + RedisConf.BLOG_COUNT_BY_SORT);
        if (StringUtils.isNotEmpty(jsonArrayList)) {
            ArrayList jsonList = JsonUtils.jsonArrayToArrayList(jsonArrayList);
            return jsonList;
        }
        List<Map<String, Object>> blogCoutByBlogSortMap = blogMapper.getBlogCountByBlogSort();
        Map<String, Integer> blogSortMap = new HashMap<>();
        for (Map<String, Object> item : blogCoutByBlogSortMap) {

            String blogSortUid = String.valueOf(item.get(SQLConf.BLOG_SORT_UID));
            // java.lang.Number是Integer,Long的父类
            Number num = (Number) item.get(SysConf.COUNT);
            Integer count = 0;
            if (num != null) {
                count = num.intValue();
            }
            blogSortMap.put(blogSortUid, count);
        }

        //把查询到的BlogSort放到Map中
        Set<String> blogSortUids = blogSortMap.keySet();
        Collection<ArticleSort> articleSortCollection = new ArrayList<>();

        if (blogSortUids.size() > 0) {
            articleSortCollection = blogSortMapper.selectBatchIds(blogSortUids);
        }

        Map<String, String> blogSortEntityMap = new HashMap<>();
        for (ArticleSort articleSort : articleSortCollection) {
            if (StringUtils.isNotEmpty(articleSort.getSortName())) {
                blogSortEntityMap.put(articleSort.getUid(), articleSort.getSortName());
            }
        }

        List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
        for (Map.Entry<String, Integer> entry : blogSortMap.entrySet()) {

            String blogSortUid = entry.getKey();

            if (blogSortEntityMap.get(blogSortUid) != null) {
                String blogSortName = blogSortEntityMap.get(blogSortUid);
                Integer count = entry.getValue();
                Map<String, Object> itemResultMap = new HashMap<>();
                itemResultMap.put(SysConf.BLOG_SORT_UID, blogSortUid);
                itemResultMap.put(SysConf.NAME, blogSortName);
                itemResultMap.put(SysConf.VALUE, count);
                resultList.add(itemResultMap);
            }
        }
        // 将 每个分类下文章数目 存入到Redis【过期时间2小时】
        if (resultList.size() > 0) {
            redisUtil.setEx(RedisConf.DASHBOARD + Constants.SYMBOL_COLON + RedisConf.BLOG_COUNT_BY_SORT, JsonUtils.objectToJson(resultList), 2, TimeUnit.HOURS);
        }
        return resultList;
    }

    @Override
    public Map<String, Object> getArticleContributeCount() {
        // 从Redis中获取文章分类下包含的文章数量
        String jsonMap = redisUtil.get(RedisConf.DASHBOARD + Constants.SYMBOL_COLON + RedisConf.BLOG_CONTRIBUTE_COUNT);
        if (StringUtils.isNotEmpty(jsonMap)) {
            Map<String, Object> resultMap = JsonUtils.jsonToMap(jsonMap);
            return resultMap;
        }
        // 获取今天结束时间
        String endTime = DateUtils.getNowTime();
        // 获取365天前的日期
        Date temp = DateUtils.getDate(endTime, -365);
        String startTime = DateUtils.dateTimeToStr(temp);
        List<Map<String, Object>> blogContributeMap = blogMapper.getBlogContributeCount(startTime, endTime);
        List<String> dateList = DateUtils.getDayBetweenDates(startTime, endTime);
        Map<String, Object> dateMap = new HashMap<>();
        for (Map<String, Object> itemMap : blogContributeMap) {
            dateMap.put(itemMap.get("DATE").toString(), itemMap.get("COUNT"));
        }
        List<List<Object>> resultList = new ArrayList<>();
        for (String item : dateList) {
            Integer count = 0;
            if (dateMap.get(item) != null) {
                count = Integer.valueOf(dateMap.get(item).toString());
            }
            List<Object> objectList = new ArrayList<>();
            objectList.add(item);
            objectList.add(count);
            resultList.add(objectList);
        }
        Map<String, Object> resultMap = new HashMap<>(Constants.NUM_TWO);
        List<String> contributeDateList = new ArrayList<>();
        contributeDateList.add(startTime);
        contributeDateList.add(endTime);
        resultMap.put(SysConf.CONTRIBUTE_DATE, contributeDateList);
        resultMap.put(SysConf.BLOG_CONTRIBUTE_COUNT, resultList);
        // 将 全年文章贡献度 存入到Redis【过期时间2小时】
        redisUtil.setEx(RedisConf.DASHBOARD + Constants.SYMBOL_COLON + RedisConf.BLOG_CONTRIBUTE_COUNT, JsonUtils.objectToJson(resultMap), 2, TimeUnit.HOURS);
        return resultMap;
    }

    @Override
    public Article getBlogByUid(String uid) {
        Article blog = blogMapper.selectById(uid);
        if (blog != null && blog.getStatus() != EStatus.DISABLED) {
            blog = setTagByArticle(blog);
            blog = setSortByArticle(blog);
            return blog;
        }
        return null;
    }

    @Override
    public List<Article> getSameBlogByBlogUid(String blogUid) {
        Article blog = articleService.getById(blogUid);
        QueryWrapper<Article> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(SQLConf.STATUS, EStatus.ENABLE);
        Page<Article> page = new Page<>();
        page.setCurrent(1);
        page.setSize(10);
        // 通过分类来获取相关文章
        String blogSortUid = blog.getArticleSortUid();
        queryWrapper.eq(SQLConf.BLOG_SORT_UID, blogSortUid);
        queryWrapper.eq(SQLConf.IS_PUBLISH, EPublish.PUBLISH);
        queryWrapper.orderByDesc(SQLConf.CREATE_TIME);
        IPage<Article> pageList = articleService.page(page, queryWrapper);
        List<Article> list = pageList.getRecords();
        list = articleService.setTagAndSortByArticleList(list);

        //过滤掉当前的文章
        List<Article> newList = new ArrayList<>();
        for (Article item : list) {
            if (item.getUid().equals(blogUid)) {
                continue;
            }
            newList.add(item);
        }
        return newList;
    }

    @Override
    public List<Article> getBlogListByTop(Integer top) {
        QueryWrapper<Article> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(SQLConf.STATUS, EStatus.ENABLE);
        Page<Article> page = new Page<>();
        page.setCurrent(1);
        page.setSize(top);
        queryWrapper.eq(SQLConf.IS_PUBLISH, EPublish.PUBLISH);
        queryWrapper.orderByDesc(SQLConf.SORT);
        IPage<Article> pageList = articleService.page(page, queryWrapper);
        List<Article> list = pageList.getRecords();
        list = articleService.setTagAndSortAndPictureByBlogList(list);
        return list;
    }

    @Override
    public IPage<Article> getPageList(ArticleVO articleVO) {
        QueryWrapper<Article> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotEmpty(articleVO.getKeyword()) && !StringUtils.isEmpty(articleVO.getKeyword().trim())) {
            queryWrapper.like(SQLConf.TITLE, articleVO.getKeyword().trim());
        }
        if (!StringUtils.isEmpty(articleVO.getTagUid())) {
            queryWrapper.like(SQLConf.TAG_UID, articleVO.getTagUid());
        }
        if (!StringUtils.isEmpty(articleVO.getArticleSortUid())) {
            queryWrapper.like(SQLConf.BLOG_SORT_UID, articleVO.getArticleSortUid());
        }
        if (!StringUtils.isEmpty(articleVO.getLevelKeyword())) {
            queryWrapper.eq(SQLConf.LEVEL, articleVO.getLevelKeyword());
        }
        if (!StringUtils.isEmpty(articleVO.getIsPublish())) {
            queryWrapper.eq(SQLConf.IS_PUBLISH, articleVO.getIsPublish());
        }
        if (!StringUtils.isEmpty(articleVO.getIsOriginal())) {
            queryWrapper.eq(SQLConf.IS_ORIGINAL, articleVO.getIsOriginal());
        }
        if(!StringUtils.isEmpty(articleVO.getType())) {
            queryWrapper.eq(SQLConf.TYPE, articleVO.getType());
        }
        Page<Article> articlePage = new Page<>(articleVO.getCurrentPage(), articleVO.getPageSize());
        queryWrapper.eq(SQLConf.STATUS, EStatus.ENABLE);
        if(StringUtils.isNotEmpty(articleVO.getOrderByAscColumn())) {
            String column = StringUtils.underLine(new StringBuffer(articleVO.getOrderByAscColumn())).toString();
            queryWrapper.orderByAsc(column);
        }else if(StringUtils.isNotEmpty(articleVO.getOrderByDescColumn())) {
            String column = StringUtils.underLine(new StringBuffer(articleVO.getOrderByDescColumn())).toString();
            queryWrapper.orderByDesc(column);
        } else {
            if (articleVO.getUseSort() == 0) {
                queryWrapper.orderByDesc(SQLConf.CREATE_TIME);
            } else {
                queryWrapper.orderByDesc(SQLConf.SORT);
            }
        }
        IPage<Article> pageList = articleService.page(articlePage, queryWrapper);
        List<Article> list = pageList.getRecords();
        if (list.size() == 0) {
            return pageList;
        }
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
                List<String> tagUidsTemp = StringUtils.changeStringToString(item.getTagUid(), SysConf.FILE_SEGMENTATION);
                for (String itemTagUid : tagUidsTemp) {
                    tagUids.add(itemTagUid);
                }
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

//        picList.forEach(item -> {
//            //pictureMap.put(item.get(SQLConf.UID).toString(), item.get(SQLConf.URL).toString());
//            pictureMap.put(item.get(SQLConf.UID).toString(), item.get(SQLConf.URL).toString());
//        });

        for (Article item : list) {
            if (StringUtils.isNotEmpty(item.getArticleSortUid())) {
                item.setArticleSort(sortMap.get(item.getArticleSortUid()));
            }
            if (StringUtils.isNotEmpty(item.getTagUid())) {
                List<String> tagUidsTemp = StringUtils.changeStringToString(item.getTagUid(), SysConf.FILE_SEGMENTATION);
                List<Tag> tagListTemp = new ArrayList<>();
                tagUidsTemp.forEach(tag -> {
                    tagListTemp.add(tagMap.get(tag));
                });
                item.setTagList(tagListTemp);
            }
            if (StringUtils.isNotEmpty(item.getFileUid())) {
                List<String> pictureUidsTemp = StringUtils.changeStringToString(item.getFileUid(), SysConf.FILE_SEGMENTATION);
                List<String> pictureListTemp = new ArrayList<>();
                pictureUidsTemp.forEach(picture -> {
                    pictureListTemp.add(pictureMap.get(picture));
                });
                item.setPhotoList(pictureListTemp);
            }
        }
        pageList.setRecords(list);
        return pageList;
    }

    @Override
    public String addArticle(ArticleVO articleVO) {
        HttpServletRequest request = RequestHolder.getRequest();
        Integer count = articleService.count(new QueryWrapper<Article>()
                                                .eq(SQLConf.LEVEL, articleVO.getLevel())
                                                .eq(SQLConf.STATUS, EStatus.ENABLE));
        Set<String> set = checkWords(articleVO.getTitle(),articleVO.getContent(),articleVO.getSummary());
        if (set != null){
            throw new InsertException(ErrorCode.INSERT_DEFAULT_ERROR, "创作失败 文章中含有敏感词" + set);
        }
        // 判断插入文章的时候，会不会超过预期设置(推荐等级有限制)
        String addVerdictResult = addVerdict(count + 1, articleVO.getLevel());
        if (StringUtils.isNotBlank(addVerdictResult)) {
            return addVerdictResult;
        }
        Article article = new Article();
        String projectName = sysParamsService.getSysParamsValueByKey(SysConf.PROJECT_NAME_);
        if (Original.ORIGINAL.equals(articleVO.getIsOriginal())) {
            Admin admin = adminService.getById(request.getAttribute(SysConf.ADMIN_UID).toString());
            if (admin != null) {
                if(StringUtils.isNotEmpty(admin.getNickName())) {
                    article.setAuthor(admin.getNickName());
                } else {
                    article.setAuthor(admin.getUserName());
                }
                article.setAdminUid(admin.getUid());
            }
            article.setArticlesPart(projectName);
        } else {
            article.setAuthor(articleVO.getAuthor());
            article.setArticlesPart(articleVO.getArticlesPart());
        }
        article.setTitle(articleVO.getTitle());
        article.setSummary(articleVO.getSummary());
        article.setContent(articleVO.getContent());
        article.setTagUid(articleVO.getTagUid());
        article.setArticleSortUid(articleVO.getArticleSortUid());
        article.setFileUid(articleVO.getFileUid());
        article.setLevel(articleVO.getLevel());
        article.setIsOriginal(articleVO.getIsOriginal());
        article.setIsPublish(articleVO.getIsPublish());
        article.setType(articleVO.getType());
        article.setOutsideLink(articleVO.getOutsideLink());
        article.setStatus(EStatus.ENABLE);
        article.setOpenComment(articleVO.getOpenComment());
        Boolean isSave = articleService.save(article);

        //保存成功后，需要发送消息到solr 和 redis
        updateSolrAndRedis(isSave, article);
        return ResultUtil.successWithMessage(MessageConf.INSERT_SUCCESS);
    }

    @Override
    public String editArticle(ArticleVO articleVO) {
        HttpServletRequest request = RequestHolder.getRequest();
        Article article = articleService.getById(articleVO.getUid());;
        Integer count = articleService.count(new QueryWrapper<Article>()
                                            .eq(SQLConf.LEVEL, articleVO.getLevel())
                                            .eq(SQLConf.STATUS, EStatus.ENABLE));

        Set<String> set = checkWords(articleVO.getTitle(),articleVO.getContent(),articleVO.getSummary());
        if (set != null){
            throw new UpdateException(ErrorCode.UPDATE_DEFAULT_ERROR, "修改失败 文章中含有敏感词"+ set);
        }
        if (article != null) {
            //传递过来的和数据库中的不同，代表用户已经修改过等级了，那么需要将count数加1
            if (!article.getLevel().equals(articleVO.getLevel())) {
                count += 1;
            }
        }
        String addVerdictResult = addVerdict(count, articleVO.getLevel());
        if (StringUtils.isNotBlank(addVerdictResult)) {
            return addVerdictResult;
        }
        Admin admin = adminService.getById(request.getAttribute(SysConf.ADMIN_UID).toString());
        article.setAdminUid(admin.getUid());
        if (Original.ORIGINAL.equals(articleVO.getIsOriginal())) {
            if(StringUtils.isNotEmpty(admin.getNickName())) {
                article.setAuthor(admin.getNickName());
            } else {
                article.setAuthor(admin.getUserName());
            }
            String projectName = sysParamsService.getSysParamsValueByKey(SysConf.PROJECT_NAME_);
            article.setArticlesPart(projectName);
        } else {
            article.setAuthor(articleVO.getAuthor());
            article.setArticlesPart(articleVO.getArticlesPart());
        }

        article.setTitle(articleVO.getTitle());
        article.setSummary(articleVO.getSummary());
        article.setContent(articleVO.getContent());
        article.setTagUid(articleVO.getTagUid());
        article.setArticleSortUid(articleVO.getArticleSortUid());
        article.setFileUid(articleVO.getFileUid());
        article.setLevel(articleVO.getLevel());
        article.setIsOriginal(articleVO.getIsOriginal());
        article.setIsPublish(articleVO.getIsPublish());
        article.setOpenComment(articleVO.getOpenComment());
        article.setUpdateTime(new Date());
        article.setType(articleVO.getType());
        article.setOutsideLink(articleVO.getOutsideLink());
        article.setStatus(EStatus.ENABLE);

        Boolean isSave = article.updateById();
        //保存成功后，需要发送消息到solr 和 redis
        updateSolrAndRedis(isSave, article);
        return ResultUtil.successWithMessage(MessageConf.UPDATE_SUCCESS);
    }

    /**
     * 敏感词检测
     *
     * @param title 标题
     * @param content 内容
     * @param summary 简介
     * @return
     */
    public Set<String> checkWords(String title,String content,String summary){
        List<String> articles = new ArrayList<>();
        articles.add(title);
        articles.add(content);
        articles.add(summary);
        SensitiveWordInit sensitiveWordInit = new SensitiveWordInit();
        List<SensitiveWord> sensitiveWords = sensitiveWordMapper.selectList(new QueryWrapper<SensitiveWord>().select("word"));
        Map sensitiveWordMap = sensitiveWordInit.initKeyWord(sensitiveWords);
        SensitiveWordUtil.sensitiveWordMap = sensitiveWordMap;
        Set<String> set = SensitiveWordUtil.getSensitiveWord(String.join(",", articles), 2);
        if (set == null || set.size() == 0){
            return null;
        }else {
            return set;
        }
    }

    @Override
    public String editBatch(List<ArticleVO> articleVOList) {
        if (articleVOList.size() <= 0) {
            return ResultUtil.errorWithMessage(MessageConf.PARAM_INCORRECT);
        }
        List<String> blogUidList = new ArrayList<>();
        Map<String, ArticleVO> blogVOMap = new HashMap<>();
        articleVOList.forEach(item -> {
            blogUidList.add(item.getUid());
            blogVOMap.put(item.getUid(), item);
        });

        Collection<Article> blogList = articleService.listByIds(blogUidList);
        blogList.forEach(blog -> {
            ArticleVO articleVO = blogVOMap.get(blog.getUid());
            if (articleVO != null) {
                blog.setAuthor(articleVO.getAuthor());
                blog.setArticlesPart(articleVO.getArticlesPart());
                blog.setTitle(articleVO.getTitle());
                blog.setSummary(articleVO.getSummary());
                blog.setContent(articleVO.getContent());
                blog.setTagUid(articleVO.getTagUid());
                blog.setArticleSortUid(articleVO.getArticleSortUid());
                blog.setFileUid(articleVO.getFileUid());
                blog.setLevel(articleVO.getLevel());
                blog.setIsOriginal(articleVO.getIsOriginal());
                blog.setIsPublish(articleVO.getIsPublish());
                blog.setSort(articleVO.getSort());
                blog.setType(articleVO.getType());
                blog.setOutsideLink(articleVO.getOutsideLink());
                blog.setStatus(EStatus.ENABLE);
            }
        });
        Boolean save = articleService.updateBatchById(blogList);

        //保存成功后，需要发送消息到solr 和 redis
        if (save) {
            Map<String, Object> map = new HashMap<>();
            map.put(SysConf.COMMAND, SysConf.EDIT_BATCH);
            //发送到RabbitMq
            rabbitTemplate.convertAndSend(SysConf.EXCHANGE_DIRECT, SysConf.MOGU_BLOG, map);
        }

        return ResultUtil.successWithMessage(MessageConf.UPDATE_SUCCESS);
    }

    @Override
    public String deleteArticle(ArticleVO articleVO) {
        Article article = articleService.getById(articleVO.getUid());
        article.setStatus(EStatus.DISABLED);
        Boolean save = article.updateById();

        //保存成功后，需要发送消息到solr 和 redis, 同时从专题管理Item中移除该文章
        if (save) {
            Map<String, Object> map = new HashMap<>();
            map.put(SysConf.COMMAND, SysConf.DELETE);
            map.put(SysConf.BLOG_UID, article.getUid());
            map.put(SysConf.LEVEL, article.getLevel());
            map.put(SysConf.CREATE_TIME, article.getCreateTime());
            //发送到RabbitMq
            rabbitTemplate.convertAndSend(SysConf.EXCHANGE_DIRECT, SysConf.MOGU_BLOG, map);
            // 移除所有包含该文章的专题Item
            List<String> blogUidList = new ArrayList<>(Constants.NUM_ONE);
            blogUidList.add(articleVO.getUid());
            subjectItemService.deleteBatchSubjectItemByBlogUid(blogUidList);
        }
        return ResultUtil.successWithMessage(MessageConf.DELETE_SUCCESS);
    }

    @Override
    public String deleteBatchArticle(List<ArticleVO> articleVoList) {
        if (articleVoList.size() <= 0) {
            return ResultUtil.errorWithMessage(MessageConf.PARAM_INCORRECT);
        }
        List<String> uidList = new ArrayList<>();
        StringBuffer uidSbf = new StringBuffer();
        articleVoList.forEach(item -> {
            uidList.add(item.getUid());
            uidSbf.append(item.getUid() + SysConf.FILE_SEGMENTATION);
        });
        Collection<Article> blogList = articleService.listByIds(uidList);

        blogList.forEach(item -> {
            item.setStatus(EStatus.DISABLED);
        });

        Boolean save = articleService.updateBatchById(blogList);
        //保存成功后，需要发送消息到solr 和 redis
        if (save) {
            Map<String, Object> map = new HashMap<>();
            map.put(SysConf.COMMAND, SysConf.DELETE_BATCH);
            map.put(SysConf.UID, uidSbf);
            //发送到RabbitMq
            rabbitTemplate.convertAndSend(SysConf.EXCHANGE_DIRECT, SysConf.MOGU_BLOG, map);

            // 移除所有包含该文章的专题Item
            subjectItemService.deleteBatchSubjectItemByBlogUid(uidList);
        }
        return ResultUtil.successWithMessage(MessageConf.DELETE_SUCCESS);
    }

    @Override
    public void deleteRedisByBlogSort() {
        // 删除Redis中文章分类下的文章数量
        redisUtil.delete(RedisConf.DASHBOARD + Constants.SYMBOL_COLON + RedisConf.BLOG_COUNT_BY_SORT);
        // 删除文章相关缓存
        deleteRedisByBlog();
    }

    @Override
    public void deleteRedisByBlogTag() {
        // 删除Redis中文章分类下的文章数量
        redisUtil.delete(RedisConf.DASHBOARD + Constants.SYMBOL_COLON + RedisConf.BLOG_COUNT_BY_TAG);
        // 删除文章相关缓存
        deleteRedisByBlog();
    }

    @Override
    public void deleteRedisByBlog() {
        // 删除文章相关缓存
        redisUtil.delete(RedisConf.NEW_BLOG);
        redisUtil.delete(RedisConf.HOT_BLOG);
        redisUtil.delete(RedisConf.BLOG_LEVEL + Constants.SYMBOL_COLON + ELevel.FIRST);
        redisUtil.delete(RedisConf.BLOG_LEVEL + Constants.SYMBOL_COLON + ELevel.SECOND);
        redisUtil.delete(RedisConf.BLOG_LEVEL + Constants.SYMBOL_COLON + ELevel.THIRD);
        redisUtil.delete(RedisConf.BLOG_LEVEL + Constants.SYMBOL_COLON + ELevel.FOURTH);
    }

    @Override
    public IPage<Article> getBlogPageByLevel(Integer level, Long currentPage, Integer useSort) {
        //从Redis中获取内容
        String jsonResult = redisUtil.get(RedisConf.BLOG_LEVEL + RedisConf.SEGMENTATION + level);
        //判断redis中是否有文章
        if (StringUtils.isNotEmpty(jsonResult)) {
            List jsonResult2List = JsonUtils.jsonArrayToArrayList(jsonResult);
            IPage pageList = new Page();
            pageList.setRecords(jsonResult2List);
            return pageList;
        }
        Page<Article> page = new Page<>();
        page.setCurrent(currentPage);
        String blogCount = null;
        switch (level) {
            case ELevel.NORMAL: {
                blogCount = sysParamsService.getSysParamsValueByKey(SysConf.BLOG_NEW_COUNT);
            }
            break;
            case ELevel.FIRST: {
                blogCount = sysParamsService.getSysParamsValueByKey(SysConf.BLOG_FIRST_COUNT);
            }
            break;
            case ELevel.SECOND: {
                blogCount = sysParamsService.getSysParamsValueByKey(SysConf.BLOG_SECOND_COUNT);
            }
            break;
            case ELevel.THIRD: {
                blogCount = sysParamsService.getSysParamsValueByKey(SysConf.BLOG_THIRD_COUNT);
            }
            break;
            case ELevel.FOURTH: {
                blogCount = sysParamsService.getSysParamsValueByKey(SysConf.BLOG_FOURTH_COUNT);
            }
            break;
        }
        if (StringUtils.isEmpty(blogCount)) {
            log.error(MessageConf.PLEASE_CONFIGURE_SYSTEM_PARAMS);
        } else {
            page.setSize(Long.valueOf(blogCount));
        }

        IPage<Article> pageList = articleService.getBlogPageByLevel(page, level, useSort);
        List<Article> list = pageList.getRecords();

        // 一级推荐或者二级推荐没有内容时，自动把top5填充至一级推荐和二级推荐中
        if ((level == SysConf.ONE || level == SysConf.TWO) && list.size() == 0) {
            QueryWrapper<Article> queryWrapper = new QueryWrapper<>();
            Page<Article> hotPage = new Page<>();
            hotPage.setCurrent(1);
            String blogHotCount = sysParamsService.getSysParamsValueByKey(SysConf.BLOG_HOT_COUNT);
            String blogSecondCount = sysParamsService.getSysParamsValueByKey(SysConf.BLOG_SECOND_COUNT);
            if (StringUtils.isEmpty(blogHotCount) || StringUtils.isEmpty(blogSecondCount)) {
                log.error(MessageConf.PLEASE_CONFIGURE_SYSTEM_PARAMS);
            } else {
                hotPage.setSize(Long.valueOf(blogHotCount));
            }
            queryWrapper.eq(SQLConf.STATUS, EStatus.ENABLE);
            queryWrapper.eq(SQLConf.IS_PUBLISH, EPublish.PUBLISH);
            queryWrapper.orderByDesc(SQLConf.CLICK_COUNT);
            queryWrapper.select(Article.class, i -> !i.getProperty().equals(SQLConf.CONTENT));
            IPage<Article> hotPageList = articleService.page(hotPage, queryWrapper);
            List<Article> hotBlogList = hotPageList.getRecords();
            List<Article> secondBlogList = new ArrayList<>();
            List<Article> firstBlogList = new ArrayList<>();
            for (int a = 0; a < hotBlogList.size(); a++) {
                // 当推荐大于两个的时候
                if ((hotBlogList.size() - firstBlogList.size()) > Long.valueOf(blogSecondCount)) {
                    firstBlogList.add(hotBlogList.get(a));
                } else {
                    secondBlogList.add(hotBlogList.get(a));
                }
            }

            firstBlogList = setBlog(firstBlogList);
            secondBlogList = setBlog(secondBlogList);

            // 将从数据库查询的数据缓存到redis中，设置1小时后过期 [避免 list 中没有数据而保存至 redis 的情况]
            if (firstBlogList.size() > 0) {
                redisUtil.setEx(RedisConf.BLOG_LEVEL + Constants.SYMBOL_COLON + Constants.NUM_ONE, JsonUtils.objectToJson(firstBlogList), 1, TimeUnit.HOURS);
            }
            if (secondBlogList.size() > 0) {
                redisUtil.setEx(RedisConf.BLOG_LEVEL + Constants.SYMBOL_COLON + Constants.NUM_TWO, JsonUtils.objectToJson(secondBlogList), 1, TimeUnit.HOURS);
            }

            switch (level) {
                case SysConf.ONE: {
                    pageList.setRecords(firstBlogList);
                }
                break;
                case SysConf.TWO: {
                    pageList.setRecords(secondBlogList);
                }
                break;
            }
            return pageList;
        }

        list = setBlog(list);
        pageList.setRecords(list);

        // 将从数据库查询的数据缓存到redis中 [避免 list 中没有数据而保存至 redis 的情况]
        if (list.size() > 0) {
            redisUtil.setEx(SysConf.BLOG_LEVEL + SysConf.REDIS_SEGMENTATION + level, JsonUtils.objectToJson(list).toString(), 1, TimeUnit.HOURS);
        }
        return pageList;
    }

    @Override
    public IPage<Article> getHotBlog() {
        //从Redis中获取内容
        String jsonResult = redisUtil.get(RedisConf.HOT_BLOG);
        //判断redis中是否有文章
        if (StringUtils.isNotEmpty(jsonResult)) {
            List jsonResult2List = JsonUtils.jsonArrayToArrayList(jsonResult);
            IPage pageList = new Page();
            pageList.setRecords(jsonResult2List);
            return pageList;
        }
        QueryWrapper<Article> queryWrapper = new QueryWrapper<>();
        Page<Article> page = new Page<>();
        page.setCurrent(0);
        String blogHotCount = sysParamsService.getSysParamsValueByKey(SysConf.BLOG_HOT_COUNT);
        if (StringUtils.isEmpty(blogHotCount)) {
            log.error(MessageConf.PLEASE_CONFIGURE_SYSTEM_PARAMS);
        } else {
            page.setSize(Long.valueOf(blogHotCount));
        }
        queryWrapper.eq(SQLConf.STATUS, EStatus.ENABLE);
        queryWrapper.eq(SQLConf.IS_PUBLISH, EPublish.PUBLISH);
        queryWrapper.orderByDesc(SQLConf.CLICK_COUNT);
        //因为首页并不需要显示内容，所以需要排除掉内容字段
        queryWrapper.select(Article.class, i -> !i.getProperty().equals(SQLConf.CONTENT));
        IPage<Article> pageList = articleService.page(page, queryWrapper);
        List<Article> list = pageList.getRecords();
        list = setBlog(list);
        pageList.setRecords(list);
        // 将从数据库查询的数据缓存到redis中[避免list中没有数据而保存至redis的情况]
        if (list.size() > 0) {
            redisUtil.setEx(RedisConf.HOT_BLOG, JsonUtils.objectToJson(list), 1, TimeUnit.HOURS);
        }
        return pageList;
    }

    @Override
    public IPage<Article> getNewBlog(Long currentPage, Long pageSize) {
        String articleNewCount = sysParamsService.getSysParamsValueByKey(SysConf.BLOG_NEW_COUNT);
        if (StringUtils.isEmpty(articleNewCount)) {
            log.error(MessageConf.PLEASE_CONFIGURE_SYSTEM_PARAMS);
        }

//        // 判断Redis中是否缓存了第一页的内容
//        if (currentPage == 1L) {
//            //从Redis中获取内容
//            String jsonResult = redisUtil.get(RedisConf.NEW_BLOG);
//            //判断redis中是否有文章
//            if (StringUtils.isNotEmpty(jsonResult)) {
//                IPage pageList = JsonUtils.jsonToPojo(jsonResult, Page.class);
//                return pageList;
//            }
//        }

        QueryWrapper<Article> queryWrapper = new QueryWrapper<>();
        Page<Article> page = new Page<>();
        page.setCurrent(currentPage);
        page.setSize(Long.valueOf(articleNewCount));
        queryWrapper.eq(SQLConf.STATUS, EStatus.ENABLE);
        queryWrapper.eq(BaseSQLConf.IS_PUBLISH, EPublish.PUBLISH);
        queryWrapper.orderByDesc(SQLConf.CREATE_TIME);

        //因为首页并不需要显示内容，所以需要排除掉内容字段
        queryWrapper.select(Article.class, i -> !i.getProperty().equals(SQLConf.CONTENT));

        IPage<Article> pageList = articleService.page(page, queryWrapper);
        List<Article> list = pageList.getRecords();

        if (list.size() <= 0) {
            return pageList;
        }

        list = setBlog(list);
        pageList.setRecords(list);

        //将从最新文章缓存到redis中
//        if (currentPage == 1L) {
//            redisUtil.setEx(RedisConf.NEW_BLOG, JsonUtils.objectToJson(pageList), 1, TimeUnit.HOURS);
//        }
        return pageList;
    }

    @Override
    public IPage<Article> getBlogBySearch(Long currentPage, Long pageSize) {
        QueryWrapper<Article> queryWrapper = new QueryWrapper<>();
        Page<Article> page = new Page<>();
        page.setCurrent(currentPage);
        String blogNewCount = sysParamsService.getSysParamsValueByKey(SysConf.BLOG_NEW_COUNT);
        if (StringUtils.isEmpty(blogNewCount)) {
            log.error(MessageConf.PLEASE_CONFIGURE_SYSTEM_PARAMS);
        } else {
            page.setSize(Long.valueOf(blogNewCount));
        }
        queryWrapper.eq(SQLConf.STATUS, EStatus.ENABLE);
        queryWrapper.eq(BaseSQLConf.IS_PUBLISH, EPublish.PUBLISH);
        queryWrapper.orderByDesc(SQLConf.CREATE_TIME);
        IPage<Article> pageList = articleService.page(page, queryWrapper);
        List<Article> list = pageList.getRecords();
        if (list.size() <= 0) {
            return pageList;
        }
        list = setBlog(list);
        pageList.setRecords(list);
        return pageList;
    }

    @Override
    public IPage<Article> getBlogByTime(Long currentPage, Long pageSize) {
        QueryWrapper<Article> queryWrapper = new QueryWrapper<>();
        Page<Article> page = new Page<>();
        page.setCurrent(currentPage);
        page.setSize(pageSize);
        queryWrapper.eq(SQLConf.STATUS, EStatus.ENABLE);
        queryWrapper.eq(BaseSQLConf.IS_PUBLISH, EPublish.PUBLISH);
        queryWrapper.orderByDesc(SQLConf.CREATE_TIME);
        //因为首页并不需要显示内容，所以需要排除掉内容字段
        queryWrapper.select(Article.class, i -> !i.getProperty().equals(SQLConf.CONTENT));
        IPage<Article> pageList = articleService.page(page, queryWrapper);
        List<Article> list = pageList.getRecords();
        list = setBlog(list);
        pageList.setRecords(list);
        return pageList;
    }

    @Override
    public Integer getBlogPraiseCountByUid(String uid) {
        Integer pariseCount = 0;
        if (StringUtils.isEmpty(uid)) {
            log.error("传入的UID为空");
            return pariseCount;
        }
        //从Redis取出用户点赞数据
        String pariseJsonResult = redisUtil.get(RedisConf.BLOG_PRAISE + RedisConf.SEGMENTATION + uid);
        if (!StringUtils.isEmpty(pariseJsonResult)) {
            pariseCount = Integer.parseInt(pariseJsonResult);
        }
        return pariseCount;
    }

    @Override
    public String praiseBlogByUid(String uid) {
        if (StringUtils.isEmpty(uid)) {
            return ResultUtil.errorWithMessage(MessageConf.PARAM_INCORRECT);
        }

        HttpServletRequest request = RequestHolder.getRequest();
        // 如果用户登录了
        if (request.getAttribute(SysConf.USER_UID) != null) {
            String userUid = request.getAttribute(SysConf.USER_UID).toString();
            QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq(SQLConf.USER_UID, userUid);
            queryWrapper.eq(SQLConf.BLOG_UID, uid);
            queryWrapper.eq(SQLConf.TYPE, ECommentType.PRAISE);
            queryWrapper.last(SysConf.LIMIT_ONE);
            Comment praise = commentService.getOne(queryWrapper);
            if (praise != null) {
                return ResultUtil.errorWithMessage(MessageConf.YOU_HAVE_BEEN_PRISE);
            }
        } else {
            return ResultUtil.errorWithMessage(MessageConf.PLEASE_LOGIN_TO_PRISE);
        }
        Article blog = articleService.getById(uid);
        String pariseJsonResult = redisUtil.get(RedisConf.BLOG_PRAISE + RedisConf.SEGMENTATION + uid);
        if (StringUtils.isEmpty(pariseJsonResult)) {
            //给该文章点赞数
            redisUtil.set(RedisConf.BLOG_PRAISE + RedisConf.SEGMENTATION + uid, "1");
            blog.setCollectCount(1);
            blog.updateById();

        } else {
            Integer count = blog.getCollectCount() + 1;
            //给该文章点赞 +1
            redisUtil.set(RedisConf.BLOG_PRAISE + RedisConf.SEGMENTATION + uid, count.toString());
            blog.setCollectCount(count);
            blog.updateById();
        }
        // 已登录用户，向评论表添加点赞数据
        if (request.getAttribute(SysConf.USER_UID) != null) {
            String userUid = request.getAttribute(SysConf.USER_UID).toString();
            Comment comment = new Comment();
            comment.setUserUid(userUid);
            comment.setBlogUid(uid);
            comment.setSource(ECommentSource.BLOG_INFO.getCode());
            comment.setType(ECommentType.PRAISE);
            comment.insert();
        }
        return ResultUtil.successWithData(blog.getCollectCount());
    }

    @Override
    public IPage<Article> getSameBlogByTagUid(String tagUid) {
        QueryWrapper<Article> queryWrapper = new QueryWrapper<>();
        Page<Article> page = new Page<>();
        page.setCurrent(1);
        page.setSize(10);
        queryWrapper.like(SQLConf.TAG_UID, tagUid);
        queryWrapper.orderByDesc(SQLConf.CREATE_TIME);
        queryWrapper.eq(SQLConf.STATUS, EStatus.ENABLE);
        queryWrapper.eq(SQLConf.IS_PUBLISH, EPublish.PUBLISH);
        IPage<Article> pageList = articleService.page(page, queryWrapper);
        List<Article> list = pageList.getRecords();
        list = articleService.setTagAndSortByArticleList(list);
        pageList.setRecords(list);
        return pageList;
    }

    @Override
    public IPage<Article> getListByBlogSortUid(String blogSortUid, Long currentPage, Long pageSize) {
        //分页
        Page<Article> page = new Page<>();
        page.setCurrent(currentPage);
        page.setSize(pageSize);
        QueryWrapper<Article> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(SQLConf.STATUS, EStatus.ENABLE);
        queryWrapper.orderByDesc(SQLConf.CREATE_TIME);
        queryWrapper.eq(BaseSQLConf.IS_PUBLISH, EPublish.PUBLISH);
        queryWrapper.eq(SQLConf.BLOG_SORT_UID, blogSortUid);

        //因为首页并不需要显示内容，所以需要排除掉内容字段
        queryWrapper.select(Article.class, i -> !i.getProperty().equals(SQLConf.CONTENT));
        IPage<Article> pageList = articleService.page(page, queryWrapper);

        //给文章增加标签和分类
        List<Article> list = articleService.setTagAndSortAndPictureByBlogList(pageList.getRecords());
        pageList.setRecords(list);
        return pageList;
    }

    @Override
    public Map<String, Object> getBlogByKeyword(String keywords, Long currentPage, Long pageSize) {
        final String keyword = keywords.trim();
        QueryWrapper<Article> queryWrapper = new QueryWrapper<>();
        queryWrapper.and(wrapper -> wrapper.like(SQLConf.TITLE, keyword).or().like(SQLConf.SUMMARY, keyword));
        queryWrapper.eq(SQLConf.STATUS, EStatus.ENABLE);
        queryWrapper.eq(SQLConf.IS_PUBLISH, EPublish.PUBLISH);
        queryWrapper.select(Article.class, i -> !i.getProperty().equals(SQLConf.CONTENT));
        queryWrapper.orderByDesc(SQLConf.CLICK_COUNT);
        Page<Article> page = new Page<>();
        page.setCurrent(currentPage);
        page.setSize(pageSize);

        IPage<Article> iPage = articleService.page(page, queryWrapper);
        List<Article> blogList = iPage.getRecords();
        List<String> blogSortUidList = new ArrayList<>();
        Map<String, String> pictureMap = new HashMap<>();
        final StringBuffer fileUids = new StringBuffer();
        blogList.forEach(item -> {
            // 获取图片uid
            blogSortUidList.add(item.getArticleSortUid());
            if (StringUtils.isNotEmpty(item.getFileUid())) {
                fileUids.append(item.getFileUid() + SysConf.FILE_SEGMENTATION);
            }
            // 给标题和简介设置高亮
            item.setTitle(getHitCode(item.getTitle(), keyword));
            item.setSummary(getHitCode(item.getSummary(), keyword));

        });

        // 调用图片接口，获取图片
        String pictureList = null;
        if (fileUids != null) {
            pictureList = this.pictureFeignClient.getPicture(fileUids.toString(), SysConf.FILE_SEGMENTATION);
        }
        List<Map<String, Object>> picList = webUtil.getPictureMap(pictureList);

        picList.forEach(item -> {
            pictureMap.put(item.get(SQLConf.UID).toString(), item.get(SQLConf.URL).toString());
        });

        Collection<ArticleSort> articleSortList = new ArrayList<>();
        if (blogSortUidList.size() > 0) {
            articleSortList = articleSortService.listByIds(blogSortUidList);
        }

        Map<String, String> blogSortMap = new HashMap<>();
        articleSortList.forEach(item -> {
            blogSortMap.put(item.getUid(), item.getSortName());
        });

        // 设置分类名 和 图片
        blogList.forEach(item -> {
            if (blogSortMap.get(item.getArticleSortUid()) != null) {
                item.setBlogSortName(blogSortMap.get(item.getArticleSortUid()));
            }

            //获取图片
            if (StringUtils.isNotEmpty(item.getFileUid())) {
                List<String> pictureUidsTemp = StringUtils.changeStringToString(item.getFileUid(), SysConf.FILE_SEGMENTATION);
                List<String> pictureListTemp = new ArrayList<>();

                pictureUidsTemp.forEach(picture -> {
                    pictureListTemp.add(pictureMap.get(picture));
                });
                // 只设置一张标题图
                if (pictureListTemp.size() > 0) {
                    item.setPhotoUrl(pictureListTemp.get(0));
                } else {
                    item.setPhotoUrl("");
                }
            }
        });

        Map<String, Object> map = new HashMap<>();
        // 返回总记录数
        map.put(SysConf.TOTAL, iPage.getTotal());
        // 返回总页数
        map.put(SysConf.TOTAL_PAGE, iPage.getPages());
        // 返回当前页大小
        map.put(SysConf.PAGE_SIZE, pageSize);
        // 返回当前页
        map.put(SysConf.CURRENT_PAGE, iPage.getCurrent());
        // 返回数据
        map.put(SysConf.BLOG_LIST, blogList);
        return map;
    }

    @Override
    public IPage<Article> searchBlogByTag(String tagUid, Long currentPage, Long pageSize) {
        Tag tag = tagService.getById(tagUid);
        if (tag != null) {
            HttpServletRequest request = RequestHolder.getRequest();
            String ip = IpUtils.getIpAddr(request);
            //从Redis取出数据，判断该用户24小时内，是否点击过该标签
            String jsonResult = redisUtil.get(RedisConf.TAG_CLICK + RedisConf.SEGMENTATION + ip + "#" + tagUid);
            if (StringUtils.isEmpty(jsonResult)) {
                //给标签点击数增加
                int clickCount = tag.getClickCount() + 1;
                tag.setClickCount(clickCount);
                tag.updateById();
                //将该用户点击记录存储到redis中, 24小时后过期
                redisUtil.setEx(RedisConf.TAG_CLICK + RedisConf.SEGMENTATION + ip + RedisConf.WELL_NUMBER + tagUid, clickCount + "",
                        24, TimeUnit.HOURS);
            }
        }
        QueryWrapper<Article> queryWrapper = new QueryWrapper<>();
        Page<Article> page = new Page<>();
        page.setCurrent(currentPage);
        page.setSize(pageSize);

        queryWrapper.like(SQLConf.TAG_UID, tagUid);
        queryWrapper.eq(SQLConf.STATUS, EStatus.ENABLE);
        queryWrapper.eq(BaseSQLConf.IS_PUBLISH, EPublish.PUBLISH);
        queryWrapper.orderByDesc(SQLConf.CREATE_TIME);
        queryWrapper.select(Article.class, i -> !i.getProperty().equals(SysConf.CONTENT));
        IPage<Article> pageList = articleService.page(page, queryWrapper);
        List<Article> list = pageList.getRecords();
        list = articleService.setTagAndSortAndPictureByBlogList(list);
        pageList.setRecords(list);
        return pageList;
    }

    @Override
    public IPage<Article> searchBlogByBlogSort(String blogSortUid, Long currentPage, Long pageSize) {
        ArticleSort articleSort = articleSortService.getById(blogSortUid);
        if (articleSort != null) {
            HttpServletRequest request = RequestHolder.getRequest();
            String ip = IpUtils.getIpAddr(request);

            //从Redis取出数据，判断该用户24小时内，是否点击过该分类
            String jsonResult = redisUtil.get(RedisConf.TAG_CLICK + RedisConf.SEGMENTATION + ip + RedisConf.WELL_NUMBER + blogSortUid);
            if (StringUtils.isEmpty(jsonResult)) {
                //给标签点击数增加
                int clickCount = articleSort.getClickCount() + 1;
                articleSort.setClickCount(clickCount);
                articleSort.updateById();
                //将该用户点击记录存储到redis中, 24小时后过期
                redisUtil.setEx(RedisConf.TAG_CLICK + RedisConf.SEGMENTATION + ip + RedisConf.WELL_NUMBER + blogSortUid, clickCount + "",
                        24, TimeUnit.HOURS);
            }
        }

        QueryWrapper<Article> queryWrapper = new QueryWrapper<>();
        Page<Article> page = new Page<>();
        page.setCurrent(currentPage);
        page.setSize(pageSize);
        queryWrapper.eq(SQLConf.BLOG_SORT_UID, blogSortUid);
        queryWrapper.orderByDesc(SQLConf.CREATE_TIME);
        queryWrapper.eq(BaseSQLConf.IS_PUBLISH, EPublish.PUBLISH);
        queryWrapper.eq(BaseSQLConf.STATUS, EStatus.ENABLE);
        // 排除文章详情
        queryWrapper.select(Article.class, i -> !i.getProperty().equals(SysConf.CONTENT));
        IPage<Article> pageList = articleService.page(page, queryWrapper);
        List<Article> list = pageList.getRecords();
        list = articleService.setTagAndSortAndPictureByBlogList(list);
        pageList.setRecords(list);
        return pageList;
    }

    @Override
    public IPage<Article> searchBlogByAuthor(String author, Long currentPage, Long pageSize) {
        QueryWrapper<Article> queryWrapper = new QueryWrapper<>();

        Page<Article> page = new Page<>();
        page.setCurrent(currentPage);
        page.setSize(pageSize);
        queryWrapper.eq(SQLConf.AUTHOR, author);
        queryWrapper.eq(BaseSQLConf.IS_PUBLISH, EPublish.PUBLISH);
        queryWrapper.eq(BaseSQLConf.STATUS, EStatus.ENABLE);
        queryWrapper.orderByDesc(SQLConf.CREATE_TIME);
        queryWrapper.select(Article.class, i -> !i.getProperty().equals(SysConf.CONTENT));
        IPage<Article> pageList = articleService.page(page, queryWrapper);
        List<Article> list = pageList.getRecords();
        list = articleService.setTagAndSortAndPictureByBlogList(list);
        pageList.setRecords(list);
        return pageList;
    }

    @Override
    public String getBlogTimeSortList() {
        //从Redis中获取内容
        String monthResult = redisUtil.get(SysConf.MONTH_SET);
        //判断redis中时候包含归档的内容
        if (StringUtils.isNotEmpty(monthResult)) {
            List list = JsonUtils.jsonArrayToArrayList(monthResult);
            return ResultUtil.successWithData(list);
        }
        // 第一次启动的时候归档
        QueryWrapper<Article> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(SQLConf.STATUS, EStatus.ENABLE);
        queryWrapper.orderByDesc(SQLConf.CREATE_TIME);
        queryWrapper.eq(SQLConf.IS_PUBLISH, EPublish.PUBLISH);
        //因为首页并不需要显示内容，所以需要排除掉内容字段
        queryWrapper.select(Article.class, i -> !i.getProperty().equals(SQLConf.CONTENT));
        List<Article> list = articleService.list(queryWrapper);

        //给文章增加标签、分类、图片
        list = articleService.setTagAndSortAndPictureByBlogList(list);

        Map<String, List<Article>> map = new HashMap<>();
        Iterator iterable = list.iterator();
        Set<String> monthSet = new TreeSet<>();
        while (iterable.hasNext()) {
            Article blog = (Article) iterable.next();
            Date createTime = blog.getCreateTime();

            String month = new SimpleDateFormat("yyyy年MM月").format(createTime).toString();

            monthSet.add(month);

            if (map.get(month) == null) {
                List<Article> blogList = new ArrayList<>();
                blogList.add(blog);
                map.put(month, blogList);
            } else {
                List<Article> blogList = map.get(month);
                blogList.add(blog);
                map.put(month, blogList);
            }
        }

        // 缓存该月份下的所有文章  key: 月份   value：月份下的所有文章
        map.forEach((key, value) -> {
            redisUtil.set(SysConf.BLOG_SORT_BY_MONTH + SysConf.REDIS_SEGMENTATION + key, JsonUtils.objectToJson(value).toString());
        });

        //将从数据库查询的数据缓存到redis中
        redisUtil.set(SysConf.MONTH_SET, JsonUtils.objectToJson(monthSet).toString());
        return ResultUtil.successWithData(monthSet);
    }

    @Override
    public String getArticleByMonth(String monthDate) {
        if (StringUtils.isEmpty(monthDate)) {
            return ResultUtil.errorWithMessage(MessageConf.PARAM_INCORRECT);
        }
        //从Redis中获取内容
        String contentResult = redisUtil.get(SysConf.BLOG_SORT_BY_MONTH + SysConf.REDIS_SEGMENTATION + monthDate);

        //判断redis中时候包含该日期下的文章
        if (StringUtils.isNotEmpty(contentResult)) {
            List list = JsonUtils.jsonArrayToArrayList(contentResult);
            return ResultUtil.successWithData(list);
        }

        // 第一次启动的时候归档
        QueryWrapper<Article> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(SQLConf.STATUS, EStatus.ENABLE);
        queryWrapper.orderByDesc(SQLConf.CREATE_TIME);
        queryWrapper.eq(BaseSQLConf.IS_PUBLISH, EPublish.PUBLISH);
        //因为首页并不需要显示内容，所以需要排除掉内容字段
        queryWrapper.select(Article.class, i -> !i.getProperty().equals(SQLConf.CONTENT));
        List<Article> list = articleService.list(queryWrapper);

        //给文章增加标签、分类、图片
        list = articleService.setTagAndSortAndPictureByBlogList(list);

        Map<String, List<Article>> map = new HashMap<>();
        Iterator iterable = list.iterator();
        Set<String> monthSet = new TreeSet<>();
        while (iterable.hasNext()) {
            Article blog = (Article) iterable.next();
            Date createTime = blog.getCreateTime();

            String month = new SimpleDateFormat("yyyy年MM月").format(createTime).toString();

            monthSet.add(month);

            if (map.get(month) == null) {
                List<Article> blogList = new ArrayList<>();
                blogList.add(blog);
                map.put(month, blogList);
            } else {
                List<Article> blogList = map.get(month);
                blogList.add(blog);
                map.put(month, blogList);
            }
        }

        // 缓存该月份下的所有文章  key: 月份   value：月份下的所有文章
        map.forEach((key, value) -> {
            redisUtil.set(SysConf.BLOG_SORT_BY_MONTH + SysConf.REDIS_SEGMENTATION + key, JsonUtils.objectToJson(value).toString());
        });
        //将从数据库查询的数据缓存到redis中
        redisUtil.set(SysConf.MONTH_SET, JsonUtils.objectToJson(monthSet));
        return ResultUtil.successWithData(map.get(monthDate));
    }

    /**
     * 添加时校验
     *
     * @param count
     * @param level
     * @return
     */
    private String addVerdict(Integer count, Integer level) {

        //添加的时候进行判断
        switch (level) {
            case ELevel.FIRST: {
                Long blogFirstCount = Long.valueOf(sysParamsService.getSysParamsValueByKey(SysConf.BLOG_FIRST_COUNT));
                if (count > blogFirstCount) {
                    return ResultUtil.errorWithMessage("一级推荐不能超过" + blogFirstCount + "个");
                }
            }
            break;
            case ELevel.SECOND: {
                Long blogSecondCount = Long.valueOf(sysParamsService.getSysParamsValueByKey(SysConf.BLOG_SECOND_COUNT));
                if (count > blogSecondCount) {
                    return ResultUtil.errorWithMessage("二级推荐不能超过" + blogSecondCount + "个");
                }
            }
            break;
            case ELevel.THIRD: {
                Long blogThirdCount = Long.valueOf(sysParamsService.getSysParamsValueByKey(SysConf.BLOG_THIRD_COUNT));
                if (count > blogThirdCount) {
                    return ResultUtil.errorWithMessage("三级推荐不能超过" + blogThirdCount + "个");
                }
            }
            break;
            case ELevel.FOURTH: {
                Long blogFourthCount = Long.valueOf(sysParamsService.getSysParamsValueByKey(SysConf.BLOG_FOURTH_COUNT));
                if (count > blogFourthCount) {
                    return ResultUtil.errorWithMessage("四级推荐不能超过" + blogFourthCount + "个");
                }
            }
            break;
            default: {
            }
        }
        return null;
    }

    /**
     * 保存成功后，需要发送消息到solr 和 redis
     *
     * @param isSave
     * @param blog
     */
    private void updateSolrAndRedis(Boolean isSave, Article blog) {
        // 保存操作，并且文章已设置发布
        if (isSave && EPublish.PUBLISH.equals(blog.getIsPublish())) {
            Map<String, Object> map = new HashMap<>();
            map.put(SysConf.COMMAND, SysConf.ADD);
            map.put(SysConf.BLOG_UID, blog.getUid());
            map.put(SysConf.LEVEL, blog.getLevel());
            map.put(SysConf.CREATE_TIME, blog.getCreateTime());

            //发送到RabbitMq
            rabbitTemplate.convertAndSend(SysConf.EXCHANGE_DIRECT, SysConf.MOGU_BLOG, map);

        } else if (EPublish.NO_PUBLISH.equals(blog.getIsPublish())) {

            //这是需要做的是，是删除redis中的该条文章数据
            Map<String, Object> map = new HashMap<>();
            map.put(SysConf.COMMAND, SysConf.EDIT);
            map.put(SysConf.BLOG_UID, blog.getUid());
            map.put(SysConf.LEVEL, blog.getLevel());
            map.put(SysConf.CREATE_TIME, blog.getCreateTime());

            //发送到RabbitMq
            rabbitTemplate.convertAndSend(SysConf.EXCHANGE_DIRECT, SysConf.MOGU_BLOG, map);
        }
    }

    /**
     * 设置文章的分类标签和内容
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

            //设置分类
            if (StringUtils.isNotEmpty(item.getArticleSortUid())) {
                item.setArticleSort(sortMap.get(item.getArticleSortUid()));
            }

            //获取标签
            if (StringUtils.isNotEmpty(item.getTagUid())) {
                List<String> tagUidsTemp = StringUtils.changeStringToString(item.getTagUid(), SysConf.FILE_SEGMENTATION);
                List<Tag> tagListTemp = new ArrayList<Tag>();

                tagUidsTemp.forEach(tag -> {
                    if (tagMap.get(tag) != null) {
                        tagListTemp.add(tagMap.get(tag));
                    }
                });
                item.setTagList(tagListTemp);
            }

            //获取图片
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

    /**
     * 添加高亮
     *
     * @param str
     * @param keyword
     * @return
     */
    private String getHitCode(String str, String keyword) {
        if (StringUtils.isEmpty(keyword) || StringUtils.isEmpty(str)) {
            return str;
        }
        String startStr = "<span style = 'color:red'>";
        String endStr = "</span>";
        // 判断关键字是否直接是搜索的内容，否者直接返回
        if (str.equals(keyword)) {
            return startStr + str + endStr;
        }
        String lowerCaseStr = str.toLowerCase();
        String lowerKeyword = keyword.toLowerCase();
        String[] lowerCaseArray = lowerCaseStr.split(lowerKeyword);
        Boolean isEndWith = lowerCaseStr.endsWith(lowerKeyword);

        // 计算分割后的字符串位置
        Integer count = 0;
        List<Map<String, Integer>> list = new ArrayList<>();
        List<Map<String, Integer>> keyList = new ArrayList<>();
        for (int a = 0; a < lowerCaseArray.length; a++) {
            // 将切割出来的存储map
            Map<String, Integer> map = new HashMap<>();
            Map<String, Integer> keyMap = new HashMap<>();
            map.put("startIndex", count);
            Integer len = lowerCaseArray[a].length();
            count += len;
            map.put("endIndex", count);
            list.add(map);
            if (a < lowerCaseArray.length - 1 || isEndWith) {
                // 将keyword存储map
                keyMap.put("startIndex", count);
                count += keyword.length();
                keyMap.put("endIndex", count);
                keyList.add(keyMap);
            }
        }
        // 截取切割对象
        List<String> arrayList = new ArrayList<>();
        for (Map<String, Integer> item : list) {
            Integer start = item.get("startIndex");
            Integer end = item.get("endIndex");
            String itemStr = str.substring(start, end);
            arrayList.add(itemStr);
        }
        // 截取关键字
        List<String> keyArrayList = new ArrayList<>();
        for (Map<String, Integer> item : keyList) {
            Integer start = item.get("startIndex");
            Integer end = item.get("endIndex");
            String itemStr = str.substring(start, end);
            keyArrayList.add(itemStr);
        }

        StringBuffer sb = new StringBuffer();
        for (int a = 0; a < arrayList.size(); a++) {
            sb.append(arrayList.get(a));
            if (a < arrayList.size() - 1 || isEndWith) {
                sb.append(startStr);
                sb.append(keyArrayList.get(a));
                sb.append(endStr);
            }
        }
        return sb.toString();
    }
}
