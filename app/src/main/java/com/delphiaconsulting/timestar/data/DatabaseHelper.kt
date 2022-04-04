package com.delphiaconsulting.timestar.data

import android.content.Context
import com.delphiaconsulting.timestar.BuildConfig
import com.delphiaconsulting.timestar.data.DaoMaster.createAllTables
import org.greenrobot.greendao.database.Database
import org.greenrobot.greendao.database.DatabaseOpenHelper
import java.util.*

class DatabaseHelper(context: Context, name: String) : DatabaseOpenHelper(context, name, BuildConfig.DB_VERSION) {

    override fun onCreate(db: Database) = createAllTables(db, true)

    override fun onUpgrade(db: Database, oldVersion: Int, newVersion: Int) {
        val migrations = ArrayList<Migration>()
        migrations.add(Migration2())
        migrations.filter { oldVersion < it.version }
                .sortedBy { it.version }
                .forEach { it.applyMigration(db) }
    }

    private class Migration2 : Migration {
        override val version = 2

        override fun applyMigration(db: Database) = db.execSQL("ALTER TABLE '${OrgItemEntityDao.TABLENAME}' ADD COLUMN '${OrgItemEntityDao.Properties.Order.columnName}' INTEGER;")
    }

    private interface Migration {
        val version: Int

        fun applyMigration(db: Database)
    }
}
