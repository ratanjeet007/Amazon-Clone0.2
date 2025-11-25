import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.sql.*;

public class AmazonClone extends JFrame {

   
    interface Storable { void saveToDB() throws Exception; }

    static class InvalidProductException extends Exception {
        InvalidProductException(String msg) { super(msg); }
    }

    static class Product {
        final int id;
        final String name;
        final double price;
        final String category;
        final String imgName;

        Product(int id, String name, double price, String category, String imgName) throws InvalidProductException {
            if (price <= 0) throw new InvalidProductException("Price must be positive for " + name);
            if (name == null || name.trim().isEmpty()) throw new InvalidProductException("Name required");
            this.id = id; this.name = name; this.price = price; this.category = category; this.imgName = imgName;
        }

        public String displayName() { return name + " - ₹" + String.format("%.2f", price); }
    }

    static class ElectronicProduct extends Product {
        ElectronicProduct(int id, String name, double price, String imgName) throws InvalidProductException {
            super(id, name, price, "Electronics", imgName);
        }
        @Override public String displayName() { return super.displayName() + " [Electronics]"; }
    }

    static class FashionProduct extends Product {
        FashionProduct(int id, String name, double price, String imgName) throws InvalidProductException {
            super(id, name, price, "Fashion", imgName);
        }
        @Override public String displayName() { return super.displayName() + " [Fashion]"; }
    }

    static class CartItem {
        final Product product;
        int quantity;
        CartItem(Product p, int q){ this.product = p; this.quantity = q; }
        double getTotal() { return product.price * quantity; }
    }

    static class GenericRepository<T> {
        private final ArrayList<T> list = new ArrayList<>();
        void add(T item) { list.add(item); }
        Collection<T> getAll() { return Collections.unmodifiableList(list); }
        boolean isEmpty() { return list.isEmpty(); }
    }

    static class ProductRepository {
        private final ArrayList<Product> list = new ArrayList<>();
        private final HashMap<Integer, Product> map = new HashMap<>();
        void add(Product p){ list.add(p); map.put(p.id, p); }
        Product findById(int id){ return map.get(id); }
        Collection<Product> getAll(){ return Collections.unmodifiableList(list); }
    }

    static class Cart {
        private final HashMap<Integer, CartItem> items = new HashMap<>();

        public synchronized void addProduct(Product p) {
            if (items.containsKey(p.id)) items.get(p.id).quantity++;
            else items.put(p.id, new CartItem(p,1));
        }

        public synchronized void removeProduct(int id) { items.remove(id); }

        public synchronized void updateQuantity(int id, int qty) {
            if (!items.containsKey(id)) return;
            if (qty <= 0) items.remove(id);
            else items.get(id).quantity = qty;
        }

        public synchronized Collection<CartItem> getItems() { return new ArrayList<>(items.values()); }

        public synchronized double getTotal() {
            double total = 0.0;
            for (CartItem ci : items.values()) total += ci.getTotal();
            return total;
        }

        public synchronized void clear() { items.clear(); }

        public synchronized boolean isEmpty() { return items.isEmpty(); }
    }

    static class DatabaseConnection {
        static Connection getConnection() throws Exception {
            // CHANGE credentials if you want DB support
            String url = "jdbc:mysql://localhost:3306/amazon_clone?useSSL=false&allowPublicKeyRetrieval=true";
            String user = "root";
            String pass = "password";
            try { Class.forName("com.mysql.cj.jdbc.Driver"); } catch (ClassNotFoundException ignored){}
            return DriverManager.getConnection(url, user, pass);
        }
    }

    static class ProductDAO implements Storable {
        private final Product p;
        ProductDAO(Product p){ this.p = p; }
        @Override public void saveToDB() throws Exception {
            Connection c = DatabaseConnection.getConnection();
            String sql = "INSERT INTO products(id,name,price,category,img) VALUES(?,?,?,?,?)";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1,p.id); ps.setString(2,p.name); ps.setDouble(3,p.price);
                ps.setString(4,p.category); ps.setString(5,p.imgName); ps.executeUpdate();
            } finally { c.close(); }
        }
    }

    static class OrderDAO {
        static void saveOrder(String customer, String payment, double total) throws Exception {
            Connection c = DatabaseConnection.getConnection();
            String sql = "INSERT INTO orders(customer_name,payment_method,total_amount) VALUES(?,?,?)";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1,customer); ps.setString(2,payment); ps.setDouble(3,total); ps.executeUpdate();
            } finally { c.close(); }
        }
    }


    static class OrderProcessor implements Runnable {
        private final String customer;
        private final double total;
        OrderProcessor(String customer, double total){ this.customer = customer; this.total = total; }
        @Override public void run(){
            try {
                System.out.println("[OrderProcessor] Processing order for " + customer + " | ₹" + String.format("%.2f", total));
                Thread.sleep(900);
                System.out.println("[OrderProcessor] Payment verified");
                Thread.sleep(700);
                System.out.println("[OrderProcessor] Inventory reserved");
                Thread.sleep(600);
                System.out.println("[OrderProcessor] Order completed for " + customer);
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
    }


    class ProductController {
        private final ProductRepository repo;
        ProductController(ProductRepository repo){ this.repo = repo; }
        void loadSample() {
            try {
                repo.add(new ElectronicProduct(1,"Laptop",55000,"laptop.png"));
                repo.add(new ElectronicProduct(2,"Smartphone",29999,"smartphone.png"));
                repo.add(new Product(3,"Headphones",2499,"Accessories","headphones.png"));
                repo.add(new FashionProduct(4,"Shoes",1999,"shoes.png"));
                repo.add(new Product(5,"Backpack",999,"Bags","backpack.png"));
            } catch (InvalidProductException e) { JOptionPane.showMessageDialog(AmazonClone.this, "Load error: "+e.getMessage()); }
        }
        Collection<Product> all(){ return repo.getAll(); }
        Product find(int id){ return repo.findById(id); }
    }

    class CartController {
        private final Cart cart;
        CartController(Cart cart){ this.cart = cart; }
        void add(Product p){ cart.addProduct(p); }
        void remove(int id){ cart.removeProduct(id); }
        void update(int id,int q){ cart.updateQuantity(id,q); }
        Collection<CartItem> items(){ return cart.getItems(); }
        double total(){ return cart.getTotal(); }
        void clear(){ cart.clear(); }
        boolean isEmpty(){ return cart.isEmpty(); }
    }

    class CheckoutController {
        private final CartController cartCtrl;
        CheckoutController(CartController c){ this.cartCtrl = c; }
        boolean validate(String name, String addr, String phone, String upi, String pay){
            if (name==null || name.trim().isEmpty()) return false;
            if (addr==null || addr.trim().isEmpty()) return false;
            if (phone==null || phone.trim().length()!=10) return false;
            if ("UPI".equals(pay)) return upi!=null && upi.contains("@");
            return true;
        }
        void process(String name, String payment){
            double total = cartCtrl.total();
            // try save to DB (optional)
            try { OrderDAO.saveOrder(name,payment,total); System.out.println("Order saved to DB."); }
            catch (Exception ex) { System.err.println("DB save failed: " + ex.getMessage()); }
            // start background processing
            new Thread(new OrderProcessor(name,total)).start();
            cartCtrl.clear();
        }
    }

    private final ProductRepository productRepo = new ProductRepository();
    private final ProductController productController = new ProductController(productRepo);
    private final Cart cart = new Cart();
    private final CartController cartController = new CartController(cart);
    private final CheckoutController checkoutController = new CheckoutController(cartController);
    private final DefaultListModel<String> cartModel = new DefaultListModel<>();
    private final JList<String> cartList = new JList<>(cartModel);
    private final JLabel totalLabel = new JLabel("Total: ₹0.00");
    private final JPanel productPanel = new JPanel(new GridLayout(0,2,10,10));

    public AmazonClone() {
        super("Amazon Clone");
        setSize(980,620);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8,8));
        getContentPane().setBackground(new Color(220,235,255));
        setLocationRelativeTo(null);

        JLabel header = new JLabel("Amazon Clone 0.2", JLabel.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 28));
        header.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        add(header, BorderLayout.NORTH);

        productController.loadSample();
        loadProductsToUI();

        JScrollPane productScroll = new JScrollPane(productPanel);
        add(productScroll, BorderLayout.CENTER);

        JPanel right = new JPanel(new BorderLayout(6,6));
        right.setPreferredSize(new Dimension(340, 520));
        JLabel cartTitle = new JLabel("Shopping Cart", JLabel.CENTER);
        cartTitle.setFont(new Font("Arial", Font.BOLD, 18));
        right.add(cartTitle, BorderLayout.NORTH);
        right.add(new JScrollPane(cartList), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new GridLayout(0,1,6,6));
        totalLabel.setFont(new Font("Arial", Font.BOLD, 16));
        bottom.add(totalLabel);

        JPanel btns = new JPanel(new GridLayout(1,2,6,6));
        JButton removeBtn = new JButton("Remove");
        JButton checkoutBtn = new JButton("Checkout");
        btns.add(removeBtn); btns.add(checkoutBtn);
        bottom.add(btns);

        right.add(bottom, BorderLayout.SOUTH);
        add(right, BorderLayout.EAST);

        removeBtn.addActionListener(e -> {
            int idx = cartList.getSelectedIndex();
            if (idx == -1) { JOptionPane.showMessageDialog(this, "Select item to remove."); return; }
            String sel = cartModel.get(idx);
            // format: [id] name xqty - ₹xx.xx
            int a = sel.indexOf('['), b = sel.indexOf(']');
            if (a!=-1 && b!=-1){
                try { int id = Integer.parseInt(sel.substring(a+1,b)); cartController.remove(id); refreshCartView(); }
                catch (NumberFormatException ex){ /* ignore */ }
            }
        });

        checkoutBtn.addActionListener(e -> openCheckoutDialog());

        setVisible(true);
    }

    private void loadProductsToUI(){
        productPanel.removeAll();
        for (Product p : productController.all()){
            JPanel card = new JPanel(new BorderLayout(4,4));
            card.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            JLabel name = new JLabel(p.displayName(), JLabel.CENTER); name.setFont(new Font("SansSerif", Font.BOLD,14));
            card.add(name, BorderLayout.NORTH);
            JLabel img = new JLabel("[img: "+p.imgName+"]", JLabel.CENTER); img.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
            card.add(img, BorderLayout.CENTER);
            JPanel south = new JPanel(new GridLayout(1,2,4,4));
            JButton add = new JButton("Add to Cart");
            JButton info = new JButton("Info");
            south.add(add); south.add(info);
            card.add(south, BorderLayout.SOUTH);

            add.addActionListener(e -> { cartController.add(p); refreshCartView(); });
            info.addActionListener(e -> JOptionPane.showMessageDialog(this,
                    p.name + "\nCategory: " + p.category + "\nPrice: ₹" + String.format("%.2f", p.price),
                    "Product Info", JOptionPane.INFORMATION_MESSAGE));

            productPanel.add(card);
        }
        productPanel.revalidate(); productPanel.repaint();
    }

    private void refreshCartView(){
        cartModel.clear();
        for (CartItem ci : cartController.items()){
            String entry = "["+ci.product.id+"] " + ci.product.name + " x" + ci.quantity + " - ₹" + String.format("%.2f", ci.getTotal());
            cartModel.addElement(entry);
        }
        totalLabel.setText("Total: ₹" + String.format("%.2f", cartController.total()));
    }

    private void openCheckoutDialog(){
        if (cartController.isEmpty()) { JOptionPane.showMessageDialog(this, "Cart is empty."); return; }

        JTextField name = new JTextField();
        JTextField addr = new JTextField();
        JTextField phone = new JTextField();
        JTextField upi = new JTextField();
        String[] pays = {"UPI","Cash on Delivery"};
        JComboBox<String> payBox = new JComboBox<>(pays);

        JPanel panel = new JPanel(new GridLayout(0,2,6,6));
        panel.add(new JLabel("Name:")); panel.add(name);
        panel.add(new JLabel("Address:")); panel.add(addr);
        panel.add(new JLabel("Phone (10 digits):")); panel.add(phone);
        panel.add(new JLabel("Payment:")); panel.add(payBox);
        panel.add(new JLabel("UPI ID (if UPI):")); panel.add(upi);

        int res = JOptionPane.showConfirmDialog(this, panel, "Checkout", JOptionPane.OK_CANCEL_OPTION);
        if (res != JOptionPane.OK_OPTION) return;

        String nm = name.getText().trim(); String ad = addr.getText().trim(); String ph = phone.getText().trim();
        String pm = payBox.getSelectedItem().toString(); String upiId = upi.getText().trim();

        if (!checkoutController.validate(nm,ad,ph,upiId,pm)) {
            JOptionPane.showMessageDialog(this, "Invalid inputs. Check name, address, phone and UPI format.");
            return;
        }

        StringBuilder sb = new StringBuilder("ORDER SUMMARY:\n\n");
        for (CartItem ci : cartController.items()) sb.append(ci.product.name).append(" x").append(ci.quantity).append(" = ₹").append(String.format("%.2f",ci.getTotal())).append("\n");
        sb.append("\nTotal: ₹").append(String.format("%.2f",cartController.total())).append("\nPayment: ").append(pm).append("\n\nProceed?");
        int conf = JOptionPane.showConfirmDialog(this, sb.toString(), "Confirm", JOptionPane.YES_NO_OPTION);
        if (conf != JOptionPane.YES_OPTION) return;

        // process
        checkoutController.process(nm, pm);
        JOptionPane.showMessageDialog(this, "Order placed. Thank you, " + nm + "!");
        refreshCartView();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AmazonClone());
    }
}
