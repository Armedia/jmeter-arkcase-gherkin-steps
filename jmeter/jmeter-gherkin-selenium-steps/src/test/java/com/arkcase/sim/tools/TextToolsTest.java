/*******************************************************************************
 * #%L
 * Armedia JMeter Gherkin Plugin
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TextToolsTest {

	private static final Object[] EMPTY_ARR = {};

	@Test
	public void testInterpolate() {
		final int MAX = 100;

		StringBuilder e = new StringBuilder();
		for (int i = 0; i < MAX; i++) {
			if (e.length() > 0) {
				e.append(", ");
			}
			e.append(String.format("%02d=${%d}", i, i));
		}

		List<Object> args = new ArrayList<>(MAX);
		final String pattern = e.toString();
		for (int max = 0; max < 100; max++) {

			// Clear the data
			args.clear();

			e.setLength(0);
			// Generate the arguments and expected output
			for (int i = 0; i < MAX; i++) {
				if (e.length() > 0) {
					e.append(", ");
				}
				if (i < max) {
					Object arg = UUID.randomUUID();
					args.add(arg);
					e.append(String.format("%02d=%s", i, arg));
				} else {
					e.append(String.format("%02d=${%d}", i, i));
				}
			}

			// Substitute and compare
			Assertions.assertEquals(e.toString(),
				TextTools.interpolate(pattern, args.toArray(TextToolsTest.EMPTY_ARR)));

		}
	}

}
