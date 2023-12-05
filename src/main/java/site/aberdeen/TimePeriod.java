package site.aberdeen;

public enum TimePeriod {
    WEEKLY(1),
    MONTHLY(4.3333333),
    YEARLY(52);

    private final double value;

    private TimePeriod(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }


}

