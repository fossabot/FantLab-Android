package org.odddev.fantlab.autors.autor

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.requery.Persistable
import io.requery.reactivex.KotlinReactiveEntityStore
import org.odddev.fantlab.core.db.AuthorRecord
import org.odddev.fantlab.core.db.AuthorRecordEntity
import org.odddev.fantlab.core.di.Injector
import org.odddev.fantlab.core.models.Author
import org.odddev.fantlab.core.network.IServerApi
import org.odddev.fantlab.core.sample.deserialize.AuthorPageInfo
import org.odddev.fantlab.core.sample.deserialize.writeToDb
import javax.inject.Inject

class AuthorProvider : IAuthorProvider {

	@Inject
	lateinit var serverApi: IServerApi

	@Inject
	lateinit var requery: KotlinReactiveEntityStore<Persistable>

	init {
		Injector.getAppComponent().inject(this)
	}

	/**
	 * После получения данных алгоритм такой:
	 * 1. Для каждой записи из модели считываем из таблицы уже существующую запись с таким же primary_key
	 * (если уже загрузили, берем из памяти)
	 * 2. Поочередно сравниваем записи и выбираем наиболее полный/актуальный вариант
	 * 3. Записываем актуальный результат в базу
	 * 4. Считываем из базы записи, необходимые для отображения информации на экране
	 */
	override fun getAuthor(id: Int): Observable<Iterable<AuthorRecord>> =
			serverApi.getAuthor(id)
					.flatMap { authorPageInfo -> authorPageInfo.writeToDb(requery) }
					.subscribeOn(Schedulers.io())
					.observeOn(AndroidSchedulers.mainThread())
}
