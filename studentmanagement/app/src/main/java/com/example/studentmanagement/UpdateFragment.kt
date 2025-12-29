package com.example.studentmanagement

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.studentmanagement.databinding.FragmentUpdateBinding

class UpdateFragment : Fragment() {

    private var _binding: FragmentUpdateBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StudentViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUpdateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.selectedStudent.observe(viewLifecycleOwner) { student ->
            student?.let {
                binding.edtName.setText(it.name)
                binding.edtMssv.setText(it.mssv)
            }
        }

        binding.btnUpdate.setOnClickListener {
            val name = binding.edtName.text.toString().trim()
            val mssv = binding.edtMssv.text.toString().trim()
            val position = viewModel.selectedPosition.value ?: -1

            if (name.isNotEmpty() && mssv.isNotEmpty() && position != -1) {
                viewModel.updateStudent(position, Student(mssv, name))
                viewModel.clearSelection()
                Toast.makeText(requireContext(), "Cập nhật thành công", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            } else {
                Toast.makeText(requireContext(), "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnCancel.setOnClickListener {
            viewModel.clearSelection()
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
