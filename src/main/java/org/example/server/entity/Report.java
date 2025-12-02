package org.example.server.entity;

public class Report {
    
    private Integer id;
    private Integer patient_id;
    private Integer doctor_id;
    private String title;
    private String content;
    private String date;

    public Report() {
    }

    public Report(Integer id, Integer patient_id, Integer doctor_id, String title, String content, String date) {
        this.id = id;
        this.patient_id = patient_id;
        this.doctor_id = doctor_id;
        this.title = title;
        this.content = content;
        this.date = date;

    }
    
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPatient_id() {
        return patient_id;
    }

    public void setPatient_id(Integer patient_id) {
        this.patient_id = patient_id;
    }

    public Integer getDoctor_id() {
        return doctor_id;
    }

    public void setDoctor_id(Integer doctor_id) {
        this.doctor_id = doctor_id;
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "Report [id=" + id + ", patient_id=" + patient_id + ", doctor_id=" + doctor_id + ", title=" + title
                + ", content=" + content + ", date=" + date + "]";
    }
}
