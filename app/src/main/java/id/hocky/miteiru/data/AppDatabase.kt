package id.hocky.miteiru.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import id.hocky.miteiru.data.dao.PreviousCaptureDao
import id.hocky.miteiru.data.dao.SmallCaptureDao
import id.hocky.miteiru.data.entities.PreviousCapture
import id.hocky.miteiru.data.entities.SmallCapture
import id.hocky.miteiru.data.entities.TextBoxConverter

@Database(
    entities = [SmallCapture::class, PreviousCapture::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(TextBoxConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun smallCaptureDao(): SmallCaptureDao
    abstract fun previousCaptureDao(): PreviousCaptureDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "miteiru_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

