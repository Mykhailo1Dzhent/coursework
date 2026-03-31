package com.example.coursework.models;

import java.time.LocalDate;

public class RestaurantOwner extends User {
    private String name;
    private String surname;
    private String email;
    private String phone;
    private LocalDate birthdate;

    public RestaurantOwner(int id, String username, String password,
                           String name, String surname, String email,
                           String phone, LocalDate birthdate) {
        super(id, username, password, "RESTAURANT");
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.phone = phone;
        this.birthdate = birthdate;
    }

    public String getName()        { return name; }
    public String getSurname()     { return surname; }
    public String getEmail()       { return email; }
    public String getPhone()       { return phone; }
    public LocalDate getBirthdate() { return birthdate; }

    public void setName(String name)           { this.name = name; }
    public void setSurname(String surname)     { this.surname = surname; }
    public void setEmail(String email)         { this.email = email; }
    public void setPhone(String phone)         { this.phone = phone; }
    public void setBirthdate(LocalDate birthdate) { this.birthdate = birthdate; }
}