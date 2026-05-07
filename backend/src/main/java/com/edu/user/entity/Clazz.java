package com.edu.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("class")
public class Clazz {
    private Long id;
    private Long gradeId;
    private String name;
    private Long teacherId;
    private Integer studentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
