package co.chatsdk.contact;

import co.chatsdk.core.utils.StringChecker;

/**
 * Created by ben on 10/9/17.
 */

public class Name {

    public String first;
    public String last;

    public Name(String first, String last) {
        this.first = first;
        this.last = last;
    }

    public String getFirst() {
        return first != null ? first : "";
    }

    public String getLast() {
        return last != null ? last : "";
    }

    public String fullName () {
        if(!StringChecker.isNullOrEmpty(first) && !StringChecker.isNullOrEmpty(last)) {
            return getFirst() + " " + getLast();
        }
        if(!StringChecker.isNullOrEmpty(first)) {
            return getFirst();
        }
        if(!StringChecker.isNullOrEmpty(last)) {
            return getLast();
        }
        return "";
    }


}
