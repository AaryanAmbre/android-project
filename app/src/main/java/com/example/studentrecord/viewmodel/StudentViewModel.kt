package com.example.studentrecord.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.studentrecord.data.Student
import com.example.studentrecord.data.StudentDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StudentViewModel(private val dao: StudentDao) : ViewModel() {

    val studentsList: StateFlow<List<Student>> = dao.getAllStudents().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun addStudent(student: Student) {
        viewModelScope.launch { dao.insertStudent(student) }
    }

    fun updateStudent(student: Student) {
        viewModelScope.launch { dao.updateStudent(student) }
    }

    fun deleteStudent(student: Student) {
        viewModelScope.launch { dao.deleteStudent(student) }
    }
}

class StudentViewModelFactory(private val dao: StudentDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StudentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StudentViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
