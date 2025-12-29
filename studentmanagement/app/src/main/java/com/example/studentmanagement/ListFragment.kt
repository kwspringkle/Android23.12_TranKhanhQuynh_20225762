package com.example.studentmanagement

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studentmanagement.databinding.FragmentListBinding

class ListFragment : Fragment() {

    // View Binding
    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!

    // Lấy ViewModel được chia sẻ từ Activity
    private val viewModel: StudentViewModel by activityViewModels()

    private lateinit var adapter: StudentAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        adapter = StudentAdapter(
            studentList = emptyList(),
            onStudentClick = { student, position ->
                // Khi click vào sinh viên -> chọn và chuyển sang màn hình Update
                viewModel.selectStudent(student, position)
                findNavController().navigate(R.id.action_listFragment_to_updateFragment)
            },
            onDeleteClick = { position ->
                // Khi click nút xóa
                viewModel.deleteStudent(position)
            }
        )
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupObservers() {
        // Lắng nghe thay đổi từ ViewModel
        viewModel.studentList.observe(viewLifecycleOwner) { students ->
            adapter.updateList(students)
        }
    }

    private fun setupClickListeners() {
        binding.btnAdd.setOnClickListener {
            findNavController().navigate(R.id.action_listFragment_to_addFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
