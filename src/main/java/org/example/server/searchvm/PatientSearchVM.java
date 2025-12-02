package org.example.server.searchvm;

import java.io.Serializable;

public class PatientSearchVM implements Serializable {
    private static final long serialVersionUID = 1L;
    private String lastName;
    private String firstName;
    private String birthDateFrom;
    private String birthDateTo;
    private Integer doctorId;

    public PatientSearchVM() {
    }

    public PatientSearchVM(String lastName, String firstName, String birthDateFrom, String birthDateTo,
            Integer doctorId) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.birthDateFrom = birthDateFrom;
        this.birthDateTo = birthDateTo;
        this.doctorId = doctorId;
    }

    // Getters and Setters
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getBirthDateFrom() {
        return birthDateFrom;
    }

    public void setBirthDateFrom(String birthDateFrom) {
        this.birthDateFrom = birthDateFrom;
    }

    public String getBirthDateTo() {
        return birthDateTo;
    }

    public void setBirthDateTo(String birthDateTo) {
        this.birthDateTo = birthDateTo;
    }

    public Integer getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Integer doctorId) {
        this.doctorId = doctorId;
    }
}
