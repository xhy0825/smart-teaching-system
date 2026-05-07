package com.edu.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("grade")
public class Grade {
    private Long id;
    private Long schoolId;
    private String name;
    private Integer level;  // 1-小学, 2-初中, 3-高中
    private Integer sequence;
    private LocalDateTime createdAt;
}
