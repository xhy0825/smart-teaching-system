package com.edu.exam.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.edu.common.exception.BusinessException;
import com.edu.common.util.TenantContextHolder;
import com.edu.exam.entity.QuestionBank;
import com.edu.exam.mapper.QuestionBankMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionBankService extends ServiceImpl<QuestionBankMapper, QuestionBank> {

    @Transactional
    public QuestionBank createBank(QuestionBank bank) {
        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("租户上下文缺失");
        }
        bank.setTenantId(tenantId);
        bank.setIsPublic(0);
        baseMapper.insert(bank);
        log.info("创建题库: id={}, name={}", bank.getId(), bank.getName());
        return bank;
    }

    public List<QuestionBank> listByTenant() {
        Long tenantId = TenantContextHolder.getTenantId();
        LambdaQueryWrapper<QuestionBank> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuestionBank::getTenantId, tenantId)
                .or().eq(QuestionBank::getIsPublic, 1)
                .orderByDesc(QuestionBank::getCreatedAt);
        return baseMapper.selectList(wrapper);
    }

    public List<QuestionBank> listBySubject(String subject) {
        Long tenantId = TenantContextHolder.getTenantId();
        LambdaQueryWrapper<QuestionBank> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuestionBank::getTenantId, tenantId)
                .eq(QuestionBank::getSubject, subject)
                .or().eq(QuestionBank::getIsPublic, 1)
                        .eq(QuestionBank::getSubject, subject)
                .orderByDesc(QuestionBank::getCreatedAt);
        return baseMapper.selectList(wrapper);
    }

    public QuestionBank getBankById(Long bankId) {
        return baseMapper.selectById(bankId);
    }

    @Transactional
    public void updateBank(QuestionBank bank) {
        baseMapper.updateById(bank);
        log.info("更新题库: id={}", bank.getId());
    }

    @Transactional
    public void deleteBank(Long bankId) {
        baseMapper.deleteById(bankId);
        log.info("删除题库: id={}", bankId);
    }
}
