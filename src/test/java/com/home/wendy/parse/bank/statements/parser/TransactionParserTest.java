package com.home.wendy.parse.bank.statements.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class TransactionParserTest {

	// private static final String METHOD_NAME_PARSE_LINE = "parseLine";

	// private static Method methodParseLine;

	// private TransactionParser sp;

	// @BeforeClass
	// public static void setUpBeforeClass() throws NoSuchMethodException,
	// SecurityException {
	// methodParseLine =
	// StatementParser.class.getDeclaredMethod(METHOD_NAME_PARSE_LINE,
	// String.class);
	// methodParseLine.setAccessible(true);
	// }

	// @Before
	// public void setUp() {
	//// sp = new TransactionParser(null);
	// }

	@Test
	public void parseLineTypical() throws IllegalAccessException, IllegalArgumentException {

		String line = "70719 POS Purchase - 10/19 Mach ID 000000 Walgreens Swe O 30.00";
		TransactionParser tp = new TransactionParser(line);
		tp.parseLine();

		assertNotNull(tp.getDate());
		assertEquals("70719", tp.getDate());
		assertNotNull(tp.getDescription());
		assertEquals("POS Purchase - 10/19 Mach ID 000000 Walgreens Swe O", tp.getDescription());
		assertNotNull(tp.getAmount());
		assertEquals("30.00", tp.getAmount());
	}

	@Test
	public void parseLineSecondLineEndsWithDigits() throws IllegalAccessException, IllegalArgumentException {

		String line = "Winston Salem NC 6014 0003291";
		TransactionParser tp = new TransactionParser(line);
		tp.parseLine();

		assertNull(tp.getDate());
		assertNull(tp.getDescription());
		assertNull(tp.getAmount());
	}

	@Test
	public void parseLineWithDailyEndingBalance() throws IllegalAccessException, IllegalArgumentException {

		String line = "10119 POS Purchase - 10/19 Mach ID 000000 K & G Salvage East Bend 91.35 1779.38";
		TransactionParser tp = new TransactionParser(line);
		tp.parseLine();

		assertNotNull(tp.getDate());
		assertEquals("10119", tp.getDate());
		assertNotNull(tp.getDescription());
		assertEquals("POS Purchase - 10/19 Mach ID 000000 K & G Salvage East Bend", tp.getDescription());
		assertNotNull(tp.getAmount());
		assertEquals("91.35", tp.getAmount());
	}

	@Test
	public void parseLineWithAmountLessThanTen() throws IllegalAccessException, IllegalArgumentException {

		String line = "107 POS Purchase 10/14 Shell Service Station Winston Sale NC 639";
		TransactionParser tp = new TransactionParser(line);
		tp.parseLine();

		assertNotNull(tp.getDate());
		assertEquals("107", tp.getDate());
		assertNotNull(tp.getDescription());
		assertEquals("POS Purchase 10/14 Shell Service Station Winston Sale NC", tp.getDescription());
		assertNotNull(tp.getAmount());
		assertEquals("639", tp.getAmount());
	}
}