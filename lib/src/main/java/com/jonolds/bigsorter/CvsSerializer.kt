package com.jonolds.bigsorter

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.apache.commons.csv.CSVRecord
import java.io.*
import java.nio.charset.Charset
import java.util.function.Function


internal class CsvSerializer(private val format: CSVFormat, private val charset: Charset) : SerializerBS<CSVRecord> {


	override fun createReader(inStr: InputStream): ReaderBS<CSVRecord> {

		return object : ReaderBS<CSVRecord> {
			var it: Iterator<CSVRecord>? = null
			var isr: InputStreamReader? = null

			@Throws(IOException::class)
			override fun read(): CSVRecord? {
				if (it == null) {
					isr = InputStreamReader(inStr, charset)
					it = format.parse(isr).iterator()
				}
				return if (it!!.hasNext()) {
					it!!.next()
				} else {
					null
				}
			}

			@Throws(IOException::class)
			override fun close() {
				if (isr != null) {
					isr!!.close()
				}
			}
		}
	}

	override fun createWriter(out: OutputStream): WriterBS<CSVRecord> {
		return object : WriterBS<CSVRecord> {

			var printer: CSVPrinter? = null
			private var ps: PrintStream? = null


			@Throws(IOException::class)
			override fun write(value: CSVRecord?) {
				value ?: return
				if (printer == null) {
					ps = PrintStream(out, false, charset)
					printer = format.print(ps)
					// print header line
					val h = value.parser.headerNames
					if (h.isNotEmpty()) {
						printer!!.printRecord(h)
					}
				}
				printer!!.printRecord(value)
			}


			override fun <S> map(mapper: Function<in S?, out CSVRecord?>): WriterBS<S> {
				val tWriter: WriterBS<CSVRecord> = this

				return object : WriterBS<S> {
					@Throws(IOException::class)
					override fun close() {
						tWriter.close()
					}

					@Throws(IOException::class)
					override fun write(value: S?) {
						tWriter.write(mapper.apply(value))
					}

					@Throws(IOException::class)
					override fun flush() {
						tWriter.flush()
					}
				}
			}

			@Throws(IOException::class)
			override fun close() {
				flush()
			}

			override fun flush() {
				if (ps != null) {
					ps!!.flush()
				}
			}
		}
	}
}