package com.opsbeach.connect.github.dto;

import java.util.Map;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class DashboardDto {
    
    Map<String, Object> openCloseGraph;
    Map<String, Object> slaMeanGraph;
    int openPrsCount;
    int openPrPercent;
    int slaMiss;
    int slaMissPercent;
    int closePrsCount;
    int closePrPercent;
    int totalPrsCount;
    int totalPrPercent;
}
