package com.edu.ppt.dto;

import lombok.Data;

import java.util.List;

@Data
public class PptSlideResponse {
    private Integer pageIndex;
    private String title;
    private String content;
    private List<Long> questionIds;
}