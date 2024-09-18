package com.taskmanagement.presentation.ui.gallery

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.taskmanagement.databinding.FragmentImagePreviewDialogBinding

class ImagePreviewDialogFragment : DialogFragment() {

    private var _binding: FragmentImagePreviewDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImagePreviewDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imageUrl = arguments?.getString(ARG_URL)
        imageUrl?.let {
            Glide.with(requireActivity())
                .load(it)
                .into(binding.imageViewPreview)
            Log.d("ImagePreviewDialogFragment", "Image URL: $it")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_URL = "image_url"

        fun newInstance(url: String): ImagePreviewDialogFragment {
            return ImagePreviewDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_URL, url)
                }
            }
        }
    }
}
