/*
 * Copyright 2012-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring.demo.doge.filesystem;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.springframework.util.Assert;

/**
 * Base for {@link Iterator}s that selectively {@link #isElementFiltered(Object) filters}
 * items from an underlying source.
 * 
 * @author Phillip Webb
 * @param <E> the element type
 */
public abstract class FilteredIterator<E> implements Iterator<E> {

	private final Iterator<E> sourceIterator;

	private E next;

	/**
	 * Create a new {@link FilteredIterator} instance.
	 * @param sourceIterator the source iterator.
	 */
	public FilteredIterator(Iterator<E> sourceIterator) {
		Assert.notNull(sourceIterator, "SourceIterator must not be null");
		this.sourceIterator = sourceIterator;
	}

	@Override
	public boolean hasNext() {
		ensureNextHasBeenFetched();
		return this.next != null;
	}

	@Override
	public E next() {
		try {
			ensureNextHasBeenFetched();
			if (this.next == null) {
				throw new NoSuchElementException();
			}
			return this.next;
		}
		finally {
			this.next = null;
		}
	}

	@Override
	public void remove() {
		this.sourceIterator.remove();
	}

	private void ensureNextHasBeenFetched() {
		while (this.next == null && this.sourceIterator.hasNext()) {
			E candidate = this.sourceIterator.next();
			if (!isElementFiltered(candidate)) {
				this.next = candidate;
			}
		}
	}

	/**
	 * Determines if the element should be filtered.
	 * @param element the element
	 * @return <tt>true</tt> if the element is filtered
	 */
	protected abstract boolean isElementFiltered(E element);
}
