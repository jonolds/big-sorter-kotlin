package com.jonolds.bigsorter

import java.util.function.Supplier

class ConcatReader<T>(
	val suppliers: List<Supplier<out ReaderBS<out T>>>
) {


	fun <T> forEach(action: (T) -> Unit) {

		for (supplier in suppliers) {

			supplier.get().use { reader ->

				try {
					val v = reader.read()
				}
				catch (e: Exception) {
					return@use
				}


			}


		}



	}


}