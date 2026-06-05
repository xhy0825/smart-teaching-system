package com.edu.ai.service;

import com.edu.ai.provider.AIProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

/**
 * API 成本监控服务
 * 跟踪 API 调用成本，支持每日/每月统计
 * 使用内存存储（适用于 H2 无 Redis 环境）
 */
@Slf4j
@Service
public class CostMonitoringService {

    private final AIProvider provider;
    private final RedisTemplate<String, String> redisTemplate;

    // 内存成本统计（H2 环境无 Redis 时的备份）
    private final AtomicLong dailyCostBackup = new AtomicLong(0);
    private final AtomicLong monthlyCostBackup = new AtomicLong(0);

    // Claude API 价格（每百万 tokens）
    private static final double INPUT_PRICE_PER_MILLION = 3.0;
    private static final double OUTPUT_PRICE_PER_MILLION = 15.0;

    // 日期格式化
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");

    public CostMonitoringService(AIProvider provider,
                                 RedisTemplate<String, String> redisTemplate) {
        this.provider = provider;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 计算最近一次 API 调用成本
     */
    public double calculateLastCallCost() {
        long totalTokens = provider.getTokenCount();
        // 简化估算：假设输入占 60%，输出占 40%（Claude API 典型比例）
        long inputTokens = (long) (totalTokens * 0.6);
        long outputTokens = totalTokens - inputTokens;

        double estimatedCost = (inputTokens / 1_000_000.0) * INPUT_PRICE_PER_MILLION
                + (outputTokens / 1_000_000.0) * OUTPUT_PRICE_PER_MILLION;
        return estimatedCost;
    }

    /**
     * 记录 API 调用成本
     */
    public void trackCost(double estimatedCost) {
        long cents = (long) (estimatedCost * 100);
        dailyCostBackup.addAndGet(cents);
        monthlyCostBackup.addAndGet(cents);

        log.info("API 调用成本：estimated={} USD, daily-total={} USD",
                String.format("%.4f", estimatedCost),
                String.format("%.2f", getDailyCost()));
    }

    /**
     * 获取今日总成本
     */
    public double getDailyCost() {
        return dailyCostBackup.get() / 100.0;
    }

    /**
     * 获取本月总成本
     */
    public double getMonthlyCost() {
        return monthlyCostBackup.get() / 100.0;
    }

    /**
     * 获取调用统计
     */
    public String getCostReport() {
        long callCount = provider.getCallCount();
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
        dailyCostBackup.set(0);
        log.info("重置今日成本");
    }
}
