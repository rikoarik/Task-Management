package com.taskmanagement.presentation.ui.gallery

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.taskmanagement.data.repository.FileRepository
import com.taskmanagement.databinding.FragmentGalleryBinding
import com.taskmanagement.presentation.adapter.GalleryAdapter
import com.taskmanagement.presentation.viewmodel.GalleryViewModel
import com.taskmanagement.presentation.viewmodel.GalleryViewModelFactory
import java.util.UUID

class GalleryFragment : Fragment() {

    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding!!

    private lateinit var galleryAdapter: GalleryAdapter
    private lateinit var galleryViewModel: GalleryViewModel
    private lateinit var progressDialog: ProgressDialog

    private val selectMediaLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val selectedUri: Uri? = result.data?.data
            selectedUri?.let {
                val fileName = it.lastPathSegment ?: "Unknown file"
                val fileId = UUID.randomUUID().toString()
                progressDialog.show()
                galleryViewModel.uploadFile(fileId, fileName, it)
                Toast.makeText(requireContext(), "File selected: $fileName", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Uploading file...")
        progressDialog.setCancelable(false)

        val fileRepository = FileRepository()
        val factory = GalleryViewModelFactory(fileRepository)
        galleryViewModel = ViewModelProvider(this, factory).get(GalleryViewModel::class.java)

        galleryAdapter = GalleryAdapter { url, name ->
            val fileType = getFileTypeFromName(name)

            when (fileType) {
                "image" -> {
                    displayImage(url)
                }
                "video" -> {
                    playVideo(url)
                }
                else -> {
                    Toast.makeText(requireContext(), "Unsupported file type", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.recyclerViewGallery.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = galleryAdapter
        }

        binding.fabAddFile.setOnClickListener {
            openGallery()
        }

        galleryViewModel.fileUploadResult.observe(viewLifecycleOwner) { fileUrl ->
            progressDialog.dismiss()
            fileUrl?.let {
                Toast.makeText(requireContext(), "File uploaded: $fileUrl", Toast.LENGTH_SHORT).show()
            }
        }

        galleryViewModel.uploadError.observe(viewLifecycleOwner) { errorMessage ->
            progressDialog.dismiss()
            errorMessage?.let {
                Toast.makeText(requireContext(), "Error: $it", Toast.LENGTH_SHORT).show()
            }
        }

        galleryViewModel.loadFiles()
        galleryViewModel.files.observe(viewLifecycleOwner) { files ->
            files?.let { galleryAdapter.setMediaItems(it) }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
        }
        selectMediaLauncher.launch(intent)
    }

    private fun displayImage(url: String) {
        val dialog = ImagePreviewDialogFragment.newInstance(url)
        dialog.show(parentFragmentManager, "ImagePreview")
    }

    private fun playVideo(url: String) {
        val dialog = VideoPreviewDialogFragment.newInstance(url)
        dialog.show(parentFragmentManager, "VideoPreview")
    }
    private fun getFileTypeFromName(fileName: String): String {
        val fileExtension = fileName.substringAfterLast('.', "").lowercase()

        return when (fileExtension) {
            "jpg", "jpeg", "png", "gif", "bmp" -> "image"
            "mp4", "avi", "mov", "mkv" -> "video"
            else -> "unknown"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }
}
