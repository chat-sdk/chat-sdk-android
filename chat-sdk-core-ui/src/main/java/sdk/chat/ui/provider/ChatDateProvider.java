package sdk.chat.ui.provider;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.Date;

import sdk.chat.core.utils.CurrentLocale;

public class ChatDateProvider {

    protected PrettyTime prettyTime = new PrettyTime(CurrentLocale.get());

    public String from(Date date) {
        Date now = new Date();
        if (date.getTime() < now.getTime()) {
            return prettyTime.format(date);
        } else {
            return prettyTime.format(now);
        }
    }

}
