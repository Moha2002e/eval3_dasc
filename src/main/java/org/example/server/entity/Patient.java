package org.example.server.entity;

import java.io.Serializable;

public class Patient implements Entity {
    private static final long serialVersionUID = 1L;
    private Integer id ;
    private String last_name ;
    private String first_name ;
    private String birth_date ;


    public Patient(){

    }
    public Patient(Integer id, String last_name, String first_name, String birth_date) {
        this.id = id;
        this.last_name = last_name;
        this.first_name = first_name;
        this.birth_date = birth_date;
    }
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public String getLast_name() {
        return last_name;
    }
    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }
    public String getFirst_name() {
        return first_name;
    }
    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }
    public String getBirth_date() {
        return birth_date;
    }
    public void setBirth_date(String birth_date) {
        this.birth_date = birth_date;
    }
    @Override
    public String toString() {
        // Pour l’affichage dans les listes, retourner simplement le nom complet
        String ln = last_name != null ? last_name : "";
        String fn = first_name != null ? first_name : "";
        return ln + (fn.isBlank() ? "" : (" " + fn));
    }
}

