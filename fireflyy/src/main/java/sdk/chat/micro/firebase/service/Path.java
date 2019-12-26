package sdk.chat.micro.firebase.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Path {

    protected ArrayList<String> components = new ArrayList<>();

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
        String path = "/";
        for (String component: components) {
            path = path + "/" + component;
        }
        return path;
    }

    public Path child(String child) {
        components.add(child);
        return this;
    }

    public Path children(String... children) {
        components.addAll(Arrays.asList(children));
        return this;
    }

    public List<String> getComponents() {
        return components;
    }

}
