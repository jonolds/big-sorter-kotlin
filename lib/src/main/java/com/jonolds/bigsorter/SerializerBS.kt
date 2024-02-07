package com.jonolds.bigsorter

import com.fasterxml.jackson.databind.node.ObjectNode
import com.github.davidmoten.guavamini.Preconditions
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVRecord
import java.io.*
import java.nio.charset.Charset


interface SerializerBS<T> : InputStreamReaderFactory<T>, OutputStreamWriterFactory<T> {
	
	
	@Throws(FileNotFoundException::class)
	fun createReaderFile(file: File): ReaderBS<T> {
		return createReader(BufferedInputStream(FileInputStream(file)))
	}

	companion object {
		
		@JvmOverloads
		@JvmStatic
		fun linesUtf8(delimiter: LineDelimiter = LineDelimiter.LINE_FEED): SerializerBS<String> {
			Preconditions.checkNotNull(delimiter)
			return if (delimiter == LineDelimiter.LINE_FEED) {
				LinesSerializer.LINES_UTF8_LF
			} else {
				LinesSerializer.LINES_UTF8_CR_LF
			}
		}

		@JvmStatic
		fun lines(charset: Charset): SerializerBS<String> {
			Preconditions.checkNotNull(charset)
			return LinesSerializer(charset, LineDelimiter.LINE_FEED)
		}

		@JvmStatic
		fun lines(charset: Charset, delimiter: LineDelimiter): SerializerBS<String> {
			Preconditions.checkNotNull(charset)
			Preconditions.checkNotNull(delimiter)
			return LinesSerializer(charset, delimiter)
		}

		@JvmStatic
		fun <T : Serializable?> java(): SerializerBS<T> {
			return JavaSerializer.instance()
		}

		@JvmStatic
		fun fixedSizeRecord(size: Int): SerializerBS<ByteArray> {
			Preconditions.checkArgument(size > 0)
			return FixedSizeRecordSerializer(size)
		}

		@JvmStatic
		fun csv(format: CSVFormat, charset: Charset): SerializerBS<CSVRecord> {
			Preconditions.checkNotNull(format, "format cannot be null")
			Preconditions.checkNotNull(charset, "charset cannot be null")
			return CsvSerializer(format, charset)
		}

		@JvmStatic
		fun jsonArray(): SerializerBS<ObjectNode?> {
			return JsonArraySerializer.INSTANCE
		}

		@JvmStatic
		fun <T> dataSerializer2(
			reader: FunctionBS<in DataInputStream, out T>,
			writer: BiConsumerBS<in DataOutputStream, in T>
		): SerializerBS<T> {
			return object : DataSerializer<T>() {


				override fun read(dis: DataInputStream): T? {
					return try {
						reader.apply(dis)
					} catch (e: EOFException) {
						null
					} catch (e: Exception) {
						throw RuntimeException(e)
					}
				}

				override fun write(dos: DataOutputStream, value: T?) {
					try {
						value ?: return
						writer.accept(dos, value)
					} catch (e: Exception) {
						throw RuntimeException(e)
					}
				}
			}
		}
	}
}