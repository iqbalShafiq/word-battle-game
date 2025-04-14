package id.usecase.word_battle.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Manages sound effects for game events
 */
class SoundManager(private val context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val soundPool: SoundPool by lazy {
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(attributes)
            .build()
    }

    // Sound IDs (these would come from your R.raw resources)
    private var submitSoundId: Int = 0
    private var correctSoundId: Int = 0
    private var errorSoundId: Int = 0
    private var roundStartId: Int = 0
    private var roundEndId: Int = 0

    init {
        scope.launch {
            // Load sounds asynchronously
            // Replace these resource IDs with your actual sound files
            /*submitSoundId = soundPool.load(context, R.raw.submit_word, 1)
            correctSoundId = soundPool.load(context, R.raw.correct_word, 1)
            errorSoundId = soundPool.load(context, R.raw.error, 1)
            roundStartId = soundPool.load(context, R.raw.round_start, 1)
            roundEndId = soundPool.load(context, R.raw.round_end, 1)*/
        }
    }

    /**
     * Play submit word sound
     */
    fun playSubmitSound() {
        if (submitSoundId > 0) {
            soundPool.play(submitSoundId, 1f, 1f, 1, 0, 1f)
        }
    }

    /**
     * Play correct word sound
     */
    fun playCorrectSound() {
        if (correctSoundId > 0) {
            soundPool.play(correctSoundId, 1f, 1f, 1, 0, 1f)
        }
    }

    /**
     * Play error sound
     */
    fun playErrorSound() {
        if (errorSoundId > 0) {
            soundPool.play(errorSoundId, 1f, 1f, 1, 0, 1f)
        }
    }

    /**
     * Play round start sound
     */
    fun playRoundStartSound() {
        if (roundStartId > 0) {
            soundPool.play(roundStartId, 1f, 1f, 1, 0, 1f)
        }
    }

    /**
     * Play round end sound
     */
    fun playRoundEndSound() {
        if (roundEndId > 0) {
            soundPool.play(roundEndId, 1f, 1f, 1, 0, 1f)
        }
    }

    /**
     * Release sound pool resources
     */
    fun release() {
        soundPool.release()
    }
}