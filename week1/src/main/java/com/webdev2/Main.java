package com.webdev2;

public class Main {
    public static void main(String[] args) {
        Roster roster = new Roster();

        try {
            Student s1 = new Student("Alice", 20, 92.5);
            Student s2 = new Student("Bob", 22, 85.0);
            
            roster.addStudent(s1);
            roster.addStudent(s2);

            System.out.println("--- Class Roster ---");
            roster.displayRoster();

        } catch (InvalidAgeException e) {
            System.err.println("Error creating student: " + e.getMessage());
        }
    }
}