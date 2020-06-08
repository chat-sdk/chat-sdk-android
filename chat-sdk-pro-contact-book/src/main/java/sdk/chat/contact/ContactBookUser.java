package sdk.chat.contact;

import java.util.ArrayList;

import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.User;
import sdk.chat.core.interfaces.UserListItem;
import sdk.chat.core.utils.StringChecker;

/**
 * Created by ben on 10/9/17.
 */

public class ContactBookUser implements UserListItem {

    private ArrayList<Name> names = new ArrayList<>();
    private ArrayList<String> emailAddresses = new ArrayList<>();
    private ArrayList<String> phoneNumbers = new ArrayList<>();
    private User user;

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
        if (user != null) {
            return user.getEntityID();
        }
        return null;
    }

    public void setUser(User user) {
        this.user = user;
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

//        for(Name name : names) {
//            if(!StringChecker.isNullOrEmpty(name.fullName())) {
//                indexes.add(new SearchIndex(Keys.Name, name.fullName()));
//            }
//        }

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
        if (user != null) {
            return user.getStatus();
        }
        return "";
    }

    @Override
    public String getAvailability() {
        if (user != null) {
            return user.getAvailability();
        }
        return "";
    }

    @Override
    public String getAvatarURL() {
        if (user != null) {
            return user.getAvatarURL();
        }
        return "";
    }

    @Override
    public Boolean getIsOnline() {
        if (user != null) {
            return user.getIsOnline();
        }
        return false;
    }

    public boolean isUser(User user) {
        // Check to see if we have a details match
        for (String address: emailAddresses) {
            if (address.equals(user.getEmail())) {
                return true;
            }
        }
        for (String phone: phoneNumbers) {
            if (phone.equals(user.getPhoneNumber())) {
                return true;
            }
        }
        return false;
    }
}
