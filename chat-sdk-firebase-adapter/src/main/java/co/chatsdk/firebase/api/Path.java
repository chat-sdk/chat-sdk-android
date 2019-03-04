package co.chatsdk.firebase.api;

import java.util.ArrayList;

public class Path {

    protected ArrayList<Ref> references = new ArrayList<>();

    public Path (Ref... refs) {
        for(Ref ref : refs) {
            references.add(ref);
        }
    }

    public ArrayList<Ref> getReferences() {
        return references;
    }

}
