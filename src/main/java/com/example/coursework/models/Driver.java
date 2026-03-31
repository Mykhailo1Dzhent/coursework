package com.example.coursework.models;

import java.time.LocalDate;

public class Driver extends User {
    private String name;
    private String surname;
    private String email;
    private String phone;
    private String vehicle;
    private String licenseId;
    private LocalDate birthdate;

    public Driver(int id, String username, String password,
                  String name, String surname, String email,
                  String phone, String vehicle, String licenseId,
                  LocalDate birthdate) {
        super(id, username, password, "DRIVER");
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.phone = phone;
        this.vehicle = vehicle;
        this.licenseId = licenseId;
        this.birthdate = birthdate;
    }

    public String getName()        { return name; }
    public String getSurname()     { return surname; }
    public String getEmail()       { return email; }
    public String getPhone()       { return phone; }
    public String getVehicle()     { return vehicle; }
    public String getLicenseId()   { return licenseId; }
    public LocalDate getBirthdate() { return birthdate; }

    public void setName(String name)           { this.name = name; }
    public void setSurname(String surname)     { this.surname = surname; }
    public void setEmail(String email)         { this.email = email; }
    public void setPhone(String phone)         { this.phone = phone; }
    public void setVehicle(String vehicle)     { this.vehicle = vehicle; }
    public void setLicenseId(String licenseId) { this.licenseId = licenseId; }
    public void setBirthdate(LocalDate birthdate) { this.birthdate = birthdate; }
}