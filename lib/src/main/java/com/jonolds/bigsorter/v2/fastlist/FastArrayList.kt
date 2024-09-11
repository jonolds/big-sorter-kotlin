package com.jonolds.bigsorter.v2.fastlist

import java.io.IOException
import java.io.ObjectOutputStream
import java.util.*
import java.util.function.Consumer
import java.util.function.Predicate
import java.util.function.UnaryOperator
import kotlin.math.max

@Suppress("UNCHECKED_CAST")
class FastArrayList<E> private constructor(
	private val clazz: Class<E>
): AbstractFastList2<E>(), MutableList<E>, RandomAccess, Cloneable {


	private fun makeArray(num: Int, initializer: (Int) -> E): Array<E> {
		val result = java.lang.reflect.Array.newInstance(clazz, num) as Array<E>
		for (i in 0 until num)
			result[i] = initializer(i)
		return result
	}


	private fun makeArray(num: Int): Array<E> {
		return java.lang.reflect.Array.newInstance(clazz, num) as Array<E>
	}



	private val EMPTY_ELEMENTDATA = makeArray(0)

	var elementData: Array<E> = EMPTY_ELEMENTDATA

	override var size: Int = 0


	constructor(initialCapacity: Int, clazz: Class<E>): this(clazz) {
		if (initialCapacity > 0) {
			this.elementData = makeArray(initialCapacity)
		} else if (initialCapacity == 0) {
			this.elementData = EMPTY_ELEMENTDATA
		} else {
			throw IllegalArgumentException("Illegal Capacity: $initialCapacity")
		}
	}

	/**
	 * Constructs an empty list with an initial capacity of ten.
	 */


	/**
	 * Constructs a list containing the elements of the specified
	 * collection, in the order they are returned by the collection's
	 * iterator.
	 *
	 * @param c the collection whose elements are to be placed into this list
	 * @throws NullPointerException if the specified collection is null
	 */
	constructor(c: Collection<E>, clazz: Class<E>): this(clazz) {
		val itr = c.iterator()
		elementData = makeArray(c.size) { itr.next() }
		size = elementData.size
		if (elementData.isNotEmpty()) {
			// c.toArray might (incorrectly) not return Object[] (see 6260652)
//			if (elementData.javaClass != Array<E>::class.java)
//				elementData = Arrays.copyOf(elementData, size, Array<Any>::class.java)
		} else {
			// replace with empty array.
			this.elementData = EMPTY_ELEMENTDATA
		}
	}

	/**
	 * Trims the capacity of this <tt>ArrayList</tt> instance to be the
	 * list's current size.  An application can use this operation to minimize
	 * the storage of an <tt>ArrayList</tt> instance.
	 */
	fun trimToSize() {
		modCount++
		if (size < elementData.size) {
			elementData = if (size == 0) EMPTY_ELEMENTDATA
			else elementData.copyOf(size) as Array<E>
		}
	}

	/**
	 * Increases the capacity of this <tt>ArrayList</tt> instance, if
	 * necessary, to ensure that it can hold at least the number of elements
	 * specified by the minimum capacity argument.
	 *
	 * @param   minCapacity   the desired minimum capacity
	 */
	@Suppress("ReplaceArrayEqualityOpWithArraysEquals")
	fun ensureCapacity(minCapacity: Int) {
		// any size if not default element table
		// larger than default for default empty table. It's already supposed to be at default size.
		val minExpand = if (elementData != DEFAULTCAPACITY_EMPTY_ELEMENTDATA) 0
		else DEFAULT_CAPACITY

		if (minCapacity > minExpand)
			ensureExplicitCapacity(minCapacity)
	}

	@PublishedApi
	internal fun ensureCapacityInternal(minCapacity: Int) =
		ensureExplicitCapacity(calculateCapacity(elementData, minCapacity))

	private fun ensureExplicitCapacity(minCapacity: Int) {
		modCount++

		// overflow-conscious code
		if (minCapacity - elementData.size > 0) grow(minCapacity)
	}

	/**
	 * Increases the capacity to ensure that it can hold at least the
	 * number of elements specified by the minimum capacity argument.
	 *
	 * @param minCapacity the desired minimum capacity
	 */
	private fun grow(minCapacity: Int) {
		// overflow-conscious code
		val oldCapacity = elementData.size
		var newCapacity = oldCapacity + (oldCapacity shr 1)
		if (newCapacity - minCapacity < 0) newCapacity = minCapacity
		if (newCapacity - MAX_ARRAY_SIZE > 0) newCapacity = hugeCapacity(minCapacity)
		// minCapacity is usually close to size, so this is a win:
		elementData = elementData.copyOf(newCapacity) as Array<E>
	}

	/**
	 * Returns <tt>true</tt> if this list contains no elements.
	 *
	 * @return <tt>true</tt> if this list contains no elements
	 */
	override fun isEmpty(): Boolean {
		return size == 0
	}

	/**
	 * Returns <tt>true</tt> if this list contains the specified element.
	 * More formally, returns <tt>true</tt> if and only if this list contains
	 * at least one element <tt>e</tt> such that
	 * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>.
	 *
	 * @param element element whose presence in this list is to be tested
	 * @return <tt>true</tt> if this list contains the specified element
	 */
	override fun contains(element: E): Boolean {
		return indexOf(element) >= 0
	}

	/**
	 * Returns the index of the first occurrence of the specified element
	 * in this list, or -1 if this list does not contain the element.
	 * More formally, returns the lowest index <tt>i</tt> such that
	 * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>,
	 * or -1 if there is no such index.
	 */
	override fun indexOf(element: E): Int {
		if (element == null) {
			for (i in 0 until size) if (elementData[i] == null) return i
		} else {
			for (i in 0 until size) if (element == elementData[i]) return i
		}
		return -1
	}

	/**
	 * Returns the index of the last occurrence of the specified element
	 * in this list, or -1 if this list does not contain the element.
	 * More formally, returns the highest index <tt>i</tt> such that
	 * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>,
	 * or -1 if there is no such index.
	 */
	override fun lastIndexOf(element: E): Int {
		if (element == null) {
			for (i in size - 1 downTo 0) if (elementData[i] == null) return i
		} else {
			for (i in size - 1 downTo 0) if (element == elementData[i]) return i
		}
		return -1
	}

	/**
	 * Returns a shallow copy of this <tt>ArrayList</tt> instance.  (The
	 * elements themselves are not copied.)
	 *
	 * @return a clone of this <tt>ArrayList</tt> instance
	 */
	public override fun clone(): Any {
		try {
			val v = FastArrayList(clazz)
			v.elementData = elementData.copyOf(size) as Array<E>
			v.modCount = 0
			return v
		} catch (e: CloneNotSupportedException) {
			// this shouldn't happen, since we are Cloneable
			throw InternalError(e)
		}
	}

	/**
	 * Returns an array containing all of the elements in this list
	 * in proper sequence (from first to last element).
	 *
	 *
	 * The returned array will be "safe" in that no references to it are
	 * maintained by this list.  (In other words, this method must allocate
	 * a new array).  The caller is thus free to modify the returned array.
	 *
	 *
	 * This method acts as bridge between array-based and collection-based
	 * APIs.
	 *
	 * @return an array containing all of the elements in this list in
	 * proper sequence
	 */
	override fun toArray(): Array<E?> {
		return elementData.copyOf(size)
	}

	/**
	 * Returns an array containing all of the elements in this list in proper
	 * sequence (from first to last element); the runtime type of the returned
	 * array is that of the specified array.  If the list fits in the
	 * specified array, it is returned therein.  Otherwise, a new array is
	 * allocated with the runtime type of the specified array and the size of
	 * this list.
	 *
	 *
	 * If the list fits in the specified array with room to spare
	 * (i.e., the array has more elements than the list), the element in
	 * the array immediately following the end of the collection is set to
	 * <tt>null</tt>.  (This is useful in determining the length of the
	 * list *only* if the caller knows that the list does not contain
	 * any null elements.)
	 *
	 * @param a the array into which the elements of the list are to
	 * be stored, if it is big enough; otherwise, a new array of the
	 * same runtime type is allocated for this purpose.
	 * @return an array containing the elements of the list
	 * @throws ArrayStoreException if the runtime type of the specified array
	 * is not a supertype of the runtime type of every element in
	 * this list
	 * @throws NullPointerException if the specified array is null
	 */
	override fun <T> toArray(a: Array<T?>): Array<T?> {
		if (a.size < size) // Make a new array of a's runtime type, but my contents:
			return Arrays.copyOf(elementData, size, a.javaClass) as Array<T?>
		System.arraycopy(elementData, 0, a, 0, size)
		if (a.size > size) a[size] = null
		return a
	}

	// Positional Access Operations
	fun elementData(index: Int): E {
		return elementData[index]
	}

	/**
	 * Returns the element at the specified position in this list.
	 *
	 * @param  index index of the element to return
	 * @return the element at the specified position in this list
	 * @throws IndexOutOfBoundsException {@inheritDoc}
	 */
	override inline operator fun get(index: Int): E {
//		rangeCheck(index)
		return elementData(index)
	}

	/**
	 * Replaces the element at the specified position in this list with
	 * the specified element.
	 *
	 * @param index index of the element to replace
	 * @param element element to be stored at the specified position
	 * @return the element previously at the specified position
	 * @throws IndexOutOfBoundsException {@inheritDoc}
	 */
	override inline operator fun set(index: Int, element: E): E {
//		rangeCheck(index)
		val oldValue = elementData(index)
		elementData[index] = element
		return oldValue
	}

	/**
	 * Appends the specified element to the end of this list.
	 *
	 * @param element element to be appended to this list
	 * @return <tt>true</tt> (as specified by [Collection##add])
	 */
	override inline fun add(element: E): Boolean {
		ensureCapacityInternal(size + 1) // Increments modCount!!
		elementData[size++] = element
		return true
	}

	/**
	 * Inserts the specified element at the specified position in this
	 * list. Shifts the element currently at that position (if any) and
	 * any subsequent elements to the right (adds one to their indices).
	 *
	 * @param index index at which the specified element is to be inserted
	 * @param element element to be inserted
	 * @throws IndexOutOfBoundsException {@inheritDoc}
	 */
	override inline fun add(index: Int, element: E) {
		rangeCheckForAdd(index)

		ensureCapacityInternal(size + 1) // Increments modCount!!
		System.arraycopy(
			elementData, index, elementData, index + 1,
			size - index
		)
		elementData[index] = element
		size++
	}

	/**
	 * Removes the element at the specified position in this list.
	 * Shifts any subsequent elements to the left (subtracts one from their
	 * indices).
	 *
	 * @param index the index of the element to be removed
	 * @return the element that was removed from the list
	 * @throws IndexOutOfBoundsException {@inheritDoc}
	 */
	override fun removeAt(index: Int): E {
		rangeCheck(index)

		modCount++

		val elementData = elementData as Array<E?>
		val oldValue = elementData(index)

		val numMoved = size - index - 1
		if (numMoved > 0)
			System.arraycopy(elementData, index + 1, elementData, index, numMoved)

		elementData[--size] = null // clear to let GC do its work

		return oldValue
	}

	/**
	 * Removes the first occurrence of the specified element from this list,
	 * if it is present.  If the list does not contain the element, it is
	 * unchanged.  More formally, removes the element with the lowest index
	 * <tt>i</tt> such that
	 * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>
	 * (if such an element exists).  Returns <tt>true</tt> if this list
	 * contained the specified element (or equivalently, if this list
	 * changed as a result of the call).
	 *
	 * @param element element to be removed from this list, if present
	 * @return <tt>true</tt> if this list contained the specified element
	 */
	override fun remove(element: E): Boolean {
		if (element == null) {
			for (index in 0 until size)
				if (elementData[index] == null) {
					fastRemove(index)
					return true
				}
		} else {
			for (index in 0 until size) if (element == elementData[index]) {
				fastRemove(index)
				return true
			}
		}
		return false
	}

	/*
     * Private remove method that skips bounds checking and does not
     * return the value removed.
     */
	private fun fastRemove(index: Int) {
		modCount++

		val elementData = elementData as Array<E?>
		val numMoved = size - index - 1
		if (numMoved > 0) System.arraycopy(
			elementData, index + 1, elementData, index,
			numMoved
		)
		elementData[--size] = null // clear to let GC do its work
	}

	/**
	 * Removes all of the elements from this list.  The list will
	 * be empty after this call returns.
	 */
	override fun clear() {
		modCount++

		val elementData = elementData as Array<E?>
		// clear to let GC do its work
		elementData.fill(null)
//		for (i in 0 until size)
//			elementData[i] = null

		size = 0
	}

	/**
	 * Appends all of the elements in the specified collection to the end of
	 * this list, in the order that they are returned by the
	 * specified collection's Iterator.  The behavior of this operation is
	 * undefined if the specified collection is modified while the operation
	 * is in progress.  (This implies that the behavior of this call is
	 * undefined if the specified collection is this list, and this
	 * list is nonempty.)
	 *
	 * @param elements collection containing elements to be added to this list
	 * @return <tt>true</tt> if this list changed as a result of the call
	 * @throws NullPointerException if the specified collection is null
	 */
	override fun addAll(elements: Collection<E>): Boolean {
		val list = elements.toList()
		val a: Array<E> = makeArray(elements.size) { list[it] }
		val numNew = a.size
		ensureCapacityInternal(size + numNew) // Increments modCount
		System.arraycopy(a, 0, elementData, size, numNew)
		size += numNew
		return numNew != 0
	}

	/**
	 * Inserts all of the elements in the specified collection into this
	 * list, starting at the specified position.  Shifts the element
	 * currently at that position (if any) and any subsequent elements to
	 * the right (increases their indices).  The new elements will appear
	 * in the list in the order that they are returned by the
	 * specified collection's iterator.
	 *
	 * @param index index at which to insert the first element from the
	 * specified collection
	 * @param elements collection containing elements to be added to this list
	 * @return <tt>true</tt> if this list changed as a result of the call
	 * @throws IndexOutOfBoundsException {@inheritDoc}
	 * @throws NullPointerException if the specified collection is null
	 */
	override fun addAll(index: Int, elements: Collection<E>): Boolean {
		rangeCheckForAdd(index)

		val list = elements.toList()
		val a: Array<E> = makeArray(elements.size) { list[it] }

		val numNew = a.size
		ensureCapacityInternal(size + numNew) // Increments modCount

		val numMoved = size - index
		if (numMoved > 0) System.arraycopy(
			elementData, index, elementData, index + numNew,
			numMoved
		)

		System.arraycopy(a, 0, elementData, index, numNew)
		size += numNew
		return numNew != 0
	}

	/**
	 * Removes from this list all of the elements whose index is between
	 * `fromIndex`, inclusive, and `toIndex`, exclusive.
	 * Shifts any succeeding elements to the left (reduces their index).
	 * This call shortens the list by `(toIndex - fromIndex)` elements.
	 * (If `toIndex==fromIndex`, this operation has no effect.)
	 *
	 * @throws IndexOutOfBoundsException if `fromIndex` or
	 * `toIndex` is out of range
	 * (`fromIndex < 0 ||
	 * fromIndex >= size() ||
	 * toIndex > size() ||
	 * toIndex < fromIndex`)
	 */
	override fun removeRange(fromIndex: Int, toIndex: Int) {
		modCount++
		val numMoved = size - toIndex
		System.arraycopy(
			elementData, toIndex, elementData, fromIndex,
			numMoved
		)

		val elementData = elementData as Array<E?>
		// clear to let GC do its work
		val newSize = size - (toIndex - fromIndex)
		for (i in newSize until size) {
			elementData[i] = null
		}
		size = newSize
	}

	/**
	 * Checks if the given index is in range.  If not, throws an appropriate
	 * runtime exception.  This method does *not* check if the index is
	 * negative: It is always used immediately prior to an array access,
	 * which throws an ArrayIndexOutOfBoundsException if index is negative.
	 */
	private fun rangeCheck(index: Int) {
		if (index >= size) throw IndexOutOfBoundsException(outOfBoundsMsg(index))
	}

	/**
	 * A version of rangeCheck used by add and addAll.
	 */
	@PublishedApi
	internal fun rangeCheckForAdd(index: Int) {
		if (index > size || index < 0) throw IndexOutOfBoundsException(outOfBoundsMsg(index))
	}


	private fun outOfBoundsMsg(index: Int): String {
		return "Index: $index, Size: $size"
	}


	override fun removeAll(elements: Collection<E>): Boolean {
		Objects.requireNonNull(elements)
		return batchRemove(elements, false)
	}


	override fun retainAll(elements: Collection<E>): Boolean {
		Objects.requireNonNull(elements)
		return batchRemove(elements, true)
	}

	private fun batchRemove(c: Collection<*>, complement: Boolean): Boolean {
		val elementData = this.elementData as Array<E?>
		var r = 0
		var w = 0
		var modified = false
		try {
			while (r < size) {
				if (c.contains(elementData[r]) == complement) elementData[w++] = elementData[r]
				r++
			}
		} finally {
			// Preserve behavioral compatibility with AbstractCollection,
			// even if c.contains() throws.
			if (r != size) {
				System.arraycopy(
					elementData, r,
					elementData, w,
					size - r
				)
				w += size - r
			}
			if (w != size) {
				// clear to let GC do its work
				for (i in w until size) elementData[i] = null
				modCount += size - w
				size = w
				modified = true
			}
		}
		return modified
	}

	/**
	 * Save the state of the <tt>ArrayList</tt> instance to a stream (that
	 * is, serialize it).
	 *
	 * @serialData The length of the array backing the <tt>ArrayList</tt>
	 * instance is emitted (int), followed by all of its elements
	 * (each an <tt>Object</tt>) in the proper order.
	 */
	@Throws(IOException::class)
	private fun writeObject(s: ObjectOutputStream) {
		// Write out element count, and any hidden stuff
		val expectedModCount = modCount
		s.defaultWriteObject()

		// Write out size as capacity for behavioural compatibility with clone()
		s.writeInt(size)

		// Write out all elements in the proper order.
		for (i in 0 until size) {
			s.writeObject(elementData[i])
		}

		if (modCount != expectedModCount) {
			throw ConcurrentModificationException()
		}
	}


	/**
	 * Returns a list iterator over the elements in this list (in proper
	 * sequence), starting at the specified position in the list.
	 * The specified index indicates the first element that would be
	 * returned by an initial call to [next][ListIterator.next].
	 * An initial call to [previous][ListIterator.previous] would
	 * return the element with the specified index minus one.
	 *
	 *
	 * The returned list iterator is [*fail-fast*](#fail-fast).
	 *
	 * @throws IndexOutOfBoundsException {@inheritDoc}
	 */
	override fun listIterator(index: Int): MutableListIterator<E> {
		if (index < 0 || index > size) throw IndexOutOfBoundsException("Index: $index")
		return ListItr(index)
	}

	/**
	 * Returns a list iterator over the elements in this list (in proper
	 * sequence).
	 *
	 *
	 * The returned list iterator is [*fail-fast*](#fail-fast).
	 *
	 * @see .listIterator
	 */
	override fun listIterator(): MutableListIterator<E> {
		return ListItr(0)
	}

	/**
	 * Returns an iterator over the elements in this list in proper sequence.
	 *
	 *
	 * The returned iterator is [*fail-fast*](#fail-fast).
	 *
	 * @return an iterator over the elements in this list in proper sequence
	 */
	override fun iterator(): MutableIterator<E> {
		return Itr()
	}

	/**
	 * An optimized version of AbstractList.Itr
	 */
	private open inner class Itr : MutableIterator<E> {
		var cursor: Int = 0 // index of next element to return
		var lastRet: Int = -1 // index of last element returned; -1 if no such
		var expectedModCount: Int = modCount

		override fun hasNext(): Boolean {
			return cursor != size
		}

		override fun next(): E {
			checkForComodification()
			val i = cursor
			if (i >= size) throw NoSuchElementException()
			val elementData = this@FastArrayList.elementData
			if (i >= elementData.size) throw ConcurrentModificationException()
			cursor = i + 1
			return elementData[i.also { lastRet = it }]
		}

		override fun remove() {
			check(lastRet >= 0)
			checkForComodification()

			try {
				this@FastArrayList.removeAt(lastRet)
				cursor = lastRet
				lastRet = -1
				expectedModCount = modCount
			} catch (ex: IndexOutOfBoundsException) {
				throw ConcurrentModificationException()
			}
		}


		override fun forEachRemaining(consumer: Consumer<in E>) {
			Objects.requireNonNull(consumer)
			val size = this@FastArrayList.size
			var i = cursor
			if (i >= size) {
				return
			}
			val elementData = this@FastArrayList.elementData
			if (i >= elementData.size) {
				throw ConcurrentModificationException()
			}
			while (i != size && modCount == expectedModCount) {
				consumer.accept(elementData[i++])
			}
			// update once at end of iteration to reduce heap write traffic
			cursor = i
			lastRet = i - 1
			checkForComodification()
		}

		fun checkForComodification() {
			if (modCount != expectedModCount) throw ConcurrentModificationException()
		}
	}

	/**
	 * An optimized version of AbstractList.ListItr
	 */
	private inner class ListItr(index: Int) : Itr(), MutableListIterator<E> {
		init {
			cursor = index
		}

		override fun hasPrevious(): Boolean {
			return cursor != 0
		}

		override fun nextIndex(): Int {
			return cursor
		}

		override fun previousIndex(): Int {
			return cursor - 1
		}

		override fun previous(): E {
			checkForComodification()
			val i: Int = cursor - 1
			if (i < 0) throw NoSuchElementException()
			val elementData = this@FastArrayList.elementData
			if (i >= elementData.size) throw ConcurrentModificationException()
			cursor = i
			return elementData[i.also { lastRet = it }]
		}

		override fun set(element: E) {
			check(lastRet >= 0)
			checkForComodification()

			try {
				this@FastArrayList[lastRet] = element
			} catch (ex: IndexOutOfBoundsException) {
				throw ConcurrentModificationException()
			}
		}

		override fun add(element: E) {
			checkForComodification()

			try {
				val i: Int = cursor
				this@FastArrayList.add(i, element)
				cursor = i + 1
				lastRet = -1
				expectedModCount = modCount
			} catch (ex: IndexOutOfBoundsException) {
				throw ConcurrentModificationException()
			}
		}
	}

	/**
	 * Returns a view of the portion of this list between the specified
	 * `fromIndex`, inclusive, and `toIndex`, exclusive.  (If
	 * `fromIndex` and `toIndex` are equal, the returned list is
	 * empty.)  The returned list is backed by this list, so non-structural
	 * changes in the returned list are reflected in this list, and vice-versa.
	 * The returned list supports all of the optional list operations.
	 *
	 *
	 * This method eliminates the need for explicit range operations (of
	 * the sort that commonly exist for arrays).  Any operation that expects
	 * a list can be used as a range operation by passing a subList view
	 * instead of a whole list.  For example, the following idiom
	 * removes a range of elements from a list:
	 * <pre>
	 * list.subList(from, to).clear();
	</pre> *
	 * Similar idioms may be constructed for [.indexOf] and
	 * [.lastIndexOf], and all of the algorithms in the
	 * [Collections] class can be applied to a subList.
	 *
	 *
	 * The semantics of the list returned by this method become undefined if
	 * the backing list (i.e., this list) is *structurally modified* in
	 * any way other than via the returned list.  (Structural modifications are
	 * those that change the size of this list, or otherwise perturb it in such
	 * a fashion that iterations in progress may yield incorrect results.)
	 *
	 * @throws IndexOutOfBoundsException {@inheritDoc}
	 * @throws IllegalArgumentException {@inheritDoc}
	 */
	override fun subList(fromIndex: Int, toIndex: Int): MutableList<E> {
		subListRangeCheck(fromIndex, toIndex, size)
		return SubList(this, 0, fromIndex, toIndex)
	}

	private inner class SubList(
		private val parent: AbstractFastList2<E>,
		offset: Int,
		fromIndex: Int,
		toIndex: Int
	) : AbstractFastList2<E>(), RandomAccess {


		private val offset: Int = offset + fromIndex
		override var size: Int = toIndex - fromIndex
			get() {
				checkForComodification()
				return field
			}

		private val parentOffset: Int = offset

		init {
			this.modCount = this@FastArrayList.modCount
		}

		override fun set(index: Int, element: E): E {
			rangeCheck(index)
			checkForComodification()
			val oldValue = this@FastArrayList.elementData(offset + index)
			elementData[offset + index] = element
			return oldValue
		}

		override fun get(index: Int): E {
			rangeCheck(index)
			checkForComodification()
			return this@FastArrayList.elementData(offset + index)
		}


		override fun add(index: Int, element: E) {
			rangeCheckForAdd(index)
			checkForComodification()
			parent.add(parentOffset + index, element)
			this.modCount = parent.modCount
			this.size++
		}

		override fun removeAt(index: Int): E {
			rangeCheck(index)
			checkForComodification()
			val result = parent.removeAt(parentOffset + index)
			this.modCount = parent.modCount
			this.size--
			return result
		}

		override fun removeRange(fromIndex: Int, toIndex: Int) {
			checkForComodification()
			parent.removeRange(
				parentOffset + fromIndex,
				parentOffset + toIndex
			)
			this.modCount = parent.modCount
			this.size -= toIndex - fromIndex
		}

		override fun addAll(elements: Collection<E>): Boolean {
			return addAll(this.size, elements)
		}

		override fun addAll(index: Int, elements: Collection<E>): Boolean {
			rangeCheckForAdd(index)
			val cSize = elements.size
			if (cSize == 0) return false

			checkForComodification()
			parent.addAll(parentOffset + index, elements)
			this.modCount = parent.modCount
			this.size += cSize
			return true
		}

		override fun iterator(): MutableIterator<E> {
			return listIterator()
		}

		override fun listIterator(index: Int): MutableListIterator<E> {
			checkForComodification()
			rangeCheckForAdd(index)
			val offset = this.offset

			return object : MutableListIterator<E> {
				var cursor: Int = index
				var lastRet: Int = -1
				var expectedModCount: Int = this@FastArrayList.modCount

				override fun hasNext(): Boolean {
					return cursor != this@SubList.size
				}

				override fun next(): E {
					checkForComodification()
					val i = cursor
					if (i >= this@SubList.size) throw NoSuchElementException()
					val elementData: Array<E> = this@FastArrayList.elementData
					if (offset + i >= elementData.size) throw ConcurrentModificationException()
					cursor = i + 1
					return elementData[offset + (i.also { lastRet = it })]
				}

				override fun hasPrevious(): Boolean {
					return cursor != 0
				}

				override fun previous(): E {
					checkForComodification()
					val i = cursor - 1
					if (i < 0) throw NoSuchElementException()
					val elementData: Array<E> = this@FastArrayList.elementData
					if (offset + i >= elementData.size) throw ConcurrentModificationException()
					cursor = i
					return elementData[offset + (i.also { lastRet = it })]
				}

				override fun forEachRemaining(consumer: Consumer<in E>) {
					Objects.requireNonNull(consumer)
					val size = this@SubList.size
					var i = cursor
					if (i >= size) {
						return
					}
					val elementData = this@FastArrayList.elementData
					if (offset + i >= elementData.size) {
						throw ConcurrentModificationException()
					}
					while (i != size && modCount == expectedModCount) {
						consumer.accept(elementData[offset + (i++)])
					}
					// update once at end of iteration to reduce heap write traffic
					cursor = i
					lastRet = cursor
					checkForComodification()
				}

				override fun nextIndex(): Int {
					return cursor
				}

				override fun previousIndex(): Int {
					return cursor - 1
				}

				override fun remove() {
					check(lastRet >= 0)
					checkForComodification()

					try {
						this@SubList.removeAt(lastRet)
						cursor = lastRet
						lastRet = -1
						expectedModCount = this@FastArrayList.modCount
					} catch (ex: IndexOutOfBoundsException) {
						throw ConcurrentModificationException()
					}
				}

				override fun set(element: E) {
					check(lastRet >= 0)
					checkForComodification()

					try {
						this@FastArrayList[offset + lastRet] = element
					} catch (ex: IndexOutOfBoundsException) {
						throw ConcurrentModificationException()
					}
				}

				override fun add(element: E) {
					checkForComodification()

					try {
						val i = cursor
						this@SubList.add(i, element)
						cursor = i + 1
						lastRet = -1
						expectedModCount = this@FastArrayList.modCount
					} catch (ex: IndexOutOfBoundsException) {
						throw ConcurrentModificationException()
					}
				}

				fun checkForComodification() {
					if (expectedModCount != this@FastArrayList.modCount) throw ConcurrentModificationException()
				}
			}
		}

		override fun subList(fromIndex: Int, toIndex: Int): MutableList<E> {
			subListRangeCheck(fromIndex, toIndex, size)
			return SubList(this, offset, fromIndex, toIndex)
		}

		private fun rangeCheck(index: Int) {
			if (index < 0 || index >= this.size) throw IndexOutOfBoundsException(outOfBoundsMsg(index))
		}

		private fun rangeCheckForAdd(index: Int) {
			if (index < 0 || index > this.size) throw IndexOutOfBoundsException(outOfBoundsMsg(index))
		}

		private fun outOfBoundsMsg(index: Int): String {
			return "Index: " + index + ", Size: " + this.size
		}

		private fun checkForComodification() {
			if (this@FastArrayList.modCount != this.modCount) throw ConcurrentModificationException()
		}

		override fun spliterator(): Spliterator<E> {
			TODO("Method Not Implemented")
		}
	}

	override fun forEach(action: Consumer<in E>) {
		Objects.requireNonNull(action)
		val expectedModCount = modCount
		val elementData = elementData
		val size = this.size
		var i = 0
		while (modCount == expectedModCount && i < size) {
			action.accept(elementData[i])
			i++
		}
		if (modCount != expectedModCount) {
			throw ConcurrentModificationException()
		}
	}

	/**
	 * Creates a *[late-binding](Spliterator.html#binding)*
	 * and *fail-fast* [Spliterator] over the elements in this
	 * list.
	 *
	 *
	 * The `Spliterator` reports [Spliterator.SIZED],
	 * [Spliterator.SUBSIZED], and [Spliterator.ORDERED].
	 * Overriding implementations should document the reporting of additional
	 * characteristic values.
	 *
	 * @return a `Spliterator` over the elements in this list
	 * @since 1.8
	 */
	override fun spliterator(): Spliterator<E> {
		return ArrayListSpliterator(this, 0, -1, 0)
//		TODO("Method Not Implemented")
	}



	override fun removeIf(filter: Predicate<in E>): Boolean {
		Objects.requireNonNull(filter)
		// figure out which elements are to be removed
		// any exception thrown from the filter predicate at this stage
		// will leave the collection unmodified
		var removeCount = 0
		val removeSet = BitSet(size)
		val expectedModCount = modCount
		val size = this.size
		var i = 0
		while (modCount == expectedModCount && i < size) {
			val element = elementData[i]
			if (filter.test(element)) {
				removeSet.set(i)
				removeCount++
			}
			i++
		}
		if (modCount != expectedModCount) {
			throw ConcurrentModificationException()
		}

		// shift surviving elements left over the spaces left by removed elements
		val anyToRemove = removeCount > 0
		if (anyToRemove) {
			val newSize = size - removeCount
			i = 0
			var j = 0
			while ((i < size) && (j < newSize)) {
				i = removeSet.nextClearBit(i)
				elementData[j] = elementData[i]
				i++
				j++
			}

			val elementData = elementData as Array<E?>
			for (k in newSize until size) {
				elementData[k] = null // Let gc do its work
			}
			this.size = newSize
			if (modCount != expectedModCount) {
				throw ConcurrentModificationException()
			}
			modCount++
		}

		return anyToRemove
	}

	override fun replaceAll(operator: UnaryOperator<E>) {
		val expectedModCount = modCount
		val size = this.size
		var i = 0
		while (modCount == expectedModCount && i < size) {
			elementData[i] = operator.apply(elementData[i]!!)
			i++
		}
		if (modCount != expectedModCount) {
			throw ConcurrentModificationException()
		}
		modCount++
	}

	override fun sort(c: Comparator<in E>) {
		val expectedModCount = modCount
		Arrays.sort(elementData, 0, size, c)
		if (modCount != expectedModCount)
			throw ConcurrentModificationException()
		modCount++
	}

	fun parallelSort(c: Comparator<in E>): FastArrayList<E> {
		val expectedModCount = modCount
		Arrays.parallelSort(elementData, 0, size, c)
		if (modCount != expectedModCount)
			throw ConcurrentModificationException()
		modCount++
		return this
	}

	private fun calculateCapacity(elementData: Array<E>, minCapacity: Int): Int {
		if (elementData.contentEquals(DEFAULTCAPACITY_EMPTY_ELEMENTDATA)) {
			return max(DEFAULT_CAPACITY.toDouble(), minCapacity.toDouble())
				.toInt()
		}
		return minCapacity
	}

	companion object {

		/**
		 * Default initial capacity.
		 */
		private const val DEFAULT_CAPACITY = 10

		/**
		 * Shared empty array instance used for empty instances.
		 */


		/**
		 * Shared empty array instance used for default sized empty instances. We
		 * distinguish this from EMPTY_ELEMENTDATA to know how much to inflate when
		 * first element is added.
		 */
		private val DEFAULTCAPACITY_EMPTY_ELEMENTDATA = arrayOf<Any?>()



		/**
		 * The maximum size of array to allocate.
		 * Some VMs reserve some header words in an array.
		 * Attempts to allocate larger arrays may result in
		 * OutOfMemoryError: Requested array size exceeds VM limit
		 */
		private const val MAX_ARRAY_SIZE = Int.MAX_VALUE - 8

		private fun hugeCapacity(minCapacity: Int): Int {
			if (minCapacity < 0) // overflow
				throw OutOfMemoryError()
			return if ((minCapacity > MAX_ARRAY_SIZE)) Int.MAX_VALUE else MAX_ARRAY_SIZE
		}

		fun subListRangeCheck(fromIndex: Int, toIndex: Int, size: Int) {
			if (fromIndex < 0) throw IndexOutOfBoundsException("fromIndex = $fromIndex")
			if (toIndex > size) throw IndexOutOfBoundsException("toIndex = $toIndex")
			require(fromIndex <= toIndex) {
				"fromIndex(" + fromIndex +
						") > toIndex(" + toIndex + ")"
			}
		}
	}

}


inline fun <reified T> fastArrayList(c: Collection<T>): FastArrayList<T> = FastArrayList(c, T::class.java)

inline fun <reified T> fastArrayList(initialCapacity: Int): FastArrayList<T> = FastArrayList(initialCapacity, T::class.java)



class ArrayListSpliterator<E>(
	private val list: FastArrayList<E>, // current index, modified on advance/split
	private var index: Int, // -1 until used; then one past last index
	private var fence: Int,
	// initialized when fence set
	private var expectedModCount: Int
) : Spliterator<E> {


	private fun getFence(): Int { // initialize fence to size on first use
		var hi: Int = fence // (a specialized variant appears in method forEach)
		val lst: FastArrayList<E> = list
		if (fence < 0) {
			expectedModCount = lst.modCount
			fence = lst.size
			hi = fence
		}
		return hi
	}

	override fun trySplit(): ArrayListSpliterator<E>? {
		val hi = getFence()
		val lo = index
		val mid = (lo + hi) ushr 1
		return if ((lo >= mid)) null else  // divide range in half unless too small
			ArrayListSpliterator(
				list, lo, mid.also { index = it },
				expectedModCount
			)
	}

	override fun tryAdvance(action: Consumer<in E>): Boolean {
		val hi = getFence()
		val i = index
		if (i < hi) {
			index = i + 1
			val e = list.elementData[i]
			action.accept(e)
			if (list.modCount != expectedModCount) throw ConcurrentModificationException()
			return true
		}
		return false
	}

	override fun forEachRemaining(action: Consumer<in E>) {
		var hi: Int = fence
		val lst: FastArrayList<E> = list
		val a: Array<E> = lst.elementData


		// hoist accesses and checks from loop
		val mc: Int =
			if (fence < 0) {
				hi = lst.size
				lst.modCount
			}
			else expectedModCount


		var i: Int = index
		index = hi
		if (i >= 0 && hi <= a.size) {
			while (i < hi) {
				action.accept(a[i])
				++i
			}
			if (lst.modCount == mc) return
		}

		throw ConcurrentModificationException()
	}

	override fun estimateSize(): Long {
		return (getFence() - index).toLong()
	}

	override fun characteristics(): Int {
		return Spliterator.ORDERED or Spliterator.SIZED or Spliterator.SUBSIZED
	}
}