package sdk.chat.core.dao;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Unique;

import java.io.File;

import sdk.chat.core.base.AbstractEntity;
import sdk.chat.core.rigs.FileUploadable;
import sdk.chat.core.rigs.Uploadable;
import sdk.chat.core.storage.UploadStatus;

@Entity(active = true)
public class CachedFile extends AbstractEntity {

    public enum Type {
        None,
        Upload,
        Download
    }

    @Id
    private Long id;

    @Unique
    private String entityID;
    private String identifier;

    private Integer type;
    private String localPath;
    private String remotePath;
    private String name;
    private String messageKey;
    private boolean reportProgress;

    private String mimeType;
    private Integer status;

    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    @Generated(hash = 1018577903)
    private transient CachedFileDao myDao;
    @Generated(hash = 1409345268)
    public CachedFile(Long id, String entityID, String identifier, Integer type,
            String localPath, String remotePath, String name, String messageKey,
            boolean reportProgress, String mimeType, Integer status) {
        this.id = id;
        this.entityID = entityID;
        this.identifier = identifier;
        this.type = type;
        this.localPath = localPath;
        this.remotePath = remotePath;
        this.name = name;
        this.messageKey = messageKey;
        this.reportProgress = reportProgress;
        this.mimeType = mimeType;
        this.status = status;
    }
    @Generated(hash = 61789621)
    public CachedFile() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getIdentifier() {
        return this.identifier;
    }
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
    public Integer getType() {
        return this.type;
    }
    public void setType(Integer type) {
        this.type = type;
    }
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getMimeType() {
        return this.mimeType;
    }
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
    public Integer getStatus() {
        return this.status;
    }
    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }
    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }

    @Override
    public void setEntityID(String entityID) {
        this.entityID = entityID;
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 237338814)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getCachedFileDao() : null;
    }

    @Override
    public String getEntityID() {
        return entityID;
    }

    public void setUploadStatus(UploadStatus status) {
        this.setStatus(status.ordinal());
    }

    public UploadStatus getUploadStatus() {
        if (getStatus() != null) {
            return UploadStatus.values()[getStatus()];
        }
        return UploadStatus.None;
    }

    public void setFileType(Type type) {
        this.setType(type.ordinal());
    }

    public Type getFileType() {
        if (getType() != null) {
            return Type.values()[getType()];
        }
        return Type.None;
    }
    public String getLocalPath() {
        return this.localPath;
    }
    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }
    public String getRemotePath() {
        return this.remotePath;
    }
    public void setRemotePath(String remotePath) {
        this.remotePath = remotePath;
        update();
    }

    public Uploadable getUploadable() {
        File file = new File(localPath);
        if (file.exists() && name != null && mimeType != null) {
            FileUploadable uploadable = new FileUploadable(file, name, mimeType, messageKey);
            uploadable.reportProgress = reportProgress;
            return uploadable;
        }
        return null;
    }
    public String getMessageKey() {
        return this.messageKey;
    }
    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }
    public boolean getReportProgress() {
        return this.reportProgress;
    }
    public void setReportProgress(boolean reportProgress) {
        this.reportProgress = reportProgress;
    }

}
