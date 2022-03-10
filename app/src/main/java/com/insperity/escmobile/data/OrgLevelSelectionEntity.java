package com.insperity.escmobile.data;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.ToOne;

/**
 * Created by dxsier on 12/27/16.
 */

@Entity
public class OrgLevelSelectionEntity {
    @Id(autoincrement = true)
    private Long id;
    @ToOne(joinProperty = "punchId")
    private PunchEntity punch;
    private Long punchId;
    @ToOne(joinProperty = "orgLevelId")
    private OrgLevelEntity orgLevel;
    private Long orgLevelId;
    @ToOne(joinProperty = "orgItemId")
    private OrgItemEntity orgItem;
    private Long orgItemId;

    @Override
    public String toString() {
        return String.format("%s / %s / %s", getPunch().getPunchCategory(), getOrgLevel(), getOrgItem());
    }

    /**
     * Used to resolve relations
     */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /**
     * Used for active entity operations.
     */
    @Generated(hash = 597205578)
    private transient OrgLevelSelectionEntityDao myDao;

    @Generated(hash = 1878359895)
    public OrgLevelSelectionEntity(Long id, Long punchId, Long orgLevelId, Long orgItemId) {
        this.id = id;
        this.punchId = punchId;
        this.orgLevelId = orgLevelId;
        this.orgItemId = orgItemId;
    }

    @Generated(hash = 935749615)
    public OrgLevelSelectionEntity() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPunchId() {
        return this.punchId;
    }

    public void setPunchId(Long punchId) {
        this.punchId = punchId;
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

    @Generated(hash = 289936510)
    private transient Long punch__resolvedKey;

    /**
     * To-one relationship, resolved on first access.
     */
    @Generated(hash = 789485842)
    public PunchEntity getPunch() {
        Long __key = this.punchId;
        if (punch__resolvedKey == null || !punch__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            PunchEntityDao targetDao = daoSession.getPunchEntityDao();
            PunchEntity punchNew = targetDao.load(__key);
            synchronized (this) {
                punch = punchNew;
                punch__resolvedKey = __key;
            }
        }
        return punch;
    }

    /**
     * called by internal mechanisms, do not call yourself.
     */
    @Generated(hash = 733975090)
    public void setPunch(PunchEntity punch) {
        synchronized (this) {
            this.punch = punch;
            punchId = punch == null ? null : punch.getId();
            punch__resolvedKey = punchId;
        }
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
    @Generated(hash = 45531151)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getOrgLevelSelectionEntityDao() : null;
    }
}
