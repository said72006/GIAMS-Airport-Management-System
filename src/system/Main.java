package system;

import file.FileManager;
import model.Flight;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {

    public static void main(String[] args) {
        Airport gazaAirport = Airport.getInstance();

        String passengersFilePath = "src/file/passengers.txt";
        String pilotsFilePath = "src/file/pilots.txt";
        String attendantsFilePath = "src/file/attendants.txt";
        String flightsLogFilePath = "src/file/flights_log.txt";

        // Load people first so flights can be linked to existing pilots/passengers.
        FileManager.loadPassengers(passengersFilePath, gazaAirport);
        FileManager.loadPilots(pilotsFilePath, gazaAirport);
        FileManager.loadAttendants(attendantsFilePath, gazaAirport);
        FileManager.loadFlights(flightsLogFilePath, gazaAirport);

        int lastId = FileManager.getLastFlightId(flightsLogFilePath);
        Flight.setIdCounter(lastId + 1);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            AirportGUI gui = new AirportGUI(
                    gazaAirport,
                    passengersFilePath,
                    pilotsFilePath,
                    attendantsFilePath,
                    flightsLogFilePath
            );
            gui.setVisible(true);
        });
    }
}
