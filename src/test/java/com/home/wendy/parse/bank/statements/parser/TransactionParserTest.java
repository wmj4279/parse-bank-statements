package com.home.wendy.parse.bank.statements.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.BeforeClass;
import org.junit.Test;

public class TransactionParserTest {

	private static final String METHOD_NAME_REPAIR_DATE = "repairDate";
	private static final String DEFAULT_DATE = "07/18";

	private static Method methodRepairDate;

	private TransactionParser sp;

	@BeforeClass
	public static void setUpBeforeClass() throws NoSuchMethodException, SecurityException {
		methodRepairDate = TransactionParser.class.getDeclaredMethod(METHOD_NAME_REPAIR_DATE, String.class,
				String.class);
		methodRepairDate.setAccessible(true);
	}

	// @Before
	// public void setUp() {
	//// sp = new TransactionParser(null);
	// }

	@Test
	public void parseLineTypical() {

		String line = "70719 POS Purchase - 10/19 Mach ID 000000 Walgreens Swe O 30.00";
		TransactionParser tp = new TransactionParser(line, DEFAULT_DATE);
		String expectedResult = "\"" + DEFAULT_DATE
				+ "\",\"POS Purchase - 10/19 Mach ID 000000 Walgreens Swe O\",\"30.00\"";
		String result = tp.parseLine();

		assertNotNull(result);
		assertEquals(expectedResult, result);
		assertNotNull(tp.getDate());
		assertEquals(DEFAULT_DATE, tp.getDate());
		assertNotNull(tp.getDescription());
		assertEquals("POS Purchase - 10/19 Mach ID 000000 Walgreens Swe O", tp.getDescription());
		assertNotNull(tp.getAmount());
		assertEquals("30.00", tp.getAmount());
	}

	@Test
	public void parseLineSecondLineEndsWithDigits() {

		String line = "Winston Salem NC 6014 0003291";
		TransactionParser tp = new TransactionParser(line, DEFAULT_DATE);
		String result = tp.parseLine();

		assertNull(result);
		assertNull(tp.getDate());
		assertNull(tp.getDescription());
		assertNull(tp.getAmount());
	}

	@Test
	public void parseLineWithDailyEndingBalance() {

		String line = "10119 POS Purchase - 10/19 Mach ID 000000 K & G Salvage East Bend 91.35 1779.38";
		String expectedResult = "\"" + DEFAULT_DATE
				+ "\",\"POS Purchase - 10/19 Mach ID 000000 K & G Salvage East Bend\",\"91.35\"";

		TransactionParser tp = new TransactionParser(line, DEFAULT_DATE);
		String result = tp.parseLine();

		assertNotNull(result);
		assertEquals(expectedResult, result);
		assertNotNull(tp.getDate());
		assertEquals(DEFAULT_DATE, tp.getDate());
		assertNotNull(tp.getDescription());
		assertEquals("POS Purchase - 10/19 Mach ID 000000 K & G Salvage East Bend", tp.getDescription());
		assertNotNull(tp.getAmount());
		assertEquals("91.35", tp.getAmount());
	}

	@Test
	public void parseLineWithAmountLessThanTen() {

		String line = "107 POS Purchase 10/14 Shell Service Station Winston Sale NC 639";
		String expectedResult = "\"" + DEFAULT_DATE
				+ "\",\"POS Purchase 10/14 Shell Service Station Winston Sale NC\",\"639\"";
		TransactionParser tp = new TransactionParser(line, DEFAULT_DATE);
		String result = tp.parseLine();

		assertNotNull(result);
		assertEquals(expectedResult, result);
		assertNotNull(tp.getDate());
		assertEquals(DEFAULT_DATE, tp.getDate());
		assertNotNull(tp.getDescription());
		assertEquals("POS Purchase 10/14 Shell Service Station Winston Sale NC", tp.getDescription());
		assertNotNull(tp.getAmount());
		assertEquals("639", tp.getAmount());
	}

	@Test
	public void repairDate_validDate() throws IllegalAccessException, IllegalArgumentException {
		TransactionParser tp = new TransactionParser(null, null);
		String testDate = "07/14";
		try {
			String result = (String) methodRepairDate.invoke(tp, testDate, DEFAULT_DATE);
			assertNotNull(result);
			assertEquals(testDate, result);
		} catch (InvocationTargetException e) {
			e.getTargetException().printStackTrace();
			fail("Unexpected Exception");
		}
	}

	@Test
	public void repairDate_validFormatWithLeadingSevenOnMonthAndDay()
			throws IllegalAccessException, IllegalArgumentException {
		TransactionParser tp = new TransactionParser(null, null);

		// In this case, the date was actually 10/13. For whatever reason, the ones were
		// read as sevens
		String testDate = "70/73";
		try {
			String result = (String) methodRepairDate.invoke(tp, testDate, DEFAULT_DATE);
			assertNotNull(result);
			assertEquals("10/13", result);
		} catch (InvocationTargetException e) {
			e.getTargetException().printStackTrace();
			fail("Unexpected Exception");
		}
	}
	
	@Test
	public void repairDate_validFormatWithLeadingSevenOnMonth()
			throws IllegalAccessException, IllegalArgumentException {
		TransactionParser tp = new TransactionParser(null, null);

		// In this case, the date was actually 10/13. For whatever reason, the ones were
		// read as sevens
		String testDate = "70/23";
		try {
			String result = (String) methodRepairDate.invoke(tp, testDate, DEFAULT_DATE);
			assertNotNull(result);
			assertEquals("10/23", result);
		} catch (InvocationTargetException e) {
			e.getTargetException().printStackTrace();
			fail("Unexpected Exception");
		}
	}

	@Test
	public void repairDate_validFormatWithLeadingSevenOnDay() throws IllegalAccessException, IllegalArgumentException {
		TransactionParser tp = new TransactionParser(null, null);

		// In this case, the date was actually 10/13. For whatever reason, the ones were
		// read as sevens
		String testDate = "10/73";
		try {
			String result = (String) methodRepairDate.invoke(tp, testDate, DEFAULT_DATE);
			assertNotNull(result);
			assertEquals("10/13", result);
		} catch (InvocationTargetException e) {
			e.getTargetException().printStackTrace();
			fail("Unexpected Exception");
		}
	}

	@Test
	public void repairDate_fiveDigitWhereSlashWasReadAsAOne() throws IllegalAccessException, IllegalArgumentException {
		TransactionParser tp = new TransactionParser(null, null);

		// In this case, the date was actually 10/19
		String testDate = "10119";
		try {
			String result = (String) methodRepairDate.invoke(tp, testDate, DEFAULT_DATE);
			assertNotNull(result);
			assertEquals(DEFAULT_DATE, result);
		} catch (InvocationTargetException e) {
			e.getTargetException().printStackTrace();
			fail("Unexpected Exception");
		}
	}

	@Test
	public void repairDate_fiveDigitWhereFirstDigitAndSlashWereReadAsASeven()
			throws IllegalAccessException, IllegalArgumentException {
		TransactionParser tp = new TransactionParser(null, null);

		// In this case, the date was actually 10/19
		String testDate = "70719";
		try {
			String result = (String) methodRepairDate.invoke(tp, testDate, DEFAULT_DATE);
			assertNotNull(result);
			assertEquals(DEFAULT_DATE, result);
		} catch (InvocationTargetException e) {
			e.getTargetException().printStackTrace();
			fail("Unexpected Exception");
		}
	}

	@Test
	public void repairDate_threeDigitWithoutSlash() throws IllegalAccessException, IllegalArgumentException {
		TransactionParser tp = new TransactionParser(null, null);

		// In this case, the date was actually 10/19. The slash was read as a 7 and the
		// last two digits were not read for whatever reason
		String testDate = "107";
		try {
			String result = (String) methodRepairDate.invoke(tp, testDate, DEFAULT_DATE);
			assertNotNull(result);
			assertEquals(DEFAULT_DATE, result);
		} catch (InvocationTargetException e) {
			e.getTargetException().printStackTrace();
			fail("Unexpected Exception");
		}
	}
}