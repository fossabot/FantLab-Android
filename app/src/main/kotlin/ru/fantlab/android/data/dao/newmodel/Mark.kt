package ru.fantlab.android.data.dao.newmodel

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Mark(
		val mark: Int,
		@SerializedName("mark_date") val markDate: String,
		@SerializedName("mark_date_iso") val markDateIso: String,
		@SerializedName("mark_id") val markId: Int,
		@SerializedName("user_avatar") val userAvatar: String,
		@SerializedName("user_id") val userId: Int,
		@SerializedName("user_name") val userName: String,
		@SerializedName("user_sex") val userSex: String,
		@SerializedName("user_work_classif_flag") val userClassifiedWork: Int,
		@SerializedName("user_work_response_flag") val userWroteResponseForWork: Int,
		@SerializedName("work_author") val workAuthor: String,
		@SerializedName("work_author_orig") val workAuthorOrig: String,
		@SerializedName("work_id") val workId: Int,
		@SerializedName("work_image") val workImage: String?,
		@SerializedName("work_name") val workName: String,
		@SerializedName("work_name_orig") val workNameOrig: String,
		@SerializedName("work_type") val workType: String,
		@SerializedName("work_type_id") val workTypeId: Int,
		@SerializedName("work_year") val workYear: Int
) : Parcelable