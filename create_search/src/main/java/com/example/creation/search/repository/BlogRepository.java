package com.example.creation.search.repository;

import com.example.creation.search.pojo.ESBlogIndex;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * BlogRepository操作类
 * 在ElasticsearchRepository中我们可以使用Not Add Like Or Between等关键词自动创建查询语句
 *
 */
public interface BlogRepository extends ElasticsearchRepository<ESBlogIndex, String> {
}
