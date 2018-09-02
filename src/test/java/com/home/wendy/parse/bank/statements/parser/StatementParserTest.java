package com.home.wendy.parse.bank.statements.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class StatementParserTest {

	private static final String METHOD_NAME_PARSE_LINE = "parseLine";

	private static Method methodParseLine;

	private StatementParser sp;

	@BeforeClass
	public static void setUpBeforeClass() throws NoSuchMethodException, SecurityException {
		methodParseLine = StatementParser.class.getDeclaredMethod(METHOD_NAME_PARSE_LINE, String.class);
		methodParseLine.setAccessible(true);
	}

	@Before
	public void setUp() {
		sp = new StatementParser(null);
	}

	@Test
	public void parseLineTypical() throws IllegalAccessException, IllegalArgumentException {

		String line = "70719 POS Purchase - 10/19 Mach ID 000000 Walgreens Swe O 30.00";
		try {
			methodParseLine.invoke(sp, line);
		} catch (InvocationTargetException e) {
			e.getTargetException().printStackTrace();
		}

		assertNotNull(sp.getDate());
		assertEquals("70719", sp.getDate());
		assertNotNull(sp.getDescription());
		assertEquals("POS Purchase - 10/19 Mach ID 000000 Walgreens Swe O", sp.getDescription());
		assertNotNull(sp.getAmount());
		assertEquals("30.00", sp.getAmount());
	}

	@Test
	public void parseLineSecondLineEndsWithDigits() throws IllegalAccessException, IllegalArgumentException {

		String line = "Winston Salem NC 6014 0003291";
		try {
			methodParseLine.invoke(sp, line);
		} catch (InvocationTargetException e) {
			e.getTargetException().printStackTrace();
		}

		assertNull(sp.getDate());
		assertNull(sp.getDescription());
		assertNull(sp.getAmount());
	}

	@Test
	public void parseLineWithDailyEndingBalance() throws IllegalAccessException, IllegalArgumentException {

		String line = "10119 POS Purchase - 10/19 Mach ID 000000 K & G Salvage East Bend 91.35 1779.38";
		try {
			methodParseLine.invoke(sp, line);
		} catch (InvocationTargetException e) {
			e.getTargetException().printStackTrace();
		}

		assertNotNull(sp.getDate());
		assertEquals("10119", sp.getDate());
		assertNotNull(sp.getDescription());
		assertEquals("POS Purchase - 10/19 Mach ID 000000 K & G Salvage East Bend", sp.getDescription());
		assertNotNull(sp.getAmount());
		assertEquals("91.35", sp.getAmount());
	}

	@Test
	public void parseLineWithAmountLessThanTen() throws IllegalAccessException, IllegalArgumentException {

		String line = "107 POS Purchase 10/14 Shell Service Station Winston Sale NC 639";
		try {
			methodParseLine.invoke(sp, line);
		} catch (InvocationTargetException e) {
			e.getTargetException().printStackTrace();
		}

		assertNotNull(sp.getDate());
		assertEquals("107", sp.getDate());
		assertNotNull(sp.getDescription());
		assertEquals("POS Purchase 10/14 Shell Service Station Winston Sale NC", sp.getDescription());
		assertNotNull(sp.getAmount());
		assertEquals("639", sp.getAmount());
	}
}