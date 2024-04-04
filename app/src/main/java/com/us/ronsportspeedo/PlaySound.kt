package com.us.ronsportspeedo



import android.content.Context
import android.media.MediaPlayer


class SoundPlayer(private val context: Context) {

   private fun playSound(soundResourceId: Int, isLooping: Boolean = false) {
      // val context = LocalContext
       val mediaPlayer = MediaPlayer.create(context, soundResourceId)

       isLooping.also { mediaPlayer?.isLooping = it }

        mediaPlayer?.setOnCompletionListener {
            mediaPlayer.release()
        }

        mediaPlayer?.start()
    }

    fun playSoundForSpeed(speed: Float?) {
        when {
            speed!! > 80.0f -> {
                // Play a high-speed sound
                playSound(R.raw.slow)
            }
            speed > 40.0f -> {
                // Play a medium-speed sound
                playSound(R.raw.kipup)
            }
            speed> 20.0f ->{
                playSound(R.raw.increase)
            }

        }
    }
}
