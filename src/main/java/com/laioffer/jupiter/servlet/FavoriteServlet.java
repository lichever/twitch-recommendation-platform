package com.laioffer.jupiter.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laioffer.jupiter.db.MySQLConnection;
import com.laioffer.jupiter.db.MySQLException;
import com.laioffer.jupiter.entity.FavoriteRequestBody;
import com.laioffer.jupiter.entity.Item;
import com.laioffer.jupiter.entity.LoginRequestBody;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@WebServlet(name = "FavoriteServlet", urlPatterns = {"/favorite"})// value = "/favorite"
public class FavoriteServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        String userId = request.getParameter("user_id");


        // Check if the session is still valid, which means the user has been logged in successfully.
        HttpSession session = request.getSession(false);//如果server里面没存这个sessionID 返回null
        if (session == null) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);//403  服务器已经理解请求，但是拒绝执行它。

            /*
            401 Unauthorized响应应该用来表示缺失或错误的认证；
            403 Forbidden响应应该在这之后用，当用户被认证后，但用户没有被授权在特定资源上执行操作。
            */

            return;
        }
        String userId = (String) session.getAttribute("user_id");


        try (MySQLConnection conn = new MySQLConnection()) {
            Map<String, List<Item>> itemMap = conn.getFavoriteItems(userId);

//            response.setContentType("application/json;charset=UTF-8");
//            response.getWriter().print(new ObjectMapper().writeValueAsString(itemMap));//convert to json string to front end
            ServletUtil.writeData(response, itemMap);

        } catch (MySQLException e) {
            throw new ServletException(e);
        }


    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // Get user ID from request URL, this is a temporary solution since we don’t support session now
//        String userId = request.getParameter("user_id");

        // Check if the session is still valid, which means the user has been logged in successfully.
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        String userId = (String) session.getAttribute("user_id");




        // Get favorite item information from request body
/*        ObjectMapper mapper = new ObjectMapper();
        FavoriteRequestBody body = mapper.readValue(request.getReader(), FavoriteRequestBody.class);*/

        // Read user data from the request body and map the body to our defined obj
        FavoriteRequestBody body= ServletUtil.readRequestBody(FavoriteRequestBody.class, request);

        if (body == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }


        //can change to use try-with
        MySQLConnection connection = null;
        try {
            // Save the favorite item to the database
            connection = new MySQLConnection();
            connection.setFavoriteItem(userId, body.getFavoriteItem());
        } catch (MySQLException e) {
            throw new ServletException(e);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }


    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        String userId = request.getParameter("user_id");


        // Check if the session is still valid, which means the user has been logged in successfully.
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);//403
            return;
        }
        String userId = (String) session.getAttribute("user_id");



       /* ObjectMapper mapper = new ObjectMapper();
        FavoriteRequestBody body = mapper.readValue(request.getReader(), FavoriteRequestBody.class);*/

        // Read user data from the request body and map the body to our defined obj
        FavoriteRequestBody body= ServletUtil.readRequestBody(FavoriteRequestBody.class, request);

        if (body == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);//400
            return;
        }

        try (MySQLConnection conn = new MySQLConnection()) {// 如果多个 resource 用分号隔开
            conn.unsetFavoriteItem(userId, body.getFavoriteItem().getId());
        } catch (MySQLException e) {
            throw new ServletException(e);
        }

    }
}
