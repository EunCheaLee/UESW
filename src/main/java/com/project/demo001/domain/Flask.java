package com.project.demo001.domain;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Flask {

    private List<String> labels;
    private List<Integer> 사고건수;
    private List<Integer> 사망자수;
    private List<Integer> 부상자수;
    
}
