package com.webdev2;

public class Student implements Gradable {
    private String name;
    private int age;
    private double score;

    public Student(String name, int age, double score) throws InvalidAgeException {
        if (age < 0 || age > 120) {
            throw new InvalidAgeException("Invalid age: " + age);
        }
        this.name = name;
        this.age = age;
        this.score = score;
    }

    public String getName() { return name; }
    public int getAge() { return age; }
    public double getScore() { return score; }

    @Override
    public double calculateGrade() {
        return score;
    }

    @Override
    public String toString() {
        return "Student{name='" + name + "', age=" + age + ", score=" + score + "}";
    }
}