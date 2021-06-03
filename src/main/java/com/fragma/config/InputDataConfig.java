package com.fragma.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "input")
public class InputDataConfig {
    private String queryForExcelSheetData;
    private String sheetName;
    private String queryForTotalCases;
    private String queryForTotalFailedCases;

/*
    @Value("name.query")
    private Map<String,String> map=new LinkedHashMap<>();*/


    public String getQueryForExcelSheetData() {
        return queryForExcelSheetData;
    }

    public void setQueryForExcelSheetData(String queryForExcelSheetData) {
        this.queryForExcelSheetData = queryForExcelSheetData;
    }

    public String getSheetName() {
        return sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    public String getQueryForTotalCases() {
        return queryForTotalCases;
    }

    public void setQueryForTotalCases(String queryForTotalCases) {
        this.queryForTotalCases = queryForTotalCases;
    }

    public String getQueryForTotalFailedCases() {
        return queryForTotalFailedCases;
    }

    public void setQueryForTotalFailedCases(String queryForTotalFailedCases) {
        this.queryForTotalFailedCases = queryForTotalFailedCases;
    }
}
