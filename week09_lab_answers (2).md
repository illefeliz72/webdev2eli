# WEBDEV2 — Week 9 Lab Answers

Name: HERRERIA ELI FELIZE
Student ID: 2511101
GitHub repo: https://github.com/illefeliz72/webdev2eli

---

## Task 1 — One-to-One + One-to-Many Mappings

**Approach / explanation:**
`User` owns the one-to-one because it holds the `profile_id` foreign key through `@JoinColumn`, and `UserProfile` is the inverse side using `mappedBy = "profile"`. `Employee` owns the one-to-many because it holds the `department_id` foreign key, and `Department` is the inverse side using `mappedBy = "department"`. `Department.addEmployee()` and `removeEmployee()` update both sides so the in-memory objects stay consistent with the database.

**Code / endpoint(s) used (if applicable):**

User.java
```java
@OneToOne(cascade = CascadeType.ALL)
@JoinColumn(name = "profile_id")
private UserProfile profile;
```

UserProfile.java
```java
@OneToOne(mappedBy = "profile")
private User user;
```

Department.java
```java
@OneToMany(mappedBy = "department", cascade = CascadeType.ALL, orphanRemoval = true)
private List<Employee> employees = new ArrayList<>();

public void addEmployee(Employee e) {
    employees.add(e);
    e.setDepartment(this);
}

public void removeEmployee(Employee e) {
    employees.remove(e);
    e.setDepartment(null);
}
```

Employee.java
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "department_id", nullable = false)
private Department department;
```



**Reflection question:**
_Which entity owns the `Department`–`Employee` relationship and why, and what value must `mappedBy` contain?_

Your answer:
`Employee` owns the relationship because it holds the foreign key column (`department_id`), and in JPA the side with the foreign key is the owning side. `mappedBy` on `Department.employees` must contain the **field name** in `Employee` that points back, which is `"department"`, not the column name.

---

## Task 2 — Many-to-Many Join Table + Association Entity

**Approach / explanation:**
First I modeled Student–Course as a direct `@ManyToMany` with a `student_course` join table. I then replaced it with an `Enrollment` association entity so the relationship can carry its own data (`enrollmentDate`, `grade`). `Student` and `Course` now use `@OneToMany(mappedBy = ...)` toward `Enrollment`, and `Enrollment` owns both foreign keys.

**Code / endpoint(s) used (if applicable):**

Step 1: Student.java (basic many-to-many, owning side)
```java
@ManyToMany
@JoinTable(name = "student_course",
    joinColumns = @JoinColumn(name = "student_id"),
    inverseJoinColumns = @JoinColumn(name = "course_id"))
private Set<Course> courses = new HashSet<>();
```

Step 1: Course.java
```java
@ManyToMany(mappedBy = "courses")
private Set<Student> students = new HashSet<>();
```

Step 2: Enrollment.java
```java
@Entity
@Table(name = "enrollment",
       uniqueConstraints = @UniqueConstraint(
           name = "uk_student_course",
           columnNames = {"student_id", "course_id"}))
public class Enrollment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate enrollmentDate;
    private String grade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
}
```

Step 2: Student.java and Course.java
```java
@OneToMany(mappedBy = "student", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
private List<Enrollment> enrollments = new ArrayList<>();
```



**Reflection question:**
_When must a many-to-many relationship be modeled as an association entity instead of a direct `@ManyToMany`, and what did the unique constraint protect in your test?_

Your answer:
A many-to-many must become an association entity when the relationship itself has extra attributes (such as grade or enrollment date), because a plain `@JoinTable` can only store the two foreign keys. The unique constraint `uk_student_course` protected against the same student being enrolled in the same course twice, and the database rejected the duplicate with a constraint violation.

---

## Task 3 — Fetch Strategies Experiment — LAZY vs EAGER

**Approach / explanation:**
I set the associations to `FetchType.LAZY`, loaded an `Employee` outside a transaction, and called `employee.getDepartment().getName()`, which caused `LazyInitializationException`. I then fixed it two ways: (a) `@Transactional` on the service method, and (b) a fetch join query that loads the Employee and its Department together. I compared the number of SQL queries each one issued.

**Code / endpoint(s) used (if applicable):**

Fix (a): service method
```java
@Transactional(readOnly = true)
public String getDepartmentName(Long employeeId) {
    Employee e = employeeRepository.findById(employeeId).orElseThrow();
    return e.getDepartment().getName();
}
```

Fix (b): EmployeeRepository.java
```java
@Query("SELECT e FROM Employee e JOIN FETCH e.department WHERE e.id = :id")
Optional<Employee> findWithDepartment(@Param("id") Long id);
```


Observation: PASTE HERE after you count (2 to 3 sentences: how many queries each variant ran, and which was more efficient).

**Reflection question:**
_What causes `LazyInitializationException`, and why is prefer-LAZY plus explicit fetch join/`@EntityGraph` preferable to marking everything EAGER?_

Your answer:
`LazyInitializationException` happens when code touches a lazy association (here, the department proxy) after the Hibernate session has closed, so there is no connection left to load the data. Marking everything EAGER makes every query load related data whether it is needed or not, which can trigger extra queries or huge joins, especially on collections. Keeping LAZY by default and fetching related data explicitly with a fetch join or `@EntityGraph` loads only what each use case needs.

---

## Task 4 — Cascade + orphanRemoval — Safe vs. Dangerous

**Approach / explanation:**
`Department.employees` uses `cascade = {PERSIST, MERGE}` with `orphanRemoval = true` because employees belong to exactly one department (composition), so saving a Department saves its Employees and removing an Employee from the list deletes its row. `Student` and `Course` use only `PERSIST` and `MERGE` because a Course is shared by many students, so cascading REMOVE could delete a Course that other students still use.

**Code / endpoint(s) used (if applicable):**

Safe (composition): Department.java
```java
@OneToMany(mappedBy = "department",
           cascade = {CascadeType.PERSIST, CascadeType.MERGE},
           orphanRemoval = true)
private List<Employee> employees = new ArrayList<>();
```

Shared reference: Student.java
```java
@OneToMany(mappedBy = "student",
           cascade = {CascadeType.PERSIST, CascadeType.MERGE})
private List<Enrollment> enrollments = new ArrayList<>();
```



**Reflection question:**
_What is the difference between `CascadeType.REMOVE` and `orphanRemoval = true`, and in which situation is each appropriate or dangerous?_

Your answer:
`CascadeType.REMOVE` deletes the children when the **parent is deleted**, while `orphanRemoval = true` deletes a child when it is **removed from the parent's collection**, even if the parent stays. Both are appropriate for composition, where children cannot exist without the parent (Department and Employee). Both are dangerous for shared entities such as Course, because deleting one Student could delete a Course that other students still use.

---

## Task 5 — Capstone Milestone 2 — ERD to JPA Entity Model

**Approach / explanation:**
PASTE HERE after you check your own Milestone 1 ERD: 2 to 4 sentences on how your capstone entities, cardinalities, owning sides, and foreign keys map to your classes. Use the table below.

**Code / endpoint(s) used (if applicable):**
```java
```

**ERD → entity table (fill in from your own ERD):**

| ERD entity / relationship | Class | Field | Cardinality | Owning side | FK / join table | Fetch | Cascade (one-line reason) |
|---|---|---|---|---|---|---|---|
|  |  |  |  |  |  |  |  |



**Reflection question:**
_For one relationship in your capstone model, state the cardinality, the owning side, where the FK/join table lives, and why you chose that fetch and cascade setting._

Your answer:
Write this after you finish the table above. It must describe your own capstone ERD.

---

