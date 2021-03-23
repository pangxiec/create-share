package com.example.creation.search.restapi;

import com.example.creation.commons.entity.Article;
import com.example.creation.commons.feign.WebFeignClient;
import com.example.creation.search.global.MessageConf;
import com.example.creation.search.global.SysConf;
import com.example.creation.search.pojo.ESBlogIndex;
import com.example.creation.search.repository.BlogRepository;
import com.example.creation.search.service.ElasticSearchService;
import com.example.creation.utils.ResultUtil;
import com.example.creation.utils.StringUtils;
import com.example.creation.utils.WebUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ElasticSearch RestAPI
 *
 */
@RequestMapping("/search")
@Api(value = "ElasticSearch相关接口", tags = {"ElasticSearch相关接口"})
@RestController
public class ElasticSearchRestApi {

    @Resource
    ElasticsearchTemplate elasticsearchTemplate;
    @Resource
    private ElasticSearchService searchService;
    @Resource
    private BlogRepository blogRepository;
    @Resource
    private WebFeignClient webFeignClient;


    @ApiOperation(value = "通过ElasticSearch搜索文章", notes = "通过ElasticSearch搜索文章", response = String.class)
    @GetMapping("/elasticSearchBlog")
    public String searchBlog(HttpServletRequest request,
                             @RequestParam(required = false) String keywords,
                             @RequestParam(name = "currentPage", required = false, defaultValue = "1") Integer
                                     currentPage,
                             @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer
                                     pageSize) {

        if (StringUtils.isEmpty(keywords)) {
            return ResultUtil.result(SysConf.ERROR, MessageConf.KEYWORD_IS_NOT_EMPTY);
        }
        return ResultUtil.result(SysConf.SUCCESS, searchService.search(keywords, currentPage, pageSize));
    }

    @ApiOperation(value = "通过uids删除ElasticSearch文章索引", notes = "通过uids删除ElasticSearch文章索引", response = String.class)
    @PostMapping("/deleteElasticSearchByUids")
    public String deleteElasticSearchByUids(@RequestParam(required = true) String uids) {

        List<String> uidList = StringUtils.changeStringToString(uids, SysConf.FILE_SEGMENTATION);

        for (String uid : uidList) {
            blogRepository.deleteById(uid);
        }

        return ResultUtil.result(SysConf.SUCCESS, MessageConf.DELETE_SUCCESS);
    }

    @ApiOperation(value = "通过文章uid删除ElasticSearch文章索引", notes = "通过uid删除文章", response = String.class)
    @PostMapping("/deleteElasticSearchByUid")
    public String deleteElasticSearchByUid(@RequestParam(required = true) String uid) {
        blogRepository.deleteById(uid);
        return ResultUtil.result(SysConf.SUCCESS, MessageConf.DELETE_SUCCESS);
    }

    @ApiOperation(value = "ElasticSearch通过文章Uid添加索引", notes = "添加文章", response = String.class)
    @PostMapping("/addElasticSearchIndexByUid")
    public String addElasticSearchIndexByUid(@RequestParam(required = true) String uid) {

        String result = webFeignClient.getBlogByUid(uid);

        Article eblog = WebUtils.getData(result, Article.class);
        if (eblog == null) {
            return ResultUtil.result(SysConf.ERROR, MessageConf.INSERT_FAIL);
        }
        ESBlogIndex blog = searchService.buidBlog(eblog);
        blogRepository.save(blog);
        return ResultUtil.result(SysConf.SUCCESS, MessageConf.INSERT_SUCCESS);
    }

    @ApiOperation(value = "ElasticSearch初始化索引", notes = "ElasticSearch初始化索引", response = String.class)
    @PostMapping("/initElasticSearchIndex")
    public String initElasticSearchIndex() throws ParseException {
        elasticsearchTemplate.deleteIndex(ESBlogIndex.class);
        elasticsearchTemplate.createIndex(ESBlogIndex.class);
        elasticsearchTemplate.putMapping(ESBlogIndex.class);

        Long page = 1L;
        Long row = 10L;
        Integer size = 0;

        do {
            // 查询blog信息
            String result = webFeignClient.getBlogBySearch(page, row);

            //构建blog
            List<Article> blogList = WebUtils.getList(result, Article.class);
            size = blogList.size();

            List<ESBlogIndex> esBlogIndexList = blogList.stream()
                    .map(searchService::buidBlog).collect(Collectors.toList());

            //存入索引库
            blogRepository.saveAll(esBlogIndexList);
            // 翻页
            page++;
        } while (size == 15);

        return ResultUtil.result(SysConf.SUCCESS, MessageConf.OPERATION_SUCCESS);
    }
}
