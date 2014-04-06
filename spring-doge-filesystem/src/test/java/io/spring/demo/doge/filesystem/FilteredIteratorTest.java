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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link FilteredIterator}.
 * 
 * @author Phillip Webb
 */
public class FilteredIteratorTest {

	private static final Collection<Integer> NUMBERS = Arrays.asList(0, 1, 2, 3, 4, 5, 6,
			7, 8, 9);

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void shouldFilter() {
		EvenNumbersIterator evenNumbers = new EvenNumbersIterator(NUMBERS.iterator());
		assertThat(evenNumbers.hasNext(), is(true));
		assertThat(evenNumbers.next(), is(new Integer(0)));
		assertThat(evenNumbers.hasNext(), is(true));
		assertThat(evenNumbers.next(), is(new Integer(2)));
		assertThat(evenNumbers.hasNext(), is(true));
		assertThat(evenNumbers.next(), is(new Integer(4)));
		assertThat(evenNumbers.hasNext(), is(true));
		assertThat(evenNumbers.next(), is(new Integer(6)));
		assertThat(evenNumbers.hasNext(), is(true));
		assertThat(evenNumbers.next(), is(new Integer(8)));
		assertThat(evenNumbers.hasNext(), is(false));
	}

	@Test
	public void shouldThrowNoSuchElementException() throws Exception {
		EvenNumbersIterator evenNumbers = new EvenNumbersIterator(NUMBERS.iterator());
		while (evenNumbers.hasNext()) {
			evenNumbers.next();
		}
		this.thrown.expect(NoSuchElementException.class);
		evenNumbers.next();
	}

	@Test
	public void shouldSupportMultipleCallsToHasNext() throws Exception {
		EvenNumbersIterator evenNumbers = new EvenNumbersIterator(NUMBERS.iterator());
		int i = 0;
		while (evenNumbers.hasNext()) {
			assertTrue(evenNumbers.hasNext());
			assertThat(evenNumbers.next(), is(new Integer(i)));
			i += 2;
		}
		assertFalse(evenNumbers.hasNext());
		assertFalse(evenNumbers.hasNext());
	}

	@Test
	public void shouldRemoveFromUnderlingIterator() throws Exception {
		List<Integer> numbers = new ArrayList<Integer>(NUMBERS);
		EvenNumbersIterator evenNumbers = new EvenNumbersIterator(numbers.iterator());
		while (evenNumbers.hasNext()) {
			evenNumbers.next();
			evenNumbers.remove();
		}
		assertThat(numbers, is(Arrays.asList(1, 3, 5, 7, 9)));
	}

	private static class EvenNumbersIterator extends FilteredIterator<Integer> {

		public EvenNumbersIterator(Iterator<Integer> sourceIterator) {
			super(sourceIterator);
		}

		@Override
		protected boolean isElementFiltered(Integer element) {
			return element.intValue() % 2 != 0;
		};
	}

}
