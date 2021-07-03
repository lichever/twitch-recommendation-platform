package com.laioffer.jupiter.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laioffer.jupiter.entity.Game;
import com.laioffer.jupiter.entity.GameTest;
import com.laioffer.jupiter.external.TwitchClient;
import com.laioffer.jupiter.external.TwitchException;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;

@WebServlet(name = "GameServlet", value = "/game")
public class GameServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        //1. 接收 client的request、query      都要从DB拿数据
/*        String gamename= request.getParameter("gamename");
        response.getWriter().println("Game is "+ gamename);
        response.getWriter().println(request.getContextPath());
        response.getWriter().println(request.getQueryString());
        response.getWriter().println(request.getRequestURI());
        response.getWriter().println(request.getHeaderNames());
        response.getWriter().println(request.getContentLength());
        response.getWriter().println(request.getContentType());*/

        //2. 这种直接 写json key-pair 回复client 容易错
/*        response.setContentType("application/json");
        JSONObject game = new JSONObject();
        game.put("name", "World of Warcraft");
        game.put("developer", "Blizzard Entertainment");
        game.put("release_time", "Feb 11, 2005");
        game.put("website", "https://www.worldofwarcraft.com");
        game.put("price", 49.99);

        // Write game information to response body
        response.getWriter().print(game);*/


        //3. 所以 用这种 写到class object里面  比较安全 方便
      /*  response.setContentType("application/json");
        ObjectMapper mapper = new ObjectMapper();
        Game.Builder builder = new Game.Builder();
        builder.setName("World of Warcraft");
        builder.setDeveloper("Blizzard Entertainment");
        builder.setReleaseTime("Feb 11, 2005");
        builder.setWebsite("https://www.worldofwarcraft.com");
        builder.setPrice(49.99);

        Game game = builder.build();
        //writeValue API to serialize any Java object as JSON output
        response.getWriter().print(mapper.writeValueAsString(game));*/


        // Get gameName from request URL.
        String gameName = request.getParameter("game_name");
        String limitNumber = request.getParameter("first");//limit number for top games search 下面要检测下时候为null


        TwitchClient client = new TwitchClient();


        // Let the client know the returned data is in JSON format.
        response.setContentType("application/json;charset=UTF-8");
        try {
            // Return the dedicated game information if gameName is provided in the request URL, otherwise return the top x games.
            if (gameName != null) {
                response.getWriter().print(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(client.searchGame(gameName)));

            } else {


                // if user does not input the limit num
                if (limitNumber==null){
                    response.getWriter().print(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(client.topGames(0)));

//                    response.getWriter().println(client.topGames(0));

                }else {
                    response.getWriter().print(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(client.topGames(Integer.parseInt(limitNumber))));

//                    response.getWriter().println(client.topGames(Integer.parseInt(limitNumber)));
                }



            }

        } catch (TwitchException e) {

            throw new ServletException(e);

        }


    }
/*
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        //for a client saving and writing information 下一步存DB 所以这里不转换为object？
        JSONObject jsonRequest = new JSONObject(IOUtils.toString(request.getReader()));
        String name = jsonRequest.getString("name");
        String developer = jsonRequest.getString("developer");
        String releaseTime = jsonRequest.getString("release_time");
        String website = jsonRequest.getString("website");
        float price = jsonRequest.getFloat("price");

        // Print game information to IDE console
        System.out.println("Name is: " + name);
        System.out.println("Developer is: " + developer);
        System.out.println("Release time is: " + releaseTime);
        System.out.println("Website is: " + website);
        System.out.println("Price is: " + price);

        // Return status = ok as response body to the client
        response.setContentType("application/json");
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("status", "ok");
        response.getWriter().println(jsonResponse);


        response.getWriter().println(request.getContentType());
        response.getWriter().println(request.getContentLength());
        response.getWriter().println(request.getRequestURI());

//        response.setContentType("text/plain");
//        response.getWriter().println("status: ok");

  *//*      ObjectMapper mapper = new ObjectMapper();
        GameTest g= mapper.readValue(request.getRequestURI(), GameTest.class);
        response.getWriter().println(g);

        System.out.println(g);*//*

    }*/
}
