package com.delphiaconsulting.timestar.data;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.OrderBy;
import org.greenrobot.greendao.annotation.ToMany;
import org.greenrobot.greendao.annotation.ToOne;

import java.util.List;

/**
 * Created by dxsier on 12/27/16.
 */

@Entity
public class OrgLevelEntity {
    @Id
    private Long id;
    @NotNull
    private String name;
    @OrderBy("order")
    @ToMany(referencedJoinProperty = "orgLevelId")
    private List<OrgItemEntity> orgItems;
    @ToOne(joinProperty = "nextId")
    private OrgLevelEntity next;
    private Long nextId;

    @Override
    public String toString() {
        return String.format("%s) %s", id, name);
    }

    /**
     * Used to resolve relations
     */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /**
     * Used for active entity operations.
     */
    @Generated(hash = 1668478823)
    private transient OrgLevelEntityDao myDao;

    @Generated(hash = 250334324)
    public OrgLevelEntity(Long id, @NotNull String name, Long nextId) {
        this.id = id;
        this.name = name;
        this.nextId = nextId;
    }

    @Generated(hash = 202367390)
    public OrgLevelEntity() {
    }

    @Generated(hash = 920382127)
    private transient Long next__resolvedKey;

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getNextId() {
        return this.nextId;
    }

    public void setNextId(Long nextId) {
        this.nextId = nextId;
    }

    /**
     * To-one relationship, resolved on first access.
     */
    @Generated(hash = 1125947859)
    public OrgLevelEntity getNext() {
        Long __key = this.nextId;
        if (next__resolvedKey == null || !next__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            OrgLevelEntityDao targetDao = daoSession.getOrgLevelEntityDao();
            OrgLevelEntity nextNew = targetDao.load(__key);
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
    @Generated(hash = 982160063)
    public void setNext(OrgLevelEntity next) {
        synchronized (this) {
            this.next = next;
            nextId = next == null ? null : next.getId();
            next__resolvedKey = nextId;
        }
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 998092684)
    public List<OrgItemEntity> getOrgItems() {
        if (orgItems == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            OrgItemEntityDao targetDao = daoSession.getOrgItemEntityDao();
            List<OrgItemEntity> orgItemsNew = targetDao
                    ._queryOrgLevelEntity_OrgItems(id);
            synchronized (this) {
                if (orgItems == null) {
                    orgItems = orgItemsNew;
                }
            }
        }
        return orgItems;
    }

    /**
     * Resets a to-many relationship, making the next get call to query for a fresh result.
     */
    @Generated(hash = 794582207)
    public synchronized void resetOrgItems() {
        orgItems = null;
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
    @Generated(hash = 1076048824)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getOrgLevelEntityDao() : null;
    }
}
