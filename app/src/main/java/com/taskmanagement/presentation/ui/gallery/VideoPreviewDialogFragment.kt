package com.taskmanagement.presentation.ui.gallery

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.taskmanagement.databinding.FragmentVideoPreviewDialogBinding

class VideoPreviewDialogFragment : DialogFragment() {

    private lateinit var exoPlayer: ExoPlayer
    private var _binding: FragmentVideoPreviewDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVideoPreviewDialogBinding.inflate(inflater, container, false)

        exoPlayer = ExoPlayer.Builder(requireContext()).build()
        binding.playerView.player = exoPlayer

        val videoUrl = arguments?.getString(ARG_URL)
        videoUrl?.let {
            val mediaItem = MediaItem.fromUri(it)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        exoPlayer.release()
        _binding = null
    }

    companion object {
        private const val ARG_URL = "video_url"

        fun newInstance(url: String): VideoPreviewDialogFragment {
            return VideoPreviewDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_URL, url)
                }
            }
        }
    }
}
