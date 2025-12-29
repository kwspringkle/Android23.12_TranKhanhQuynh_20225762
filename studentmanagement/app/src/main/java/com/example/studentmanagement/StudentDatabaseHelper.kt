package com.example.studentmanagement

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class StudentDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "student_db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "students"

        private const val COLUMN_MSSV = "mssv"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_PHONE = "phone"
        private const val COLUMN_ADDRESS = "address"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = ("CREATE TABLE $TABLE_NAME ("
                + "$COLUMN_MSSV TEXT PRIMARY KEY,"
                + "$COLUMN_NAME TEXT,"
                + "$COLUMN_PHONE TEXT,"
                + "$COLUMN_ADDRESS TEXT)")
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun getAllStudents(): MutableList<Student> {
        val studentList = mutableListOf<Student>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME", null)

        if (cursor.moveToFirst()) {
            do {
                val mssv = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MSSV))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
                val phone = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE)) ?: ""
                val address = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS)) ?: ""

                studentList.add(Student(mssv, name, phone, address))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return studentList
    }

    fun addStudent(student: Student): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_MSSV, student.mssv)
            put(COLUMN_NAME, student.name)
            put(COLUMN_PHONE, student.phone)
            put(COLUMN_ADDRESS, student.address)
        }
        return db.insert(TABLE_NAME, null, values)
    }

    fun updateStudent(student: Student): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, student.name)
            put(COLUMN_PHONE, student.phone)
            put(COLUMN_ADDRESS, student.address)
        }
        return db.update(TABLE_NAME, values, "$COLUMN_MSSV = ?", arrayOf(student.mssv))
    }

    fun deleteStudent(mssv: String): Int {
        val db = this.writableDatabase
        return db.delete(TABLE_NAME, "$COLUMN_MSSV = ?", arrayOf(mssv))
    }
}
