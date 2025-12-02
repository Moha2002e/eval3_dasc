package org.example.server.searchvm;

import java.io.Serializable;

/*
Consultation = ce que tu stockeras en base. C’est ta vraie donnée.
ConsultationSearchVM = ce que tu utilises pour rechercher.*/

public class ConsultationSearchVM implements Serializable {
    private static final long serialVersionUID = 1L;
    private String patientName;
    private String doctorName;
    private String specialityName;
    private String dateFrom;
    private String dateTo;
    private String reason;

    public ConsultationSearchVM() {
    }

    public ConsultationSearchVM(String patientName, String doctorName, String specialityName,
                                String dateFrom, String dateTo, String reason) {
        this.patientName = patientName;
        this.doctorName = doctorName;
        this.specialityName = specialityName;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.reason = reason;
    }

    // Getters and Setters
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

    public String getSpecialityName() {
        return specialityName;
    }

    public void setSpecialityName(String specialityName) {
        this.specialityName = specialityName;
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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}