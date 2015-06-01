package util;


import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Created by Pine on 31/05/15.
 */
public class Generator {

    private static final String USER_GEN_URL = "http://api.randomuser.me/";
    private static final String MUSIC_GEN = "http://www.randomlists.com/data/bands.json";
    private static final String GATEWAY = "http://goparty-gateway.herokuapp.com";
    private static final String SIGN_UP_URL = GATEWAY + "/auth/signup";
    private static final String PROFILE_URL = GATEWAY + "/profile";

    private static final int musicLikesCount = 8;

    private List<User> population;
    private List<String> music;
    private CloseableHttpClient httpClient;
    private HttpGet userGetRequest;
    private HttpGet musicGetRequests;
    private Random random;
    private File data;

    public Generator(int count) {
        population = new LinkedList<>();
        httpClient = HttpClients.createDefault();
        userGetRequest = new HttpGet(USER_GEN_URL + "?results=" + count);
        musicGetRequests = new HttpGet(MUSIC_GEN);
        music = new LinkedList<>();
        random = new Random();
        data = new File("fakeUserData.txt");
    }

    private boolean signUpUser(User user) throws IOException {
        HttpPost post = new HttpPost(SIGN_UP_URL);
        post.setEntity(new StringEntity(user.getSignupJson().toString()));
        post.setHeader("Content-type", "application/json");
        String responseString = IOUtils.toString(httpClient.execute(post).getEntity().getContent());
        System.out.println("SIGNUP RESPONSE: " + responseString);
        JSONObject response = new JSONObject(responseString);
        if (response.has("access_token")) {
            user.setAuthToken(response.getString("access_token"));
            return true;
        }
        return false;
    }

    public boolean generate() throws IOException {
        try {
            HttpResponse userResponse = httpClient.execute(userGetRequest);
            if (userResponse.getStatusLine().getStatusCode() != 200)
                return false;
            HttpResponse musicResponse = httpClient.execute(musicGetRequests);
            if (musicResponse.getStatusLine().getStatusCode() != 200) {
                return false;
            }
            JSONObject usersJson = new JSONObject(IOUtils.toString(userResponse.getEntity().getContent()));
            System.out.println("users response: " + usersJson.toString() + "\n");
            extractMusic(new JSONObject(IOUtils.toString(musicResponse.getEntity().getContent())));
            JSONArray jsonArray = usersJson.getJSONArray("results");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i).getJSONObject("user");
                User user = new User(jsonObject);
                generateMusic(user, musicLikesCount);
                population.add(user);

            }
            sendProfiles();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        OutputStream os = new FileOutputStream(data, true);
        System.out.println();
        System.out.println("Population: ");
        for (User u : population) {
            System.out.println(u.toString());
            IOUtils.write(u.toString()+"\n", os);
        }
        os.close();
        return true;
    }

    private void sendProfiles() throws IOException {

        //signing up and id acquisition
        for (User user : population) {
            if (!signUpUser(user)) {
                System.out.println("User " + user.getEmail() + " failed to sign up");
                continue;
            } else
                System.out.println("User " + user.getEmail() + " successfully  signed up");

            if (!getUserId(user)) {
                System.out.println("User " + user.getEmail() + " failed to get its id");
            } else
                System.out.printf("User " + user.getEmail() + " successfully  got id " + user.getUserID());
            System.out.println();
        }
        //making friends;
        makeParty();
        //updating profiles
        for (User user : population) {

            if (!sendUserInfo(user)) {
                System.out.println("User " + user.getEmail() + " failed to update its profile");
            } else
                System.out.println("User " + user.getEmail() + " successfully updated profile");
        }
    }


    private boolean getUserId(User user) throws IOException {
        HttpGet get = new HttpGet(PROFILE_URL);
        get.addHeader("Authorization", "Bearer " + user.getAuthToken());
        get.addHeader("Content-type", "application/json");

        System.out.println("get user info response: ");
        JSONObject response = new JSONObject(IOUtils.toString(httpClient.execute(get).getEntity().getContent()));
        System.out.println(response.toString());
        if (response.has("message")) {
            String id = response.getString("message").substring(39);
            System.out.println("got id: " + id);
            user.setUserID(id);
            return true;
        }

        return false;
    }

    private boolean sendUserInfo(User user) throws IOException {
        HttpPost post = new HttpPost(PROFILE_URL);
        post.addHeader("Authorization", "Bearer " + user.getAuthToken());
        post.addHeader("Content-type", "application/json");
        System.out.println("REQUEST JSON:" + user.getDataJson().toString());
        post.setEntity(new StringEntity(user.getDataJson().toString()));
        String response = IOUtils.toString(httpClient.execute(post).getEntity().getContent());
        System.out.println("RESPONSE: " + response);
        if (response.contains("updated"))
            return true;
        return false;
    }

    private void makeParty() {
        int size = population.size();
        if (size < 2)
            return;
        for (int i = 0; i < 3 * size; i++) {
            makeFriendship(population.get(random.nextInt(size)), population.get(random.nextInt(size)));
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
        try {
            new Generator(5).generate();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
