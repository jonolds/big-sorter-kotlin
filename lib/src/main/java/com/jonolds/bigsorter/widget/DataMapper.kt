package com.jonolds.bigsorter.widget

import com.jonolds.bigsorter.ChannelReaderFactory
import com.jonolds.bigsorter.ReaderBS
import com.jonolds.bigsorter.serializers.SerializerBS
import java.io.File





class DataMapper(
	val config: DataMapperConfig = DataMapperConfig()
) {

	fun log(msg: String, vararg objects: Any?) { if (config.log) println(String.format(msg, *objects)) }

	abstract inner class Phase<T, P> {

		abstract val parent: Phase<P, *>?

		val kids: MutableList<Phase<*, T>> = ArrayList()

		open val result: List<ReaderBS<T>> = ArrayList()


//		companion object {
//
//			fun <T> input(vararg filenames: String, serializerBS: ChannelReaderFactory<T>): InputPhase<T> =
//				InputPhase(filenames.map { File2(it) { serializerBS.createFileReader(File(it), 23) } })
//		}
	}

}




class InputPhase<T>(
	val files: List<File2<T>>,
	override val parent: DataMapper.Phase<T, T>? = null
): DataMapper.Phase<T, T>() {

	override val result: List<ReaderBS<T>> by lazy {
		parent?.result.orEmpty() + files.map { it.supplier() }
	}


	fun <R> map(mapper: (T) -> R): InputPhase<R> =
		InputPhase(files.map { it.map(mapper) })

}


class MapPhase<T, R>(
	val map: (T) -> R,
	override val parent: DataMapper.Phase<T, *>
): DataMapper.Phase<R, T>() {


}



class Package2<T>(
	val serializer: SerializerBS<T>,
	val files: List<File2<T>>
)


class File2<T>(
	val uri: String,
	val supplier: () -> ReaderBS<T>
) {


	fun <R> map(mapper: (T) ->R): File2<R> =
		File2(uri) { supplier().mapper(mapper) }

}






