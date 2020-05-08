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
package com.arkcase.sim.components.misc;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

public class JsHelper {

	public static String[] trimArray(String[] arr) {
		// We don't use streams here b/c in-place modification is more efficient
		if (arr != null) {
			for (int i = 0; i < arr.length; i++) {
				arr[i] = StringUtils.trim(arr[i]);
			}
		}
		return arr;
	}

	private static final DateTimeFormatter TODAYS_FORMAT = DateTimeFormatter.ofPattern("dd LLL yyyy", Locale.US);

	public static String getTodayDate() {
		return JsHelper.getTodayDate(0);
	}

	public static String getTodayDate(int addDays) {
		return JsHelper.TODAYS_FORMAT.format(Instant.now().plus(Duration.ofDays(addDays)));
	}

	public static final int RANDOM_MIN = 1;
	public static final int RANDOM_MAX = 999;
	public static final Random RANDOM = new Random(System.nanoTime());

	public static int getRandomInteger() {
		return JsHelper.getRandomInteger(JsHelper.RANDOM_MIN, JsHelper.RANDOM_MAX);
	}

	public static int getRandomInteger(int min) {
		return JsHelper.getRandomInteger(min, JsHelper.RANDOM_MAX);
	}

	public static int getRandomInteger(int min, int max) {
		return (min + JsHelper.RANDOM.nextInt(max));
	}

	public static final String[] EMPTY_ARRAY = {};

	public static String[] cleanArray(String... arr) {
		return Stream.of(arr) //
			.filter(StringUtils::isNotEmpty) //
			.collect(Collectors.toList()) //
			.toArray(JsHelper.EMPTY_ARRAY) //
		;
	}
}
