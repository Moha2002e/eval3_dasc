package org.example.server.searchvm;


import java.io.Serializable;

public class DoctorSearchVM implements Serializable {
    private static final long serialVersionUID = 1L;
    private String lastName;
    private String firstName;
    private String specialityName;

    public DoctorSearchVM() {
    }

    public DoctorSearchVM(String lastName, String firstName, String specialityName) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.specialityName = specialityName;
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

    public String getSpecialityName() {
        return specialityName;
    }

    public void setSpecialityName(String specialityName) {
        this.specialityName = specialityName;
    }
}