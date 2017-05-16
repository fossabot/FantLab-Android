package org.odddev.fantlab.core.di

import android.content.Context
import dagger.Module
import dagger.Provides
import io.requery.Persistable
import io.requery.android.sqlite.DatabaseSource
import io.requery.reactivex.ReactiveEntityStore
import io.requery.reactivex.ReactiveSupport
import io.requery.sql.EntityDataStore
import org.odddev.fantlab.award.Models
import org.odddev.fantlab.core.network.INetworkChecker
import org.odddev.fantlab.core.network.NetworkChecker
import javax.inject.Singleton


/**
 * @author kenrube
 * *
 * @since 23.08.16
 */

@Module
class AppModule(internal val context: Context) {

	private var dataStore: ReactiveEntityStore<Persistable>? = null

	@Singleton
	@Provides
	fun getContext(): Context = context

	@Singleton
	@Provides
	fun provideNetworkChecker(): INetworkChecker = NetworkChecker()

	@Singleton
	@Provides
	fun provideRequery(): ReactiveEntityStore<Persistable> {
		if (dataStore == null) {
			val source = DatabaseSource(context, Models.DEFAULT, 1)
			val configuration = source.configuration
			dataStore = ReactiveSupport.toReactiveStore(EntityDataStore<Persistable>(configuration))
		}
		return dataStore!!
	}
}
