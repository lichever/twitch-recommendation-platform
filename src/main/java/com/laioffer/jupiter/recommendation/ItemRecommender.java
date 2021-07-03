package com.laioffer.jupiter.recommendation;

import com.laioffer.jupiter.db.MySQLConnection;
import com.laioffer.jupiter.db.MySQLException;
import com.laioffer.jupiter.entity.Game;
import com.laioffer.jupiter.entity.Item;
import com.laioffer.jupiter.entity.ItemType;
import com.laioffer.jupiter.external.TwitchClient;
import com.laioffer.jupiter.external.TwitchException;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ItemRecommender {

    //    这些参数是针对每个type的
    private static final int DEFAULT_GAME_LIMIT = 3;
    private static final int DEFAULT_PER_GAME_RECOMMENDATION_LIMIT = 10;//这个参数可以限制下，同一个game不要太多
    private static final int DEFAULT_TOTAL_RECOMMENDATION_LIMIT = 20;


    /**
     * Add a recommendByTopGames method to ItemRecommender to handle recommendation
     * when the user is not logged in. The recommendation is purely based-on top games
     * returned by Twitch.
     * <p>
     * Return a list of Item objects for the given type. Types are one of
     * [Stream, Video, Clip]. Add items are related to the top games provided
     * in the argument
     */

    private List<Item> recommendByTopGames(ItemType type, List<Game> topGames)
            throws RecommendationException {

        List<Item> recommendedItems = new ArrayList<>();
        TwitchClient client = new TwitchClient();

        boolean reachLimit = false;
        for (Game game : topGames) {
            List<Item> items;

            try {

                items = client.searchByType(game.getId(), type,
                        DEFAULT_PER_GAME_RECOMMENDATION_LIMIT);


            } catch (TwitchException e) {
                e.printStackTrace();
                throw new RecommendationException("Failed to get recommendation result" + e);
            }


            for (Item i : items) {
                if (recommendedItems.size() >= DEFAULT_TOTAL_RECOMMENDATION_LIMIT) {

                    reachLimit = true;
                    break;

                }

                recommendedItems.add(i);

            }

            if (reachLimit) {
                break;
            }

        }

        return recommendedItems;//有可能没达到limit 或 为null


    }


    /**
     * Return a list of Item objects for the given type. Types are one of [Stream, Video, Clip].
     * All items are related to the items previously favorited by the user. E.g., if a user
     * favorited some videos about game "Just Chatting", then it will return some other videos
     * about the same 'game'.
     * <p>
     * favoriteItemIds is used to prune the repetition recommended item
     */

    private List<Item> recommendByFavoriteHistory(
            Set<String> favoriteItemIds, List<String> favoriteGameIds, ItemType type)
            throws RecommendationException {

// Count the favorite game IDs from the database for the given user. E.g.
// if the favorited game ID list is ["1234", "2345", "2345", "3456"], the returned
// Map is {"1234": 1, "2345": 2, "3456": 1}

    /*Map<String, Long> favoriteGameIdByCount=favoriteGameIds.parallelStream()
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));*/


        Map<String, Long> favoriteGameIdByCount = new HashMap<>();
        for (String id : favoriteGameIds) {
            favoriteGameIdByCount.put(id, favoriteGameIdByCount.getOrDefault(id, 0L) + 1);
        }


        // Sort the game Id by count. E.g. if the input is {"1234": 1, "2345": 2, "3456": 1},
        // the returned Map is {"2345": 2, "1234": 1, "3456": 1}

        List<Map.Entry<String, Long>> sortedFavoriteGameIdListByCount
                = new ArrayList<>(favoriteGameIdByCount.entrySet());


        sortedFavoriteGameIdListByCount.sort((Map.Entry<String, Long> e1,
                                              Map.Entry<String, Long> e2) ->
                Long.compare(e2.getValue(), e1.getValue()));


        //at most has 3 gameID
        if (sortedFavoriteGameIdListByCount.size() > DEFAULT_GAME_LIMIT) {
            sortedFavoriteGameIdListByCount = sortedFavoriteGameIdListByCount.subList(0, DEFAULT_GAME_LIMIT);

        }


        List<Item> recommendedItems = new ArrayList<>();//return result
        TwitchClient client = new TwitchClient();


        // Search Twitch based on the favorite game IDs returned in the last step.
        outerloop:

        for (Map.Entry<String, Long> favoriteGame : sortedFavoriteGameIdListByCount) {

            List<Item> items;

            try {

                items = client.searchByType(favoriteGame.getKey(), type,
                        DEFAULT_PER_GAME_RECOMMENDATION_LIMIT);

            } catch (TwitchException e) {
                e.printStackTrace();
                throw new RecommendationException("Failed to get recommendation result" + e);
            }


            for (Item item : items) {

                if (recommendedItems.size() > DEFAULT_TOTAL_RECOMMENDATION_LIMIT) {

                    break outerloop;
                }

                //这里优化下，不加已收藏的 item
                if (!favoriteGameIds.contains(item.getId())) {

                    recommendedItems.add(item);
                }
            }

        }
        return recommendedItems;
    }


//add recommendItemsByUser and recommendItemsByDefault as public APIs for recommendation.

    /**
     * // Return a map of Item objects as the recommendation result. Keys of the may are
     * [Stream, Video, Clip]. Each key is corresponding to a list of Items objects, each
     * item object is a recommended item based on the previous favorite records by the user.
     * <p>
     * will use DB now!
     */

    public Map<String, List<Item>> recommendItemsByUser(String userId) throws RecommendationException {


        Map<String, List<Item>> recommendedItemMap = new HashMap<>();

        Set<String> favortiteItemIds;
        Map<String, List<String>> favoriteGameIds;

        try (MySQLConnection conn = new MySQLConnection()) {

            favortiteItemIds = conn.getFavoriteItemIds(userId);
            favoriteGameIds = conn.getFavoriteGameIds(favortiteItemIds);


        } catch (MySQLException e) {
            e.printStackTrace();
            throw new RecommendationException("Failed to get user favorite history for recommendation" + e);
        }


        for (Map.Entry<String, List<String>> entry : favoriteGameIds.entrySet()) {
            //map 的type 里面有可能为空,这里还可以改进，如果每个type没达到推荐数量上限，可以用topgames来补充

            String itemType = entry.getKey();

            if (entry.getValue().isEmpty()) {

                TwitchClient client = new TwitchClient();
                List<Game> topGames;
                try {
                    topGames = client.topGames(DEFAULT_GAME_LIMIT);
                } catch (TwitchException e) {
                    e.printStackTrace();
                    throw new RecommendationException("Failed to get top game data for recommendation" + e);
                }

                recommendedItemMap.put(itemType, recommendByTopGames(ItemType.valueOf(itemType), topGames));

            } else {

                recommendedItemMap.put(itemType, recommendByFavoriteHistory(favortiteItemIds,
                        entry.getValue(), ItemType.valueOf(itemType)));

            }

        }
        return recommendedItemMap;
    }


    /**
     * // Return a map of Item objects as the recommendation result. Keys of the may are [Stream, Video, Clip].
     * Each key is corresponding to a list of Items objects, each item object is a recommended item based on the
     * top games currently on Twitch.
     */

    public Map<String, List<Item>> recommendItemsByDefault() throws RecommendationException {
        Map<String, List<Item>> recommendedItemMap = new HashMap<>();
        TwitchClient client = new TwitchClient();

        List<Game> topGames;
        try {
            topGames = client.topGames(DEFAULT_GAME_LIMIT);
        } catch (TwitchException e) {
            e.printStackTrace();
            throw new RecommendationException("Failed to get top game data for recommendation" + e);
        }

        //对应每个type， 都搜索topgame的item
        for (ItemType type: ItemType.values()){

            recommendedItemMap.put(type.toString(), recommendByTopGames(type, topGames));

        }

        return recommendedItemMap;

    }



    

}
