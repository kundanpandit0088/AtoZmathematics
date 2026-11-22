package com.atozmathematics.feeapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String = "",
    val className: String = "",
    val monthlyFee: Double = 0.0,
    val courseFee: Double = 0.0,
    val photoUri: String? = null,
    val active: Boolean = true
)

@Entity(tableName = "payments")
data class Payment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val amount: Double,
    val feeType: String,
    val period: String,
    val paidAt: Long = System.currentTimeMillis(),
    val receiptNumber: String
)

@Entity(tableName = "attendance")
data class Attendance(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val date: String,
    val status: String
)
