package com.nizam.music_player

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.media.MediaPlayer
import android.media.audiofx.AudioEffect
import android.os.Bundle
import android.os.IBinder
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nizam.music_player.databinding.ActivityPlayerBinding
import kotlin.random.Random

@Suppress("DEPRECATION")
class PlayerActivity : AppCompatActivity(),ServiceConnection,MediaPlayer.OnCompletionListener{

    companion object {
        var isSongPlaying = false
        var musicListPA = ArrayList<SongsData>()
        var songPosition = 0
        var musicService:MusicService? = null
        @SuppressLint("StaticFieldLeak")
        lateinit var binding: ActivityPlayerBinding
        var repeat = false
        var shuffle = false
        var fifteenMinutes = false
        var thirtyMinutes = false
        var sixtyMinutes = false

    }
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //for starting service
        val intent = Intent(this@PlayerActivity,MusicService::class.java)
        bindService(intent,this@PlayerActivity, BIND_AUTO_CREATE)
        startService(intent)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initializeLayout()

        playPauseSong()

        nextSong()

        previousSong()

        shuffleSong()

        onSeekBarChange()

        repeatSong()

        equalizer()

        showBottomDialogTimer()

    }

    private fun equalizer() {
        binding.equalizer.setOnClickListener{
            try{
                val eqIntent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
                eqIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, musicService!!.mediaPlayer!!.audioSessionId)
                eqIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME,baseContext.packageName)
                eqIntent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE,AudioEffect.CONTENT_TYPE_MUSIC)
                startActivityForResult(eqIntent,69)
            } catch (e:Exception) {
                Toast.makeText(this,"Equalizer not supported",Toast.LENGTH_SHORT).show()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 69 || resultCode == RESULT_OK)
            return
    }

    private fun repeatSong() {
        binding.repeatSong.setOnClickListener{
            if(repeat) {
                repeat = false
                binding.repeatSong.setImageResource(R.drawable.repeat_icon)
            } else {
                repeat = true
                binding.repeatSong.setImageResource(R.drawable.repeat_icon_true)
            }
        }
    }

    private fun onSeekBarChange() {
        binding.seekBarPA.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                if(p2) musicService!!.mediaPlayer!!.seekTo(p1)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) = Unit

            override fun onStopTrackingTouch(p0: SeekBar?) = Unit

        })
    }

    private fun previousSong() {
        binding.previousSong.setOnClickListener{
            playPreviousSong()
            musicService!!.createMediaPlayer()
        }
    }

    private fun nextSong() {
        binding.nextSong.setOnClickListener{
            playNextSong()
            musicService!!.createMediaPlayer()
        }
    }

    //Initializes layout and all variable and retrieves the value from intent.
    private fun initializeLayout() {
        if(shuffle) binding.shuffleButton.setImageResource(R.drawable.shuffle_icon_true)
        songPosition = intent.getIntExtra("index",0)

        when(intent.getStringExtra("class")) {
            "MusicAdapter" -> {
                musicListPA.addAll(MainActivity.musicListMA)
            }

            "MainActivity" -> {
                musicListPA.addAll(MainActivity.musicListMA)
                songPosition = intent.getIntExtra("index",0)
            }
        }
    }

    //this function generates an random index and shuffles the song.
    private fun shuffleSong() {
        binding.shuffleButton.setOnClickListener{
            shuffle = if(shuffle) {
                binding.shuffleButton.setImageResource(R.drawable.shuffle_icon)
                false
            } else {
                binding.shuffleButton.setImageResource(R.drawable.shuffle_icon_true)
                true
            }
        }
    }

    //generates Random number other than current song playing.
    private fun randomNumber():Int {
        var temp = Random.nextInt(0, musicListPA.size)
        while (temp == songPosition) {
            temp = Random.nextInt(
                0,
                musicListPA.size
            )
            if(temp != songPosition) {
                break
            }
        }
        return temp
    }

    //this function is used to play the previous song and it responds to previousSongButton.

    //plays or Pauses the song..
    private fun playPauseSong() {
        //Pause Or Play Button OnClickListener
        binding.pausePlayButton.setOnClickListener{
            if(isSongPlaying) {
                pauseMusic()
            } else {
                playMusic()
            }
        }
    }

    //this is called when the user clicks on Back Button.
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    //this function initializes Media-player if it is null and starts playing the song if it is already initialized.


    //setting song name and album image to the song.


    //plays the songs if it is paused.
    private fun playMusic() {
        binding.pausePlayButton.setIconResource(R.drawable.pause_icon)
        musicService!!.showNotification(R.drawable.pause_icon_notification)
        isSongPlaying = true
        musicService!!.mediaPlayer!!.start()
    }

    //pauses the songs if it is playing.
    private fun pauseMusic() {
        binding.pausePlayButton.setIconResource(R.drawable.play_icon)
        musicService!!.showNotification(R.drawable.play_icon_notification)
        isSongPlaying = false
        musicService!!.mediaPlayer!!.pause()
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as MusicService.MyBinder
        musicService = binder.currentService()
        musicService!!.createMediaPlayer()
        musicService!!.mediaPlayer!!.setOnCompletionListener(this)
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        musicService = null
    }

    override fun onCompletion(p0: MediaPlayer?) {
        if(!repeat)
            playNextSong()
        if(!repeat && shuffle) {
            songPosition = randomNumber()
            musicService!!.createMediaPlayer()
        }
        musicService!!.createMediaPlayer()
    }

    private fun showBottomDialogTimer() {
        if(fifteenMinutes || thirtyMinutes || sixtyMinutes)
            binding.timer.setImageResource(R.drawable.timer_icon_true)
        binding.timer.setOnClickListener{
            if(!(fifteenMinutes || thirtyMinutes || sixtyMinutes)) {
                val dialog = BottomSheetDialog(this@PlayerActivity)
                dialog.setContentView(R.layout.bottom_sheet_layout)
                dialog.show()
                dialog.findViewById<LinearLayout>(R.id.fifteenMinutes)?.setOnClickListener {
                    fifteenMinutes = true
                    Thread{
                        Thread.sleep(15 * 60000)
                        if(fifteenMinutes) {
                            exitApplication()
                            fifteenMinutes = false
                        }
                    }.start()
                    binding.timer.setImageResource(R.drawable.timer_icon_true)
                    dialog.dismiss()
                }
                dialog.findViewById<LinearLayout>(R.id.thirtyMinutes)?.setOnClickListener {
                    thirtyMinutes = true
                    Thread{
                        Thread.sleep(30 * 60000)
                        if(thirtyMinutes) {
                            exitApplication()
                            thirtyMinutes = false
                        }
                    }.start()
                    binding.timer.setImageResource(R.drawable.timer_icon_true)
                    dialog.dismiss()
                }
                dialog.findViewById<LinearLayout>(R.id.sixtyMinutes)?.setOnClickListener {
                    sixtyMinutes = true
                    Thread{
                        Thread.sleep(60 * 60000)
                        if(sixtyMinutes) {
                            exitApplication()
                            sixtyMinutes = false
                        }
                    }.start()
                    binding.timer.setImageResource(R.drawable.timer_icon_true)
                    dialog.dismiss()
                }
            } else {
                val dialog = MaterialAlertDialogBuilder(this@PlayerActivity)
                    .setTitle("Cancel Timer")
                    .setMessage("Do you want to cancel the timer?")
                    .setPositiveButton("Yes"){dialog,_ ->
                        fifteenMinutes = false
                        thirtyMinutes = false
                        sixtyMinutes = false
                        dialog.dismiss()
                        binding.timer.setImageResource(R.drawable.timer_icon)
                    }
                    .setNegativeButton("No"){dialog,_ ->
                        dialog.dismiss()
                    }
                dialog.show()
            }
        }
    }

    private fun exitApplication() {
        val intent = Intent(this@PlayerActivity,MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        intent.putExtra("EXIT",true)
        startActivity(intent)
    }
}