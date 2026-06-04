package com.edu.ai.service;

import com.edu.ai.client.ClaudeAPIClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

/**
 * API 成本监控服务
 * 跟踪 API 调用成本，支持每日/每月统计
 */
@Slf4j
@Service
public class CostMonitoringService {

    private final ClaudeAPIClient claudeAPIClient;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${ai.cost.input-price:3.0}")  // $3 per 1M input tokens
    private double inputPricePerMillion;

    @Value("${ai.cost.output-price:15.0}")  // $15 per 1M output tokens
    private double outputPricePerMillion;

    @Value("${ai.cost.daily-limit:50.0}")  // $50 daily limit
    private double dailyLimit;

    private static final String DAILY_COST_KEY = "ai:cost:daily:";
    private static final String MONTHLY_COST_KEY = "ai:cost:monthly:";

    // 内存备份（当 Redis 不可用）
    private final AtomicLong dailyCostBackup = new AtomicLong(0);
    private final AtomicLong monthlyCostBackup = new AtomicLong(0);

    public CostMonitoringService(ClaudeAPIClient claudeAPIClient,
                                 RedisTemplate<String, String> redisTemplate) {
        this.claudeAPIClient = claudeAPIClient;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 计算最近一次 API 调用成本（区分输入/输出 token）
     * @return 估算成本（美元）
     */
    public double calculateLastCallCost() {
        long totalTokens = claudeAPIClient.getTokenCount();
        // 简化估算：假设输入占 60%，输出占 40%（Claude API 典型比例）
        long inputTokens = (long) (totalTokens * 0.6);
        long outputTokens = totalTokens - inputTokens;

        double estimatedCost = (inputTokens / 1_000_000.0) * inputPricePerMillion
                + (outputTokens / 1_000_000.0) * outputPricePerMillion;
        return estimatedCost;
    }

    /**
     * 记录 API 调用成本
     * @param estimatedCost 估算成本
     */
    public void trackCost(double estimatedCost) {
        String dailyKey = DAILY_COST_KEY + getCurrentDate();
        String monthlyKey = MONTHLY_COST_KEY + getCurrentMonth();

        try {
            // 累加到 Redis
            redisTemplate.opsForValue().increment(dailyKey, (long) (estimatedCost * 100));  // 存储为分（避免小数）
            redisTemplate.opsForValue().increment(monthlyKey, (long) (estimatedCost * 100));
            redisTemplate.expire(dailyKey, 24 * 60 * 60, java.util.concurrent.TimeUnit.SECONDS);  // 24 小时过期

            // 检查是否超过每日限额
            String dailyCostStr = redisTemplate.opsForValue().get(dailyKey);
            if (dailyCostStr != null && Long.parseLong(dailyCostStr) / 100.0 > dailyLimit) {
                log.warn("⚠️ 每日 API 成本限额已超：{}/{}", Long.parseLong(dailyCostStr) / 100.0, dailyLimit);
            }

        } catch (Exception e) {
            // Redis 不可用，使用内存备份
            log.warn("Redis 不可用，使用内存备份记录成本：{}", e.getMessage());
            dailyCostBackup.addAndGet((long) (estimatedCost * 100));
            monthlyCostBackup.addAndGet((long) (estimatedCost * 100));
        }

        log.info("API 调用成本：estimated={} USD, daily-total={} USD",
                String.format("%.4f", estimatedCost),
                String.format("%.2f", getDailyCost()));
    }

    /**
     * 获取今日总成本
     */
    public double getDailyCost() {
        try {
            String dailyKey = DAILY_COST_KEY + getCurrentDate();
            String costStr = redisTemplate.opsForValue().get(dailyKey);
            if (costStr != null) {
                return Long.parseLong(costStr) / 100.0;
            }
        } catch (Exception e) {
            log.warn("Redis 不可用，返回内存备份：{}", e.getMessage());
        }
        return dailyCostBackup.get() / 100.0;
    }

    /**
     * 获取本月总成本
     */
    public double getMonthlyCost() {
        try {
            String monthlyKey = MONTHLY_COST_KEY + getCurrentMonth();
            String costStr = redisTemplate.opsForValue().get(monthlyKey);
            if (costStr != null) {
                return Long.parseLong(costStr) / 100.0;
            }
        } catch (Exception e) {
            log.warn("Redis 不可用，返回内存备份：{}", e.getMessage());
        }
        return monthlyCostBackup.get() / 100.0;
    }

    /**
     * 获取调用统计
     */
    public String getCostReport() {
        long callCount = claudeAPIClient.getCallCount();
        double dailyCost = getDailyCost();
        double monthlyCost = getMonthlyCost();
        double avgCostPerCall = callCount > 0 ? monthlyCost / callCount : 0;

        return String.format(
                "API 成本报告：\n  调用次数：%d\n  今日成本：$%.2f\n  本月成本：$%.2f\n  平均每次：$%.4f",
                callCount, dailyCost, monthlyCost, avgCostPerCall
        );
    }

    /**
     * 重置今日成本（测试用）
     */
    public void resetDailyCost() {
        try {
            String dailyKey = DAILY_COST_KEY + getCurrentDate();
            redisTemplate.delete(dailyKey);
            dailyCostBackup.set(0);
            log.info("重置今日成本");
        } catch (Exception e) {
            log.warn("重置失败：{}", e.getMessage());
        }
    }

    private String getCurrentDate() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
    }

    private String getCurrentMonth() {
        return new java.text.SimpleDateFormat("yyyy-MM").format(new java.util.Date());
    }
}
