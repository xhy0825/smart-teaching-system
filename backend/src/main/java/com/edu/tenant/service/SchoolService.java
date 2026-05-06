package com.edu.tenant.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.edu.common.exception.BusinessException;
import com.edu.tenant.entity.School;
import com.edu.tenant.mapper.SchoolMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchoolService extends ServiceImpl<SchoolMapper, School> {

    @Transactional
    public School createSchool(School school) {
        LambdaQueryWrapper<School> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(School::getTenantId, school.getTenantId())
                .eq(School::getName, school.getName());
        if (baseMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("学校名称已存在");
        }
        baseMapper.insert(school);
        log.info("创建学校成功: id={}, tenantId={}", school.getId(), school.getTenantId());
        return school;
    }

    public List<School> listByTenant(Long tenantId) {
        LambdaQueryWrapper<School> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(School::getTenantId, tenantId)
                .orderByDesc(School::getCreatedAt);
        return baseMapper.selectList(wrapper);
    }

    public School getDefaultSchool(Long tenantId) {
        List<School> schools = listByTenant(tenantId);
        if (schools.isEmpty()) {
            return null;
        }
        return schools.get(0);
    }
}
