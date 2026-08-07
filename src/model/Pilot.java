package model;

public class Pilot extends Employee {
    private String license;
    private String certificate;
    private int flightHours;

    // Constructor الكامل - يمرر الـ nationalId إلى كلاس Employee (الأب)
    public Pilot(String name, String nationalId, String nationality, double salary, Shift shift, String license, String certificate, int flightHours) {
        super(name, nationalId, nationality, salary, shift); // استدعاء أول سطر إلزامي (R3)
        this.license = license;
        this.certificate = certificate;
        this.flightHours = flightHours;
    }


    public Pilot(String name, String nationalId, double salary, Shift shift, String license, String certificate, int flightHours) {
        this(name, nationalId, "Palestinian", salary, shift, license, certificate, flightHours);
    }

    // Getters and Setters
    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public String getCertificate() {
        return certificate;
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

    public int getFlightHours() {
        return flightHours;
    }

    public void setFlightHours(int flightHours) {
        if (flightHours >= 0)
            this.flightHours = flightHours;
        else
            this.flightHours = 0;
    }

    // الـ Override النهائي لطباعة معلومات الطيار كاملة (R4)
    @Override
    public void displayInfo() {
        System.out.println("=== Pilot Info ===");
        super.displayInfo(); // يستدعي Employee المتصل بـ Person ليطبع كل البيانات الأساسية والوظيفية
        System.out.println("License: " + license);
        System.out.println("Certificate: " + certificate);
        System.out.println("Flight Hours: " + flightHours + " hours");
    }
}