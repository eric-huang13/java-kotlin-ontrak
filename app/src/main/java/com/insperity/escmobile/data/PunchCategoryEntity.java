package com.insperity.escmobile.data;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.OrderBy;
import org.greenrobot.greendao.annotation.ToMany;
import org.greenrobot.greendao.annotation.Unique;

import java.util.List;

/**
 * Created by dxsier on 12/27/16.
 */

@Entity
public class PunchCategoryEntity {
    @Id
    private Long id;
    @NotNull
    private String name;
    @NotNull
    private String description;
    private Boolean defaultOrgLevel;
    @ToMany(referencedJoinProperty = "punchCategoryId")
    @OrderBy("datetime DESC")
    private List<PunchEntity> punches;
    @Unique
    private Integer order;

    @Override
    public String toString() {
        return String.format("%s) %s - %s", id, name, description);
    }

    /**
     * Used to resolve relations
     */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /**
     * Used for active entity operations.
     */
    @Generated(hash = 259809173)
    private transient PunchCategoryEntityDao myDao;

    @Generated(hash = 167912951)
    public PunchCategoryEntity(Long id, @NotNull String name,
                               @NotNull String description, Boolean defaultOrgLevel, Integer order) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.defaultOrgLevel = defaultOrgLevel;
        this.order = order;
    }

    @Generated(hash = 961417000)
    public PunchCategoryEntity() {
    }

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

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getDefaultOrgLevel() {
        return this.defaultOrgLevel;
    }

    public void setDefaultOrgLevel(Boolean defaultOrgLevel) {
        this.defaultOrgLevel = defaultOrgLevel;
    }

    public Integer getOrder() {
        return this.order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 783728697)
    public List<PunchEntity> getPunches() {
        if (punches == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            PunchEntityDao targetDao = daoSession.getPunchEntityDao();
            List<PunchEntity> punchesNew = targetDao
                    ._queryPunchCategoryEntity_Punches(id);
            synchronized (this) {
                if (punches == null) {
                    punches = punchesNew;
                }
            }
        }
        return punches;
    }

    /**
     * Resets a to-many relationship, making the next get call to query for a fresh result.
     */
    @Generated(hash = 1764356859)
    public synchronized void resetPunches() {
        punches = null;
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
    @Generated(hash = 1241286098)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getPunchCategoryEntityDao() : null;
    }
}
