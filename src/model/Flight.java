package model;

import exceptions.DuplicatePassengerException;
import exceptions.FlightFullException;
import exceptions.InvalidFlightStatusException;

import java.util.ArrayList;

public class Flight {

    private static int idCounter = 1;
    private static int totalFlightsToday = 0;

    private int id;
    private String destination;
    private Pilot pilot;
    private int capacity;
    private FlightStatus flightStatus;
    private ArrayList<Passenger> passengers;


    // Constructor للرحلات الجديدة
    public Flight(String destination, Pilot pilot, int capacity, ArrayList<Passenger> passengers) {

        this.id = idCounter++;
        this.destination = destination;
        this.pilot = pilot;
        if (capacity <= 0 || (passengers != null && passengers.size() > capacity)) {
            throw new IllegalArgumentException("Capacity must be positive and fit existing passengers");
        }
        this.capacity = capacity;

        // الرحلة تبدأ مجدولة
        this.flightStatus = FlightStatus.SCHEDULED;

        this.passengers = passengers != null
                ? new ArrayList<>(passengers)
                : new ArrayList<>();
    }


    // Constructor خاص بقراءة الملفات
    public Flight(int id, String destination, Pilot pilot,
                  int capacity, FlightStatus status,
                  ArrayList<Passenger> passengers) {

        this.id = id;
        this.destination = destination;
        this.pilot = pilot;
        if (capacity <= 0 || (passengers != null && passengers.size() > capacity)) {
            throw new IllegalArgumentException("Capacity must be positive and fit existing passengers");
        }
        this.capacity = capacity;
        this.flightStatus = status;

        this.passengers = passengers != null
                ? new ArrayList<>(passengers)
                : new ArrayList<>();
    }


    public static void setIdCounter(int nextValue) {
        idCounter = nextValue;
    }


    public static int getTotalFlightsToday() {
        return totalFlightsToday;
    }


    // تستخدم بعد تحميل الرحلات من الملف
    public static void calculateTotalFlightsToday(ArrayList<Flight> flights) {

        totalFlightsToday = 0;

        for (Flight flight : flights) {
            if (flight.getFlightStatus() == FlightStatus.DEPARTED) {
                totalFlightsToday++;
            }
        }
    }

    // Getters and Setters
    public int getId() {
        return id;
    }


    public String getDestination() {
        return destination;
    }


    public void setDestination(String destination) {
        this.destination = destination;
    }


    public Pilot getPilot() {
        return pilot;
    }


    public void setPilot(Pilot pilot) {
        this.pilot = pilot;
    }


    public int getCapacity() {
        return capacity;
    }


    public void setCapacity(int capacity) {
        if (capacity <= 0 || (passengers != null && passengers.size() > capacity)) {
            throw new IllegalArgumentException("Capacity must be positive and fit existing passengers");
        }
        this.capacity = capacity;
    }


    public FlightStatus getFlightStatus() {
        return flightStatus;
    }


    public ArrayList<Passenger> getPassengers() {
        return passengers;
    }


    public void bookPassenger(Passenger passenger)
            throws DuplicatePassengerException, FlightFullException {
        if (passenger == null) {
            throw new IllegalArgumentException("Passenger cannot be null");
        }


        for (Passenger p : passengers) {

            if (p.getNationalId().equals(passenger.getNationalId())) {

                throw new DuplicatePassengerException(
                        "Passenger with ID "
                                + passenger.getNationalId()
                                + " already exists."
                );
            }
        }


        if (passengers.size() >= capacity) {

            throw new FlightFullException(
                    "Flight is full. Cannot add passenger."
            );
        }


        passengers.add(passenger);
    }


    public void changeStatus(FlightStatus newStatus)
            throws InvalidFlightStatusException {


        boolean validTransition = false;


        switch (flightStatus) {

            case SCHEDULED:

                validTransition =
                        newStatus == FlightStatus.BOARDING ||
                                newStatus == FlightStatus.CANCELLED;

                break;


            case BOARDING:

                validTransition =
                        newStatus == FlightStatus.DEPARTED ||
                                newStatus == FlightStatus.CANCELLED;

                break;


            case DEPARTED:
            case CANCELLED:

                validTransition = false;

                break;
        }


        if (!validTransition) {

            throw new InvalidFlightStatusException(
                    "Cannot change status from "
                            + flightStatus
                            + " to "
                            + newStatus
            );
        }


        if (newStatus == FlightStatus.DEPARTED) {
            totalFlightsToday++;
        }


        flightStatus = newStatus;
    }


    public void displayFlightInfo() {

        System.out.println(
                "Flight ID: " + id
                        + " | Destination: " + destination
                        + " | Status: " + flightStatus
        );


        System.out.println(
                "Pilot: "
                        + (pilot != null ? pilot.getName() : "None")
        );


        System.out.println(
                "Passengers: "
                        + passengers.size()
                        + "/"
                        + capacity
        );
    }
}