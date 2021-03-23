package com.example.creation.web.requestLimit;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * RequestLimitConfig
 *
 */
@RefreshScope
@ConfigurationProperties(prefix = "request-limit")
@Component
@Data
public class RequestLimitConfig {

    /**
     * 允许访问的数量
     */
    public int amount;
    /**
     * 时间段
     */
    public long time;
}
