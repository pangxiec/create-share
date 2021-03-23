package com.example.creation.spider.service.impl;


import com.example.creation.spider.entity.BlogSpider;
import com.example.creation.spider.mapper.BlogSpiderMapper;
import com.example.creation.spider.service.BlogSpiderService;
import com.example.creation.base.serviceImpl.SuperServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 文章爬取服务实现类
 * </p>
 *
 */
@Slf4j
@Service
public class BlogSpiderServiceImpl extends SuperServiceImpl<BlogSpiderMapper, BlogSpider> implements BlogSpiderService {

}
