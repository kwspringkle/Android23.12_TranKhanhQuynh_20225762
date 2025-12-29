package com.example.filemanager

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.widget.PopupMenu
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvPath: TextView
    private lateinit var adapter: FileAdapter
    private var currentPath: File = Environment.getExternalStorageDirectory()
    private var selectedFile: File? = null // File đang được nhấn giữ
    private var fileToCopy: File? = null // File đang chờ copy

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Setup Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        tvPath = findViewById(R.id.tvPath)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        checkPermission()
    }

    // 1. Kiểm tra quyền truy cập bộ nhớ
    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            } else {
                loadFiles(currentPath)
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 100)
            } else {
                loadFiles(currentPath)
            }
        }
    }
    
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadFiles(currentPath)
        } else {
            Toast.makeText(this, "Cần quyền truy cập bộ nhớ để sử dụng ứng dụng", Toast.LENGTH_LONG).show()
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Kiểm tra lại quyền khi quay lại app (sau khi cấp quyền trên Android 11+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager() && !::adapter.isInitialized) {
                loadFiles(currentPath)
            }
        }
    }

    // 2. Hiển thị danh sách file
    private fun loadFiles(directory: File) {
        tvPath.text = directory.absolutePath
        val files = directory.listFiles()?.toList() ?: emptyList()

        adapter = FileAdapter(files,
            onFileClick = { file -> openFileOrFolder(file) },
            onFileLongClick = { file, view -> showContextMenu(file, view) }
        )
        recyclerView.adapter = adapter
    }

    // 3. Xử lý mở File hoặc Folder
    private fun openFileOrFolder(file: File) {
        if (file.isDirectory) {
            currentPath = file
            loadFiles(currentPath)
        } else {
            openFileContent(file)
        }
    }

    private fun openFileContent(file: File) {
        val fileName = file.name.lowercase()
        val extension = if (fileName.contains(".")) {
            fileName.substring(fileName.lastIndexOf(".") + 1)
        } else {
            ""
        }
        
        // Chỉ hỗ trợ TXT và ảnh (BMP, JPG, PNG) theo yêu cầu
        when {
            extension == "txt" || extension in listOf("jpg", "jpeg", "png", "bmp") -> {
                val intent = Intent(this, ViewFileActivity::class.java)
                intent.putExtra("file_path", file.absolutePath)
                startActivity(intent)
            }
            else -> {
                Toast.makeText(this, "Chỉ hỗ trợ xem file TXT hoặc ảnh (BMP, JPG, PNG)", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 4. Option Menu: Tạo mới [cite: 9]
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.add(0, 1, 0, "Tạo thư mục mới")
        menu?.add(0, 2, 0, "Tạo file văn bản mới")
        if (fileToCopy != null) menu?.add(0, 3, 0, "Dán file đã copy") // Nút Paste
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            1 -> showCreateDialog(isFolder = true)
            2 -> showCreateDialog(isFolder = false)
            3 -> pasteFile()
        }
        return super.onOptionsItemSelected(item)
    }

    // 5. Context Menu: Đổi tên, Xóa, Copy [cite: 5, 8]
    private fun showContextMenu(file: File, view: View) {
        selectedFile = file
        val popupMenu = PopupMenu(this, view)
        popupMenu.menu.add(0, 101, 0, "Đổi tên")
        popupMenu.menu.add(0, 102, 0, "Xóa")
        if (file.isFile) {
            popupMenu.menu.add(0, 103, 0, "Sao chép")
        }
        
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                101 -> showRenameDialog(file)
                102 -> showDeleteDialog(file)
                103 -> {
                    fileToCopy = file
                    Toast.makeText(this, "Đã copy. Hãy đến thư mục đích và chọn 'Dán' từ menu góc phải", Toast.LENGTH_LONG).show()
                    invalidateOptionsMenu() // Cập nhật menu để hiện nút Paste
                }
            }
            true
        }
        popupMenu.show()
    }

    // --- CÁC HÀM XỬ LÝ DIALOG & LOGIC ---

    // Dialog tạo mới (Thư mục hoặc File) [cite: 9]
    private fun showCreateDialog(isFolder: Boolean) {
        val input = EditText(this)
        input.setPadding(50, 20, 50, 20)
        AlertDialog.Builder(this)
            .setTitle(if (isFolder) "Tạo thư mục" else "Tạo file text")
            .setView(input)
            .setPositiveButton("OK") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) {
                    try {
                        val fileName = if (isFolder) name else {
                            // Nếu đã có extension .txt thì không thêm nữa
                            if (name.endsWith(".txt", ignoreCase = true)) name else "$name.txt"
                        }
                        val newFile = File(currentPath, fileName)
                        
                        if (isFolder) {
                            if (newFile.exists()) {
                                Toast.makeText(this, "Thư mục đã tồn tại", Toast.LENGTH_SHORT).show()
                            } else if (newFile.mkdir()) {
                                loadFiles(currentPath)
                                Toast.makeText(this, "Tạo thư mục thành công", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this, "Không thể tạo thư mục", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            if (newFile.exists()) {
                                Toast.makeText(this, "File đã tồn tại", Toast.LENGTH_SHORT).show()
                            } else if (newFile.createNewFile()) {
                                loadFiles(currentPath)
                                Toast.makeText(this, "Tạo file thành công", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this, "Không thể tạo file", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Vui lòng nhập tên", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    // Dialog Đổi tên [cite: 5, 8]
    private fun showRenameDialog(file: File) {
        val input = EditText(this)
        input.setText(file.name)
        AlertDialog.Builder(this)
            .setTitle("Đổi tên")
            .setView(input)
            .setPositiveButton("OK") { _, _ ->
                val newName = input.text.toString()
                val newFile = File(file.parent, newName)
                if (file.renameTo(newFile)) {
                    loadFiles(currentPath)
                    Toast.makeText(this, "Đổi tên thành công", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    // Dialog Xóa (Xác nhận AlertDialog) [cite: 5, 8]
    private fun showDeleteDialog(file: File) {
        AlertDialog.Builder(this)
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc muốn xóa ${file.name}?")
            .setPositiveButton("Xóa") { _, _ ->
                file.deleteRecursively() // Xóa cả thư mục con nếu có
                loadFiles(currentPath)
                Toast.makeText(this, "Đã xóa", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    // Logic Copy - Paste [cite: 8]
    private fun pasteFile() {
        val src = fileToCopy ?: return
        
        if (!src.exists()) {
            Toast.makeText(this, "File nguồn không tồn tại", Toast.LENGTH_SHORT).show()
            fileToCopy = null
            invalidateOptionsMenu()
            return
        }
        
        val dst = File(currentPath, src.name)

        AlertDialog.Builder(this)
            .setTitle("Sao chép")
            .setMessage("Sao chép ${src.name} vào ${currentPath.name}?")
            .setPositiveButton("Đồng ý") { _, _ ->
                try {
                    if (dst.exists()) {
                        // Hỏi có muốn ghi đè không
                        AlertDialog.Builder(this)
                            .setTitle("File đã tồn tại")
                            .setMessage("File ${dst.name} đã tồn tại. Bạn có muốn ghi đè?")
                            .setPositiveButton("Ghi đè") { _, _ ->
                                try {
                                    if (dst.delete()) {
                                        src.copyTo(dst, overwrite = true)
                                        loadFiles(currentPath)
                                        fileToCopy = null
                                        invalidateOptionsMenu()
                                        Toast.makeText(this, "Sao chép thành công", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(this, "Không thể xóa file cũ", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(this, "Lỗi sao chép: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .setNegativeButton("Hủy", null)
                            .show()
                    } else {
                        // File chưa tồn tại, copy bình thường
                        try {
                            src.copyTo(dst, overwrite = false)
                            loadFiles(currentPath)
                            fileToCopy = null
                            invalidateOptionsMenu()
                            Toast.makeText(this, "Sao chép thành công", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(this, "Lỗi sao chép: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Lỗi sao chép: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    // Xử lý nút Back để quay lại thư mục cha
    override fun onBackPressed() {
        if (currentPath.absolutePath != Environment.getExternalStorageDirectory().absolutePath) {
            currentPath = currentPath.parentFile ?: Environment.getExternalStorageDirectory()
            loadFiles(currentPath)
        } else {
            super.onBackPressed()
        }
    }
}