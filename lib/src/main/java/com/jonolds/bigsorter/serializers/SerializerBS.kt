package com.jonolds.bigsorter.serializers

import com.fasterxml.jackson.databind.node.ObjectNode
import com.jonolds.bigsorter.*
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVRecord
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.EOFException
import java.io.Serializable
import java.nio.charset.Charset


interface SerializerBS<T> : ReaderFactory<T>, WriterFactory<T> {


	fun makeArray(size: Int): Array<T> =
		TODO("Not yet implemented")

	companion object {

		fun linesUtf8(delimiter: LineDelimiter = LineDelimiter.LINE_FEED): SerializerBS<String> =
			if (delimiter == LineDelimiter.LINE_FEED) LinesSerializer.LINES_UTF8_LF
			else LinesSerializer.LINES_UTF8_CR_LF

		fun lines(charset: Charset): SerializerBS<String> = LinesSerializer(charset, LineDelimiter.LINE_FEED)

		fun lines(charset: Charset, delimiter: LineDelimiter): SerializerBS<String> = LinesSerializer(charset, delimiter)

		fun <T : Serializable?> java(): SerializerBS<T> = JavaSerializer.instance()

		fun fixedSizeRecord(size: Int): SerializerBS<ByteArray> {
			check(size > 0)
			return FixedSizeRecordSerializer(size)
		}

		fun csv(format: CSVFormat, charset: Charset): SerializerBS<CSVRecord> = CsvSerializer(format, charset)

		fun jsonArray(): SerializerBS<ObjectNode?> = JsonArraySerializer.INSTANCE

		fun <T> dataSerializer2(
			reader: (DataInputStream) -> T,
			writer: (DataOutputStream, T) -> Unit
		): SerializerBS<T> = object : DataSerializer<T>() {

			override fun read(dis: DataInputStream): T? = try {
				reader(dis)
			} catch (e: EOFException) {
				null
			} catch (e: Exception) {
				throw RuntimeException(e)
			}

			override fun write(dos: DataOutputStream, value: T?): Unit = try {
				value?.let { writer.invoke(dos, it) } ?: Unit
			} catch (e: Exception) {
				throw RuntimeException(e)
			}
		}
	}
}

abstract class SerializerAbstract<T>(

): SerializerBS<T> {

}


interface StreamSerializer<T>: SerializerBS<T>, StreamReaderFactory<T>, StreamWriterFactory<T>


interface ChannelSerializer<T>: SerializerBS<T>, ChannelReaderFactory<T>, ChannelWriterFactory<T>