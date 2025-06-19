package com.project.demo001.domain;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Flask {

    // 실시간 지하철 정보용 - HTML과 일치하도록 변수명 수정
    private String trainLineNm;
    private String updnLine;
    private String arvlMsg2;
    private int remain_seconds;     // ✅ snake_case로
    private String remain_time;     // ✅ snake_case로
    private String line_name;       // ✅ snake_case로
   
    @Data
    @AllArgsConstructor
    public static class StationData {
        private String station;
        private List<Flask> arrivals;
    }
}
