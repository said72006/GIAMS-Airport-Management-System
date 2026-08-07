package model;

public abstract class Person {
    // static field: لتوليد ID تلقائي للنظام
    private static int nextId = 1;

    private int id;              // معرّف النظام التلقائي
    private String nationalId;   // رقم الهوية الشخصية
    private String name;
    private String nationality;

    // Constructor الكامل (يستقبل جميع البيانات الأساسية)
    public Person(String name, String nationalId, String nationality) {
        this.id = nextId++;
        this.name = name;
        this.nationalId = nationalId;
        this.nationality = nationality;
    }

    // Constructor مُثقل (Overloaded) للتسلسل باستخدام this() (مطلوب في R2)
    public Person(String name, String nationalId) {
        this(name, nationalId, "Palestinian"); // جنسية افتراضية في حال عدم الإدخال
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public String getNationalId() {
        return nationalId;
    }

    public void setNationalId(String nationalId) {
        this.nationalId = nationalId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    // دالة طباعة البيانات التي سيتم عمل Override لها في الأبناء
    public void displayInfo() {
        System.out.println("System ID: " + id);
        System.out.println("National ID: " + nationalId);
        System.out.println("Name: " + name);
        System.out.println("Nationality: " + nationality);
    }
}