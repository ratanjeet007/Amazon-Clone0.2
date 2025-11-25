# Amazon Clone – Java Swing Application

##  Overview

This project is a **Java Swing–based Amazon Clone** built using:

* **OOP** (Inheritance, Polymorphism, Interfaces, Exception Handling)
* **DSA Concepts** (ArrayList, HashMap, Generics)
* **MVC Architecture**
* **Multithreading + Synchronization**
* **JDBC + DAO Layer (Database Connectivity)**
* **Modular GUI (Swing)**

The application allows users to:

* Browse products
* Add/remove items from cart
* Update quantity
* Checkout using **UPI/COD**
* Store orders into a database (optional)

---

##  Features

###  Product System

* Stored using **ArrayList**
* Fast lookup using **HashMap (O(1))**
* Supports **electronics, fashion, accessories** etc.
* Includes **polymorphism** via product subclasses:

  * `ElectronicProduct`
  * `FashionProduct`

###  Cart System

* Uses synchronized methods (thread-safe)
* Stores items as `CartItem`
* Supports:

  * Add product
  * Remove product
  * Update quantity
  * Calculate totals

### OOP Concepts Used

| Concept                | Implementation                                   |
| ---------------------- | ------------------------------------------------ |
| **Inheritance**        | ElectronicProduct, FashionProduct extend Product |
| **Polymorphism**       | Overridden displayName()                         |
| **Interface**          | Storable for DAO classes                         |
| **Exception Handling** | InvalidProductException                          |

###  Multithreading

* Order processing runs in a **background thread**.
* Uses `synchronized` in Cart class.

###  Database Connectivity (JDBC)

* MySQL (can be changed to SQLite)
* DAO classes:

  * ProductDAO
  * OrderDAO
* Saves order info from checkout.

---

## Project Structure (Single-file Demonstration)

```
AmazonClone.java
```

Contains:

* Models
* Controllers
* Views (Swing)
* DAO Classes
* Multithreading Logic
* Main Method

---

##  MVC Architecture

### Model

* Product
* Cart
* CartItem
* Checkout logic

### View

* Product Panel
* Cart List UI
* Checkout Dialog

### Controller

* ProductController
* CartController
* CheckoutController

---

##  DSA Concepts Used

* **ArrayList** → Product display
* **HashMap** → Cart mapping, product lookup
* **Generics** → GenericRepository<T>
* **Custom Classes** → CartItem, Product subclasses

---

##  UML Diagram (Text Representation)

```
Product <|-- ElectronicProduct
Product <|-- FashionProduct

AmazonClone --> ProductController
AmazonClone --> CartController
AmazonClone --> CheckoutController

Cart --> CartItem (1..*)
ProductRepo --> Product (1..*)
CheckoutController --> OrderDAO
```

---

##  Flowchart (Text Version)

```
START
 ↓
Load Products
 ↓
User Browses Products → Add to Cart
 ↓
Update Cart → Modify Quantity / Remove Item
 ↓
Proceed to Checkout
 ↓
Validate Inputs
 ↓
Show Summary
 ↓
Save Order to DB (Optional)
 ↓
Process Order (Thread)
 ↓
Order Completed → Cart Cleared
END
```

---

## How to Run

### 1️⃣ Compile

```
javac AmazonClone.java
```

### 2️⃣ Run

```
java AmazonClone
```

### 3️⃣ MySQL Setup (Optional)

Create database:

```
CREATE DATABASE amazon_clone;
```

Create table:

```
CREATE TABLE orders (
  id INT AUTO_INCREMENT PRIMARY KEY,
  customer_name VARCHAR(50),
  payment_method VARCHAR(20),
  total_amount DOUBLE
);
```

---

##  Future Enhancements

* Login/Signup system
* Product images loader
* Search bar
* Order history panel
* Admin dashboard

---

##  Author: **Ratanjeet Kumar Singh**

A complete academic + practical project for Java Swing + DSA + OOP + DB connectivity.

If you want, I can also generate:
A **PDF project documentation**
A **PPT**
ZIP file with **full code** + **images**
Full modular version with folders (`model/`, `view/`, `controller/`, `dao/`)


