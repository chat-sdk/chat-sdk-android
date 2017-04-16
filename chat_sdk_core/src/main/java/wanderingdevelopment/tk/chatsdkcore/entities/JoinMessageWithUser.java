package wanderingdevelopment.tk.chatsdkcore.entities;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.converter.PropertyConverter;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by kykrueger on 2016-10-23.
 */

@Entity
public class JoinMessageWithUser {

    @Id
    private Long id;
    private Long threadId;
    private Long userId;
    @Convert(converter = ReadStatusConverter.class, columnType = Integer.class)
    private ReadStatus readStatus;

    @Generated(hash = 1056730186)
    public JoinMessageWithUser(Long id, Long threadId, Long userId, ReadStatus readStatus) {
        this.id = id;
        this.threadId = threadId;
        this.userId = userId;
        this.readStatus = readStatus;
    }

    @Generated(hash = 1276602709)
    public JoinMessageWithUser() {
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

    public Long getUserId() {
        return this.userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public ReadStatus getReadStatus() {
        return this.readStatus;
    }

    public void setReadStatus(ReadStatus readStatus) {
        this.readStatus = readStatus;
    }


    /***
     *  Custom class converters start here
     ***/
    public enum ReadStatus {
        DEFAULT(0), DELIVERED(1), READ(2);

        final int id;

        ReadStatus(int id) {
            this.id = id;
        }
    }

    public static class ReadStatusConverter implements PropertyConverter<ReadStatus, Integer> {
        @Override
        public ReadStatus convertToEntityProperty(Integer databaseValue) {
            if (databaseValue == null) {
                return null;
            }
            for (ReadStatus readStatus : ReadStatus.values()) {
                if (readStatus.id == databaseValue) {
                    return readStatus;
                }
            }
            return ReadStatus.DEFAULT;
        }

        @Override
        public Integer convertToDatabaseValue(ReadStatus entityProperty) {
            return entityProperty == null ? null : entityProperty.id;
        }
    }

}
