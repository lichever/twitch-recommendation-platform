package com.laioffer.jupiter.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laioffer.jupiter.db.MySQLConnection;
import com.laioffer.jupiter.db.MySQLException;
import com.laioffer.jupiter.entity.LoginRequestBody;
import com.laioffer.jupiter.entity.LoginResponseBody;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;

@WebServlet(name = "LoginServlet", value = "/login")
public class LoginServlet extends HttpServlet {


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // Read user data from the request body and map the body to our defined obj
       /* ObjectMapper mapper = new ObjectMapper();
        LoginRequestBody body = mapper.readValue(request.getReader(), LoginRequestBody.class);*/

        LoginRequestBody body= ServletUtil.readRequestBody(LoginRequestBody.class, request);

        if (body == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        String username;

        try (MySQLConnection conn = new MySQLConnection()) {
            // Verify if the user ID and password are correct
            String userId = body.getUserId();
            String password = ServletUtil.encryptPassword(body.getUserId(), body.getPassword());
            username = conn.verifyLogin(userId, password);


        } catch (MySQLException e) {
            throw new ServletException(e);


        }

        // Create a new session for the user if user ID and password are correct,
        // otherwise return Unauthorized error.
        if (!username.isEmpty()) {// 如果用户名字本来就是空，verifyLogin返回也是 一个空格
            // Create a new session, put user ID as an attribute into the session object,
            // and set the expiration time to 600 seconds.
            HttpSession session = request.getSession();
            session.setAttribute("user_id", body.getUserId());//RecommendationServlet will use user_id
            session.setMaxInactiveInterval(600);

            LoginResponseBody loginResponseBody = new LoginResponseBody(body.getUserId(), username);
            ServletUtil.writeData(response, loginResponseBody);


        } else {

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);//401  访问由于认证的 凭据无效被拒绝。

        }


    }

}
