package co.chatsdk.android.app.queries;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.Date;
import java.util.List;

import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.MessageDao;
import static co.chatsdk.core.dao.DaoCore.daoSession;

public class MessageQueries {

    /**
     * Fetch Messages for a thread between dates
     * @param thread
     * @param from
     * @param to
     * @param limit
     * @param orderDesc
     * @return List of messages for thread
     */
    public List<Message> fetchMessages (Thread thread, Date from, Date to, int limit, boolean orderDesc) {
        QueryBuilder<Message> qb = daoSession.queryBuilder(Message.class);
        qb.where(MessageDao.Properties.ThreadId.eq(thread.getId()));

        qb.where(MessageDao.Properties.Date.isNotNull());
        qb.where(MessageDao.Properties.SenderId.isNotNull());

        qb.where(MessageDao.Properties.Date.lt(from.getTime()));
        qb.where(MessageDao.Properties.Date.gt(to.getTime()));

        if (orderDesc) {
            qb.orderDesc(MessageDao.Properties.Date);
        } else {
            qb.orderAsc(MessageDao.Properties.Date);
        }

        qb.limit(limit);
        return qb.list();
    }

}
