package co.chatsdk.ui.chat.options;

public class ChatOptionBuilder {

    String title;
    Integer iconResourceId;
    BaseChatOption.Action action;

    public ChatOptionBuilder () {

    }

    public ChatOptionBuilder icon (Integer iconResourceId) {
        this.iconResourceId = iconResourceId;
        return this;
    }

    public ChatOptionBuilder icon (String title) {
        this.title = title;
        return this;
    }

    public ChatOptionBuilder action (BaseChatOption.Action action) {
        this.action = action;
        return this;
    }

    public BaseChatOption build () {
        return new BaseChatOption(title, iconResourceId, action);
    }

}
