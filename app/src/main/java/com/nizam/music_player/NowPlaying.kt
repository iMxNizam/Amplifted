package com.nizam.music_player

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.nizam.music_player.databinding.FragmentNowPlayingBinding

class NowPlaying : Fragment() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var binding: FragmentNowPlayingBinding
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_now_playing, container, false)
        binding = FragmentNowPlayingBinding.bind(view)
        binding.root.visibility = View.INVISIBLE
        return view
    }

    override fun onResume() {
        super.onResume()
        if (PlayerActivity.musicService != null) {
            binding.nowPlayingSongName.isSelected = true
            // on setting it visible
            binding.root.visibility = View.VISIBLE
            if (PlayerActivity.isSongPlaying) {
                binding.nowPlayingPlayPause.setImageResource(R.drawable.pause_icon_notification)
            } else {
                binding.nowPlayingPlayPause.setImageResource(R.drawable.play_icon_notification)
            }

            setNowPlaying(requireContext())

            //onClick Listener for playPause Button on Now Playing
            binding.nowPlayingPlayPause.setOnClickListener {
                if (PlayerActivity.isSongPlaying) {
                    pauseMusic()
                } else {
                    playMusic()
                }
            }

            binding.nowPlayingNext.setOnClickListener {
                playNextSong()
                PlayerActivity.musicService!!.createMediaPlayer(false)
            }

            binding.root.setOnClickListener {
                val intent = Intent(requireContext(), PlayerActivity::class.java)
                if(PlayerActivity.external) {
                    intent.putExtra("class", "External")
                } else {
                    intent.putExtra("class", "Now Playing")
                }
                intent.putExtra("index", PlayerActivity.songPosition)
                ContextCompat.startActivity(requireContext(), intent, null)
            }
        }
    }

    //to pause the music from the Now Playing
    private fun playMusic() {
        PlayerActivity.musicService!!.mediaPlayer!!.start()
        binding.nowPlayingPlayPause.setImageResource(R.drawable.pause_icon_notification)
        PlayerActivity.musicService!!.showNotification(R.drawable.pause_icon_notification,PlaybackStateCompat.STATE_PLAYING)
        PlayerActivity.binding.pausePlayButton.setIconResource(R.drawable.pause_icon)
        PlayerActivity.isSongPlaying = true
    }

    //to play the music from the Now Playing
    private fun pauseMusic() {
        stopped = false
        PlayerActivity.musicService!!.mediaPlayer!!.pause()
        binding.nowPlayingPlayPause.setImageResource(R.drawable.play_icon_notification)
        PlayerActivity.musicService!!.showNotification(R.drawable.play_icon_notification, PlaybackStateCompat.STATE_PAUSED)
        PlayerActivity.binding.pausePlayButton.setIconResource(R.drawable.play_icon)
        PlayerActivity.isSongPlaying = false
    }
}