package util;


import com.oracle.tools.packager.Log;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Created by Pine on 31/05/15.
 */
public class Generator {

    private static final String USER_GEN_URL = "http://api.randomuser.me/";
    private static final String MUSIC_GEN = "http://www.randomlists.com/data/bands.json";
    private static final int musicLikesCount = 5;

    private List<User> population;
    private List<String> music;
    private CloseableHttpClient httpClient;
    private HttpGet userGetRequest;
    private HttpGet musicGetRequests;
    private Random random;

    public Generator(int count) {
        population = new LinkedList<>();
        httpClient = HttpClients.createDefault();
        userGetRequest = new HttpGet(USER_GEN_URL + "?results=" + count);
        musicGetRequests = new HttpGet(MUSIC_GEN);
        music = new LinkedList<>();
        random = new Random();
    }

    public boolean generate() {
        try {
            HttpResponse userResponse = httpClient.execute(userGetRequest);
            if (userResponse.getStatusLine().getStatusCode() != 200)
                return false;
            HttpResponse musicResponse = httpClient.execute(musicGetRequests);
            if (musicResponse.getStatusLine().getStatusCode() != 200) {
                return false;
            }
            JSONObject usersJson = new JSONObject(IOUtils.toString(userResponse.getEntity().getContent()));
            System.out.println("users response: " + usersJson.toString());
            extractMusic(new JSONObject(IOUtils.toString(musicResponse.getEntity().getContent())));
            JSONArray jsonArray = usersJson.getJSONArray("results");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i).getJSONObject("user");
                User user = new User(jsonObject);
                generateMusic(user, musicLikesCount);
                population.add(user);
            }
            makeParty();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        System.out.println("AAAAAAAAAAAAAAA");
        for (User u: population){
            System.out.println(u.toString());
        }
        return true;
    }

    private void makeParty() {
        int size = population.size();
        if (size < 2)
            return;
        for (int i = 0; i < 3 * size; i++) {
            makeFriendship(population.get(random.nextInt(size)),population.get(random.nextInt(size)));
        }
    }

    private void extractMusic(JSONObject musicJson) {
        JSONArray jsonArray = musicJson.getJSONArray("data");
        JSONObject band;
        for (int i = 0; i < jsonArray.length(); i++) {
            band = jsonArray.getJSONObject(i);
            music.add(band.getString("name"));
        }
    }

    public void generateMusic(User user, int musicLikesCount) {
        for (int i = 0; i < musicLikesCount; i++) {
            user.likeMusicBand(getMusicBand());
        }
    }

    private String getMusicBand() {
        return music.get(random.nextInt(music.size()));
    }

    private void makeFriendship(User a, User b) {
        if (a != b) {
            b.addFriend(a);
            a.addFriend(b);
        }
    }

    public static void main(String[] args) {
        new Generator(10).generate();
    }

}
