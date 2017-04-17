package wanderingdevelopment.tk.chatsdkcore.entities;

import com.google.gson.Gson;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;

import org.greenrobot.greendao.annotation.Generated;

import wanderingdevelopment.tk.chatsdkcore.BuildConfig;
import wanderingdevelopment.tk.chatsdkcore.EqualsWithNulls;

/**
 * Created by kykrueger on 2016-10-22.
 */

@Entity
public class User {
    @Id
    private Long id;

    private String name;
    private String photoPath;
    private String metadata;
    private String userName;

    @Generated(hash = 370148902)
    public User(Long id, String name, String photoPath, String metadata, String userName) {
        this.id = id;
        this.name = name;
        this.photoPath = photoPath;
        this.metadata = metadata;
        this.userName = userName;
    }

    @Generated(hash = 586692638)
    public User() {
    }

    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getPhotoPath() {
        return this.photoPath;
    }
    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }
    public String getMetadata() {
        return this.metadata;
    }
    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
    public String getUserName() {
        return this.userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /***
     * Metadata fields
     */
    public String getAvailability(){
        UserMetaData userMetaData = parseUserMetadata();
        return userMetaData.getAvailability();
    }

    public void setAvailability(String availability){
        Gson gson = new Gson();

        UserMetaData userMetaData = parseUserMetadata();
        userMetaData.setAvailability(availability);
        metadata = gson.toJson(userMetaData);
    }

    public String getEmail(){
        UserMetaData userMetaData = parseUserMetadata();
        return userMetaData.getEmail();
    }

    public void setEmail(String email){
        Gson gson = new Gson();

        UserMetaData userMetaData = parseUserMetadata();
        userMetaData.setEmail(email);
        metadata = gson.toJson(userMetaData);
    }

    public String getPhoneNumber(){
        UserMetaData userMetaData = parseUserMetadata();
        return userMetaData.getPhoneNumber();
    }

    public void setPhoneNumber(String phoneNumber){
        Gson gson = new Gson();

        UserMetaData userMetaData = parseUserMetadata();
        userMetaData.setPhoneNumber(phoneNumber);
        metadata = gson.toJson(userMetaData);
    }

    public String getCountryName(){
        UserMetaData userMetaData = parseUserMetadata();
        return userMetaData.getCountryName();
    }

    public void setCountryName(String countryName){
        Gson gson = new Gson();

        UserMetaData userMetaData = parseUserMetadata();
        userMetaData.setCountryName(countryName);
        metadata = gson.toJson(userMetaData);
    }

    public String getLocation(){
        UserMetaData userMetaData = parseUserMetadata();
        return userMetaData.getLocation();
    }

    public void setLocation(String location){
        Gson gson = new Gson();

        UserMetaData userMetaData = parseUserMetadata();
        userMetaData.setLocation(location);
        metadata = gson.toJson(userMetaData);
    }

    public String getBirthday(){
        UserMetaData userMetaData = parseUserMetadata();
        return userMetaData.getBirthday();
    }

    public void setBirthday(String birthday){
        Gson gson = new Gson();
        UserMetaData userMetaData = parseUserMetadata();
        userMetaData.setBirthday(birthday);
        metadata = gson.toJson(userMetaData);
    }

    private UserMetaData parseUserMetadata(){

        Gson gson = new Gson();
        UserMetaData userMetaData = gson.fromJson(metadata, UserMetaData.class);

        if (userMetaData == null) {
            userMetaData = new UserMetaData();
        }

        return userMetaData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User model = (User) o;

        if (!EqualsWithNulls.equalsWithNulls(id, model.getId())) return false;
        if (!EqualsWithNulls.equalsWithNulls(getUserName(), model.getUserName())) return false;
        if (!EqualsWithNulls.equalsWithNulls(getAvailability(), model.getAvailability())) return false;
        if (!EqualsWithNulls.equalsWithNulls(getName(),model.getName())) return false;
        if (!EqualsWithNulls.equalsWithNulls(getEmail(), model.getEmail())) return false;
        if (!EqualsWithNulls.equalsWithNulls(getCountryName(), model.getEmail())) return false;

        return true;

    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    private class UserMetaData{
        private String availability;
        private String email;
        private String phoneNumber;
        private String countryName;
        private String location;
        private String birthday;

        String getAvailability() {
            return availability;
        }

        void setAvailability(String availability) {
            this.availability = availability;
        }

        String getEmail() {
            return email;
        }

        void setEmail(String email) {
            this.email = email;
        }

        String getPhoneNumber() {
            return phoneNumber;
        }

        void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        String getCountryName() { return countryName; }

        void setCountryName(String countryName) { this.countryName = countryName; }

        String getLocation() { return location; }

        void setLocation(String location) { this.location = location; }

        String getBirthday() { return birthday; }

        void setBirthday(String birthday) { this.birthday = birthday; }
    }

}
