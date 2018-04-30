package ru.fantlab.android.data.dao.model

import android.os.Parcelable
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Edition(
		val content: List<String>?,
		@SerializedName("copies") val copyCount: Int,
		@SerializedName("correct_level") val correctLevel: Float,
		@SerializedName("cover_type") val coverType: String,
		val creators: Creators,
		val description: String,
		@SerializedName("edition_id") val id: Int,
		@SerializedName("edition_name") val name: String,
		@SerializedName("edition_type") val type: String,
		@SerializedName("edition_type_plus") val additionalTypes: List<String?>,
		val format: String,
		@SerializedName("format_mm") val formatMm: String,
		val image: String,
		@SerializedName("image_preview") val preview: String,
		@SerializedName("images_plus") val additionalImages: AdditionalImages?,
		val isbns: List<String?>,
		@SerializedName("lang") val language: String,
		@SerializedName("lang_code") val languageCode: String,
		val notes: String,
		val pages: Int,
		val series: List<Series?>,
		@SerializedName("type") val typeId: Int,
		val year: Int
) : Parcelable {
	@Parcelize
	data class Creators(
		val authors: List<Author>?,
		val compilers: List<Compiler>?,
		val publishers: List<Publisher>?
	) : Parcelable

	@Parcelize
	data class Author(
			val id: Int,
			@SerializedName("is_opened") val isOpened: Int,
			val name: String,
			val type: String
	) : Parcelable

	@Parcelize
	data class Compiler(
			val id: Int?,
			val name: String,
			val type: String?
	) : Parcelable

	@Parcelize
	data class Publisher(
			val id: Int?,
			val name: String,
			val type: String?
	) : Parcelable

	@Parcelize
	data class AdditionalImages(
			val cover: Cover,
			val plus: List<Image>?,
			val spine: Spine?
	) : Parcelable

	// известный максимум обложек с корешками - 3
	@Parcelize
	data class Cover(
			@SerializedName("1") val cover1: Image,
			@SerializedName("2") val cover2: Image,
			@SerializedName("3") val cover3: Image
	) : Parcelable

	@Parcelize
	data class Spine(
			@SerializedName("1") val spine1: Image,
			@SerializedName("2") val spine2: Image,
			@SerializedName("3") val spine3: Image
	) : Parcelable

	@Parcelize
	data class Image(
			val image: String,
			@SerializedName("image_preview") val preview: String?
	) : Parcelable

	@Parcelize
	data class Series(
			val id: Int,
			@SerializedName("is_opened") val isOpened: Int,
			val name: String,
			val type: String
	) : Parcelable

	class Deserializer : ResponseDeserializable<Edition> {
		override fun deserialize(content: String): Edition {
			return Gson().fromJson(content, Edition::class.java)
		}
	}
}