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
public class JoinThreadWithUser {
    @Id
    private Long id;
    private Long threadId;
    private Long userId;
    @Convert(converter = StateConverter.class, columnType = Integer.class)
    private State state;

    @Generated(hash = 163799116)
    public JoinThreadWithUser(Long id, Long threadId, Long userId, State state) {
        this.id = id;
        this.threadId = threadId;
        this.userId = userId;
        this.state = state;
    }

    @Generated(hash = 841063619)
    public JoinThreadWithUser() {
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

    public State getState() {
        return this.state;
    }

    public void setState(State state) {
        this.state = state;
    }


    /***
     *  Custom class converters start here
    ***/
    public enum State {
        DEFAULT(0), TYPING(1);

        final int id;

        State(int id) {
            this.id = id;
        }
    }

    public static class StateConverter implements PropertyConverter<State, Integer> {
        @Override
        public State convertToEntityProperty(Integer databaseValue) {
            if (databaseValue == null) {
                return null;
            }
            for (State state : State.values()) {
                if (state.id == databaseValue) {
                    return state;
                }
            }
            return State.DEFAULT;
        }

        @Override
        public Integer convertToDatabaseValue(State entityProperty) {
            return entityProperty == null ? null : entityProperty.id;
        }
    }

}
