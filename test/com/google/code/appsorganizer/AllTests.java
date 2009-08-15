/*
 * Copyright (C) 2009 Apps Organizer
 *
 * This file is part of Apps Organizer
 *
 * Apps Organizer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Apps Organizer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Apps Organizer.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.google.code.appsorganizer;

import junit.framework.Test;
import junit.framework.TestSuite;
import android.test.suitebuilder.TestSuiteBuilder;

/**
 * A test suite containing all tests for ApiDemos.
 * 
 * To run all suites found in this apk: $ adb shell am instrument -w \
 * com.example.android.apis.tests/android.test.InstrumentationTestRunner
 * 
 * To run just this suite from the command line: $ adb shell am instrument -w \
 * -e class com.example.android.apis.AllTests \
 * com.example.android.apis.tests/android.test.InstrumentationTestRunner
 * 
 * To run an individual test case, e.g.
 * {@link com.example.android.apis.os.MorseCodeConverterTest}: $ adb shell am
 * instrument -w \ -e class com.example.android.apis.os.MorseCodeConverterTest \
 * com.example.android.apis.tests/android.test.InstrumentationTestRunner
 * 
 * To run an individual test, e.g.
 * {@link com.example.android.apis.os.MorseCodeConverterTest#testCharacterS()}:
 * $ adb shell am instrument -w \ -e class
 * com.example.android.apis.os.MorseCodeConverterTest#testCharacterS \
 * com.example.android.apis.tests/android.test.InstrumentationTestRunner
 */
public class AllTests extends TestSuite {

	public static Test suite() {
		return new TestSuiteBuilder(AllTests.class).includeAllPackagesUnderHere().build();
	}
}
