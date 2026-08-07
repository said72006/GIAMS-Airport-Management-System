package model;

import java.util.ArrayList;

public class FlightAttendant extends Employee {
    private ArrayList<String> languages;
    private int numberOfFlights;

    public FlightAttendant(String name, String nationalId, String nationality, double salary, Shift shift, ArrayList<String> languages, int numberOfFlights) {
        super(name, nationalId, nationality, salary, shift);
        this.languages = languages;
        this.numberOfFlights = numberOfFlights;
    }

    public FlightAttendant(String name, String nationalId, double salary, Shift shift, ArrayList<String> languages, int numberOfFlights) {
        this(name, nationalId, "Palestinian", salary, shift, languages, numberOfFlights);
    }

    // Getters and Setters
    public ArrayList<String> getLanguages() {
        return languages;
    }

    public void setLanguages(ArrayList<String> languages) {
        this.languages = languages;
    }

    public int getNumberOfFlights() {
        return numberOfFlights;
    }

    public void setNumberOfFlights(int numberOfFlights) {
        if (numberOfFlights >= 0)
            this.numberOfFlights = numberOfFlights;
        else
            this.numberOfFlights = 0;
    }

    // الـ Override النهائي لطباعة معلومات المضيف كاملة (R4)
    @Override
    public void displayInfo() {
        System.out.println("=== Flight Attendant Info ===");
        super.displayInfo(); // يستدعي Employee المتصل بـ Person ليطبع كل البيانات الأساسية والوظيفية
        System.out.println("Languages: " + languages);
        System.out.println("Number of Flights: " + numberOfFlights);
    }
}