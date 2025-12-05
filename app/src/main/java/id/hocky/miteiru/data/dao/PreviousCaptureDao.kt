package id.hocky.miteiru.data.dao

import androidx.room.*
import id.hocky.miteiru.data.entities.PreviousCapture
import kotlinx.coroutines.flow.Flow

@Dao
interface PreviousCaptureDao {
    @Query("SELECT * FROM previous_captures ORDER BY createdAt DESC")
    fun getAllPreviousCaptures(): Flow<List<PreviousCapture>>

    @Query("SELECT * FROM previous_captures ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentPreviousCaptures(limit: Int): Flow<List<PreviousCapture>>

    @Query("SELECT * FROM previous_captures WHERE id = :id")
    suspend fun getPreviousCaptureById(id: Long): PreviousCapture?

    @Insert
    suspend fun insert(previousCapture: PreviousCapture): Long

    @Update
    suspend fun update(previousCapture: PreviousCapture)

    @Delete
    suspend fun delete(previousCapture: PreviousCapture)

    @Query("DELETE FROM previous_captures WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE previous_captures SET textBoxesJson = :textBoxesJson WHERE id = :id")
    suspend fun updateTextBoxes(id: Long, textBoxesJson: String)
}

