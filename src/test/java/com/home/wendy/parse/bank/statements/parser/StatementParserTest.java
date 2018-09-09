package com.home.wendy.parse.bank.statements.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class StatementParserTest {

	private static final String METHOD_NAME_FIND_YEAR = "findYear";

	private static Method methodFindYear;

	private StatementParser sp;

	@BeforeClass
	public static void setUpBeforeClass() throws NoSuchMethodException, SecurityException {
		methodFindYear = StatementParser.class.getDeclaredMethod(METHOD_NAME_FIND_YEAR, String.class);
		methodFindYear.setAccessible(true);
	}

	@Before
	public void setUp() {
		sp = new StatementParser(null, null);
	}

	@Test
	public void findYear_11() throws IllegalAccessException, IllegalArgumentException {
		String path = "/testing/statements/Scan11-2011p1-4.txt";
		String result = null;
		try {
			result = (String) methodFindYear.invoke(sp, path);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}

		assertNotNull(result);
		assertEquals("11", result);
	}

	@Test
	public void findYear_13() throws IllegalAccessException, IllegalArgumentException {
		String path = "/testing/statements/Scan11-2013p1-1.txt";
		String result = null;
		try {
			result = (String) methodFindYear.invoke(sp, path);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}

		assertNotNull(result);
		assertEquals("13", result);
	}

}
