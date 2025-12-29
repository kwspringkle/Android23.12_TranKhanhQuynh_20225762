package com.example.studentmanagement

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class StudentViewModel(application: Application) : AndroidViewModel(application) {

    private val dbHelper = StudentDatabaseHelper(application)

    private val _studentList = MutableLiveData<MutableList<Student>>()
    val studentList: LiveData<MutableList<Student>> = _studentList

    private val _selectedStudent = MutableLiveData<Student?>()
    val selectedStudent: LiveData<Student?> = _selectedStudent

    private val _selectedPosition = MutableLiveData<Int>(-1)
    val selectedPosition: LiveData<Int> = _selectedPosition

    init {
        loadStudents()
    }

    private fun loadStudents() {
        _studentList.value = dbHelper.getAllStudents()
    }

    fun addStudent(student: Student) {
        val result = dbHelper.addStudent(student)
        if (result != -1L) {
            loadStudents()
        }
    }

    fun updateStudent(position: Int, student: Student) {
        val result = dbHelper.updateStudent(student)
        if (result > 0) {
            loadStudents()
        }
    }

    fun deleteStudent(position: Int) {
        val currentList = _studentList.value
        if (currentList != null && position in currentList.indices) {
            val studentToDelete = currentList[position]
            val result = dbHelper.deleteStudent(studentToDelete.mssv)
            if (result > 0) {
                loadStudents()
            }
        }
    }

    fun selectStudent(student: Student, position: Int) {
        _selectedStudent.value = student
        _selectedPosition.value = position
    }

    fun clearSelection() {
        _selectedStudent.value = null
        _selectedPosition.value = -1
    }
}
