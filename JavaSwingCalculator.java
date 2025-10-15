/*
Java Swing Calculator - Beginner-friendly single-file app
File: JavaSwingCalculator.java

This file contains a complete GUI calculator using Java Swing.
It supports: 0-9 digits, decimal point, +, -, ×, ÷, =, C (clear), ⌫ (backspace), ± (toggle sign).

STEP-BY-STEP GUIDE (read before exploring the code):
- The program uses a JFrame as the main window and a JTextField for the display.
- Buttons are created for digits and operations and wired to a single ActionListener.
- When you press a digit/decimal, the digit is appended to the display unless the previous step cleared it.
- When you press an operator (+ - * /), the calculator stores the current number and the chosen operator.
- Pressing = computes the result using the stored operator and operands.
- Edge cases handled: dividing by zero (shows error), preventing multiple decimals in a number.

---- CODE START ----
*/

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;

public class JavaSwingCalculator extends JFrame implements ActionListener {
    private final JTextField display;
    private double firstOperand = 0;
    private String operator = "";
    private boolean startNewNumber = true; // whether next digit press starts a new number
    private final DecimalFormat df = new DecimalFormat("0.##########"); // trim trailing zeros

    public JavaSwingCalculator() {
        setTitle("Calculator");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(360, 480);
        setLocationRelativeTo(null); // center
        setLayout(new BorderLayout(8, 8));

        // Display
        display = new JTextField("0");
        display.setEditable(false);
        display.setHorizontalAlignment(SwingConstants.RIGHT);
        display.setFont(new Font("SansSerif", Font.BOLD, 32));
        display.setBackground(Color.WHITE);
        display.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(display, BorderLayout.NORTH);

        // Buttons panel
        JPanel buttons = new JPanel();
        buttons.setLayout(new GridLayout(5, 4, 6, 6));

        String[] labels = {
            "C", "⌫", "±", "÷",
            "7", "8", "9", "×",
            "4", "5", "6", "-",
            "1", "2", "3", "+",
            "0", "0", ".", "="
        };
        // NOTE: use "0" twice so we can create a wider 0 button visually later by spanning cells

        for (String label : labels) {
            // create button
            JButton b = new JButton(label);
            b.setFont(new Font("SansSerif", Font.PLAIN, 22));
            b.addActionListener(this);

            // Style some buttons
            if (label.equals("C")) {
                b.setForeground(Color.RED.darker());
            } else if (label.equals("=") || label.equals("+") || label.equals("-") || label.equals("×") || label.equals("÷")) {
                b.setForeground(Color.BLUE.darker());
            }

            buttons.add(b);
        }

        // Make 0 button span two cells horizontally by placing a panel wrapper
        // We'll remove the extra "0" button that acted as a placeholder and replace two cells with one big button.
        // To keep the code simple and single-file, after adding all components we will adjust the grid.

        add(buttons, BorderLayout.CENTER);

        // Improve layout: make the last row's first two cells one wide 0 button.
        // Simpler approach: rebuild the bottom row manually.
        JPanel bottom = new JPanel(new GridLayout(1, 4, 6, 6));
        JButton zero = new JButton("0");
        zero.setFont(new Font("SansSerif", Font.PLAIN, 22));
        zero.addActionListener(this);

        JButton dot = new JButton(".");
        dot.setFont(new Font("SansSerif", Font.PLAIN, 22));
        dot.addActionListener(this);

        JButton equals = new JButton("=");
        equals.setFont(new Font("SansSerif", Font.PLAIN, 22));
        equals.addActionListener(this);

        // Remove the existing bottom row components we added earlier (we will instead re-create the whole button area more cleanly)
        getContentPane().remove(buttons);

        // New clean button grid using GridBagLayout for flexibility
        JPanel grid = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.weightx = 1;
        gbc.weighty = 1;

        String[][] layout = {
            {"C", "⌫", "±", "÷"},
            {"7", "8", "9", "×"},
            {"4", "5", "6", "-"},
            {"1", "2", "3", "+"},
            {"0", "0", ".", "="}
        };

        for (int r = 0; r < layout.length; r++) {
            for (int c = 0; c < layout[r].length; c++) {
                String label = layout[r][c];
                // skip the placeholder second "0"
                if (r == 4 && c == 1) continue;

                gbc.gridx = c;
                gbc.gridy = r;

                JButton btn = new JButton(label);
                btn.setFont(new Font("SansSerif", Font.PLAIN, 22));
                btn.addActionListener(this);
                if (label.equals("C")) btn.setForeground(Color.RED.darker());
                if (label.equals("=") || label.equals("+") || label.equals("-") || label.equals("×") || label.equals("÷")) btn.setForeground(Color.BLUE.darker());

                if (r == 4 && c == 0) {
                    // Make the 0 button span two columns
                    gbc.gridwidth = 2;
                    grid.add(btn, gbc);
                    gbc.gridwidth = 1;
                } else {
                    grid.add(btn, gbc);
                }
            }
        }

        add(grid, BorderLayout.CENTER);

        // Keyboard support (optional but helpful)
        addKeyBindings(grid);

        setVisible(true);
    }

    private void addKeyBindings(JComponent comp) {
        // map keys to button actions using InputMap/ActionMap
        String digits = "0123456789";
        for (char d : digits.toCharArray()) {
            comp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(d), "digit" + d);
            comp.getActionMap().put("digit" + d, new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    appendDigit(String.valueOf(d));
                }
            });
        }
        comp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "equals");
        comp.getActionMap().put("equals", new AbstractAction() { public void actionPerformed(ActionEvent e) { calculateResult(); } });
        comp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('.'), "dot");
        comp.getActionMap().put("dot", new AbstractAction() { public void actionPerformed(ActionEvent e) { appendDot(); } });
        // operators
        comp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('+'), "plus");
        comp.getActionMap().put("plus", new AbstractAction() { public void actionPerformed(ActionEvent e) { setOperator("+"); } });
        comp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('-'), "minus");
        comp.getActionMap().put("minus", new AbstractAction() { public void actionPerformed(ActionEvent e) { setOperator("-"); } });
        comp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('*'), "mul");
        comp.getActionMap().put("mul", new AbstractAction() { public void actionPerformed(ActionEvent e) { setOperator("×"); } });
        comp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('/'), "div");
        comp.getActionMap().put("div", new AbstractAction() { public void actionPerformed(ActionEvent e) { setOperator("÷"); } });
        comp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "back");
        comp.getActionMap().put("back", new AbstractAction() { public void actionPerformed(ActionEvent e) { backspace(); } });
        comp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "clear");
        comp.getActionMap().put("clear", new AbstractAction() { public void actionPerformed(ActionEvent e) { clearAll(); } });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd = ((JButton) e.getSource()).getText();

        if (cmd.matches("[0-9]")) {
            appendDigit(cmd);
        } else if (cmd.equals(".")) {
            appendDot();
        } else if (cmd.equals("C")) {
            clearAll();
        } else if (cmd.equals("⌫")) {
            backspace();
        } else if (cmd.equals("±")) {
            toggleSign();
        } else if (cmd.equals("+") || cmd.equals("-") || cmd.equals("×") || cmd.equals("÷")) {
            setOperator(cmd);
        } else if (cmd.equals("=")) {
            calculateResult();
        }
    }

    private void appendDigit(String digit) {
        if (startNewNumber) {
            // start fresh
            if (digit.equals("0")) {
                display.setText("0");
                startNewNumber = false; // still consider that we're editing this number
                return;
            }
            display.setText(digit);
            startNewNumber = false;
        } else {
            String text = display.getText();
            if (text.equals("0")) text = ""; // drop leading zero
            display.setText(text + digit);
        }
    }

    private void appendDot() {
        if (startNewNumber) {
            display.setText("0.");
            startNewNumber = false;
            return;
        }
        if (!display.getText().contains(".")) {
            display.setText(display.getText() + ".");
        }
    }

    private void clearAll() {
        display.setText("0");
        firstOperand = 0;
        operator = "";
        startNewNumber = true;
    }

    private void backspace() {
        if (startNewNumber) return;
        String text = display.getText();
        if (text.length() <= 1) {
            display.setText("0");
            startNewNumber = true;
        } else {
            display.setText(text.substring(0, text.length() - 1));
        }
    }

    private void toggleSign() {
        String text = display.getText();
        if (text.equals("0")) return;
        if (text.startsWith("-")) display.setText(text.substring(1));
        else display.setText("-" + text);
    }

    private void setOperator(String op) {
        try {
            firstOperand = Double.parseDouble(display.getText());
        } catch (NumberFormatException ex) {
            display.setText("Error");
            startNewNumber = true;
            operator = "";
            return;
        }
        operator = op;
        startNewNumber = true; // next digit starts new number
    }

    private void calculateResult() {
        if (operator.isEmpty()) return; // nothing to do
        double secondOperand;
        try {
            secondOperand = Double.parseDouble(display.getText());
        } catch (NumberFormatException ex) {
            display.setText("Error");
            startNewNumber = true;
            operator = "";
            return;
        }

        double result = 0.0;
        boolean error = false;
        switch (operator) {
            case "+": result = firstOperand + secondOperand; break;
            case "-": result = firstOperand - secondOperand; break;
            case "×": result = firstOperand * secondOperand; break;
            case "÷":
                if (secondOperand == 0) {
                    error = true;
                } else {
                    result = firstOperand / secondOperand;
                }
                break;
            default: return;
        }

        if (error) {
            display.setText("Cannot divide by 0");
        } else {
            display.setText(df.format(result));
        }

        // reset state
        operator = "";
        startNewNumber = true;
    }

    public static void main(String[] args) {
        // Ensure GUI updates happen on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> new JavaSwingCalculator());
    }
}

/*
---- END OF FILE ----
*/
