package com.example.coursework.models;

public class Admin extends User {
    private String name;
    private String surname;
    private String email;

    public Admin(int id, String username, String password,
                 String name, String surname, String email) {
        super(id, username, password, "ADMIN");
        this.name = name;
        this.surname = surname;
        this.email = email;
    }

    public String getName()     { return name; }
    public String getSurname()  { return surname; }
    public String getEmail()    { return email; }

    public void setName(String name)       { this.name = name; }
    public void setSurname(String surname) { this.surname = surname; }
    public void setEmail(String email)     { this.email = email; }
}
