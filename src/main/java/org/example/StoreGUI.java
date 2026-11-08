package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class StoreGUI extends JFrame {

    private JTable table;
    private DefaultTableModel tableModel;
    private JSpinner stockSpinner;

    String url = "jdbc:mysql://localhost:3306/store_db";
    String user = "root";
    String password = "admin"; // <-- CHANGE THIS

    public StoreGUI() {
        setTitle("My Store Inventory Manager");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        String[] columns = {"ID", "Name", "Category", "Price ($)", "Stock"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        table.setAutoCreateRowSorter(true);

        JScrollPane scrollPane = new JScrollPane(table);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton btnAddItem = new JButton("Add New Item");
        btnAddItem.setBackground(new Color(200, 255, 200)); // Light green
        btnAddItem.addActionListener(e -> showAddItemDialog());

        JLabel lblAmount = new JLabel("Amount:");
        stockSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 9999, 1));

        JButton btnAddStock = new JButton("Add Stock (+)");
        btnAddStock.setBackground(new Color(200, 230, 255)); // Light blue
        btnAddStock.addActionListener(e -> updateStock(1)); // 1 means add

        JButton btnRemoveStock = new JButton("Remove Stock (-)");
        btnRemoveStock.setBackground(new Color(255, 200, 200)); // Light red
        btnRemoveStock.addActionListener(e -> updateStock(-1)); // -1 means remove

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> loadDataFromDatabase());
        bottomPanel.add(btnAddItem);
        bottomPanel.add(Box.createHorizontalStrut(20)); // Adds empty space
        bottomPanel.add(lblAmount);
        bottomPanel.add(stockSpinner);
        bottomPanel.add(btnAddStock);
        bottomPanel.add(btnRemoveStock);
        bottomPanel.add(Box.createHorizontalStrut(20));
        bottomPanel.add(btnRefresh);

        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        loadDataFromDatabase();
    }

    private void showAddItemDialog() {
        JTextField txtName = new JTextField(15);
        JTextField txtCategory = new JTextField(10);
        JTextField txtPrice = new JTextField(5);
        JTextField txtStock = new JTextField(5);

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("Name:")); panel.add(txtName);
        panel.add(new JLabel("Category:")); panel.add(txtCategory);
        panel.add(new JLabel("Price:")); panel.add(txtPrice);
        panel.add(new JLabel("Initial Stock:")); panel.add(txtStock);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add New Item",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                String name = txtName.getText().trim();
                String category = txtCategory.getText().trim();
                double price = Double.parseDouble(txtPrice.getText().trim());
                int stock = Integer.parseInt(txtStock.getText().trim());

                if (name.isEmpty() || price < 0 || stock < 0) {
                    JOptionPane.showMessageDialog(this, "Please fill all fields correctly!");
                    return;
                }

                String sql = "INSERT INTO items (name, category, price, stock_quantity) VALUES (?, ?, ?, ?)";
                try (Connection conn = DriverManager.getConnection(url, user, password);
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {

                    pstmt.setString(1, name);
                    pstmt.setString(2, category);
                    pstmt.setDouble(3, price);
                    pstmt.setInt(4, stock);
                    pstmt.executeUpdate();
                }

                loadDataFromDatabase();
                JOptionPane.showMessageDialog(this, "Item added successfully!");

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Price and Stock must be valid numbers!");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage());
            }
        }
    }

    private void updateStock(int direction) {
        int selectedRow = table.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item in the table first!");
            return;
        }

        int modelRow = table.convertRowIndexToModel(selectedRow);
        int id = (int) tableModel.getValueAt(modelRow, 0);
        int currentStock = (int) tableModel.getValueAt(modelRow, 4);

        int amount = (int) stockSpinner.getValue();
        int finalChange = amount * direction;


        if (currentStock + finalChange < 0) {
            JOptionPane.showMessageDialog(this, "Error: Cannot remove more stock than what is available!");
            return;
        }

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            String sql = "UPDATE items SET stock_quantity = stock_quantity + ? WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, finalChange);
                pstmt.setInt(2, id);
                pstmt.executeUpdate();
            }

            loadDataFromDatabase();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage());
        }
    }


    private void loadDataFromDatabase() {
        tableModel.setRowCount(0);

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM items")) {

            while (rs.next()) {
                Object[] row = {
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getDouble("price"),
                        rs.getInt("stock_quantity")
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) {}

            new StoreGUI().setVisible(true);
        });
    }
}