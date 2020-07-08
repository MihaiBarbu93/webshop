package com.codecool.shop.controller;

import com.codecool.shop.dao.UserDao;
import com.codecool.shop.utils.SaltedHashPassword;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;

@WebServlet(urlPatterns = {"/login"})
public class LoginController extends HttpServlet {

    UserDao userDao;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {


        String loginUserEmail = req.getParameter("login-email");
        String password = req.getParameter("login-password");
        String loginPasswordDB = null;

        try {
            loginPasswordDB = SaltedHashPassword.generateStrongPasswordHash("parapanta");

        } catch (NoSuchAlgorithmException | InvalidKeySpecException throwables) {
            throwables.printStackTrace();
        }

        System.out.println(loginPasswordDB);

        try {
            if(SaltedHashPassword.validatePassword(password, loginPasswordDB)) {

                System.out.println("Password matching!");

                HttpSession httpSession = req.getSession(true);
                httpSession.setAttribute("sessuser", loginUserEmail.trim());

            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }

        resp.sendRedirect("/");

    }

}