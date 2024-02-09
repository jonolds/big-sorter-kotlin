package com.jonolds.bigsorter.serializers

import com.jonolds.bigsorter.ReaderBS
import com.jonolds.bigsorter.WriterBS
import java.io.*

@Suppress("UNCHECKED_CAST")
internal class JavaSerializer<T : Serializable?> : SerializerBS<T> {

	override fun createReader(inStr: InputStream): ReaderBS<T> {
		val ois = ObjectInputStream(inStr)

		return object : ReaderBS<T> {

			override fun read(): T? = try {
				ois.readObject() as T
			} catch (e: EOFException) {
				null
			} catch (e: ClassNotFoundException) {
				throw RuntimeException(e)
			}

			override fun close() = ois.close()
		}
	}


	override fun createWriter(out: OutputStream): WriterBS<T> {
		val oos = ObjectOutputStream(out)

		return object : WriterBS<T> {
			override fun write(value: T?) = oos.writeObject(value)

			override fun close() = oos.close()

			override fun flush() = oos.flush()
		}
	}

	companion object {
		private val INSTANCE = JavaSerializer<Serializable>()

        fun <T : Serializable?> instance(): JavaSerializer<T> = INSTANCE as JavaSerializer<T>
	}
}
