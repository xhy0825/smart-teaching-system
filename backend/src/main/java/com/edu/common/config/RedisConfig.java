package com.edu.common.config;

// Redis 配置类（需要 Redis 环境时启用）
// 当前 H2 模式不启用 Redis
// 
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.data.redis.connection.RedisConnectionFactory;
// import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
// import org.springframework.data.redis.core.RedisTemplate;
// import org.springframework.data.redis.serializer.StringRedisSerializer;
//
// @Configuration
// public class RedisConfig {
//
//     @Bean
//     public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
//         RedisTemplate<String, String> template = new RedisTemplate<>();
//         template.setConnectionFactory(connectionFactory);
//         template.setKeySerializer(new StringRedisSerializer());
//         template.setValueSerializer(new StringRedisSerializer());
//         return template;
//     }
// }
