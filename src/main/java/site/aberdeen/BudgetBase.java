package site.aberdeen;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Stack;

public class BudgetBase extends JPanel {

    private static final int COLUMN_LABEL = 0;
    private static final int COLUMN_TEXT_FIELD = 1;
    private static final int COLUMN_COMBO_BOX = 2;

    private final JFrame topLevelFrame;
    private final GridBagConstraints layoutConstraints = new GridBagConstraints();

    private JButton calculateButton;
    private JButton exitButton;
    private JButton undoButton;

    private JTextField[] incomeFields;
    private JTextField totalIncomeField;
    private JComboBox<TimePeriod> incomeGranularity;

    private JTextField[] spendingFields;
    private JTextField totalSpendingField;
    private JComboBox<TimePeriod> expenseGranularity;

    private JTextField overallField;
    private JComboBox<TimePeriod> overallGranularity;

    private Stack<BudgetState> undoStack;

    public BudgetBase(JFrame frame) {
        topLevelFrame = frame;
        setLayout(new GridBagLayout());
        undoStack = new Stack<>();
        initComponents();
    }

    class UpdateTotalFocusAdapter extends FocusAdapter {

        @Override
        public void focusGained(FocusEvent e){
            saveState();
        }

        @Override
        public void focusLost(FocusEvent e) {
            calculateAll();
        }
    }

    private void initComponents() {
        initIncomeComponents();
        initSpendingComponents();
        initOverallComponents();
        initListeners();
    }

    public JTextField[] getIncomeFields() {
        return incomeFields;
    }

    public JTextField[] getSpendingFields() {
        return spendingFields;
    }

    public JTextField getTotalIncomeField() {
        return totalIncomeField;
    }

    public JTextField getTotalSpendingField() {
        return totalSpendingField;
    }

    public JTextField getOverallField() {
        return overallField;
    }

    public JComboBox<TimePeriod> getIncomeGranularity() {
        return incomeGranularity;
    }

    public JComboBox<TimePeriod> getExpenseGranularity() {
        return expenseGranularity;
    }

    public JComboBox<TimePeriod> getOverallGranularity() {
        return overallGranularity;
    }

    private void initIncomeComponents() {
        addLabel("INCOME", 0, 0);

        String[] incomeLabels = {"Wages", "Loans", "Sales"};
        incomeFields = createTextFields(incomeLabels.length);
        addLabelAndFields(incomeLabels, incomeFields,0);

        addLabel("Total Income", 4, 0);
        incomeGranularity = new JComboBox<>(TimePeriod.values());
        addComboBox(incomeGranularity, 0, 1);
        totalIncomeField = createReadOnlyTextField();
        addTextField(totalIncomeField, 4, 1);
    }


    private void initSpendingComponents() {
        addLabel("SPENDING", 0, 4);

        String[] spendingLabels = {"Taxes", "Credits", "Food"};
        spendingFields = createTextFields(spendingLabels.length);
        addLabelAndFields(spendingLabels, spendingFields,4);

        addLabel("Total Spending", 4, 4);
        expenseGranularity = new JComboBox<>(TimePeriod.values());
        addComboBox(expenseGranularity, 0, 5);
        totalSpendingField = createReadOnlyTextField();
        addTextField(totalSpendingField, 4, 5);
    }

    private void initOverallComponents() {
        addLabel("Overall", 5, 1);
        overallGranularity = new JComboBox<>(TimePeriod.values());
        addComboBox(overallGranularity, 5, 2);
        overallField = createReadOnlyTextField();
        addTextField(overallField, 6, 2);
        calculateButton = new JButton("Calculate");
        addComponent(calculateButton, 5, 0, COLUMN_LABEL);
        undoButton = new JButton("Undo");
        addComponent(undoButton, 6, 0, COLUMN_LABEL);
        exitButton = new JButton("Exit");
        addComponent(exitButton, 7, 0, COLUMN_LABEL);
    }

    private void addFocusListener(String name, JTextField[] fields){
        for (int i = 0; i < fields.length; i++) {
            fields[i].setName(name + i);
            fields[i].addFocusListener(new UpdateTotalFocusAdapter());
        }
    }

    private void addActionListeners(){
        incomeGranularity.addActionListener(e -> calculateAll());
        expenseGranularity.addActionListener(e -> calculateAll());
        overallGranularity.addActionListener(e -> calculateAll());
    }

    private JTextField[] createTextFields(int count) {
        JTextField[] fields = new JTextField[count];
        for (int i = 0; i < count; i++) {
            fields[i] = createTextField();
        }
        return fields;
    }

    private void addLabelAndFields(String[] labels, JTextField[] fields, int startColumn) {
        for (int i = 0; i < labels.length; i++) {
            addLabel(labels[i], i + 1, startColumn);
            addTextField(fields[i], i + 1, startColumn + 1);
        }
    }

    private void initListeners() {
        addFocusListener("Income", incomeFields);
        addFocusListener("Spending", spendingFields);
        addActionListeners();
        exitButton.addActionListener(e -> System.exit(0));
        calculateButton.addActionListener(e -> calculateAll());
        undoButton.addActionListener(e -> undo());
    }

    private void addComponent(Component component, int row, int column, int type) {
        setCommonLayoutConstraints(row, column);

        switch (type) {
            case COLUMN_LABEL:
                add(component, layoutConstraints);
                break;
            case COLUMN_TEXT_FIELD:
                ((JTextField) component).setHorizontalAlignment(SwingConstants.RIGHT);
                add(component, layoutConstraints);
                break;
            case COLUMN_COMBO_BOX:
                add(component, layoutConstraints);
                break;
            default:
        }
    }

    private void setCommonLayoutConstraints(int gridrow, int gridcol) {
        layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
        layoutConstraints.gridx = gridcol;
        layoutConstraints.gridy = gridrow;
    }

    private JTextField createTextField() {
        JTextField textField = new JTextField("", 10);
        textField.setHorizontalAlignment(SwingConstants.RIGHT);
        return textField;
    }

    private JTextField createReadOnlyTextField() {
        JTextField textField = new JTextField("0", 10);
        textField.setHorizontalAlignment(SwingConstants.RIGHT);
        textField.setEditable(false);
        return textField;
    }

    private void addLabel(String labelText, int row, int column) {
        JLabel label = new JLabel(labelText);
        addComponent(label, row, column, COLUMN_LABEL);
    }

    private void addTextField(JTextField textField, int row, int column) {
        addComponent(textField, row, column, COLUMN_TEXT_FIELD);
    }

    private void addComboBox(JComboBox<?> comboBox, int row, int column) {
        addComponent(comboBox, row, column, COLUMN_COMBO_BOX);
    }

    public void calculateAll(){
        double income = calculateTotalIncome();
        double spending = calculateTotalSpending();
        calculateCashFlow(income, spending);
    }

    public double calculateTotalIncome() {
        double totalIncome = 0.0;
        for (JTextField field : incomeFields) {
            totalIncome += getTextFieldValue(field);
        }

        totalIncomeField.setText(String.format("%.2f", totalIncome));
        return totalIncome;
    }

    public double calculateTotalSpending() {
        double totalSpending = 0.0;
        for (JTextField field : spendingFields) {
            totalSpending += getTextFieldValue(field);
        }

        totalSpendingField.setText(String.format("%.2f", totalSpending));
        return totalSpending;
    }

    private void calculateCashFlow(double income, double spending) {
        TimePeriod incomeTimePeriod = (TimePeriod) incomeGranularity.getSelectedItem();
        TimePeriod spendingTimePeriod = (TimePeriod) expenseGranularity.getSelectedItem();
        TimePeriod overallTimePeriod = (TimePeriod) overallGranularity.getSelectedItem();

        double cashState = (income / incomeTimePeriod.getValue()) - (spending / spendingTimePeriod.getValue());

        if (cashState < 0) {
            overallField.setForeground(Color.RED);
        } else {
            overallField.setForeground(Color.BLACK);
        }
        overallField.setText(String.format("%.2f", cashState * overallTimePeriod.getValue()));
    }

    protected double getTextFieldValue(JTextField field) {
        String fieldString = field.getText();
        if (fieldString.isBlank()) {
            field.setText("0");
            return 0;
        } else {
            try {
                return Double.parseDouble(fieldString);
            } catch (NumberFormatException ex) {
                field.setText("");
                JOptionPane.showMessageDialog(topLevelFrame, "Please enter a valid number");
                return Double.NaN;
            }
        }
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Budget Calculator");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        BudgetBase newContentPane = new BudgetBase(frame);
        newContentPane.setOpaque(true);
        frame.setContentPane(newContentPane);

        frame.pack();
        frame.setVisible(true);
    }

    public void saveState() {
        BudgetState currentState = getCurrentState();
        if(!undoStack.isEmpty() && (undoStack.peek().equals(currentState)))
            return;

        undoStack.push(currentState);
    }

    public void undo() {
        if(!undoStack.isEmpty() && (undoStack.peek().equals(getCurrentState())))
            undoStack.pop();

        if (!undoStack.isEmpty()) {
            BudgetState previousState = undoStack.pop();
            setFieldsFromState(previousState);
            calculateAll();
        }
    }

    private BudgetState getCurrentState(){
        double[] incomeValues = new double[incomeFields.length];
        double[] spendingValues = new double[spendingFields.length];

        for (int i = 0; i < incomeFields.length; i++) {
            incomeValues[i] = getTextFieldValue(incomeFields[i]);
        }

        for (int i = 0; i < spendingFields.length; i++) {
            spendingValues[i] = getTextFieldValue(spendingFields[i]);
        }

        return new BudgetState(incomeValues, spendingValues);
    }

    private void setFieldsFromState(BudgetState state) {
        double[] incomeValues = state.getIncomeValues();
        double[] spendingValues = state.getSpendingValues();

        for (int i = 0; i < incomeFields.length; i++) {
            incomeFields[i].setText(String.valueOf(incomeValues[i]));
        }

        for (int i = 0; i < spendingFields.length; i++) {
            spendingFields[i].setText(String.valueOf(spendingValues[i]));
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(BudgetBase::createAndShowGUI);
    }
}