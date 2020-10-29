package com.codecool.shop.controller;

import com.codecool.shop.config.TemplateEngineUtil;
import com.codecool.shop.dao.CartDao;
import com.codecool.shop.dao.ProductCategoryDao;
import com.codecool.shop.dao.UserDao;
import com.codecool.shop.dao.implementation.CartDaoJDBC;
import com.codecool.shop.dao.implementation.ProductCategoryDaoJDBC;
import com.codecool.shop.dao.implementation.UserDaoJDBC;
import com.codecool.shop.model.AdminLog;
import com.codecool.shop.model.Cart;
import com.codecool.shop.model.Product;
import com.codecool.shop.model.User;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Properties;

@WebServlet(urlPatterns = {"/payment"})
public class PaymentController extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        AdminLog log = AdminLog.getInstance();
        log.addToFile("Payment");
        WebContext context = new WebContext(req, resp, req.getServletContext());

        HttpSession session = req.getSession();

        String userEmail = (String) session.getAttribute("sessuser");

        CartDao cartDataStore= CartDaoJDBC.getInstance();
        UserDao userDataStore = UserDaoJDBC.getInstance();
        ProductCategoryDao productCategoryDataStore = ProductCategoryDaoJDBC.getInstance();


        String forAdminLog = "Proceed checkout";


        try {
            User user = userDataStore.find(userEmail);
            Cart cart = cartDataStore.findByUserID(user.getId());
            int numOfProducts = 0;

            int noOfProducts = 0;
            for (int nrProduct : cartDataStore.getCartProducts(cart).values()) {
                noOfProducts+=nrProduct;
            }
            context.setVariable("order", cart );
            context.setVariable("noOfProducts", numOfProducts);
            context.setVariable("userSession", session.getAttribute("userSession") != null ? session.getAttribute("userSession")  : "No");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        TemplateEngine engine = TemplateEngineUtil.getTemplateEngine(req.getServletContext());



        engine.process("product/payment.html", context, resp.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        HttpSession session = req.getSession();

        String userEmail = (String) session.getAttribute("sessuser");

        CartDao cartDataStore= CartDaoJDBC.getInstance();
        UserDao userDataStore = UserDaoJDBC.getInstance();


        String forAdminLog = "Proceed checkout";

        String cardOwner = req.getParameter("cardOwner");
        String cardNumber=req.getParameter("cardNumber");
        String expirationDate=req.getParameter("expirationDate");
        String cvv=req.getParameter("CVV");
        String paypalUsername=req.getParameter("paypalUsername");
        String paypalEmail=req.getParameter("paypalEmail");
        int cardNumberFormated;
        int cvvFormated;


        try {
            User user = userDataStore.find(userEmail);
            Cart cart = cartDataStore.findByUserID(user.getId());
            float sum = 0;
            for (Product p : cartDataStore.getCartProducts(cart).keySet()){
                sum+=p.getDefaultPrice();
            }
            String sum2  = String.format("%.1f", sum);

            resp.setContentType("text/html;charset=UTF-8");
            PrintWriter out = resp.getWriter();
            String name= cart.getCustomerName();
            String to = cart.getCustomerEmail();
            String total=sum2;
            send(to,name,total);
            System.out.println("Mail send successfully");

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }


//        try{
//            cardNumberFormated=Integer.parseInt(cardNumber);
//            cvvFormated=Integer.parseInt(cvv);
//        }catch (NumberFormatException e){
//            resp.sendRedirect("/payment-error");
//        }


        resp.sendRedirect("/confirmation");
    }
    public static void send(String custEmail, String fullName, String total)
    {
        String to = custEmail;
        String host = "smtp.gmail.com";
        final String user = "codecool.shop.romania@gmail.com";
        final String pass = "1234asd@";

        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getDefaultInstance(props,
                new javax.mail.Authenticator() {
                    protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                        return new javax.mail.PasswordAuthentication(user,pass);
                    }
                });

        //Compose the message
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(user));
            message.addRecipient(Message.RecipientType.TO,new InternetAddress(to));
            message.setSubject("SHOP ORDER CONFIRMATION");
            message.setText("Hello " + fullName +",\n" +
                    "\n" +
                    "Thanks for purchasing from our shop.\n" +
                    "Your amount of:  " + total + " USD, was processed successfully" +
                    " and we'll be shipping it shortly.\n" +
                    "If you have any questions, you can always reach us at codecool.shop.romania.gmail.com\n" +
                    "\n" +
                    "Thanks again for the business and have a wonderful day! \n" +
                    "Codecool Bazar Team"

            );

            //send the message
            Transport.send(message);

            System.out.println("message sent successfully...");

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

 }