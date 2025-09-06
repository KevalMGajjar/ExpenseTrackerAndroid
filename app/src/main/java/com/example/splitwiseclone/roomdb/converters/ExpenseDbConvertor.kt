package com.example.splitwiseclone.roomdb.converters

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.example.splitwiseclone.roomdb.entities.Splits
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import javax.inject.Inject

@ProvidedTypeConverter
class ExpenseDbConvertor @Inject constructor(
    private val moshi: Moshi
) {

    private val splitListType = Types.newParameterizedType(List::class.java, Splits::class.java)

    @TypeConverter
    fun fromSplitList(list: List<Splits>): String {
        val adaptor = moshi.adapter<List<Splits>>(splitListType)
        return adaptor.toJson(list ?: emptyList())
    }

    @TypeConverter
    fun toSplitList(json: String): List<Splits> {
        val adaptor = moshi.adapter<List<Splits>>(splitListType)
        return adaptor.fromJson(json) ?: emptyList()
    }

    private val participantsListType = Types.newParameterizedType(List::class.java, String::class.java)

    @TypeConverter
    fun fromParticipantList(list: List<String>): String {
        val adaptor = moshi.adapter<List<String>>(participantsListType)
        return adaptor.toJson(list ?: emptyList())
    }

    @TypeConverter
    fun toParticipantList(json: String): List<String> {
        val adaptor = moshi.adapter<List<String>>(participantsListType)
        return adaptor.fromJson(json) ?: emptyList()
    }
}