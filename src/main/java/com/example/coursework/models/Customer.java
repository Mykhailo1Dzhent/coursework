package com.example.coursework.models;

import java.time.LocalDate;

public class Customer extends User {
    private String name;
    private String surname;
    private String email;
    private String phone;
    private String cardNo;
    private int bonusPoints;
    private String address;
    private LocalDate birthdate;

    public Customer(int id, String username, String password,
                    String name, String surname, String email,
                    String phone, String cardNo, int bonusPoints,
                    String address, LocalDate birthdate) {
        super(id, username, password, "CUSTOMER");
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.phone = phone;
        this.cardNo = cardNo;
        this.bonusPoints = bonusPoints;
        this.address = address;
        this.birthdate = birthdate;
    }

    public String getName()        { return name; }
    public String getSurname()     { return surname; }
    public String getEmail()       { return email; }
    public String getPhone()       { return phone; }
    public String getCardNo()      { return cardNo; }
    public int getBonusPoints()    { return bonusPoints; }
    public String getAddress()     { return address; }
    public LocalDate getBirthdate() { return birthdate; }

    public void setName(String name)           { this.name = name; }
    public void setSurname(String surname)     { this.surname = surname; }
    public void setEmail(String email)         { this.email = email; }
    public void setPhone(String phone)         { this.phone = phone; }
    public void setCardNo(String cardNo)       { this.cardNo = cardNo; }
    public void setBonusPoints(int points)     { this.bonusPoints = points; }
    public void setAddress(String address)     { this.address = address; }
    public void setBirthdate(LocalDate birthdate) { this.birthdate = birthdate; }
}