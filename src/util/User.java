package util;

import org.apache.commons.lang.RandomStringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Created by Pine on 31/05/15.
 */
public class User {

    private String userID;
    private String firstName;
    private String lastName;
    private String email;
    private String facebookToken;
    private String authToken;
    private int age;
    private String avatarUrl;
    private String gender;
    private String password;

    private List<User> friends;
    private List<String> favouriteBands;
    private Random random = new Random();

    public User(String firstName, String lastName, String email, String facebookToken, int age, String avatarUrl, List<User> friends, List<String> favouriteBands) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.facebookToken = facebookToken;
        this.age = age;
        this.avatarUrl = avatarUrl;
        this.friends = friends;
        this.favouriteBands = favouriteBands;
    }

    public JSONObject getSignupJson() {
        return new JSONObject().put("email", getEmail()).put("password", getPassword()).put("role", "individual");
    }

    public JSONObject getDataJson() {
        return new JSONObject().put("favourite_bands", getFavouriteBands())
                .put("picture_url", getAvatarUrl())
                .put("age", getAge())
                .put("first_name",getFirstName())
                .put("last_name",getLastName())
                .put("friends_list",listFriendsIds());
    }

    public User(JSONObject jsonUser) {

        JSONObject names = jsonUser.getJSONObject("name");
        firstName = names.getString("first");
        lastName = names.getString("last");
        gender = jsonUser.getString("gender");
        email = jsonUser.getString("email");
        password = jsonUser.getString("password");

        JSONObject pictures = jsonUser.getJSONObject("picture");
        avatarUrl = pictures.getString("thumbnail");
        friends = new ArrayList<>();
        favouriteBands = new ArrayList<>();
        age = 14 + random.nextInt(30);
        facebookToken = RandomStringUtils.randomAlphanumeric(255);

    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public void likeMusicBand(String band) {
        favouriteBands.add(band);
    }

    public void addFriend(User user) {
        friends.add(user);
    }

    public void unlikeMusicBand(String band) {
        favouriteBands.remove(band);
    }

    public void removeFriend(User user) {
        friends.remove(user);
    }

    private String listFriendsIds() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        int i = friends.size();
        for(int j = 0;j<friends.size();j++){
            sb.append(friends.get(j).getUserID());
            if(j<i-1)
                sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public String toString() {
        return "User{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", facebookToken='" + facebookToken + '\'' +
                ", accessToken='" + authToken + '\'' +
                ", age=" + age +
                ", avatarUrl='" + avatarUrl + '\'' +
                ", gender='" + gender + '\'' +
                ", password='" + password + '\'' +
                ", friends=" + listFriendsIds() + " \n" +
                ", favouriteBands=" + favouriteBands +
                '}';
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getFacebookToken() {
        return facebookToken;
    }

    public int getAge() {
        return age;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getGender() {
        return gender;
    }

    public String getPassword() {
        return password;
    }

    public List<User> getFriends() {
        return friends;
    }

    public List<String> getFavouriteBands() {
        return favouriteBands;
    }

}
