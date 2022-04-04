package com.delphiaconsulting.timestar.data;

import com.delphiaconsulting.timestar.util.PunchStatus;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.ToMany;
import org.greenrobot.greendao.annotation.ToOne;

import java.util.List;

/**
 * Created by dxsier on 12/27/16.
 */

@Entity
public class PunchEntity {
    @Id(autoincrement = true)
    private Long id;
    private Integer externalId;
    @NotNull
    private Long datetime;
    @NotNull
    private String comment;
    @PunchStatus
    private Integer syncStatus;
    @ToOne(joinProperty = "punchCategoryId")
    private PunchCategoryEntity punchCategory;
    private Long punchCategoryId;
    @ToMany(referencedJoinProperty = "punchId")
    private List<OrgLevelSelectionEntity> orgLevels;

    @Override
    public String toString() {
        return String.format("%s) %s", datetime, getPunchCategory());
    }

    /**
     * Used to resolve relations
     */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /**
     * Used for active entity operations.
     */
    @Generated(hash = 1915217549)
    private transient PunchEntityDao myDao;

    @Generated(hash = 2039323177)
    public PunchEntity(Long id, Integer externalId, @NotNull Long datetime,
                       @NotNull String comment, Integer syncStatus, Long punchCategoryId) {
        this.id = id;
        this.externalId = externalId;
        this.datetime = datetime;
        this.comment = comment;
        this.syncStatus = syncStatus;
        this.punchCategoryId = punchCategoryId;
    }

    @Generated(hash = 322268324)
    public PunchEntity() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getExternalId() {
        return this.externalId;
    }

    public void setExternalId(Integer externalId) {
        this.externalId = externalId;
    }

    public Long getDatetime() {
        return this.datetime;
    }

    public void setDatetime(Long datetime) {
        this.datetime = datetime;
    }

    public String getComment() {
        return this.comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @PunchStatus
    public Integer getSyncStatus() {
        return this.syncStatus;
    }

    public void setSyncStatus(@PunchStatus Integer syncStatus) {
        this.syncStatus = syncStatus;
    }

    public Long getPunchCategoryId() {
        return this.punchCategoryId;
    }

    public void setPunchCategoryId(Long punchCategoryId) {
        this.punchCategoryId = punchCategoryId;
    }

    @Generated(hash = 618353071)
    private transient Long punchCategory__resolvedKey;

    /**
     * To-one relationship, resolved on first access.
     */
    @Generated(hash = 1737377388)
    public PunchCategoryEntity getPunchCategory() {
        Long __key = this.punchCategoryId;
        if (punchCategory__resolvedKey == null
                || !punchCategory__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            PunchCategoryEntityDao targetDao = daoSession
                    .getPunchCategoryEntityDao();
            PunchCategoryEntity punchCategoryNew = targetDao.load(__key);
            synchronized (this) {
                punchCategory = punchCategoryNew;
                punchCategory__resolvedKey = __key;
            }
        }
        return punchCategory;
    }

    /**
     * called by internal mechanisms, do not call yourself.
     */
    @Generated(hash = 613316364)
    public void setPunchCategory(PunchCategoryEntity punchCategory) {
        synchronized (this) {
            this.punchCategory = punchCategory;
            punchCategoryId = punchCategory == null ? null : punchCategory.getId();
            punchCategory__resolvedKey = punchCategoryId;
        }
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 1799422937)
    public List<OrgLevelSelectionEntity> getOrgLevels() {
        if (orgLevels == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            OrgLevelSelectionEntityDao targetDao = daoSession
                    .getOrgLevelSelectionEntityDao();
            List<OrgLevelSelectionEntity> orgLevelsNew = targetDao
                    ._queryPunchEntity_OrgLevels(id);
            synchronized (this) {
                if (orgLevels == null) {
                    orgLevels = orgLevelsNew;
                }
            }
        }
        return orgLevels;
    }

    /**
     * Resets a to-many relationship, making the next get call to query for a fresh result.
     */
    @Generated(hash = 1226082909)
    public synchronized void resetOrgLevels() {
        orgLevels = null;
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
    @Generated(hash = 1437757733)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getPunchEntityDao() : null;
    }
}
