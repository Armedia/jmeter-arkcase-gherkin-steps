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
package com.arkcase.sim.tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JSON {

	protected static InputStream findResource(String resource, ClassLoader cl) throws IOException {
		if (StringUtils.isEmpty(resource)) {
			throw new IllegalArgumentException("Must provide a non-empty resource name");
		}
		if (cl == null) {
			cl = Thread.currentThread().getContextClassLoader();
		}
		return cl.getResourceAsStream(resource);
	}

	public static <T> T unmarshal(Class<T> klazz, String resource) throws IOException {
		return JSON.unmarshal(klazz, null, resource, null);
	}

	public static <T> T unmarshal(Class<T> klazz, String resource, Charset charset) throws IOException {
		return JSON.unmarshal(klazz, null, resource, charset);
	}

	public static <T> T unmarshal(Class<T> klazz, ClassLoader cl, String resource) throws IOException {
		return JSON.unmarshal(klazz, cl, resource, null);
	}

	public static <T> T unmarshal(Class<T> klazz, ClassLoader cl, String resource, Charset charset) throws IOException {
		final InputStream in = JSON.findResource(resource, cl);
		if (in == null) { return null; }
		try (InputStream i = in) {
			return JSON.unmarshal(klazz, in, charset);
		}
	}

	public static <T> T unmarshal(Class<T> klazz, URL url) throws IOException {
		return JSON.unmarshal(klazz, url, null);
	}

	public static <T> T unmarshal(Class<T> klazz, URL url, Charset charset) throws IOException {
		Objects.requireNonNull(klazz, "Must provide a class to unmarshal");
		Objects.requireNonNull(url, "Must provide a URL to read from");
		try (InputStream in = url.openStream()) {
			return JSON.unmarshal(klazz, in, charset);
		}
	}

	public static <T> T unmarshal(Class<T> klazz, InputStream in) throws IOException {
		return JSON.unmarshal(klazz, in, null);
	}

	public static <T> T unmarshal(Class<T> klazz, InputStream in, Charset charset) throws IOException {
		Objects.requireNonNull(klazz, "Must provide a class to unmarshal");
		Objects.requireNonNull(in, "Must provide an InputStream to read from");
		if (charset == null) {
			charset = Charset.defaultCharset();
		}
		return JSON.unmarshal(klazz, new InputStreamReader(in, charset));
	}

	public static <T> T unmarshal(Class<T> klazz, Reader r) throws IOException {
		Objects.requireNonNull(klazz, "Must provide a class to unmarshal");
		Objects.requireNonNull(r, "Must provide a Reader to read from");
		return new ObjectMapper().readValue(r, klazz);
	}
}