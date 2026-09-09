package com.webdev2;

import java.util.ArrayList;
import java.util.List;

public class Roster {
    private List<Student> students = new ArrayList<>();

    public void addStudent(Student student) {
        students.add(student);
    }

    public List<Student> getStudents() {
        return students;
    }

    public void displayRoster() {
        for (Student s : students) {
            System.out.println(s);
        }
    }
}