package file;

import model.*;
import system.Airport;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class FileManager {


    // ================= LOAD PASSENGERS =================

    public static void loadPassengers(String filePath, Airport airport) {

        File file = new File(filePath);

        if (!file.exists()) {
            System.out.println("Passengers file not found.");
            return;
        }


        try (Scanner scanner = new Scanner(file)) {


            while (scanner.hasNextLine()) {

                String line = scanner.nextLine();


                if (line.trim().isEmpty())
                    continue;


                try {

                    String[] parts = line.split("\\|");


                    if (parts.length >= 6/* في حال لاحقا قررنا نغير البيانات*/) {


                        Passenger passenger = new Passenger(
                                parts[0].trim(),
                                parts[1].trim(),
                                parts[2].trim(),
                                parts[3].trim(),
                                parts[4].trim(),
                                LocalDate.parse(parts[5].trim())
                        );


                        airport.addPerson(passenger);
                    }


                } catch (Exception e) {

                    System.out.println(
                            "Invalid passenger record skipped."
                    );
                }
            }


            System.out.println(
                    "Passengers loaded successfully."
            );


        } catch (FileNotFoundException e) {

            System.out.println(
                    "Cannot open passengers file."
            );
        }
    }


    // ================= LOAD PILOTS =================


    public static void loadPilots(String filePath, Airport airport) {


        File file = new File(filePath);


        if (!file.exists()) {

            System.out.println("Pilots file not found.");
            return;
        }


        try (Scanner scanner = new Scanner(file)) {


            while (scanner.hasNextLine()) {


                String line = scanner.nextLine();


                if (line.trim().isEmpty())
                    continue;


                try {


                    String[] parts = line.split("\\|");


                    if (parts.length >= 8) {


                        Pilot pilot = new Pilot(

                                parts[0].trim(),
                                parts[1].trim(),
                                parts[2].trim(),
                                Double.parseDouble(parts[3].trim()),
                                Shift.valueOf(parts[4].trim()),
                                parts[5].trim(),
                                parts[6].trim(),
                                Integer.parseInt(parts[7].trim())

                        );


                        airport.addPerson(pilot);

                    }


                } catch (Exception e) {

                    System.out.println(
                            "Invalid pilot record skipped."
                    );
                }

            }


            System.out.println(
                    "Pilots loaded successfully."
            );


        } catch (FileNotFoundException e) {

            System.out.println(
                    "Cannot open pilots file."
            );
        }
    }


    // ================= LOAD ATTENDANTS =================


    public static void loadAttendants(String filePath, Airport airport) {


        File file = new File(filePath);


        if (!file.exists()) {

            System.out.println(
                    "Attendants file not found."
            );

            return;
        }


        try (Scanner scanner = new Scanner(file)) {


            while (scanner.hasNextLine()) {


                String line = scanner.nextLine();


                if (line.trim().isEmpty())
                    continue;


                try {


                    String[] parts = line.split("\\|");


                    if (parts.length >= 7) {


                        ArrayList<String> languages =
                                new ArrayList<>(
                                        Arrays.asList(
                                                parts[5].split(",")
                                        )
                                );


                        FlightAttendant attendant =
                                new FlightAttendant(

                                        parts[0].trim(),
                                        parts[1].trim(),
                                        parts[2].trim(),
                                        Double.parseDouble(parts[3]),
                                        Shift.valueOf(parts[4]),
                                        languages,
                                        Integer.parseInt(parts[6])

                                );


                        airport.addPerson(attendant);

                    }


                } catch (Exception e) {

                    System.out.println(
                            "Invalid attendant record skipped."
                    );
                }


            }


            System.out.println(
                    "Flight attendants loaded successfully."
            );


        } catch (FileNotFoundException e) {

            System.out.println(
                    "Cannot open attendants file."
            );
        }

    }
    // ================= LOAD FLIGHTS =================

    public static void loadFlights(String filePath, Airport airport) {

        File file = new File(filePath);


        if (!file.exists()) {

            System.out.println("Flights file not found.");
            return;
        }


        try (Scanner scanner = new Scanner(file)) {


            while (scanner.hasNextLine()) {


                String line = scanner.nextLine();


                if (line.trim().isEmpty())
                    continue;


                try {


                    String[] parts = line.split("\\|");


                    if (parts.length >= 5) {


                        int id = Integer.parseInt(parts[0].trim());

                        String destination = parts[1].trim();

                        int capacity = Integer.parseInt(parts[2].trim());

                        FlightStatus status =
                                FlightStatus.valueOf(parts[3].trim());


                        String pilotId = parts[4].trim();


                        // البحث عن الطيار الموجود مسبقاً
                        Pilot assignedPilot = null;


                        for (Person p : airport.getPeople()) {


                            if (p instanceof Pilot &&
                                    p.getNationalId().equals(pilotId)) {


                                assignedPilot = (Pilot) p;
                                break;

                            }

                        }


                        // إذا لم يوجد الطيار لا نضيف الرحلة
                        if (assignedPilot == null) {

                            System.out.println(
                                    "Flight skipped: Pilot not found."
                            );

                            continue;
                        }


                        ArrayList<Passenger> passengers =
                                new ArrayList<>();


                        if (parts.length >= 6 &&
                                !parts[5].trim().isEmpty()) {


                            String[] passengerIds =
                                    parts[5].split(",");


                            for (String passengerId : passengerIds) {


                                for (Person p : airport.getPeople()) {


                                    if (p instanceof Passenger &&
                                            p.getNationalId()
                                                    .equals(passengerId.trim())) {


                                        passengers.add((Passenger) p);

                                        break;
                                    }

                                }

                            }

                        }


                        Flight flight =
                                new Flight(
                                        id,
                                        destination,
                                        assignedPilot,
                                        capacity,
                                        status,
                                        passengers
                                );


                        airport.addFlight(flight);


                    }


                } catch (Exception e) {


                    System.out.println(
                            "Invalid flight record skipped."
                    );

                }


            }


            // حساب الرحلات المنفذة بعد انتهاء التحميل
            Flight.calculateTotalFlightsToday(airport.getFlights());


            System.out.println(
                    "Flights loaded successfully."
            );


        } catch (FileNotFoundException e) {

            System.out.println(
                    "Cannot open flights file."
            );

        }

    }


// ================= GET LAST FLIGHT ID =================


    public static int getLastFlightId(String filePath) {


        File file = new File(filePath);


        if (!file.exists())
            return 0;


        int maxId = 0;


        try (Scanner scanner = new Scanner(file)) {


            while (scanner.hasNextLine()) {


                String line = scanner.nextLine();


                if (line.trim().isEmpty())
                    continue;


                try {


                    int id =
                            Integer.parseInt(
                                    line.split("\\|")[0]
                            );


                    if (id > maxId)
                        maxId = id;


                } catch (Exception ignored) {

                }

            }


        } catch (FileNotFoundException e) {
        }


        return maxId;

    }


// ================= SAVE PEOPLE =================


    public static void savePeople(String filePath, Airport airport,Class<?> type) {


        File file = new File(filePath);


        try (PrintWriter writer = new PrintWriter(file)) {


            for (Person p : airport.getPeople()) {


                if (type.isInstance(p)) {


                    if (p instanceof Passenger) {


                        Passenger passenger =
                                (Passenger) p;


                        writer.println(
                                passenger.getName() + "|" +
                                        passenger.getNationalId() + "|" +
                                        passenger.getNationality() + "|" +
                                        passenger.getPassportNumber() + "|" +
                                        passenger.getDestination() + "|" +
                                        passenger.getPassportExpiry()
                        );


                    } else if (p instanceof Pilot) {


                        Pilot pilot =
                                (Pilot) p;


                        writer.println(
                                pilot.getName() + "|" +
                                        pilot.getNationalId() + "|" +
                                        pilot.getNationality() + "|" +
                                        pilot.getSalary() + "|" +
                                        pilot.getShift() + "|" +
                                        pilot.getLicense() + "|" +
                                        pilot.getCertificate() + "|" +
                                        pilot.getFlightHours()
                        );


                    } else if (p instanceof FlightAttendant) {


                        FlightAttendant attendant =
                                (FlightAttendant) p;


                        writer.println(
                                attendant.getName() + "|" +
                                        attendant.getNationalId() + "|" +
                                        attendant.getNationality() + "|" +
                                        attendant.getSalary() + "|" +
                                        attendant.getShift() + "|" +
                                        String.join(",",
                                                attendant.getLanguages()) + "|" +
                                        attendant.getNumberOfFlights()
                        );

                    }

                }

            }


            System.out.println(
                    "People saved successfully."
            );


        } catch (FileNotFoundException e) {

            System.out.println(
                    "Error saving people."
            );

        }

    }


// ================= SAVE FLIGHTS =================


    public static void saveFlights(String filePath, Airport airport) {


        File file = new File(filePath);


        try (PrintWriter writer = new PrintWriter(file)) {


            for (Flight flight :
                    airport.getFlights()) {


                ArrayList<String> ids =
                        new ArrayList<>();


                for (Passenger p :
                        flight.getPassengers()) {


                    ids.add(
                            p.getNationalId()
                    );

                }


                writer.println(

                        flight.getId() + "|" +
                                flight.getDestination() + "|" +
                                flight.getCapacity() + "|" +
                                flight.getFlightStatus() + "|" +
                                flight.getPilot().getNationalId() + "|" +
                                String.join(",", ids)

                );

            }


            System.out.println(
                    "Flights saved successfully."
            );


        } catch (FileNotFoundException e) {


            System.out.println(
                    "Error saving flights."
            );

        }

    }
}