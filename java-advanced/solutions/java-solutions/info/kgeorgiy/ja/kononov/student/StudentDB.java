package info.kgeorgiy.ja.kononov.student;

import info.kgeorgiy.java.advanced.student.*;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StudentDB implements StudentQuery {

    private final Comparator<Student> STUDENT_NAME_COMPARATOR = Comparator.comparing(Student::getLastName)
            .thenComparing(Student::getFirstName)
            .reversed()
            // :NOTE: naturalOrder
            .thenComparing(Student::compareTo);

    private <T> List<T> sortedAndCollect(Stream<T> stream, Comparator<T> comparator) {
        return stream
                .sorted(comparator)
                // :NOTE: toList
                .collect(Collectors.toList());
    }

    // :NOTE: Collection.stream()
    private <V, R extends Collection<V>> R mapAndCollect(Stream<Student> stream, Function<Student, V> function,
                                                         Supplier<R> supplier) {
        return stream
                .map(function)
                .collect(Collectors.toCollection(supplier));
    }

    private <T> List<T> getParameter(List<Student> students, Function<Student, T> function) {
        return mapAndCollect(students.stream(), function, ArrayList::new);
    }

    private List<Student> sortStudentsByCondition(Collection<Student> students, Comparator<Student> comparator) {
        return sortedAndCollect(students.stream(), comparator);
    }

    private List<Student> findStudentByCondition(Collection<Student> students, Predicate<Student> predicate) {
        return sortedAndCollect(students.stream().filter(predicate), STUDENT_NAME_COMPARATOR);
    }

    private String getStudentFullName(Student student) {
        return student.getFirstName() + " " + student.getLastName();
    }

    @Override
    public List<String> getFirstNames(List<Student> students) {
        return getParameter(students, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(List<Student> students) {
        return getParameter(students, Student::getLastName);
    }

    @Override
    public List<GroupName> getGroups(List<Student> students) {
        return getParameter(students, Student::getGroup);
    }

    @Override
    public List<String> getFullNames(List<Student> students) {
        return getParameter(students, this::getStudentFullName);
    }

    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return mapAndCollect(students.stream(), Student::getFirstName, TreeSet::new);
    }

    @Override
    public String getMaxStudentFirstName(List<Student> students) {
        return students
                .stream()
                // :NOTE: naturalOrder
                .max(Comparator.comparingInt(Student::getId))
                .map(Student::getFirstName)
                .orElse("");
    }

    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return sortStudentsByCondition(students, Comparator.comparing(Student::getId));
    }

    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return sortStudentsByCondition(students, STUDENT_NAME_COMPARATOR);
    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return findStudentByCondition(students, student -> student.getFirstName().equals(name));
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return findStudentByCondition(students, student -> student.getLastName().equals(name));
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, GroupName group) {
        return findStudentByCondition(students, student -> student.getGroup().equals(group));
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, GroupName group) {
        return findStudentsByGroup(students, group)
                .stream()
                .collect(Collectors.toMap(Student::getLastName,
                        Student::getFirstName,
                        BinaryOperator.minBy(String::compareTo)));
    }
}
