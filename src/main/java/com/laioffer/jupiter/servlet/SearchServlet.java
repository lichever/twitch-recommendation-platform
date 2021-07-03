package com.laioffer.jupiter.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laioffer.jupiter.external.TwitchClient;
import com.laioffer.jupiter.external.TwitchException;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;

@WebServlet(name = "SearchServlet", value = "/search")
public class SearchServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        //参数是 json 格式   snake format
        String gameId = request.getParameter("game_id");
        if (gameId == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        TwitchClient client = new TwitchClient();


        //可选这个limit功能
        String limitString = request.getParameter("limit");
        int limit = -1; //searchItems deals with negative values
        if (limitString!=null) {

            limit = Integer.parseInt(limitString);

        }


        try {
//            response.setContentType("application/json;charset=UTF-8");
            //ObjectMapper可以将任何java built-in的数据结构转化为json格式
//            response.getWriter().print(new ObjectMapper().writeValueAsString(client.searchItems(gameId)));


//            ServletUtil.writeData(response, client.searchItems(gameId));
            ServletUtil.writeData(response, client.searchItems(gameId, limit));

        } catch (TwitchException e) {
            throw new ServletException(e);
        }


    }





/*    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {






    }
    */


}
