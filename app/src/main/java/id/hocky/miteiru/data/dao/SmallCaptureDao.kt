package id.hocky.miteiru.data.dao

import androidx.room.*
import id.hocky.miteiru.data.entities.SmallCapture
import kotlinx.coroutines.flow.Flow

@Dao
interface SmallCaptureDao {
    @Query("SELECT * FROM small_captures ORDER BY createdAt DESC")
    fun getAllSmallCaptures(): Flow<List<SmallCapture>>

    @Query("SELECT * FROM small_captures ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentSmallCaptures(limit: Int): Flow<List<SmallCapture>>

    @Query("SELECT * FROM small_captures WHERE id = :id")
    suspend fun getSmallCaptureById(id: Long): SmallCapture?

    @Insert
    suspend fun insert(smallCapture: SmallCapture): Long

    @Update
    suspend fun update(smallCapture: SmallCapture)

    @Delete
    suspend fun delete(smallCapture: SmallCapture)

    @Query("DELETE FROM small_captures WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE small_captures SET analysis = :analysis, sourceLanguage = :sourceLanguage WHERE id = :id")
    suspend fun updateAnalysis(id: Long, analysis: String, sourceLanguage: String)
}

