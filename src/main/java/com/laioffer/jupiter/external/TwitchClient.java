package com.laioffer.jupiter.external;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.laioffer.jupiter.entity.Game;
import com.laioffer.jupiter.entity.Item;
import com.laioffer.jupiter.entity.ItemType;
import com.laioffer.jupiter.servlet.ServletUtil;
import org.apache.http.HttpEntity;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

public class TwitchClient {

    private static final String TOP_GAME_URL = "https://api.twitch.tv/helix/games/top?first=%s";
    private static final String GAME_SEARCH_URL_TEMPLATE = "https://api.twitch.tv/helix/games?name=%s";
    private static final int DEFAULT_GAME_LIMIT = 20;


    private static final String STREAM_SEARCH_URL_TEMPLATE = "https://api.twitch.tv/helix/streams?game_id=%s&first=%s";
    private static final String VIDEO_SEARCH_URL_TEMPLATE = "https://api.twitch.tv/helix/videos?game_id=%s&first=%s";
    private static final String CLIP_SEARCH_URL_TEMPLATE = "https://api.twitch.tv/helix/clips?game_id=%s&first=%s";
    private static final String TWITCH_BASE_URL = "https://www.twitch.tv/";
    private static final int DEFAULT_SEARCH_LIMIT = 20;


// Build the request URL which will be used when calling Twitch APIs,
// e.g. https://api.twitch.tv/helix/games/top when trying to get top games.

    private String buildGameURL(String url, String gameName, int limit) {//
        if (gameName.isEmpty()) {// 外面应该传进来的是TOP_GAME_URL
            return String.format(url, limit);
        } else {
            try {
                // Encode special characters in URL, e.g. Rick Sun -> Rick%20Sun
                gameName = URLEncoder.encode(gameName, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();// 这里 没处理 这个异常，因为在这个case 应该没问题
            }
            return String.format(url, gameName);
        }
    }

    // Send HTTP request to Twitch Backend based on the given URL,
    // and returns the body of the HTTP response(json formatted array containing Game obj) returned from Twitch backend.

    private String searchTwitch(String url) throws TwitchException {//runtime类型 的exception可以不声明

        //1.  Define the response handler to parse
        // and return HTTP response body returned from Twitch
        ResponseHandler<String> responseHandler = response -> {//可以查看接口用了泛型，这里我要返回string
            int responseCode = response.getStatusLine().getStatusCode();

            if (responseCode != 200) {
                System.out.println("Response status: "
                        + response.getStatusLine().getReasonPhrase());
                throw new TwitchException("Failed to get result from Twitch API");
            }

            //如果code 是 200 返回值也有可能为null
            HttpEntity entity = response.getEntity();

            if (entity == null) {
                throw new TwitchException("Failed to get the entity from Twitch API");
            }

            //如果是valid 那么一定是json格式文件！！
            JSONObject obj = new JSONObject(EntityUtils.toString(entity));//这里先转成string是为了变成json
            return obj.getJSONArray("data").toString();//这里变成json是为了 get the json array named 'data'，最后再变回string
        };


        //2. send http request
        try( CloseableHttpClient httpclient = HttpClients.createDefault()) {


            // Define the HTTP request, TOKEN and CLIENT_ID are used for user authentication on Twitch backend
            HttpGet request = new HttpGet(url);

            //java里面有一套api 专门读 properties 文件
            Properties prop = new Properties();
            String propFileName = "config.properties";

            InputStream inputStream = Item.class.getClassLoader().getResourceAsStream(propFileName);
            prop.load(inputStream);

            String TOKEN = prop.getProperty("TOKEN");
            String CLIENT_ID = prop.getProperty("CLIENT_ID");


            // put the meta data in header
            request.setHeader("Authorization", TOKEN);
            request.setHeader("Client-Id", CLIENT_ID);

            return httpclient.execute(request, responseHandler);// execute的返回类型是由 responseHandler 的返回类型决定的

        } catch (IOException e) {

            e.printStackTrace();
            throw new TwitchException("Failed to get result from Twitch API");

        }
        // 用了try with resource 就可以不要finally了
        /*finally {
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/
    }


    // Convert JSON format data returned from Twitch to an Arraylist of Game objects
    private List<Game> getGameList(String data) throws TwitchException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return Arrays.asList(mapper.readValue(data, Game[].class));//先map成Game[]类型， 再转化成list
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new TwitchException("Failed to parse game data from Twitch API" + data);
        }
    }


    //2 个 public 的方法 功能  对应 endpoint： game
    // Integrate search() and getGameList() together, returns the top x popular games from Twitch.
    public List<Game> topGames(int limit) throws TwitchException {
        if (limit <= 0) {
            limit = DEFAULT_GAME_LIMIT;
        }
        return getGameList(searchTwitch(buildGameURL(TOP_GAME_URL, "", limit)));//gameName是空 表示用topgame方法
    }

    //就这个简单的 功能topGames 其实可以不用map 成 object game，直接输出string 就ok了
 /*   public String topGames(int limit) throws TwitchException {
        if (limit <= 0) {
            limit = DEFAULT_GAME_LIMIT;
        }
        return searchTwitch(buildGameURL(TOP_GAME_URL, "", limit));
    }*/


    // Integrate search() and getGameList() together, returns the dedicated game based on the game name.
    //实际twich api getgames 可以返回多个game的信息，比如用户输入一个 warcraft 可能返回多个warcraft游戏 但这我只用 返回一个game

    public Game searchGame(String gameName) throws TwitchException {
        List<Game> gameList = getGameList(searchTwitch(buildGameURL(GAME_SEARCH_URL_TEMPLATE, gameName, 0)));//这里0 因为不用limit这个参数
        if (gameList.size() != 0) {
            return gameList.get(0);
        }
        return null;
    }


    // Similar to buildGameURL, build Search URL that will be used when calling
    // Twitch API. e.g. https://api.twitch.tv/helix/clips?game_id=12924.
    private String buildSearchURL(String url, String gameId, int limit) throws TwitchException {
        try {
            gameId = URLEncoder.encode(gameId, "UTF-8");//因为gameId有可能有特殊符号
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new TwitchException("cannot encode gameID " + gameId);
        }
        return String.format(url, gameId, limit);
    }


    // Similar to getGameList, convert the json data returned from Twitch to a list of Item objects.
    private List<Item> getItemList(String data) throws TwitchException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return Arrays.asList(mapper.readValue(data, Item[].class));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new TwitchException("Failed to parse item data from Twitch API");
        }
    }


    // Returns the top x streams based on game ID.  利用了之前的searchTwitch
    private List<Item> searchStreams(String gameId, int limit) throws TwitchException {

        List<Item> streams = getItemList(searchTwitch(buildSearchURL(STREAM_SEARCH_URL_TEMPLATE, gameId, limit)));
        for (Item item : streams) {//这次有自己的field，所以要单独处理
            item.setType(ItemType.STREAM);
            item.setUrl(TWITCH_BASE_URL + item.getBroadcasterName());
        }
        return streams;
    }

    // Returns the top x clips based on game ID.
    private List<Item> searchClips(String gameId, int limit) throws TwitchException {
        List<Item> clips = getItemList(searchTwitch(buildSearchURL(CLIP_SEARCH_URL_TEMPLATE, gameId, limit)));
        for (Item item : clips) {
            item.setType(ItemType.CLIP);
        }
        return clips;
    }

    // Returns the top x videos based on game ID.
    private List<Item> searchVideos(String gameId, int limit) throws TwitchException {
        List<Item> videos = getItemList(searchTwitch(buildSearchURL(VIDEO_SEARCH_URL_TEMPLATE, gameId, limit)));

        for (Item item : videos) {
            item.setType(ItemType.VIDEO);
        }
        return videos;
    }


    /*Finally, add the public searchByType and searchItems
    to return items for a specific type, or items for all types.
     */
    public List<Item> searchByType(String gameId, ItemType type, int limit) throws TwitchException {
        List<Item> items = Collections.emptyList();

        switch (type) {
            case STREAM:
                items = searchStreams(gameId, limit);
                break;
            case VIDEO:
                items = searchVideos(gameId, limit);
                break;
            case CLIP:
                items = searchClips(gameId, limit);
                break;
        }
        // Update gameId for all items. GameId is used by recommendation function
        for (Item item : items) {
            item.setGameId(gameId);//都一样的 为啥要重设？
        }
        return items;


    }

    public Map<String, List<Item>>  searchItems(String gameId) throws TwitchException {


        return searchItems(gameId, DEFAULT_GAME_LIMIT);
    }



    public Map<String, List<Item>> searchItems(String gameId, int limit) throws TwitchException {

        if (limit<0){
            limit=DEFAULT_GAME_LIMIT;
        }

        Map<String, List<Item>> itemMap = new HashMap<>();

        for (ItemType type : ItemType.values()) {
            itemMap.put(type.toString(), searchByType(gameId, type, limit));//这里可以把这个方法改成参数limit
        }
        //问题 如果 handle  300个 结果  api只提供100个？？？用for loop 和 pagination 来绕开api的限制

        return itemMap;
    }





    //test this file
   /* public static void main(String[] args) {

        //java里面有一套api 专门读 properties 文件
        Properties prop = new Properties();
        String propFileName = "config.properties";

        InputStream inputStream = Item.class.getClassLoader().getResourceAsStream(propFileName);
        try {
            prop.load(inputStream);
            String TOKEN = prop.getProperty("TOKEN");
            String CLIENT_ID = prop.getProperty("CLIENT_ID");

            System.out.println(TOKEN);
            System.out.println(CLIENT_ID);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }*/

/*

    public static void main(String[] args) {

        Map<String, Integer> mm= new HashMap<>();

        mm.put("q", 12);
        mm.put("z", 112);
        mm.put("a", 22);


        mm= ServletUtil.sortByValue(mm);

        for(Map.Entry<String, Integer> entry: mm.entrySet()){

            System.out.println(entry.getKey()+": "+ entry.getValue());
        }

        int num=-1;
        String str="";

        try {
            num= Integer.parseInt(str);
        }catch (NumberFormatException e){

        }

        System.out.println(num);



    }
*/





}



















