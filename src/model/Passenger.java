package model;

import java.time.LocalDate;

public class Passenger extends Person {
    private String passportNumber;
    private String destination;
    private LocalDate passportExpiry;

    // Constructor الكامل - يستدعي الـ super الخاص بالأب بعد إضافة الـ nationalId
    public Passenger(String name, String nationalId, String nationality, String passportNumber, String destination, LocalDate passportExpiry) {
        super(name, nationalId, nationality);
        this.passportNumber = passportNumber;
        this.destination = destination;
        this.passportExpiry = passportExpiry;
    }

    // Constructor مختصر (Overloaded) - يتسلسل للكامل باستخدام this()
    public Passenger(String name, String nationalId, String passportNumber, String destination, LocalDate passportExpiry) {
        this(name, nationalId, "Palestinian", passportNumber, destination, passportExpiry);
    }

    // Getters and Setters
    public String getPassportNumber() {
        return passportNumber;
    }

    public void setPassportNumber(String passportNumber) {
        this.passportNumber = passportNumber;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public LocalDate getPassportExpiry() {
        return passportExpiry;
    }

    public void setPassportExpiry(LocalDate passportExpiry) {
        this.passportExpiry = passportExpiry;
    }

    // Overriding كامل مع استدعاء super.methodName()
    @Override
    public void displayInfo() {
        System.out.println("=== Passenger Info ===");
        super.displayInfo(); // طباعة الاسم، المعرف، الهوية، والجنسية من الأب
        System.out.println("Passport Number: " + passportNumber);
        System.out.println("Destination: " + destination);
        System.out.println("Passport Expiry: " + passportExpiry);
    }
}