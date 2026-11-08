   CREATE DATABASE store_db;
   USE store_db;

   CREATE TABLE items (
       id INT AUTO_INCREMENT PRIMARY KEY,
       name VARCHAR(100) NOT NULL,
       category VARCHAR(50),
       price DECIMAL(10, 2) NOT NULL,
       stock_quantity INT NOT NULL
   );

   INSERT INTO items (name, category, price, stock_quantity) VALUES
   ('Laptop', 'Electronics', 999.99, 10),
   ('Wireless Mouse', 'Electronics', 25.50, 50),
   ('Notebook', 'Stationery', 5.99, 200),
   ('Gel Pen', 'Stationery', 1.50, 500),
   ('Desk Lamp', 'Furniture', 35.00, 15);