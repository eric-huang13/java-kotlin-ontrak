package com.delphiaconsulting.timestar.data;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.ToOne;

/**
 * Created by dxsier on 12/27/16.
 */

@Entity
public class OrgDefaultEntity {
    @Id(autoincrement = true)
    private Long id;
    @ToOne(joinProperty = "orgLevelId")
    private OrgLevelEntity orgLevel;
    private Long orgLevelId;
    @ToOne(joinProperty = "orgItemId")
    private OrgItemEntity orgItem;
    private Long orgItemId;
    @ToOne(joinProperty = "nextId")
    private OrgDefaultEntity next;
    private Long nextId;
    private boolean mainDefaultRoot;

    @Override
    public String toString() {
        return String.format("%s / %s", getOrgLevel(), getOrgItem());
    }

    /**
     * Used to resolve relations
     */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /**
     * Used for active entity operations.
     */
    @Generated(hash = 1981062877)
    private transient OrgDefaultEntityDao myDao;

    @Generated(hash = 728971940)
    public OrgDefaultEntity(Long id, Long orgLevelId, Long orgItemId, Long nextId,
                            boolean mainDefaultRoot) {
        this.id = id;
        this.orgLevelId = orgLevelId;
        this.orgItemId = orgItemId;
        this.nextId = nextId;
        this.mainDefaultRoot = mainDefaultRoot;
    }

    @Generated(hash = 228988460)
    public OrgDefaultEntity() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrgLevelId() {
        return this.orgLevelId;
    }

    public void setOrgLevelId(Long orgLevelId) {
        this.orgLevelId = orgLevelId;
    }

    public Long getOrgItemId() {
        return this.orgItemId;
    }

    public void setOrgItemId(Long orgItemId) {
        this.orgItemId = orgItemId;
    }

    public Long getNextId() {
        return this.nextId;
    }

    public void setNextId(Long nextId) {
        this.nextId = nextId;
    }

    public boolean getMainDefaultRoot() {
        return this.mainDefaultRoot;
    }

    public void setMainDefaultRoot(boolean mainDefaultRoot) {
        this.mainDefaultRoot = mainDefaultRoot;
    }

    @Generated(hash = 1744461807)
    private transient Long orgLevel__resolvedKey;

    /**
     * To-one relationship, resolved on first access.
     */
    @Generated(hash = 527895622)
    public OrgLevelEntity getOrgLevel() {
        Long __key = this.orgLevelId;
        if (orgLevel__resolvedKey == null || !orgLevel__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            OrgLevelEntityDao targetDao = daoSession.getOrgLevelEntityDao();
            OrgLevelEntity orgLevelNew = targetDao.load(__key);
            synchronized (this) {
                orgLevel = orgLevelNew;
                orgLevel__resolvedKey = __key;
            }
        }
        return orgLevel;
    }

    /**
     * called by internal mechanisms, do not call yourself.
     */
    @Generated(hash = 409904564)
    public void setOrgLevel(OrgLevelEntity orgLevel) {
        synchronized (this) {
            this.orgLevel = orgLevel;
            orgLevelId = orgLevel == null ? null : orgLevel.getId();
            orgLevel__resolvedKey = orgLevelId;
        }
    }

    @Generated(hash = 1937993106)
    private transient Long orgItem__resolvedKey;

    /**
     * To-one relationship, resolved on first access.
     */
    @Generated(hash = 713068836)
    public OrgItemEntity getOrgItem() {
        Long __key = this.orgItemId;
        if (orgItem__resolvedKey == null || !orgItem__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            OrgItemEntityDao targetDao = daoSession.getOrgItemEntityDao();
            OrgItemEntity orgItemNew = targetDao.load(__key);
            synchronized (this) {
                orgItem = orgItemNew;
                orgItem__resolvedKey = __key;
            }
        }
        return orgItem;
    }

    /**
     * called by internal mechanisms, do not call yourself.
     */
    @Generated(hash = 1074777202)
    public void setOrgItem(OrgItemEntity orgItem) {
        synchronized (this) {
            this.orgItem = orgItem;
            orgItemId = orgItem == null ? null : orgItem.getId();
            orgItem__resolvedKey = orgItemId;
        }
    }

    @Generated(hash = 920382127)
    private transient Long next__resolvedKey;

    /**
     * To-one relationship, resolved on first access.
     */
    @Generated(hash = 137852804)
    public OrgDefaultEntity getNext() {
        Long __key = this.nextId;
        if (next__resolvedKey == null || !next__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            OrgDefaultEntityDao targetDao = daoSession.getOrgDefaultEntityDao();
            OrgDefaultEntity nextNew = targetDao.load(__key);
            synchronized (this) {
                next = nextNew;
                next__resolvedKey = __key;
            }
        }
        return next;
    }

    /**
     * called by internal mechanisms, do not call yourself.
     */
    @Generated(hash = 91561722)
    public void setNext(OrgDefaultEntity next) {
        synchronized (this) {
            this.next = next;
            nextId = next == null ? null : next.getId();
            next__resolvedKey = nextId;
        }
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
    @Generated(hash = 759815172)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getOrgDefaultEntityDao() : null;
    }
}
