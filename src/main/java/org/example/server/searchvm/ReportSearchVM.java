package org.example.server.searchvm;

import java.io.Serializable;

public class ReportSearchVM implements Serializable {
    private static final long serialVersionUID = 1L;
    private String patientName;
    private String doctorName;
    private String title;
    private String content;
    private String dateFrom;
    private String dateTo;

    public ReportSearchVM() {
    }

    public ReportSearchVM(String patientName, String doctorName, String title, String content, String dateFrom,
            String dateTo) {
        this.patientName = patientName;
        this.doctorName = doctorName;
        this.title = title;
        this.content = content;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(String dateFrom) {
        this.dateFrom = dateFrom;
    }

    public String getDateTo() {
        return dateTo;
    }

    public void setDateTo(String dateTo) {
        this.dateTo = dateTo;
    }
}
