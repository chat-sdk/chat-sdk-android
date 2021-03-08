package firestream.chat.firebase.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Path {

    protected List<String> components = new ArrayList<>();

    /**
     * The remainder type used to fix an issue which arises with Firestore. In Firestore
     * there are documents and collections. But sometimes we want to reference information
     * that type at a path within a document for example:
     * chats/id/meta
     * Here the id, type a document but if we generated a path from this, it would point to a
     * collection. Therefore if the path we pass in to the ref doesn't point to the correct
     * reference type, we truncate it by one and set the remainder
     */
    protected String remainder = null;

    public Path(List<String> path) {
        for (String s: path) {
            if (s!= null) {
                components.add(s);
            }
        }
    }

    public Path(String path) {
        this(Arrays.asList(path.split("/")));
    }

    public Path(String... path) {
        this(Arrays.asList(path));
    }

    public String first() {
        return components.get(0);
    }

    public String last() {
        return components.get(size()-1);
    }

    public int size() {
        return components.size();
    }

    public String get(int index) {
        if (components.size() > index) {
            return components.get(index);
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder path = new StringBuilder();
        for (String component: components) {
            path.append(component).append("/");
        }
        path.deleteCharAt(path.length() - 1);
        return path.toString();
    }

    public Path child(String child) {
        components.add(child);
        return this;
    }

    public Path children(String... children) {
        components.addAll(Arrays.asList(children));
        return this;
    }

    public Path removeLast() {
        if (components.size() > 0) {
            components.remove(components.size() - 1);
        }
        return this;
    }

    public boolean isDocument() {
        return size() % 2 == 0;
    }

    public List<String> getComponents() {
        return components;
    }

    public String getRemainder() {
        return remainder;
    }

    public void normalizeForDocument() {
        if (!isDocument()) {
            remainder = last();
            removeLast();
        }
    }

    public void normalizeForCollection() {
        if (isDocument()) {
            remainder = last();
            removeLast();
        }
    }

    /**
     * For Firestore to update nested fields on a document, you need to use a
     * dot notation. This method returns the remainder if it exists plus a
     * dotted path component
     * @param component path to extend
     * @return dotted components
     */
    public String dotPath(String component) {
        if (remainder == null) {
            return component;
        } else {
            return remainder + "." + component;
        }
    }


}
