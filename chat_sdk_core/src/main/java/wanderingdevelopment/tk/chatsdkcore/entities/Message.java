package wanderingdevelopment.tk.chatsdkcore.entities;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.converter.PropertyConverter;
import org.joda.time.DateTime;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by kykrueger on 2016-10-23.
 */

@Entity
public class Message {
    @Id
    private Long id;
    private Long threadId;
    @Convert(converter = TypeConverter.class, columnType = Integer.class)
    private Type type;
    @Convert(converter = DateTimeConverter.class, columnType = Long.class)
    private DateTime dateTime;
    private String text;
    private String payload;

    @Generated(hash = 742012812)
    public Message(Long id, Long threadId, Type type, DateTime dateTime, String text,
            String payload) {
        this.id = id;
        this.threadId = threadId;
        this.type = type;
        this.dateTime = dateTime;
        this.text = text;
        this.payload = payload;
    }

    @Generated(hash = 637306882)
    public Message() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getThreadId() {
        return this.threadId;
    }

    public void setThreadId(Long threadId) {
        this.threadId = threadId;
    }

    public Type getType() {
        return this.type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public DateTime getDateTime() {
        return this.dateTime;
    }

    public void setDateTime(DateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getPayload() {
        return this.payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    /***
     *  Custom class converters start here
     */
    public enum Type {
        DEFAULT(0), IMAGE(1), LOCATION(2);

        final int id;

        Type(int id) {
            this.id = id;
        }
    }

    public static class TypeConverter implements PropertyConverter<Type, Integer> {
        @Override
        public Type convertToEntityProperty(Integer databaseValue) {
            if (databaseValue == null) {
                return null;
            }
            for (Type type : Type.values()) {
                if (type.id == databaseValue) {
                    return type;
                }
            }
            return Type.DEFAULT;
        }

        @Override
        public Integer convertToDatabaseValue(Type entityProperty) {
            return entityProperty == null ? null : entityProperty.id;
        }
    }

    // TODO: test how this handles timezones
    public static class DateTimeConverter implements PropertyConverter<DateTime, Long> {
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
}
