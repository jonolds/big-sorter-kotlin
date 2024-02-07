package com.jonolds.bigsorter

import java.io.*
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

internal class LinesSerializer(
	private val charset: Charset,
	private val delimiter: LineDelimiter
) : SerializerBS<String> {


	override fun createReader(inStr: InputStream): ReaderBS<String> {

		return object : ReaderBS<String> {
			var br: BufferedReader = BufferedReader(InputStreamReader(inStr, charset))

			@Throws(IOException::class)
			override fun read(): String? {
				return br.readLine()
			}

			@Throws(IOException::class)
			override fun close() {
				br.close()
			}
		}
	}

	override fun createWriter(out: OutputStream): WriterBS<String> {

		return object : WriterBS<String> {
			var bw: BufferedWriter = BufferedWriter(OutputStreamWriter(out, charset))

			@Throws(IOException::class)
			override fun write(value: String?) {
				value ?: return
				bw.write(value)
				bw.write(delimiter.value())
			}

			@Throws(IOException::class)
			override fun close() {
				bw.close()
			}

			@Throws(IOException::class)
			override fun flush() {
				bw.flush()
			}
		}
	}

	companion object {
		@JvmField
        val LINES_UTF8_LF: SerializerBS<String> = LinesSerializer(StandardCharsets.UTF_8, LineDelimiter.LINE_FEED)
		@JvmField
        val LINES_UTF8_CR_LF: SerializerBS<String> =
			LinesSerializer(StandardCharsets.UTF_8, LineDelimiter.CARRIAGE_RETURN_LINE_FEED)
	}
}
