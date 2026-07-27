package com.mingzao.app.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File
import java.util.Random
import kotlin.concurrent.thread

class VoiceRecorder(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var currentPath: String? = null

    @Suppress("DEPRECATION")
    fun start(): Boolean = runCatching {
        val directory = File(context.filesDir, "voice_notes").apply { mkdirs() }
        currentPath = File(directory, "note-${System.currentTimeMillis()}.m4a").absolutePath

        recorder =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                MediaRecorder()
            }

        recorder?.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(96_000)
            setAudioSamplingRate(44_100)
            setOutputFile(currentPath)
            prepare()
            start()
        }
        true
    }.getOrElse {
        release()
        false
    }

    fun stop(): String? {
        val savedPath = currentPath
        return runCatching {
            recorder?.stop()
            savedPath
        }.getOrNull().also {
            release()
        }
    }

    fun release() {
        runCatching { recorder?.release() }
        recorder = null
        currentPath = null
    }
}

class RiverSoundPlayer {
    @Volatile
    private var playing = false
    private var audioTrack: AudioTrack? = null

    fun start() {
        if (playing) return
        playing = true

        thread(name = "mingzao-soft-noise", isDaemon = true) {
            var track: AudioTrack? = null
            try {
                val sampleRate = 44_100
                val minimum = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                )
                require(minimum > 0) { "Unsupported AudioTrack buffer size: $minimum" }
                val bufferSize = maxOf(minimum * 2, 8_192)
                track = AudioTrack(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build(),
                    AudioFormat.Builder()
                        .setSampleRate(sampleRate)
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build(),
                    bufferSize,
                    AudioTrack.MODE_STREAM,
                    AudioManager.AUDIO_SESSION_ID_GENERATE,
                )
                require(track.state == AudioTrack.STATE_INITIALIZED) {
                    "AudioTrack failed to initialize"
                }
                audioTrack = track

                val samples = ShortArray(bufferSize / 2)
                val random = Random()
                var riverLow = 0f
                var riverMid = 0f
                var fadeIn = 0f
                var sampleCursor = 0L
                val birdDuration = (sampleRate * 0.54f).toInt()
                val birdWave = FloatArray(birdDuration)
                var preparedBirdPhase = 0.0
                birdWave.indices.forEach { index ->
                    val t = index.toDouble() / sampleRate
                    val localT = if (t < 0.22) t else t - 0.30
                    if (localT in 0.0..0.18) {
                        val progress = (localT / 0.18).coerceIn(0.0, 1.0)
                        val envelope = kotlin.math.sin(Math.PI * progress)
                        val frequency = 2_250.0 + progress * 1_050.0
                        preparedBirdPhase += Math.PI * 2.0 * frequency / sampleRate
                        birdWave[index] = (
                            kotlin.math.sin(preparedBirdPhase) *
                                envelope *
                                0.15
                            ).toFloat()
                    }
                }
                var birdPosition = -1
                var nextBird = sampleRate.toLong() * (6 + random.nextInt(5))
                track.setVolume(0.68f)
                track.play()

                while (playing) {
                    val slowCurrent = 0.76f + (
                        kotlin.math.sin(
                            sampleCursor * Math.PI * 2.0 / (sampleRate * 6.5),
                        ) * 0.10
                        ).toFloat()
                    samples.indices.forEach { index ->
                        val white = random.nextFloat() * 2f - 1f
                        riverLow = riverLow * 0.995f + white * 0.005f
                        riverMid = riverMid * 0.82f + white * 0.18f
                        val river = (
                            riverMid * 0.72f +
                                white * 0.18f -
                                riverLow * 0.10f
                            ) * slowCurrent

                        if (birdPosition < 0 && sampleCursor >= nextBird) {
                            birdPosition = 0
                            nextBird = sampleCursor + sampleRate.toLong() * (35 + random.nextInt(36))
                        }

                        val bird = if (birdPosition in birdWave.indices) {
                            birdWave[birdPosition++]
                        } else {
                            birdPosition = -1
                            0f
                        }

                        fadeIn = (fadeIn + 0.000025f).coerceAtMost(1f)
                        samples[index] = (
                            (river * 0.42f + bird) *
                                Short.MAX_VALUE *
                                0.72f *
                                fadeIn
                            ).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                        sampleCursor += 1
                    }
                    val written = track.write(samples, 0, samples.size, AudioTrack.WRITE_BLOCKING)
                    if (written < 0) {
                        error("AudioTrack write failed: $written")
                    }
                }
            } catch (error: Throwable) {
                Log.e("MingzaoRiver", "River ambience playback failed", error)
            } finally {
                runCatching { track?.pause() }
                runCatching { track?.flush() }
                runCatching { track?.release() }
                audioTrack = null
                playing = false
            }
        }
    }

    fun stop() {
        playing = false
    }
}
