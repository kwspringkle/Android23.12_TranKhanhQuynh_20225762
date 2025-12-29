package com.example.studentmanagement
import java.io.Serializable

data class Student(
    var mssv: String,
    var name: String,
    var phone: String = "",
    var address: String = ""
) : Serializable
