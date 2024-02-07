//package com.jonolds.bigsorter.internal.lists
//
//import java.util.RandomAccess
//
//@Suppress("SameParameterValue")
//abstract class AbstractList2<E>: AbstractCollection<E>(), MutableList<E> {
//
//
//
//	/**
//	 * Appends the specified element to the end of this list (optional
//	 * operation).
//	 *
//	 *
//	 * Lists that support this operation may place limitations on what
//	 * elements may be added to this list.  In particular, some
//	 * lists will refuse to add null elements, and others will impose
//	 * restrictions on the type of elements that may be added.  List
//	 * classes should clearly specify in their documentation any restrictions
//	 * on what elements may be added.
//	 *
//	 *
//	 * This implementation calls `add(size(), e)`.
//	 *
//	 *
//	 * Note that this implementation throws an
//	 * `UnsupportedOperationException` unless
//	 * [add(int, E)][.add] is overridden.
//	 *
//	 * @param element element to be appended to this list
//	 * @return `true` (as specified by [java.util.Collection.add])
//	 * @throws UnsupportedOperationException if the `add` operation
//	 * is not supported by this list
//	 * @throws ClassCastException if the class of the specified element
//	 * prevents it from being added to this list
//	 * @throws NullPointerException if the specified element is null and this
//	 * list does not permit null elements
//	 * @throws IllegalArgumentException if some property of this element
//	 * prevents it from being added to this list
//	 */
//	override fun add(element: E): Boolean {
//		add(size, element)
//		return true
//	}
//
//	/**
//	 * {@inheritDoc}
//	 *
//	 * @throws IndexOutOfBoundsException {@inheritDoc}
//	 */
//	abstract override fun get(index: Int): E
//
//	/**
//	 * {@inheritDoc}
//	 *
//	 *
//	 * This implementation always throws an
//	 * `UnsupportedOperationException`.
//	 *
//	 * @throws UnsupportedOperationException {@inheritDoc}
//	 * @throws ClassCastException            {@inheritDoc}
//	 * @throws NullPointerException          {@inheritDoc}
//	 * @throws IllegalArgumentException      {@inheritDoc}
//	 * @throws IndexOutOfBoundsException     {@inheritDoc}
//	 */
//	override fun set(index: Int, element: E): E {
//		throw UnsupportedOperationException()
//	}
//
//	/**
//	 * {@inheritDoc}
//	 *
//	 *
//	 * This implementation always throws an
//	 * `UnsupportedOperationException`.
//	 *
//	 * @throws UnsupportedOperationException {@inheritDoc}
//	 * @throws ClassCastException            {@inheritDoc}
//	 * @throws NullPointerException          {@inheritDoc}
//	 * @throws IllegalArgumentException      {@inheritDoc}
//	 * @throws IndexOutOfBoundsException     {@inheritDoc}
//	 */
//	override fun add(index: Int, element: E) {
//		throw UnsupportedOperationException()
//	}
//
//	/**
//	 * {@inheritDoc}
//	 *
//	 *
//	 * This implementation always throws an
//	 * `UnsupportedOperationException`.
//	 *
//	 * @throws UnsupportedOperationException {@inheritDoc}
//	 * @throws IndexOutOfBoundsException     {@inheritDoc}
//	 */
//	override fun removeAt(index: Int): E {
//		throw UnsupportedOperationException()
//	}
//
//
//	// Search Operations
//	/**
//	 * {@inheritDoc}
//	 *
//	 *
//	 * This implementation first gets a list iterator (with
//	 * `listIterator()`).  Then, it iterates over the list until the
//	 * specified element is found or the end of the list is reached.
//	 *
//	 * @throws ClassCastException   {@inheritDoc}
//	 * @throws NullPointerException {@inheritDoc}
//	 */
//	override fun indexOf(element: E): Int {
//		val it: ListIterator<E?> = listIterator()
//		if (element == null) {
//			while (it.hasNext()) if (it.next() == null) return it.previousIndex()
//		} else {
//			while (it.hasNext()) if (element == it.next()) return it.previousIndex()
//		}
//		return -1
//	}
//
//	/**
//	 * {@inheritDoc}
//	 *
//	 *
//	 * This implementation first gets a list iterator that points to the end
//	 * of the list (with `listIterator(size())`).  Then, it iterates
//	 * backwards over the list until the specified element is found, or the
//	 * beginning of the list is reached.
//	 *
//	 * @throws ClassCastException   {@inheritDoc}
//	 * @throws NullPointerException {@inheritDoc}
//	 */
//	override fun lastIndexOf(element: E): Int {
//		val it: ListIterator<E?> = listIterator(size)
//		if (element == null) {
//			while (it.hasPrevious()) if (it.previous() == null) return it.nextIndex()
//		} else {
//			while (it.hasPrevious()) if (element == it.previous()) return it.nextIndex()
//		}
//		return -1
//	}
//
//
//	// Bulk Operations
//	/**
//	 * Removes all of the elements from this list (optional operation).
//	 * The list will be empty after this call returns.
//	 *
//	 *
//	 * This implementation calls `removeRange(0, size())`.
//	 *
//	 *
//	 * Note that this implementation throws an
//	 * `UnsupportedOperationException` unless `remove(int
//	 * index)` or `removeRange(int fromIndex, int toIndex)` is
//	 * overridden.
//	 *
//	 * @throws UnsupportedOperationException if the `clear` operation
//	 * is not supported by this list
//	 */
//	override fun clear() {
//		removeRange(0, size)
//	}
//
//	/**
//	 * {@inheritDoc}
//	 *
//	 *
//	 * This implementation gets an iterator over the specified collection
//	 * and iterates over it, inserting the elements obtained from the
//	 * iterator into this list at the appropriate position, one at a time,
//	 * using `add(int, E)`.
//	 * Many implementations will override this method for efficiency.
//	 *
//	 *
//	 * Note that this implementation throws an
//	 * `UnsupportedOperationException` unless
//	 * [add(int, E)][.add] is overridden.
//	 *
//	 * @throws UnsupportedOperationException {@inheritDoc}
//	 * @throws ClassCastException            {@inheritDoc}
//	 * @throws NullPointerException          {@inheritDoc}
//	 * @throws IllegalArgumentException      {@inheritDoc}
//	 * @throws IndexOutOfBoundsException     {@inheritDoc}
//	 */
//	override fun addAll(index: Int, elements: Collection<E>): Boolean {
//		var i = index
//		rangeCheckForAdd(i)
//		var modified = false
//		for (e in elements) {
//			add(i++, e)
//			modified = true
//		}
//		return modified
//	}
//
//
//	// Iterators
//	/**
//	 * Returns an iterator over the elements in this list in proper sequence.
//	 *
//	 *
//	 * This implementation returns a straightforward implementation of the
//	 * iterator interface, relying on the backing list's `size()`,
//	 * `get(int)`, and `remove(int)` methods.
//	 *
//	 *
//	 * Note that the iterator returned by this method will throw an
//	 * [UnsupportedOperationException] in response to its
//	 * `remove` method unless the list's `remove(int)` method is
//	 * overridden.
//	 *
//	 *
//	 * This implementation can be made to throw runtime exceptions in the
//	 * face of concurrent modification, as described in the specification
//	 * for the (protected) [.modCount] field.
//	 *
//	 * @return an iterator over the elements in this list in proper sequence
//	 */
//	override fun iterator(): MutableIterator<E> {
//		return Itr()
//	}
//
//	/**
//	 * {@inheritDoc}
//	 *
//	 *
//	 * This implementation returns `listIterator(0)`.
//	 *
//	 * @see .listIterator
//	 */
//	override fun listIterator(): MutableListIterator<E> {
//		return listIterator(0)
//	}
//
//	/**
//	 * {@inheritDoc}
//	 *
//	 *
//	 * This implementation returns a straightforward implementation of the
//	 * `ListIterator` interface that extends the implementation of the
//	 * `Iterator` interface returned by the `iterator()` method.
//	 * The `ListIterator` implementation relies on the backing list's
//	 * `get(int)`, `set(int, E)`, `add(int, E)`
//	 * and `remove(int)` methods.
//	 *
//	 *
//	 * Note that the list iterator returned by this implementation will
//	 * throw an [UnsupportedOperationException] in response to its
//	 * `remove`, `set` and `add` methods unless the
//	 * list's `remove(int)`, `set(int, E)`, and
//	 * `add(int, E)` methods are overridden.
//	 *
//	 *
//	 * This implementation can be made to throw runtime exceptions in the
//	 * face of concurrent modification, as described in the specification for
//	 * the (protected) [.modCount] field.
//	 *
//	 * @throws IndexOutOfBoundsException {@inheritDoc}
//	 */
//	override fun listIterator(index: Int): MutableListIterator<E> {
//		rangeCheckForAdd(index)
//
//		return ListItr(index)
//	}
//
//
//	open inner class Itr : MutableIterator<E> {
//		/**
//		 * Index of element to be returned by subsequent call to next.
//		 */
//		var cursor: Int = 0
//
//		/**
//		 * Index of element returned by most recent call to next or
//		 * previous.  Reset to -1 if this element is deleted by a call
//		 * to remove.
//		 */
//		var lastRet: Int = -1
//
//		/**
//		 * The modCount value that the iterator believes that the backing
//		 * List should have.  If this expectation is violated, the iterator
//		 * has detected concurrent modification.
//		 */
//		var expectedModCount: Int = modCount
//
//		override fun hasNext(): Boolean {
//			return cursor != size
//		}
//
//		override fun next(): E {
//			checkForComodification()
//			try {
//				val i = cursor
//				val next: E = get(i)
//				lastRet = i
//				cursor = i + 1
//				return next
//			} catch (e: IndexOutOfBoundsException) {
//				checkForComodification()
//				throw NoSuchElementException()
//			}
//		}
//
//		override fun remove() {
//			check(lastRet >= 0)
//			checkForComodification()
//
//			try {
//				this@AbstractList2.removeAt(lastRet)
//				if (lastRet < cursor) cursor--
//				lastRet = -1
//				expectedModCount = modCount
//			} catch (e: IndexOutOfBoundsException) {
//				throw ConcurrentModificationException()
//			}
//		}
//
//		fun checkForComodification() {
//			if (modCount != expectedModCount) throw java.util.ConcurrentModificationException()
//		}
//	}
//
//	inner class ListItr(index: Int) : Itr(), MutableListIterator<E> {
//		init {
//			cursor = index
//		}
//
//		override fun hasPrevious(): Boolean {
//			return cursor != 0
//		}
//
//		override fun previous(): E {
//			checkForComodification()
//			try {
//				val i: Int = cursor - 1
//				val previous: E = get(i)
//				cursor = i
//				lastRet = cursor
//				return previous
//			} catch (e: IndexOutOfBoundsException) {
//				checkForComodification()
//				throw NoSuchElementException()
//			}
//		}
//
//		override fun nextIndex(): Int {
//			return cursor
//		}
//
//		override fun previousIndex(): Int {
//			return cursor - 1
//		}
//
//
//		override fun set(element: E) {
//			check(lastRet >= 0)
//			checkForComodification()
//
//			try {
//				this@AbstractList2[lastRet] = element
//				expectedModCount = modCount
//			} catch (ex: IndexOutOfBoundsException) {
//				throw ConcurrentModificationException()
//			}
//		}
//
//		override fun add(element: E) {
//			checkForComodification()
//
//			try {
//				val i: Int = cursor
//				this@AbstractList2.add(i, element)
//				lastRet = -1
//				cursor = i + 1
//				expectedModCount = modCount
//			} catch (ex: IndexOutOfBoundsException) {
//				throw ConcurrentModificationException()
//			}
//		}
//	}
//
//
//	/**
//	 * {@inheritDoc}
//	 *
//	 *
//	 * This implementation returns a list that subclasses
//	 * `AbstractList`.  The subclass stores, in private fields, the
//	 * offset of the subList within the backing list, the size of the subList
//	 * (which can change over its lifetime), and the expected
//	 * `modCount` value of the backing list.  There are two variants
//	 * of the subclass, one of which implements `RandomAccess`.
//	 * If this list implements `RandomAccess` the returned list will
//	 * be an instance of the subclass that implements `RandomAccess`.
//	 *
//	 *
//	 * The subclass's `set(int, E)`, `get(int)`,
//	 * `add(int, E)`, `remove(int)`, `addAll(int,
//	 * Collection)` and `removeRange(int, int)` methods all
//	 * delegate to the corresponding methods on the backing abstract list,
//	 * after bounds-checking the index and adjusting for the offset.  The
//	 * `addAll(Collection c)` method merely returns `addAll(size,
//	 * c)`.
//	 *
//	 *
//	 * The `listIterator(int)` method returns a "wrapper object"
//	 * over a list iterator on the backing list, which is created with the
//	 * corresponding method on the backing list.  The `iterator` method
//	 * merely returns `listIterator()`, and the `size` method
//	 * merely returns the subclass's `size` field.
//	 *
//	 *
//	 * All methods first check to see if the actual `modCount` of
//	 * the backing list is equal to its expected value, and throw a
//	 * `ConcurrentModificationException` if it is not.
//	 *
//	 * @throws IndexOutOfBoundsException if an endpoint index value is out of range
//	 * `(fromIndex < 0 || toIndex > size)`
//	 * @throws IllegalArgumentException if the endpoint indices are out of order
//	 * `(fromIndex > toIndex)`
//	 */
//	override fun subList(fromIndex: Int, toIndex: Int): MutableList<E> {
//		return (if (this is RandomAccess) RandomAccessSubList2(this, fromIndex, toIndex) else SubList2(
//			this, fromIndex, toIndex
//		))
//	}
//
//	// Comparison and hashing
//	/**
//	 * Compares the specified object with this list for equality.  Returns
//	 * `true` if and only if the specified object is also a list, both
//	 * lists have the same size, and all corresponding pairs of elements in
//	 * the two lists are *equal*.  (Two elements `e1` and
//	 * `e2` are *equal* if `(e1==null ? e2==null :
//	 * e1.equals(e2))`.)  In other words, two lists are defined to be
//	 * equal if they contain the same elements in the same order.
//	 *
//	 *
//	 *
//	 * This implementation first checks if the specified object is this
//	 * list. If so, it returns `true`; if not, it checks if the
//	 * specified object is a list. If not, it returns `false`; if so,
//	 * it iterates over both lists, comparing corresponding pairs of elements.
//	 * If any comparison returns `false`, this method returns
//	 * `false`.  If either iterator runs out of elements before the
//	 * other it returns `false` (as the lists are of unequal length);
//	 * otherwise it returns `true` when the iterations complete.
//	 *
//	 * @param other the object to be compared for equality with this list
//	 * @return `true` if the specified object is equal to this list
//	 */
//	override fun equals(other: Any?): Boolean {
//		if (other === this) return true
//		if (other !is List<*>) return false
//
//		val e1: ListIterator<E> = listIterator()
//		val e2 = other.listIterator()
//		while (e1.hasNext() && e2.hasNext()) {
//			val o1 = e1.next()
//			val o2 = e2.next()!!
//			if (o1 != o2) return false
//		}
//		return !(e1.hasNext() || e2.hasNext())
//	}
//
//	/**
//	 * Returns the hash code value for this list.
//	 *
//	 *
//	 * This implementation uses exactly the code that is used to define the
//	 * list hash function in the documentation for the [List.hashCode]
//	 * method.
//	 *
//	 * @return the hash code value for this list
//	 */
//	override fun hashCode(): Int {
//		var hashCode = 1
//		for (e in this) hashCode = 31 * hashCode + (e?.hashCode() ?: 0)
//		return hashCode
//	}
//
//	/**
//	 * Removes from this list all of the elements whose index is between
//	 * `fromIndex`, inclusive, and `toIndex`, exclusive.
//	 * Shifts any succeeding elements to the left (reduces their index).
//	 * This call shortens the list by `(toIndex - fromIndex)` elements.
//	 * (If `toIndex==fromIndex`, this operation has no effect.)
//	 *
//	 *
//	 * This method is called by the `clear` operation on this list
//	 * and its subLists.  Overriding this method to take advantage of
//	 * the internals of the list implementation can *substantially*
//	 * improve the performance of the `clear` operation on this list
//	 * and its subLists.
//	 *
//	 *
//	 * This implementation gets a list iterator positioned before
//	 * `fromIndex`, and repeatedly calls `ListIterator.next`
//	 * followed by `ListIterator.remove` until the entire range has
//	 * been removed.  **Note: if `ListIterator.remove` requires linear
//	 * time, this implementation requires quadratic time.**
//	 *
//	 * @param fromIndex index of first element to be removed
//	 * @param toIndex index after last element to be removed
//	 */
//	open fun removeRange(fromIndex: Int, toIndex: Int) {
//		val it = listIterator(fromIndex)
//		var i = 0
//		val n = toIndex - fromIndex
//		while (i < n) {
//			it.next()
//			it.remove()
//			i++
//		}
//	}
//
//	/**
//	 * The number of times this list has been *structurally modified*.
//	 * Structural modifications are those that change the size of the
//	 * list, or otherwise perturb it in such a fashion that iterations in
//	 * progress may yield incorrect results.
//	 *
//	 *
//	 * This field is used by the iterator and list iterator implementation
//	 * returned by the `iterator` and `listIterator` methods.
//	 * If the value of this field changes unexpectedly, the iterator (or list
//	 * iterator) will throw a `ConcurrentModificationException` in
//	 * response to the `next`, `remove`, `previous`,
//	 * `set` or `add` operations.  This provides
//	 * *fail-fast* behavior, rather than non-deterministic behavior in
//	 * the face of concurrent modification during iteration.
//	 *
//	 *
//	 * **Use of this field by subclasses is optional.** If a subclass
//	 * wishes to provide fail-fast iterators (and list iterators), then it
//	 * merely has to increment this field in its `add(int, E)` and
//	 * `remove(int)` methods (and any other methods that it overrides
//	 * that result in structural modifications to the list).  A single call to
//	 * `add(int, E)` or `remove(int)` must add no more than
//	 * one to this field, or the iterators (and list iterators) will throw
//	 * bogus `ConcurrentModificationExceptions`.  If an implementation
//	 * does not wish to provide fail-fast iterators, this field may be
//	 * ignored.
//	 */
//	@Transient
//	var modCount: Int = 0
//
//	private fun rangeCheckForAdd(index: Int) {
//		if (index < 0 || index > size) throw java.lang.IndexOutOfBoundsException(outOfBoundsMsg(index))
//	}
//
//	private fun outOfBoundsMsg(index: Int): String {
//		return "Index: $index, Size: $size"
//	}
//
//}