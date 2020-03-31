package co.chatsdk.contact;

import java.util.ArrayList;

import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.interfaces.UserListItem;
import co.chatsdk.core.utils.StringChecker;

/**
 * Created by ben on 10/9/17.
 */

public class ContactBookUser implements UserListItem {

    private ArrayList<Name> names = new ArrayList<>();
    private ArrayList<String> emailAddresses = new ArrayList<>();
    private ArrayList<String> phoneNumbers = new ArrayList<>();
    private String entityID;

    public ArrayList<String> getEmailAddresses() {
        return emailAddresses;
    }

    public void setEmailAddresses(ArrayList<String> emailAddresses) {
        this.emailAddresses = emailAddresses;
    }

    public ArrayList<String> getPhoneNumbers() {
        return phoneNumbers;
    }

    public void setPhoneNumbers(ArrayList<String> phoneNumbers) {
        this.phoneNumbers = phoneNumbers;
    }

    public String getEntityID() {
        return entityID;
    }

    public void setEntityID(String entityID) {
        this.entityID = entityID;
    }

    public ArrayList<Name> getNames() {
        return names;
    }

    public void setNames(ArrayList<Name> names) {
        this.names = names;
    }

    public ArrayList<SearchIndex> getSearchIndexes () {
        ArrayList<SearchIndex> indexes = new ArrayList<>();

        for(String email : emailAddresses) {
            if(!StringChecker.isNullOrEmpty(email)) {
                indexes.add(new SearchIndex(Keys.Email, email));
            }
        }

        for(String phone : phoneNumbers) {
            if(!StringChecker.isNullOrEmpty(phone)) {
                indexes.add(new SearchIndex(Keys.Phone, phone));
            }
        }

        for(Name name : names) {
            if(!StringChecker.isNullOrEmpty(name.fullName())) {
                indexes.add(new SearchIndex(Keys.Name, name.fullName()));
            }
        }

        return indexes;
    }

    public boolean isContactable () {
        return getName().length() != 0 && (emailAddresses.size() > 0 || phoneNumbers.size() > 0);
    }

    @Override
    public String getName() {
        if(names.size() > 0) {
            return names.get(0).fullName();
        }
        return "";
    }

    @Override
    public String getStatus() {
        return "";
    }

    @Override
    public String getAvailability() {
        return "";
    }

    @Override
    public String getAvatarURL() {
        return null;
    }

    @Override
    public Boolean getIsOnline() {
        return false;
    }
}
