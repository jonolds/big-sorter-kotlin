//package com.jonolds.bigsorter.internal.lists
//
//import java.util.ConcurrentModificationException
//
//open class SubList2<E>(list: AbstractList2<E>, fromIndex: Int, toIndex: Int) : AbstractList2<E>() {
//	private val l: AbstractList2<E>
//	private val offset: Int
//	override var size: Int = toIndex-fromIndex
//		get() {
//			checkForComodification()
//			return field
//		}
//
//	override fun retainAll(elements: Collection<E>): Boolean {
//		throw NotImplementedError("retainAll not implemented")
//	}
//
//	override fun removeAll(elements: Collection<E>): Boolean {
//		throw NotImplementedError("removeAll not implemented")
//	}
//
//	init {
//		if (fromIndex < 0) throw IndexOutOfBoundsException("fromIndex = $fromIndex")
//		if (toIndex > list.size) throw IndexOutOfBoundsException("toIndex = $toIndex")
//		if (fromIndex > toIndex) throw IllegalArgumentException(
//			"fromIndex(" + fromIndex +
//				") > toIndex(" + toIndex + ")"
//		)
//		l = list
//		offset = fromIndex
//		this.modCount = l.modCount
//	}
//
//	override fun set(index: Int, element: E): E {
//		rangeCheck(index)
//		checkForComodification()
//		return l.set(index + offset, element)
//	}
//
//	override fun get(index: Int): E {
//		rangeCheck(index)
//		checkForComodification()
//		return l[index + offset]
//	}
//
//
//	override fun add(index: Int, element: E) {
//		rangeCheckForAdd(index)
//		checkForComodification()
//		l.add(index + offset, element)
//		this.modCount = l.modCount
//		size++
//	}
//
//
//	override fun remove(element: E): Boolean {
//		throw NotImplementedError("remove not implemented")
//	}
//
//
//
//	override fun removeAt(index: Int): E {
//		rangeCheck(index)
//		checkForComodification()
//		val result = l.removeAt(index + offset)
//		this.modCount = l.modCount
//		size--
//		return result
//	}
//
//	override fun removeRange(fromIndex: Int, toIndex: Int) {
//		checkForComodification()
//		l.removeRange(fromIndex + offset, toIndex + offset)
//		this.modCount = l.modCount
//		size -= (toIndex - fromIndex)
//	}
//
//	override fun addAll(elements: Collection<E>): Boolean {
//		return addAll(size, elements)
//	}
//
//	override fun addAll(index: Int, elements: Collection<E>): Boolean {
//		rangeCheckForAdd(index)
//		val cSize = elements.size
//		if (cSize == 0) return false
//
//		checkForComodification()
//		l.addAll(offset + index, elements)
//		this.modCount = l.modCount
//		size += cSize
//		return true
//	}
//
//	override fun iterator(): MutableIterator<E> {
//		return listIterator()
//	}
//
//	override fun listIterator(index: Int): MutableListIterator<E> {
//		checkForComodification()
//		rangeCheckForAdd(index)
//
//		return object : MutableListIterator<E> {
//			private val i = l.listIterator(index + offset)
//
//			override fun hasNext(): Boolean {
//				return nextIndex() < size
//			}
//
//			override fun next(): E {
//				if (hasNext()) return i.next()
//				else throw NoSuchElementException()
//			}
//
//			override fun hasPrevious(): Boolean {
//				return previousIndex() >= 0
//			}
//
//			override fun previous(): E {
//				if (hasPrevious()) return i.previous()
//				else throw NoSuchElementException()
//			}
//
//			override fun nextIndex(): Int {
//				return i.nextIndex() - offset
//			}
//
//			override fun previousIndex(): Int {
//				return i.previousIndex() - offset
//			}
//
//			override fun remove() {
//				i.remove()
//				this@SubList2.modCount = l.modCount
//				size--
//			}
//
//			override fun set(element: E) {
//				i.set(element)
//			}
//
//			override fun add(element: E) {
//				i.add(element)
//				this@SubList2.modCount = l.modCount
//				size++
//			}
//		}
//	}
//
//	override fun subList(fromIndex: Int, toIndex: Int): MutableList<E> {
//		return SubList2(this, fromIndex, toIndex)
//	}
//
//	private fun rangeCheck(index: Int) {
//		if (index < 0 || index >= size) throw IndexOutOfBoundsException(outOfBoundsMsg(index))
//	}
//
//	private fun rangeCheckForAdd(index: Int) {
//		if (index < 0 || index > size) throw IndexOutOfBoundsException(outOfBoundsMsg(index))
//	}
//
//	private fun outOfBoundsMsg(index: Int): String {
//		return "Index: $index, Size: $size"
//	}
//
//	private fun checkForComodification() {
//		if (this.modCount != l.modCount) throw ConcurrentModificationException()
//	}
//}