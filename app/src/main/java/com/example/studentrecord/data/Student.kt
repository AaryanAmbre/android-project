package com.example.studentrecord.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val rollNumber: String,
    val course: String,
    val javaMarks: Int,
    val dbmsMarks: Int,
    val osMarks: Int
) {
    val totalMarks: Int get() = javaMarks + dbmsMarks + osMarks
    val averageMarks: Float get() = totalMarks / 3f
}
