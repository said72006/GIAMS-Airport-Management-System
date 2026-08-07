package model;

public abstract class Employee extends Person {
    private double salary;
    private Shift shift; // الوردية

    // Constructor الكامل - يستدعي super ويمرر الـ nationalId
    public Employee(String name, String nationalId, String nationality, double salary, Shift shift) {
        super(name, nationalId, nationality); // استدعاء أول سطر إلزامي (R3)
        this.salary = salary;
        this.shift = shift;
    }

    // Constructor مختصر باستخدام this()
    public Employee(String name, String nationalId, double salary, Shift shift) {
        this(name, nationalId, "Palestinian", salary, shift);
    }

    // Getters and Setters
    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    public Shift getShift() {
        return shift;
    }

    public void setShift(Shift shift) {
        this.shift = shift;
    }

    // عمل Override لدالة الطباعة لتضمين بيانات الموظف (مطلوب في R4)
    @Override
    public void displayInfo() {
        super.displayInfo(); // يطبع بيانات الشخص الأساسية من كلاس Person
        System.out.println("Salary: $" + salary);
        System.out.println("Shift: " + shift);
    }
}