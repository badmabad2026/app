package com.example.data

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

object MoshiHelper {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val vocabListType = Types.newParameterizedType(List::class.java, VocabPreset::class.java)
    private val subtitleListType = Types.newParameterizedType(List::class.java, SubtitleLine::class.java)

    private val vocabAdapter = moshi.adapter<List<VocabPreset>>(vocabListType)
    private val subtitleAdapter = moshi.adapter<List<SubtitleLine>>(subtitleListType)

    fun vocabListToJson(list: List<VocabPreset>): String {
        return try {
            vocabAdapter.toJson(list)
        } catch (e: Exception) {
            "[]"
        }
    }

    fun jsonToVocabList(json: String): List<VocabPreset> {
        return try {
            vocabAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun subtitleListToJson(list: List<SubtitleLine>): String {
        return try {
            subtitleAdapter.toJson(list)
        } catch (e: Exception) {
            "[]"
        }
    }

    fun jsonToSubtitleList(json: String): List<SubtitleLine> {
        return try {
            subtitleAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
