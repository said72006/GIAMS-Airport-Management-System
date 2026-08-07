# GIAMS - Gaza International Airport Management System

A Java desktop application for managing the basic operations of **Gaza International Airport**.
The project was developed for **COMP 2102 - Programming Language (2)** and demonstrates core Object-Oriented Programming concepts through a modern graphical interface built with **Java Swing**.

## Main Features

- Modern dashboard with airport statistics
- Passenger management
- Pilot management
- Flight attendant management
- Create and manage flights
- Assign a pilot to a flight
- Book passengers on flights
- Update flight status
- Daily airport report
- Search and filter records
- Save and load data using text files
- Validation and custom exception handling

## Flight Status

The system supports the following flight states:

- `SCHEDULED`
- `BOARDING`
- `DEPARTED`
- `CANCELLED`

Flight status changes follow the validation rules implemented in the project.

## Technologies Used

- Java
- Java Swing
- Object-Oriented Programming (OOP)
- File I/O
- Exception Handling
- ArrayList

No external GUI libraries are required.

## OOP Concepts Applied

The project demonstrates:

- Encapsulation
- Inheritance
- Polymorphism
- Abstraction
- Constructor Overloading
- Method Overriding
- Singleton Pattern
- Custom Exceptions

## Project Structure

```text
src/
|-- model/
|   |-- Person.java
|   |-- Employee.java
|   |-- Passenger.java
|   |-- Pilot.java
|   |-- FlightAttendant.java
|   |-- Flight.java
|   |-- FlightStatus.java
|   `-- Shift.java
|
|-- system/
|   |-- Main.java
|   |-- Airport.java
|   `-- AirportGUI.java
|
|-- exceptions/
|   |-- DuplicatePassengerException.java
|   |-- FlightFullException.java
|   `-- InvalidFlightStatusException.java
|
`-- file/
    |-- FileManager.java
    |-- passengers.txt
    |-- pilots.txt
    |-- attendants.txt
    `-- flights_log.txt
```

## How to Run

### IntelliJ IDEA

1. Open the project folder in IntelliJ IDEA.
2. Make sure the `src` folder is marked as **Sources Root**.
3. Open:
   `src/system/Main.java`
4. Run the `Main` class.

The graphical interface will open automatically.

### NetBeans / Other IDEs

Import or open the folder as a Java project, make sure `src` is included in the source path, then run:

```text
system.Main
```

## Data Files

The application loads and saves its data in:

```text
src/file/passengers.txt
src/file/pilots.txt
src/file/attendants.txt
src/file/flights_log.txt
```

Keep these files in the same locations so that loading and saving continue to work correctly.

## Custom Exceptions

The system includes custom exceptions for important validation cases:

- `DuplicatePassengerException` - prevents booking the same passenger twice on one flight.
- `FlightFullException` - prevents bookings when the flight reaches maximum capacity.
- `InvalidFlightStatusException` - prevents invalid flight-status transitions.

## GUI

The graphical interface includes a modern dark sidebar, dashboard cards, searchable tables, flight-status indicators, quick actions, and notification messages while keeping the original project logic unchanged.

## Author

**Said Ahmed Abu Foda**  
COMP 2102 - Programming Language (2)
