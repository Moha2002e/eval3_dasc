package org.example.server.entity;

import java.time.OffsetDateTime;

public class Consultation implements Entity {
    private static final long serialVersionUID = 1L;
    private Integer id ;
    private Integer doctor_id ;
    private Integer patient_id ;
    private String date ;
    private String hour ;
    private String reason ;
    // Additional patient info for UI convenience
    private String patient_first_name;
    private String patient_last_name;
    private String patient_birth_date;

    public Consultation() {

    }
    public Consultation(Integer patient_id, Integer id, Integer doctor_id, String date, String reason, String hour) {
        this.patient_id = patient_id;
        this.id = id;
        this.doctor_id = doctor_id;
        this.date = date;
        this.reason = reason;
        this.hour = hour;
    }

    public Integer getId() {
        return id;
    }

    public Integer getDoctor_id() {
        return doctor_id;
    }

    public Integer getPatient_id() {
        return patient_id;
    }

    public String getDate() {
        return date;
    }

    public String getHour() {
        return hour;
    }

    public String getReason() {
        return reason;
    }

    public String getPatient_first_name() {
        return patient_first_name;
    }

    public String getPatient_last_name() {
        return patient_last_name;
    }

    public String getPatient_birth_date() {
        return patient_birth_date;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setDoctor_id(Integer doctor_id) {
        this.doctor_id = doctor_id;
    }

    public void setPatient_id(Integer patient_id) {
        this.patient_id = patient_id;
    }

    public void setPatient_first_name(String patient_first_name) {
        this.patient_first_name = patient_first_name;
    }

    public void setPatient_last_name(String patient_last_name) {
        this.patient_last_name = patient_last_name;
    }

    public void setPatient_birth_date(String patient_birth_date) {
        this.patient_birth_date = patient_birth_date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "Consultation{" +
                "id=" + id +
                ", doctor_id=" + doctor_id +
                ", patient_id=" + patient_id +
                ", date='" + date + '\'' +
                ", hour='" + hour + '\'' +
                ", reason='" + reason + '\'' +
                '}';
    }

    public OffsetDateTime getDateTime() {
        try {
            java.time.LocalDate ld = java.time.LocalDate.parse(this.date);
            java.time.LocalTime lt = this.hour == null || this.hour.isEmpty() ? java.time.LocalTime.MIDNIGHT : java.time.LocalTime.parse(this.hour);
            java.time.LocalDateTime ldt = java.time.LocalDateTime.of(ld, lt);
            java.time.ZoneOffset offset = java.time.ZoneId.systemDefault().getRules().getOffset(ldt);
            return OffsetDateTime.of(ldt, offset);
        } catch (Exception e) {
            // fallback: try parsing as full OffsetDateTime, or current moment
            try {
                return OffsetDateTime.parse(date);
            } catch (Exception ex) {
                return OffsetDateTime.now();
            }
        }
    }
}
