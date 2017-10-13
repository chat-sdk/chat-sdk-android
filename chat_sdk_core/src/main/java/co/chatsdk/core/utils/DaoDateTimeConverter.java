package co.chatsdk.core.utils;

import org.greenrobot.greendao.converter.PropertyConverter;
import org.joda.time.DateTime;

/**
 * Created by ben on 10/6/17.
 */

// TODO: test how this handles timezones
public class DaoDateTimeConverter implements PropertyConverter<DateTime, Long> {

    @Override
    public DateTime convertToEntityProperty(Long databaseValue) {
        if (databaseValue == null) {
            return null;
        }

        return new DateTime(databaseValue);
    }

    @Override
    public Long convertToDatabaseValue(DateTime dateTime) {
        return dateTime == null ? null : dateTime.getMillis();
    }

}
