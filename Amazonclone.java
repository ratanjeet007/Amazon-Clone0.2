import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import java.sql.*;

public class AmazonCloneServlet extends HttpServlet {

    public static class Product {
        int id; String name; double price;
        public Product(int id, String name, double price) {
            if(price<=0) throw new IllegalArgumentException("Invalid price");
            this.id=id; this.name=name; this.price=price;
        }
    }

    public static class CartItem {
        Product product; int quantity;
        public CartItem(Product p){ this.product=p; quantity=1; }
        public void increment(){ quantity++; }
        public double getTotal(){ return product.price*quantity; }
    }

    private Connection getConnection() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/amazon_clone",
            "root",
            "password"
        );
    }

    private void saveOrder(String customer, Map<Integer,CartItem> cart) throws Exception {
        double total = 0;
        for(CartItem ci: cart.values()) total += ci.getTotal();
        Connection c = getConnection();
        String sql="INSERT INTO orders(customer_name,total_amount) VALUES(?,?)";
        try(PreparedStatement ps=c.prepareStatement(sql)){
            ps.setString(1, customer);
            ps.setDouble(2, total);
            ps.executeUpdate();
        } finally { c.close(); }
    }
   
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        HttpSession session = req.getSession();
        Map<Integer,CartItem> cart = (Map<Integer,CartItem>)session.getAttribute("cart");
        if(cart==null) cart = new HashMap<>();

        String action = req.getParameter("action");
        res.setContentType("text/html");
        PrintWriter out = res.getWriter();
        List<Product> products = Arrays.asList(
                new Product(1,"Laptop",55000),
                new Product(2,"Mobile",30000),
                new Product(3,"Headphones",2000)
        );

        out.println("<html><head><title>Amazon Clone</title></head><body>");
        out.println("<h1>Amazon Clone - Single Servlet</h1>");

        if("add".equals(action)){
            int id=Integer.parseInt(req.getParameter("id"));
            Product p = null;
            for(Product pr: products) if(pr.id==id){p=pr; break;}
            if(p!=null){
                if(cart.containsKey(id)) cart.get(id).increment();
                else cart.put(id,new CartItem(p));
                session.setAttribute("cart", cart);
                out.println("<p>Added "+p.name+" to cart.</p>");
            }
        }

        if("remove".equals(action)){
            int id=Integer.parseInt(req.getParameter("id"));
            cart.remove(id);
            session.setAttribute("cart", cart);
            out.println("<p>Item removed from cart.</p>");
        }

        if("checkout".equals(action)){
            String name=req.getParameter("name");
            if(name==null || name.trim().isEmpty()){
                out.println("<p>Invalid name. Go back.</p>");
            } else {
                try { saveOrder(name, cart); } catch(Exception e){ out.println("DB Error: "+e.getMessage()); }
                cart.clear(); session.setAttribute("cart", cart);
                out.println("<p>Order placed successfully for "+name+".</p>");
            }
        }

        out.println("<h2>Products</h2><ul>");
        for(Product p: products){
            out.println("<li>"+p.name+" - ₹"+p.price+
                    " <a href='?action=add&id="+p.id+"'>Add to Cart</a></li>");
        }
        out.println("</ul>");

        out.println("<h2>Shopping Cart</h2>");
        if(cart.isEmpty()){ out.println("<p>Cart is empty.</p>"); }
        else {
            out.println("<ul>");
            for(CartItem ci: cart.values()){
                out.println("<li>"+ci.product.name+" x"+ci.quantity+" = ₹"+ci.getTotal()+
                        " <a href='?action=remove&id="+ci.product.id+"'>Remove</a></li>");
            }
            double total=0; for(CartItem ci: cart.values()) total+=ci.getTotal();
            out.println("</ul><p>Total: ₹"+total+"</p>");

            out.println("<h3>Checkout</h3>");
            out.println("<form method='get'>");
            out.println("Name: <input type='text' name='name' required>");
            out.println("<input type='hidden' name='action' value='checkout'>");
            out.println("<input type='submit' value='Place Order'>");
            out.println("</form>");
        }

        out.println("</body></html>");
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException { doGet(req,res); }
}
