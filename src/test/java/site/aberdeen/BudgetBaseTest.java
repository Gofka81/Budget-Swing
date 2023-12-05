package site.aberdeen;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

class BudgetBaseTest {

    private BudgetBase budgetBase;
    private CountDownLatch latch;

    @BeforeEach
    void setUp() {
        latch = new CountDownLatch(1);

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame();
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            budgetBase = new BudgetBase(frame);
            frame.setContentPane(budgetBase);
            frame.pack();
            frame.setVisible(true);
            latch.countDown();
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for setup to complete", e);
        }
    }

    @Test
    void testGetTextFieldValueWithValidNumber() {
        JTextField mockTextField = Mockito.mock(JTextField.class);
        Mockito.when(mockTextField.getText()).thenReturn("42.5");

        double result = budgetBase.getTextFieldValue(mockTextField);

        assertEquals(42.5, result);
        // Make sure that the text field content is not modified
        Mockito.verify(mockTextField, Mockito.never()).setText(Mockito.anyString());
    }

    @Test
    void testGetTextFieldValueWithBlankInput() {
        JTextField mockTextField = Mockito.mock(JTextField.class);
        Mockito.when(mockTextField.getText()).thenReturn("");

        double result = budgetBase.getTextFieldValue(mockTextField);

        assertEquals(0.0, result);
        Mockito.verify(mockTextField, Mockito.times(1)).setText("0");
    }

    @Test
    void testCalculateAllWithDifferentValues() {
        JTextField[] incomeFields = budgetBase.getIncomeFields();
        JTextField[] spendingFields = budgetBase.getSpendingFields();

        // Set different values for income fields
        for (int i = 0; i < incomeFields.length; i++) {
            incomeFields[i].setText(String.valueOf((i + 1) * 2.5));  // Use different values
        }

        // Set different values for spending fields
        for (int i = 0; i < spendingFields.length; i++) {
            spendingFields[i].setText(String.valueOf((i + 1) * 1.5));  // Use different values
        }

        // Set different granularity for income, spending, and overall
        budgetBase.getIncomeGranularity().setSelectedItem(TimePeriod.YEARLY);
        budgetBase.getExpenseGranularity().setSelectedItem(TimePeriod.MONTHLY);
        budgetBase.getOverallGranularity().setSelectedItem(TimePeriod.WEEKLY);

        budgetBase.calculateAll();

        // Calculate expected values based on the given input
        double expectedTotalIncome = (1 * 2.5) + (2 * 2.5) + (3 * 2.5);
        double expectedTotalSpending = (1 * 1.5) + (2 * 1.5) + (3 * 1.5);
        double expectedOverall = (expectedTotalIncome / TimePeriod.YEARLY.getValue())
                - (expectedTotalSpending / TimePeriod.MONTHLY.getValue());

        assertEquals(String.format("%.2f", expectedTotalIncome), budgetBase.getTotalIncomeField().getText());
        assertEquals(String.format("%.2f", expectedTotalSpending), budgetBase.getTotalSpendingField().getText());
        assertEquals(String.format("%.2f", expectedOverall), budgetBase.getOverallField().getText());
    }

    @Test
    void testCalculateAllScenario() {
        // Test the calculateAll method
        JTextField[] incomeFields = budgetBase.getIncomeFields();
        JTextField[] spendingFields = budgetBase.getSpendingFields();

        for (JTextField field : incomeFields) {
            field.setText("10.0");
        }

        for (JTextField field : spendingFields) {
            field.setText("5.0");
        }

        budgetBase.calculateAll();

        assertEquals("30.00", budgetBase.getTotalIncomeField().getText());
        assertEquals("15.00", budgetBase.getTotalSpendingField().getText());
        assertEquals("15.00", budgetBase.getOverallField().getText());
    }

    @Test
    void testSaveStateAndUndo() {
        // Test the saveState and undo functionality

        // Add some values to the fields
        JTextField[] incomeFields = budgetBase.getIncomeFields();
        for (int i = 0; i < incomeFields.length; i++) {
            incomeFields[i].setText(String.valueOf(i + 1));
        }

        // Save the state
        budgetBase.saveState();

        // Modify the values
        for (JTextField field : incomeFields) {
            field.setText("0");
        }

        // Undo and check if the values are restored
        budgetBase.undo();

        for (int i = 0; i < incomeFields.length; i++) {
            assertEquals(String.format("%.1f", i + 1.0), incomeFields[i].getText());
        }
    }

    @Test
    void testCalculateAllScenario0() {
        // Test the calculateAll method
        JTextField[] incomeFields = budgetBase.getIncomeFields();
        JTextField[] spendingFields = budgetBase.getSpendingFields();

        for (JTextField field : incomeFields) {
            field.setText("150.0");
        }

        for (JTextField field : spendingFields) {
            field.setText("10.0");
        }

        budgetBase.getIncomeGranularity().setSelectedItem(TimePeriod.MONTHLY);
        budgetBase.calculateAll();

        assertEquals("450.00", budgetBase.getTotalIncomeField().getText());
        assertEquals("30.00", budgetBase.getTotalSpendingField().getText());
        assertEquals("73.85", budgetBase.getOverallField().getText());
    }

    @Test
    void testCalculateAllScenario1() {
        setValuesAndGranularities(new double[]{10.0, 15.0, 20.0}, new double[]{5.0, 8.0, 12.0},
                TimePeriod.MONTHLY, TimePeriod.YEARLY, TimePeriod.MONTHLY);

        assertEquals("45.00", budgetBase.getTotalIncomeField().getText());
        assertEquals("25.00", budgetBase.getTotalSpendingField().getText());
        assertEquals("42.92", budgetBase.getOverallField().getText());
    }

    @Test
    void testCalculateAllScenario2() {
        setValuesAndGranularities(new double[]{8.0, 12.0, 18.0}, new double[]{4.0, 6.0, 9.0},
                TimePeriod.WEEKLY, TimePeriod.MONTHLY, TimePeriod.WEEKLY);

        // Expected results based on input values and granularities
        assertEquals("38.00", budgetBase.getTotalIncomeField().getText());
        assertEquals("19.00", budgetBase.getTotalSpendingField().getText());
        assertEquals("33.62", budgetBase.getOverallField().getText());
    }

    @Test
    void testCalculateAllScenario3() {
        setValuesAndGranularities(new double[]{5.0, 7.5, 10.0}, new double[]{3.0, 4.5, 6.0},
                TimePeriod.MONTHLY, TimePeriod.WEEKLY, TimePeriod.MONTHLY);

        assertEquals("22.50", budgetBase.getTotalIncomeField().getText());
        assertEquals("13.50", budgetBase.getTotalSpendingField().getText());
        assertEquals("-36.00", budgetBase.getOverallField().getText());
        assertEquals(Color.RED, budgetBase.getOverallField().getForeground());
    }

    @Test
    void testCalculateAllScenario4() {
        setValuesAndGranularities(new double[]{15.0, 25.0, 30.0}, new double[]{8.0, 10.0, 15.0},
                TimePeriod.YEARLY, TimePeriod.YEARLY, TimePeriod.WEEKLY);

        assertEquals("70.00", budgetBase.getTotalIncomeField().getText());
        assertEquals("33.00", budgetBase.getTotalSpendingField().getText());
        assertEquals("0.71", budgetBase.getOverallField().getText());
    }

    @Test
    void testCalculateAllScenario5() {
        setValuesAndGranularities(new double[]{12.0, 18.0, 22.0}, new double[]{6.0, 9.0, 11.0},
                TimePeriod.WEEKLY, TimePeriod.MONTHLY, TimePeriod.MONTHLY);

        assertEquals("52.00", budgetBase.getTotalIncomeField().getText());
        assertEquals("26.00", budgetBase.getTotalSpendingField().getText());
        assertEquals("199.33", budgetBase.getOverallField().getText());
    }

    private void setValuesAndGranularities(double[] incomeValues, double[] spendingValues,
                                           TimePeriod incomeGranularity, TimePeriod expenseGranularity,
                                           TimePeriod overallGranularity) {
        JTextField[] incomeFields = budgetBase.getIncomeFields();
        JTextField[] spendingFields = budgetBase.getSpendingFields();

        // Set values for income fields
        for (int i = 0; i < incomeFields.length; i++) {
            incomeFields[i].setText(String.valueOf(incomeValues[i]));
        }

        // Set values for spending fields
        for (int i = 0; i < spendingFields.length; i++) {
            spendingFields[i].setText(String.valueOf(spendingValues[i]));
        }

        // Set granularities
        budgetBase.getIncomeGranularity().setSelectedItem(incomeGranularity);
        budgetBase.getExpenseGranularity().setSelectedItem(expenseGranularity);
        budgetBase.getOverallGranularity().setSelectedItem(overallGranularity);

        // Calculate based on the provided values and granularities
        budgetBase.calculateAll();
    }

}
