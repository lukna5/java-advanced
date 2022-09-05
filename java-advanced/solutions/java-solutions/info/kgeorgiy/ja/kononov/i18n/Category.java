package info.kgeorgiy.ja.kononov.i18n;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public class Category<T> {
    Comparator<Object> comparator;
    int numOfIn;
    int numOfEach;
    T min;
    T max;
    String minLength;
    String maxLength;
    long averageSum = 0;
    double average = 0;

    boolean isDate = false;
    public double getAverage() {
        return average;
    }

    public Category(Comparator<Object> comparator) {
        this.comparator = comparator;
    }

    public T getMin() {
        return min;
    }

    public void setMin(T min) {
        if (this.min == null || comparator.compare(min, this.min) < 0) {
            this.min = min;
        }
    }

    public void setMax(T max) {
        if (this.max == null || comparator.compare(max, this.max) > 0) {
            this.max = max;
        }
    }

    public void setMinLength(String minLength) {
        if (this.minLength == null || minLength.length() < this.minLength.length()) {
            this.minLength = minLength;
        }
    }

    public void setMaxLength(String maxLength) {
        if (this.maxLength == null || maxLength.length() > this.maxLength.length()) {
            this.maxLength = maxLength;
        }
    }

    public void addAverageSum(double averageSum) {
        this.averageSum += averageSum;
    }

    public int getNumOfIn() {
        return numOfIn;
    }

    public void setNumOfIn(int numOfIn) {
        this.numOfIn = numOfIn;
    }

    public int getNumOfEach() {
        return numOfEach;
    }

    public void setNumOfEach(int numOfEach) {
        this.numOfEach = numOfEach;
    }

    public T getMax() {
        return max;
    }

    public String getMinLength() {
        return minLength;
    }

    public String getMaxLength() {
        return maxLength;
    }

    public long getAverageSum() {
        return averageSum;
    }

    public void incNumOfEach() {
        numOfEach++;
    }

    public void incNumOfAll() {
        numOfIn++;
    }

    public boolean addElement(T element) {
        return visited.add(element);
    }

    private final Set<T> visited = new HashSet<>();

    public Set<T> getVisited() {
        return visited;
    }

    public void setAverage() {
        average = ((double) averageSum) / numOfIn;
    }


}