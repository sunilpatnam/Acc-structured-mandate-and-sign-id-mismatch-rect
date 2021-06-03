package com.fragma.dto;

public class ReportDTO {
    private int totalCases;
    private int totalFailedCases;
    private long failurePercentage;
    private String dateToDisplayInReport;

    public String getDateToDisplayInReport() {
        return dateToDisplayInReport;
    }

    public void setDateToDisplayInReport(String dateToDisplayInReport) {
        this.dateToDisplayInReport = dateToDisplayInReport;
    }

    public long getFailurePercentage() {
        return failurePercentage;
    }

    public void setFailurePercentage(long failurePercentage) {
        this.failurePercentage = failurePercentage;
    }

    public int getTotalCases() {
        return totalCases;
    }

    public void setTotalCases(int totalCases) {
        this.totalCases = totalCases;
    }

    public int getTotalFailedCases() {
        return totalFailedCases;
    }

    public void setTotalFailedCases(int totalFailedCases) {
        this.totalFailedCases = totalFailedCases;
    }
}
