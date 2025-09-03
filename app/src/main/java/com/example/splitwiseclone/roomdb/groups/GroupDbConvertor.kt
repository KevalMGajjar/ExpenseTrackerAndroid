package com.example.splitwiseclone.roomdb.groups

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import javax.inject.Inject

@ProvidedTypeConverter
class GroupDbConvertor @Inject constructor(
    private val moshi: Moshi
) {


    private val memberListType = Types.newParameterizedType(List::class.java, Member::class.java)

    @TypeConverter
    fun fromMemberList(list: List<Member>): String {
        val adaptor = moshi.adapter<List<Member>>(memberListType)
        return adaptor.toJson(list ?: emptyList())
    }

    @TypeConverter
    fun toMemberList(json: String): List<Member> {
        val adaptor = moshi.adapter<List<Member>>(memberListType)
        return adaptor.fromJson(json) ?: emptyList()
    }
}