package system;

import model.*;

import java.util.ArrayList;

public class Airport {
    private static Airport instance;

    private final ArrayList<Person> people;
    private final ArrayList<Flight> flights;

    private Airport() {
        this.people = new ArrayList<>();
        this.flights = new ArrayList<>();
    }

    public static Airport getInstance() {
        if (instance == null) {
            instance = new Airport();
        }
        return instance;
    }

    public void addPerson(Person person) {
        this.people.add(person);
    }

    public void addFlight(Flight flight) {
        this.flights.add(flight);
    }

    public ArrayList<Person> getPeople() {
        return new ArrayList<>(people);
    }

    public ArrayList<Flight> getFlights() {
        return new ArrayList<>(flights);
    }

    // Text version is useful for the GUI while keeping the original console report method.
    public String buildDailyReport() {
        StringBuilder report = new StringBuilder();

        report.append("========================================\n");
        report.append("   Gaza International Airport Report\n");
        report.append("========================================\n");
        report.append("Departed flights today: ")
                .append(Flight.getTotalFlightsToday()).append("\n");
        report.append("Registered people: ")
                .append(people.size()).append("\n");
        report.append("Total flights: ")
                .append(flights.size()).append("\n");
        report.append("----------------------------------------\n");

        for (Person p : people) {
            if (p instanceof Pilot) {
                Pilot pilot = (Pilot) p;
                report.append("PILOT\n");
                appendPersonInfo(report, pilot);
                report.append("Salary: $").append(pilot.getSalary()).append("\n");
                report.append("Shift: ").append(pilot.getShift()).append("\n");
                report.append("License: ").append(pilot.getLicense()).append("\n");
                report.append("Certificate: ").append(pilot.getCertificate()).append("\n");
                report.append("Flight Hours: ").append(pilot.getFlightHours()).append("\n");
                report.append("Classification: ")
                        .append(pilot.getFlightHours() > 1000
                                ? "Experienced Captain"
                                : "Captain Under Training")
                        .append("\n");

            } else if (p instanceof FlightAttendant) {
                FlightAttendant attendant = (FlightAttendant) p;
                report.append("FLIGHT ATTENDANT\n");
                appendPersonInfo(report, attendant);
                report.append("Salary: $").append(attendant.getSalary()).append("\n");
                report.append("Shift: ").append(attendant.getShift()).append("\n");
                report.append("Languages: ").append(attendant.getLanguages()).append("\n");
                report.append("Number of Flights: ").append(attendant.getNumberOfFlights()).append("\n");
                report.append("Classification: ")
                        .append(attendant.getLanguages().size() > 2
                                ? "Qualified for long international flights"
                                : "Qualified for regional flights")
                        .append("\n");

            } else if (p instanceof Passenger) {
                Passenger passenger = (Passenger) p;
                report.append("PASSENGER\n");
                appendPersonInfo(report, passenger);
                report.append("Passport: ").append(passenger.getPassportNumber()).append("\n");
                report.append("Destination: ").append(passenger.getDestination()).append("\n");
                report.append("Passport Expiry: ").append(passenger.getPassportExpiry()).append("\n");
                report.append("Document Status: ")
                        .append(passenger.getPassportExpiry().isBefore(java.time.LocalDate.now())
                                ? "Passport expired"
                                : "Passport valid for travel")
                        .append("\n");
            }

            report.append("----------------------------------------\n");
        }

        return report.toString();
    }

    private void appendPersonInfo(StringBuilder report, Person p) {
        report.append("System ID: ").append(p.getId()).append("\n");
        report.append("National ID: ").append(p.getNationalId()).append("\n");
        report.append("Name: ").append(p.getName()).append("\n");
        report.append("Nationality: ").append(p.getNationality()).append("\n");
    }

    public void generateDailyReport() {
        System.out.print(buildDailyReport());
    }
}
