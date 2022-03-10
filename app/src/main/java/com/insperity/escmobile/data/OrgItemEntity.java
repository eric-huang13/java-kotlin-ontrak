package com.insperity.escmobile.data;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.ToOne;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dxsier on 12/27/16.
 */

@Entity
public class OrgItemEntity {
    @Id
    private Long id;
    @NotNull
    private String label;
    @ToOne(joinProperty = "orgLevelId")
    private OrgLevelEntity orgLevel;
    private Long orgLevelId;
    private String nextOrgItems;
    private Integer order;
    private transient List<Long> nextOrgItemIds;

    @Override
    public String toString() {
        if (daoSession == null) {
            return label;
        }
        return String.format("%s / %s) %s", getOrgLevel(), id, label);
    }

    public OrgItemEntity setNextOrgItemIds(List<Long> nextOrgItemIds) {
        this.nextOrgItemIds = nextOrgItemIds;
        StringBuilder builder = new StringBuilder();
        for (Long id : this.nextOrgItemIds) {
            if (builder.length() > 0) {
                builder.append(" ");
            }
            builder.append(id);
        }
        this.nextOrgItems = builder.toString();
        return this;
    }

    public List<Long> getNextOrgItemIds() {
        String[] ids = nextOrgItems.trim().split("\\s+");
        if (nextOrgItemIds == null) {
            nextOrgItemIds = new ArrayList<>();
        }
        if (!nextOrgItemIds.isEmpty()) {
            nextOrgItemIds.clear();
        }
        for (String id : ids) {
            if (id.isEmpty()) {
                continue;
            }
            nextOrgItemIds.add(Long.valueOf(id));
        }
        return nextOrgItemIds;
    }

    /**
     * Used to resolve relations
     */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /**
     * Used for active entity operations.
     */
    @Generated(hash = 474255096)
    private transient OrgItemEntityDao myDao;

    @Generated(hash = 511902476)
    public OrgItemEntity(Long id, @NotNull String label, Long orgLevelId, String nextOrgItems,
                         Integer order) {
        this.id = id;
        this.label = label;
        this.orgLevelId = orgLevelId;
        this.nextOrgItems = nextOrgItems;
        this.order = order;
    }

    @Generated(hash = 1281453424)
    public OrgItemEntity() {
    }

    @Generated(hash = 1744461807)
    private transient Long orgLevel__resolvedKey;

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLabel() {
        return this.label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Long getOrgLevelId() {
        return this.orgLevelId;
    }

    public void setOrgLevelId(Long orgLevelId) {
        this.orgLevelId = orgLevelId;
    }

    public String getNextOrgItems() {
        return this.nextOrgItems;
    }

    public void setNextOrgItems(String nextOrgItems) {
        this.nextOrgItems = nextOrgItems;
    }

    public Integer getOrder() {
        return this.order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

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
    @Generated(hash = 892213766)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getOrgItemEntityDao() : null;
    }
}
