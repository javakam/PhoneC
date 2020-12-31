package ando.guard.database

import ando.guard.database.DataSourceManager.loadBlockedNumbersFromDB
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import java.io.File

/**
 * Title: SystemDbHelper
 * <p>
 * Description:
 * </p>
 * @author javakam
 * @date 2020/12/31  15:16
 */
object SystemDBHelper {

    var mDB: SQLiteDatabase? = null

    private val dbFile: File by lazy { loadBlockedNumbersFromDB() }

    /**
     * 打开数据库
     */
    private fun openDB(): Boolean {
        if (hasDB()) {
            if (mDB == null || mDB?.isOpen == false) {
                mDB = SQLiteDatabase.openDatabase(
                    dbFile.absolutePath,
                    null,
                    SQLiteDatabase.OPEN_READWRITE
                )
            }
            return true
        }
        return false
    }

    /**
     * 判断数据库是否存在
     */
    private fun hasDB(): Boolean {
        return dbFile.exists()
    }

    private fun getString(cursor: Cursor, columnName: String): String? {
        return cursor.getString(cursor.getColumnIndex(columnName))
    }

    private fun getInt(cursor: Cursor, columnName: String): Int {
        return cursor.getInt(cursor.getColumnIndex(columnName))
    }


}