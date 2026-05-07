package com.edu.exam.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.edu.common.exception.BusinessException;
import com.edu.common.util.TenantContextHolder;
import com.edu.exam.entity.ExamTemplate;
import com.edu.exam.mapper.ExamTemplateMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExamTemplateService extends ServiceImpl<ExamTemplateMapper, ExamTemplate> {

    @Transactional
    public ExamTemplate createTemplate(ExamTemplate template) {
        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("租户上下文缺失");
        }
        template.setTenantId(tenantId);
        baseMapper.insert(template);
        log.info("创建试卷模板: id={}, name={}", template.getId(), template.getName());
        return template;
    }

    public List<ExamTemplate> listByTenant() {
        Long tenantId = TenantContextHolder.getTenantId();
        LambdaQueryWrapper<ExamTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExamTemplate::getTenantId, tenantId)
                .orderByDesc(ExamTemplate::getCreatedAt);
        return baseMapper.selectList(wrapper);
    }

    public List<ExamTemplate> listBySubject(String subject) {
        Long tenantId = TenantContextHolder.getTenantId();
        LambdaQueryWrapper<ExamTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExamTemplate::getTenantId, tenantId)
                .eq(ExamTemplate::getSubject, subject)
                .orderByDesc(ExamTemplate::getCreatedAt);
        return baseMapper.selectList(wrapper);
    }

    public ExamTemplate getTemplateById(Long templateId) {
        return baseMapper.selectById(templateId);
    }

    @Transactional
    public void updateTemplate(ExamTemplate template) {
        baseMapper.updateById(template);
        log.info("更新试卷模板: id={}", template.getId());
    }

    @Transactional
    public void deleteTemplate(Long templateId) {
        baseMapper.deleteById(templateId);
        log.info("删除试卷模板: id={}", templateId);
    }
}
