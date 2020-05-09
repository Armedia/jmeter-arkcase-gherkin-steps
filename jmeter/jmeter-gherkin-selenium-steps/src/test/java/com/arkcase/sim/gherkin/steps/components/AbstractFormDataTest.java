/*-
 * #%L
 * Armedia ArkCase JMeter Gherkin+Selenium Step Implementations
 * %%
 * Copyright (C) 2020 Armedia, LLC
 * %%
 * This file is part of the ArkCase software.
 *
 * If the software was purchased under a paid ArkCase license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 *
 * ArkCase is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ArkCase is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ArkCase. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package com.arkcase.sim.gherkin.steps.components;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AbstractFormDataTest {

	private static final String TEST_FORMS = "testForms.json";

	private InputStream findResource(String resource, ClassLoader cl) {
		return cl.getResourceAsStream(resource);
	}

	@Test
	public void testFindResource() throws Exception {
		Assertions.assertNull(JSON.findResource(UUID.randomUUID().toString(), null));

		final Charset charset = Charset.defaultCharset();
		try (InputStream expected = findResource(AbstractFormDataTest.TEST_FORMS,
			Thread.currentThread().getContextClassLoader())) {
			try (InputStream actual = JSON.findResource(AbstractFormDataTest.TEST_FORMS, null)) {
				Assertions.assertEquals(IOUtils.toString(expected, charset), IOUtils.toString(actual, charset));
			}
		}

		for (int i = 0; i < 10; i++) {
			final String expectedName = String.format("resource-%02d", i);
			final InputStream empty = new InputStream() {
				@Override
				public int read() throws IOException {
					return -1;
				}
			};
			ClassLoader myCl = new ClassLoader(Thread.currentThread().getContextClassLoader()) {
				@Override
				public InputStream getResourceAsStream(String name) {
					Assertions.assertEquals(name, expectedName);
					return empty;
				}
			};

			try (InputStream actual = JSON.findResource(expectedName, myCl)) {
				Assertions.assertSame(empty, actual);
			}
		}
	}

	@Test
	public void testLoadString() throws IOException {
		AbstractFormData.JSON.Container container = JSON.unmarshal(AbstractFormData.JSON.Container.class,
			AbstractFormDataTest.TEST_FORMS);
		Assertions.assertNotNull(container);
	}
}
