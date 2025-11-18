package org.example.ootoutfitoftoday.common.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ootcommon.dashboard.constant.DashboardAdminCacheNames;
import com.ootcommon.dashboard.constant.DashboardUserCacheNames;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.StringUtils;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    @Value("${spring.data.redis.password:}")
    private String password;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(host);
        config.setPort(port);

        if (StringUtils.hasText(password)) {
            config.setPassword(password);
        }

        return new LettuceConnectionFactory(config);
    }

    @Bean
    @Primary
    public ObjectMapper globalObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return mapper;
    }

    private ObjectMapper redisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        return mapper;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());

        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(redisObjectMapper());

        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(jsonSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();

        return template;
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate() {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(redisConnectionFactory());

        return template;
    }

    @Bean
    public CacheManager cacheManager() {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .disableCachingNullValues()
                .computePrefixWith(cacheName -> cacheName + "::")
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer(redisObjectMapper())
                        )
                );

        return RedisCacheManager.builder(redisConnectionFactory())
                .cacheDefaults(defaultConfig)
                .withCacheConfiguration("userCache",
                        defaultConfig.entryTtl(Duration.ofMinutes(10))
                )
                .withCacheConfiguration("userExistsCache",
                        defaultConfig.entryTtl(Duration.ofMinutes(1))
                )
                .withCacheConfiguration("salePostListCache",
                        defaultConfig.entryTtl(Duration.ofMinutes(10))
                )
                .withCacheConfiguration(DashboardAdminCacheNames.USER,
                        defaultConfig.entryTtl(Duration.ofMinutes(3))
                )
                .withCacheConfiguration(DashboardAdminCacheNames.CLOTHES,
                        defaultConfig.entryTtl(Duration.ofMinutes(3))
                )
                .withCacheConfiguration(DashboardAdminCacheNames.SALE_POST,
                        defaultConfig.entryTtl(Duration.ofMinutes(3))
                )
                .withCacheConfiguration(DashboardAdminCacheNames.CATEGORY,
                        defaultConfig.entryTtl(Duration.ofMinutes(3))
                )
                .withCacheConfiguration(DashboardUserCacheNames.SUMMARY,
                        defaultConfig.entryTtl(Duration.ofHours(25))
                )
                .withCacheConfiguration(DashboardUserCacheNames.WEAR_STATISTICS,
                        defaultConfig.entryTtl(Duration.ofHours(25))
                )
                .build();
    }
}