# Amazon-Clone0.2#  – Java Swing Application

This project is a Java Swing–based **Amazon Clone** that simulates a simple e‑commerce shopping experience. It includes product display, image support, cart management, and uses DSA concepts to improve performance and structure.

---

## Features

###  Product Display

* Shows product list using Swing components.
* Each product has:

  * **Name**
  * **Price (₹)**
  * **Category**
  * **Product Image**

###  Add to Cart

* Each product has an "Add to Cart" button.
* Items added to the cart appear in a separate cart panel.
* Uses **ArrayList / LinkedList** for cart data storage.

###  Cart Management

* Displays all cart items.
* Calculates total price dynamically.
* Allows removing items (if implemented).

###  Image Support

* Product images are displayed using:

  ```java
  new ImageIcon(getClass().getResource("images/imageName.png"));
  ```
* Four sample images added (based on user-provided photos).

###  UI / Theme

* Light Blue Theme using:

  ```java
  getContentPane().setBackground(new Color(173, 216, 230));
  ```
* Clean layout with GridLayout / BorderLayout combinations.

###  DSA Used

* **ArrayList** for product storage.
* **LinkedList** (optional improvement) for cart operations.
* Searching a product by ID using **Linear Search**.
* Sorting products by price **(optional future feature)**.

---

##  Project Structure

```
AmazonClone/
│
├── AmazonClone.java
├── images/
│   ├── img1.png
│   ├── img2.png
│   ├── img3.png
│   └── img4.png
└── README.md
```

---

##  How to Run

1. Install Java JDK 8 or above.
2. Place all images inside `/images` folder.
3. Compile:

   ```bash
   javac AmazonClone.java
   ```
4. Run:

   ```bash
   java AmazonClone
   ```

---

##  Future Enhancements

* Login & Signup system.
* Checkout system with receipt generation.
* Product search bar.
* Sorting / filtering using DSA.
* Database integration using MySQL.

---

##  Author

Developed by **Ratanjet Kumar Singh**.

Feel free to upgrade and customize more features!
