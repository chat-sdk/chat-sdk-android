package sdk.chat.micro.types;

public class BaseType {

    protected String type;

    public BaseType(String type) {
        if (type != null) {
            this.type = type;
        } else {
            this.type = "";
        }
    }

    public BaseType(BaseType type) {
        this.type = type.get();
    }

    public String get() {
        return type;
    }

    public boolean equals(BaseType type) {
        return this.get().equals(type.get());
    }

    public static BaseType none() {
        return new BaseType("");
    }

}
