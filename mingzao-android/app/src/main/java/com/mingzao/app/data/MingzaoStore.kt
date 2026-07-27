package com.mingzao.app.data

import android.content.Context
import androidx.core.content.edit
import org.json.JSONArray
import org.json.JSONObject

data class PetProfile(
    val adopted: Boolean = false,
    val type: String = "cat",
    val breed: String = "orange",
    val name: String = "小满",
    val mood: Int = 5,
    val journeyDay: Int = 1,
    val fish: Int = 2,
)

data class NightNote(
    val id: Long = System.currentTimeMillis(),
    val text: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isVoice: Boolean = false,
    val audioPath: String? = null,
    val durationSeconds: Int = 0,
    val completed: Boolean = false,
)

class MingzaoStore(context: Context) {
    private val preferences =
        context.getSharedPreferences("mingzao_local_store", Context.MODE_PRIVATE)

    fun loadProfile(): PetProfile = PetProfile(
        adopted = preferences.getBoolean("adopted", false),
        type = preferences.getString("pet_type", "cat") ?: "cat",
        breed = preferences.getString("pet_breed", "orange") ?: "orange",
        name = preferences.getString("pet_name", "小满") ?: "小满",
        mood = preferences.getInt("mood", 5),
        journeyDay = preferences.getInt("journey_day", 1),
        fish = preferences.getInt("fish", 2),
    )

    fun saveProfile(profile: PetProfile) {
        preferences.edit {
            putBoolean("adopted", profile.adopted)
            putString("pet_type", profile.type)
            putString("pet_breed", profile.breed)
            putString("pet_name", profile.name)
            putInt("mood", profile.mood)
            putInt("journey_day", profile.journeyDay)
            putInt("fish", profile.fish)
        }
    }

    fun loadNotes(): List<NightNote> {
        val raw = preferences.getString("night_notes", "[]") ?: "[]"
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.getJSONObject(index)
                    add(
                        NightNote(
                            id = item.getLong("id"),
                            text = item.getString("text"),
                            createdAt = item.getLong("createdAt"),
                            isVoice = item.optBoolean("isVoice"),
                            audioPath = item.optString("audioPath").ifBlank { null },
                            durationSeconds = item.optInt("durationSeconds"),
                            completed = item.optBoolean("completed"),
                        ),
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    fun saveNotes(notes: List<NightNote>) {
        val array = JSONArray()
        notes.forEach { note ->
            array.put(
                JSONObject()
                    .put("id", note.id)
                    .put("text", note.text)
                    .put("createdAt", note.createdAt)
                    .put("isVoice", note.isVoice)
                    .put("audioPath", note.audioPath.orEmpty())
                    .put("durationSeconds", note.durationSeconds)
                    .put("completed", note.completed),
            )
        }
        preferences.edit { putString("night_notes", array.toString()) }
    }
}
