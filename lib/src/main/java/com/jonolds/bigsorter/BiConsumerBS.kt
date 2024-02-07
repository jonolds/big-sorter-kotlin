package com.jonolds.bigsorter

fun interface BiConsumerBS<S, T> {
	@Throws(Exception::class)
	fun accept(s: S, t: T)
}
