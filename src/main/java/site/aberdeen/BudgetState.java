package site.aberdeen;

import java.util.Arrays;

public class BudgetState {
    private final double[] incomeValues;
    private final double[] spendingValues;

    public BudgetState(double[] incomeValues, double[] spendingValues){
        this.incomeValues = Arrays.copyOf(incomeValues, incomeValues.length);
        this.spendingValues = Arrays.copyOf(spendingValues, spendingValues.length);
    }

    public double[] getIncomeValues() {
        return Arrays.copyOf(incomeValues, incomeValues.length);
    }

    public double[] getSpendingValues() {
        return Arrays.copyOf(spendingValues, spendingValues.length);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BudgetState that = (BudgetState) o;
        return Arrays.equals(incomeValues, that.incomeValues) && Arrays.equals(spendingValues, that.spendingValues);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(incomeValues);
        result = 31 * result + Arrays.hashCode(spendingValues);
        return result;
    }
}
