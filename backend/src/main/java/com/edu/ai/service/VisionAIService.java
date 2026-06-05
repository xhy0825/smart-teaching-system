package com.edu.ai.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.edu.ai.provider.AIProvider;
import com.edu.ai.dto.GradingRequest;
import com.edu.ai.dto.GradingResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Base64;

/**
 * 拍照批改服务（Vision API）
 * 使用 AI 多模态能力识别手写答案并批改
 */
@Slf4j
@Service
public class VisionAIService {

    private final AIProvider provider;

    @Value("${ai.vision.accuracy-threshold:0.85}")
    private double accuracyThreshold;

    public VisionAIService(@Qualifier("cloudAIProvider") AIProvider provider) {
        this.provider = provider;
    }

    /**
     * 批改拍照上传的答案
     * @param request 批改请求（包含图片路径或Base64）
     * @return 批改结果
     */
    public GradingResponse gradePhoto(GradingRequest request) {
        GradingResponse response = new GradingResponse();
        response.setSuccess(false);

        try {
            // 读取图片并转换为 Base64
            String imageBase64 = loadAndEncodeImage(request.getImagePath());
            if (imageBase64 == null || imageBase64.isEmpty()) {
                response.setErrorMessage("图片读取失败");
                return response;
            }

            // 构建 Vision API 提示词
            String prompt = buildVisionGradingPrompt(request);

            // 调用 AI Provider（支持 Vision）
            String result = provider.chatWithVision(prompt, imageBase64);

            // 解析结果
            parseVisionGradingResult(result, response);
            response.setSuccess(true);

            log.info("拍照批改成功: score={}, accuracy={}", response.getScore(), response.getAccuracy());

        } catch (Exception e) {
            log.error("拍照批改失败: {}", e.getMessage());
            response.setErrorMessage(e.getMessage());
        }

        return response;
    }

    /**
     * 加载图片并编码为 Base64
     * 安全：验证路径在允许目录内，防止目录遍历攻击
     */
    private String loadAndEncodeImage(String imagePath) {
        try {
            // 安全验证：检查路径遍历
            java.io.File file = new java.io.File(imagePath);
            String canonicalPath = file.getCanonicalPath();
            String uploadDir = new java.io.File("uploads").getCanonicalPath();

            if (!canonicalPath.startsWith(uploadDir)) {
                log.warn("非法路径（目录遍历攻击）：{}", imagePath);
                return null;
            }

            if (!file.exists()) {
                log.warn("图片文件不存在: {}", imagePath);
                return null;
            }

            byte[] fileContent = java.nio.file.Files.readAllBytes(file.toPath());
            return Base64.getEncoder().encodeToString(fileContent);

        } catch (Exception e) {
            log.error("图片编码失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 构建 Vision 批改提示词
     */
    private String buildVisionGradingPrompt(GradingRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请批改以下手写答案：\n\n");
        prompt.append("题目：").append(request.getQuestionContent()).append("\n");
        prompt.append("标准答案：").append(request.getCorrectAnswer()).append("\n");
        prompt.append("满分：").append(request.getMaxScore()).append("\n\n");

        prompt.append("请仔细识别图片中的手写答案，并按以下JSON格式返回批改结果：\n");
        prompt.append("{\"score\":得分数字,\"isCorrect\":0或1或2,\"accuracy\":识别准确率0-1,\"analysis\":\"批改分析\"}\n");
        prompt.append("isCorrect: 0表示错误，1表示正确，2表示部分正确。\n");
        prompt.append("accuracy: 0-1之间的小数，表示手写识别的准确率。\n");
        prompt.append("只返回JSON，不要有其他文字。");

        return prompt.toString();
    }

    /**
     * 解析 AI Vision 返回结果
     */
    private void parseVisionGradingResult(String result, GradingResponse response) {
        try {
            JSONObject obj = JSON.parseObject(result);
            response.setScore(obj.getBigDecimal("score"));
            response.setIsCorrect(obj.getInteger("isCorrect"));

            // 识别准确率
            Double accuracy = obj.getDouble("accuracy");
            if (accuracy != null) {
                response.setAccuracy(accuracy);
                // 准确率低于阈值标记需复核
                if (accuracy < accuracyThreshold) {
                    response.setNeedReview(true);
                    log.warn("识别准确率低于阈值: accuracy={}, threshold={}", accuracy, accuracyThreshold);
                }
            }

            response.setAnalysis(obj.getString("analysis"));

        } catch (Exception e) {
            log.warn("解析拍照批改结果失败: {}", e.getMessage());
            response.setScore(new java.math.BigDecimal("0"));
            response.setIsCorrect(0);
            response.setAccuracy(0.0);
        }
    }

    /**
     * 获取识别准确率阈值
     */
    public double getAccuracyThreshold() {
        return accuracyThreshold;
    }
}
