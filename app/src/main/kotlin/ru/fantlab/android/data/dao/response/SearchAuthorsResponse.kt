package ru.fantlab.android.data.dao.response

import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.JsonParser
import ru.fantlab.android.data.dao.Pageable
import ru.fantlab.android.data.dao.model.SearchAuthor
import ru.fantlab.android.provider.rest.DataManager

data class SearchAuthorsResponse(
		val authors: Pageable<SearchAuthor>
) {
	class Deserializer(private val perPage: Int) : ResponseDeserializable<SearchAuthorsResponse> {

		override fun deserialize(content: String): SearchAuthorsResponse {
			val jsonObject = JsonParser().parse(content).asJsonObject
			val items: ArrayList<SearchAuthor> = arrayListOf()
			val array = jsonObject.getAsJsonArray("matches")
			array.map {
				items.add(DataManager.gson.fromJson(it, SearchAuthor::class.java))
			}
			val totalCount = jsonObject.getAsJsonPrimitive("total_found").asInt
			val lastPage = (totalCount - 1) / perPage + 1
			val authors = Pageable(lastPage, totalCount, items)
			return SearchAuthorsResponse(authors)
		}
	}
}