package com.atozmathematics.feeapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM students WHERE active = 1 ORDER BY name")
    fun observeStudents(): Flow<List<Student>>

    @Insert
    suspend fun addStudent(student: Student): Long

    @Query("SELECT * FROM payments ORDER BY paidAt DESC")
    fun observePayments(): Flow<List<Payment>>

    @Insert
    suspend fun addPayment(payment: Payment)

    @Query("SELECT * FROM attendance WHERE date = :date")
    fun observeAttendance(date: String): Flow<List<Attendance>>

    @Insert
    suspend fun markAttendance(attendance: Attendance)

    @Query("SELECT * FROM students WHERE id = :id LIMIT 1")
    suspend fun getStudent(id: Long): Student?
}
