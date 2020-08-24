/*******************************************************************************
 * #%L
 * Armedia ArkCase
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
 *******************************************************************************/
package com.arkcase.sim.tools;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.apache.commons.io.IOUtils;

public class ScriptTools {

	public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

	public static String loadScript(String scriptName) throws IOException {
		return ScriptTools.loadScript(scriptName, null);
	}

	public static String loadScript(String scriptName, Charset charset) throws IOException {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		URL url = cl.getResource(scriptName);
		if (url == null) { throw new FileNotFoundException(scriptName); }
		return ScriptTools.loadScript(url, charset);
	}

	public static String loadScript(URL url, Charset charset) throws IOException {
		if (charset == null) {
			charset = ScriptTools.DEFAULT_CHARSET;
		}
		try (InputStream in = Objects.requireNonNull(url, "Must provide a URL to fetch").openStream()) {
			return IOUtils.toString(in, charset);
		}
	}
}
