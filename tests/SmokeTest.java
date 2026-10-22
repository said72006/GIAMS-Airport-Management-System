import model.*;
import system.Airport;

import java.time.LocalDate;
import java.util.ArrayList;

public class SmokeTest {
    private static void check(boolean condition, String explanation) {
        if (!condition) throw new AssertionError(explanation);
    }

    public static void main(String[] args) throws Exception {
        ArrayList<Passenger> external = new ArrayList<>();
        Flight flight = new Flight("Gaza", null, 2, external);
        external.add(new Passenger("Other", "ID1", "P001", "Gaza", LocalDate.now().plusYears(2)));
        check(flight.getPassengers().isEmpty(), "Flight must copy original passenger list");
        flight.getPassengers().add(new Passenger("Bypass", "ID-BYPASS", "PB",
                "Gaza", LocalDate.now().plusYears(2)));
        check(flight.getPassengers().isEmpty(), "Getter must not expose internal passenger list");

        Passenger alice = new Passenger("Alice", "ID2", "P002", "Gaza", LocalDate.now().plusYears(2));
        flight.bookPassenger(alice);
        try {
            flight.bookPassenger(alice);
            throw new AssertionError("Duplicate passenger must fail");
        } catch (exceptions.DuplicatePassengerException expected) { }
        try {
            flight.setCapacity(0);
            throw new AssertionError("Zero capacity must fail");
        } catch (IllegalArgumentException expected) { }

        try {
            new Flight("Invalid", null, 0, null);
            throw new AssertionError("Nonpositive capacity must fail");
        } catch (IllegalArgumentException expected) { }

        flight.changeStatus(FlightStatus.BOARDING);
        flight.changeStatus(FlightStatus.DEPARTED);
        try {
            flight.changeStatus(FlightStatus.SCHEDULED);
            throw new AssertionError("Cannot reverse departed status");
        } catch (exceptions.InvalidFlightStatusException expected) { }

        Airport airport = Airport.getInstance();
        airport.getPeople().add(new Passenger("Bypass", "ID-OTHER", "PX",
                "Gaza", LocalDate.now().plusYears(1)));
        check(airport.getPeople().isEmpty(), "People getter must not expose mutable internals");
        airport.getFlights().add(flight);
        check(airport.getFlights().isEmpty(), "Flights getter must not expose mutable internals");
        airport.addPerson(new Passenger("Expired", "TEST3", "EXPIRED",
                "Gaza", LocalDate.now().minusDays(1)));
        airport.addPerson(new Passenger("Valid", "TEST4", "VALID",
                "Gaza", LocalDate.now().plusYears(1)));
        String report = airport.buildDailyReport();
        check(report.contains("Passport expired"), "Past expiry must be detected");
        check(report.contains("Passport valid for travel"), "Future expiry must be valid");
        System.out.println("Smoke tests passed");
    }
}
