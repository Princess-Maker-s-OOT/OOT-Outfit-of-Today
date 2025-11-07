package org.example.ootoutfitoftoday.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
@EnableCaching    // @Cacheable, @CacheEvict 등 캐시 애노테이션 활성화
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    @Value("${spring.data.redis.password:}")
    private String password;

    /**
     * Redis 연결 팩토리
     * - Lettuce 클라이언트 사용(Spring Boot 기본)
     * - 비동기/논블로킹 방식으로 성능 우수
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {

        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(host);
        config.setPort(port);

        // 비밀번호가 있는 경우에만 설정(CI 환경 대응)
        if (StringUtils.hasText(password)) {
            config.setPassword(password);
        }

        return new LettuceConnectionFactory(config);
    }

    /**
     * ObjectMapper 설정
     * - Java 객체 ↔ JSON 변환용
     * - LocalDateTime 등 Java 8 시간 타입 지원
     */
    @Bean
    public ObjectMapper redisObjectMapper() {

        ObjectMapper mapper = new ObjectMapper();
        // LocalDateTime, LocalDate 등 지원
        mapper.registerModule(new JavaTimeModule());
        // ISO-8601 형식으로 날짜 직렬화
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return mapper;
    }

    /**
     * RedisTemplate 설정
     * - Redis에 데이터 직접 저장/조회할 때 사용
     * - 예: redisTemplate.opsForValue().set("key", value)
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate() {

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());

        // 직렬화 설정
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(redisObjectMapper());

        // Key는 String으로, Value는 JSON으로 직렬화
        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(jsonSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();

        return template;
    }

    /**
     * CacheManager 설정
     * - @Cacheable, @CachePut, @CacheEvict 어노테이션 사용 시 동작
     * - 캐시별로 다른 TTL 설정 가능
     */
    @Bean
    public CacheManager cacheManager() {
        // 기본 캐시 설정
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))  // 기본 TTL 1시간
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer(redisObjectMapper())
                        )
                );

        // 캐시별 개별 설정(예시)
        return RedisCacheManager.builder(redisConnectionFactory())
                .cacheDefaults(defaultConfig)
                // 특정 캐시는 다른 TTL 적용 가능
                .withCacheConfiguration("clothesCache",
                        RedisCacheConfiguration.defaultCacheConfig()
                                // 옷 정보는 30분 캐싱
                                .entryTtl(Duration.ofMinutes(30))
                )
                .withCacheConfiguration("userCache",
                        RedisCacheConfiguration.defaultCacheConfig()
                                // 사용자 정보는 10분 캐싱
                                .entryTtl(Duration.ofMinutes(10))
                )
                .build();
    }
}