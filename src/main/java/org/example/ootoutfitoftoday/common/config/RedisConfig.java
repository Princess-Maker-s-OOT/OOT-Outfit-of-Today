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
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.StringUtils;

import java.time.Duration;

/**
 * Redis 설정 클래스
 * - Redis 연결 설정
 * - RedisTemplate 설정(데이터 직접 저장/조회용)
 * - CacheManager 설정(@Cacheable 등 캐시 어노테이션용)
 */
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
    public CacheManager cacheManager() {
        // 기본 캐시 설정
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .disableCachingNullValues()  // ← 추가: null 값 캐싱 방지
                .computePrefixWith(cacheName -> cacheName + "::")  // ← 추가: prefix 명시
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new GenericJackson2JsonRedisSerializer(redisObjectMapper())
                        )
                );

        return RedisCacheManager.builder(redisConnectionFactory())
                .cacheDefaults(defaultConfig)
                .withCacheConfiguration("clothesCache",
                        defaultConfig.entryTtl(Duration.ofMinutes(30))
                )
                .withCacheConfiguration("userCache",
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
                // 사용자 대시보드: 24시간 배치 주기 → TTL 25시간
                .withCacheConfiguration(DashboardUserCacheNames.SUMMARY,
                        defaultConfig.entryTtl(Duration.ofHours(25))
                )
                .withCacheConfiguration(DashboardUserCacheNames.WEAR_STATISTICS,
                        defaultConfig.entryTtl(Duration.ofHours(25))
                )
                .build();
    }
}