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
	private static final String DEFAULT_DATE = "07/09";
	private static final String YEAR = "18";
	private static final String FORWARD_SLASH = "/";
	private static final String DEFAULT_DATE_WITH_YEAR = DEFAULT_DATE.concat(FORWARD_SLASH).concat(YEAR);

	private static Method methodRepairDate;

	@BeforeClass
	public static void setUpBeforeClass() throws NoSuchMethodException, SecurityException {
		methodRepairDate = TransactionParser.class.getDeclaredMethod(METHOD_NAME_REPAIR_DATE, String.class,
				String.class, String.class);
		methodRepairDate.setAccessible(true);
	}

	@Test
	public void parseLineTypicalDebit() {

		String line = "70719 POS Purchase - 10/19 Mach ID 000000 Walgreens Swe O 30.00";
		TransactionParser tp = new TransactionParser(line, DEFAULT_DATE, YEAR);
		String expectedResult = "\"" + DEFAULT_DATE_WITH_YEAR
				+ "\",\"POS Purchase - 10/19 Mach ID 000000 Walgreens Swe O\",\"\",\"30.00\"";
		String result = tp.parseLine();

		assertNotNull(result);
		assertEquals(expectedResult, result);
		assertNotNull(tp.getDate());
		assertEquals(DEFAULT_DATE_WITH_YEAR, tp.getDate());
		assertNotNull(tp.getDescription());
		assertEquals("POS Purchase - 10/19 Mach ID 000000 Walgreens Swe O", tp.getDescription());
		assertNotNull(tp.getDebit());
		assertEquals("30.00", tp.getDebit());
		assertNull(tp.getCredit());
	}

	@Test
	public void parseDebitLine_SecondLineEndsWithDigits() {

		String line = "Winston Salem NC 6014 0003291";
		TransactionParser tp = new TransactionParser(line, DEFAULT_DATE, YEAR);
		String result = tp.parseLine();

		assertNull(result);
		assertNull(tp.getDate());
		assertNull(tp.getDescription());
		assertNull(tp.getDebit());
		assertNull(tp.getCredit());
	}

	@Test
	public void parseDebit_LineWithDailyEndingBalance() {

		String line = "10119 POS Purchase - 10/19 Mach ID 000000 K & G Salvage East Bend 91.35 1779.38";
		String expectedResult = "\"" + DEFAULT_DATE_WITH_YEAR
				+ "\",\"POS Purchase - 10/19 Mach ID 000000 K & G Salvage East Bend\",\"\",\"91.35\"";

		TransactionParser tp = new TransactionParser(line, DEFAULT_DATE, YEAR);
		String result = tp.parseLine();

		assertNotNull(result);
		assertEquals(expectedResult, result);
		assertNotNull(tp.getDate());
		assertEquals(DEFAULT_DATE_WITH_YEAR, tp.getDate());
		assertNotNull(tp.getDescription());
		assertEquals("POS Purchase - 10/19 Mach ID 000000 K & G Salvage East Bend", tp.getDescription());
		assertNotNull(tp.getDebit());
		assertEquals("91.35", tp.getDebit());
		assertNull(tp.getCredit());
	}

	@Test
	public void parseDebit_LineWithAmountLessThanTen() {

		String line = "107 POS Purchase 10/14 Shell Service Station Winston Sale NC 639";
		String expectedResult = "\"" + DEFAULT_DATE_WITH_YEAR
				+ "\",\"POS Purchase 10/14 Shell Service Station Winston Sale NC\",\"\",\"6.39\"";
		TransactionParser tp = new TransactionParser(line, DEFAULT_DATE, YEAR);
		String result = tp.parseLine();

		assertNotNull(result);
		assertEquals(expectedResult, result);
		assertNotNull(tp.getDate());
		assertEquals(DEFAULT_DATE_WITH_YEAR, tp.getDate());
		assertNotNull(tp.getDescription());
		assertEquals("POS Purchase 10/14 Shell Service Station Winston Sale NC", tp.getDescription());
		assertNotNull(tp.getDebit());
		assertEquals("6.39", tp.getDebit());
		assertNull(tp.getCredit());
	}

	@Test
	public void parseDebit_LineWithAlphaInDate() {

		String line = "T0n7 ‘Check Crd Purchase 10/13 North Point Chrysi 336-7590599 NC 50.73";
		String expectedResult = "\"" + DEFAULT_DATE_WITH_YEAR
				+ "\",\"Check Crd Purchase 10/13 North Point Chrysi 336-7590599 NC\",\"\",\"50.73\"";
		TransactionParser tp = new TransactionParser(line, DEFAULT_DATE, YEAR);
		String result = tp.parseLine();

		assertNotNull(result);
		assertEquals(expectedResult, result);
		assertNotNull(tp.getDate());
		assertEquals(DEFAULT_DATE_WITH_YEAR, tp.getDate());
		assertNotNull(tp.getDescription());
		assertEquals("Check Crd Purchase 10/13 North Point Chrysi 336-7590599 NC", tp.getDescription());
		assertNotNull(tp.getDebit());
		assertEquals("50.73", tp.getDebit());
		assertNull(tp.getCredit());
	}

	@Test
	public void parseCredit_AtmCheckDepositLine() {

		String line = "10/17 ‘ATM Check Deposit - 10/17 Mach ID 20820 2925 Reynolda Rd 2,978.99";
		String expectedResult = "\"10/17" + FORWARD_SLASH + YEAR
				+ "\",\"ATM Check Deposit - 10/17 Mach ID 20820 2925 Reynolda Rd\",\"2978.99\",\"\"";
		TransactionParser tp = new TransactionParser(line, DEFAULT_DATE, YEAR);
		String result = tp.parseLine();

		assertNotNull(result);
		assertEquals(expectedResult, result);
		assertNotNull(tp.getDate());
		assertEquals("10/17" + FORWARD_SLASH + YEAR, tp.getDate());
		assertNotNull(tp.getDescription());
		assertEquals("ATM Check Deposit - 10/17 Mach ID 20820 2925 Reynolda Rd", tp.getDescription());
		assertNull(tp.getDebit());
		assertNotNull(tp.getCredit());
		assertEquals("2978.99", tp.getCredit());
	}

	@Test
	public void parseLine_SingleDigitDayOfMonth() {

		String line = "11/3 Check Crd Purchase 11/02 Two Brothers Citgo Winston Sale NC 22.29";
		TransactionParser tp = new TransactionParser(line, DEFAULT_DATE, YEAR);
		String expectedResult = "\"11/03" + FORWARD_SLASH + YEAR
				+ "\",\"Check Crd Purchase 11/02 Two Brothers Citgo Winston Sale NC\",\"\",\"22.29\"";
		String result = tp.parseLine();

		assertNotNull(result);
		assertEquals(expectedResult, result);
		assertNotNull(tp.getDate());
		assertEquals("11/03" + FORWARD_SLASH + YEAR, tp.getDate());
		assertNotNull(tp.getDescription());
		assertEquals("Check Crd Purchase 11/02 Two Brothers Citgo Winston Sale NC", tp.getDescription());
		assertNotNull(tp.getDebit());
		assertEquals("22.29", tp.getDebit());
		assertNull(tp.getCredit());
	}

	@Test
	public void parseCredit_AtmCheckDepositLineWithDailyEndingBalance() {

		String line = "10/17 ‘ATM Check Deposit - 10/17 Mach ID 20820 2925 Reynolda Rd 2,978.99 3,000.00";
		String expectedResult = "\"10/17" + FORWARD_SLASH + YEAR
				+ "\",\"ATM Check Deposit - 10/17 Mach ID 20820 2925 Reynolda Rd\",\"2978.99\",\"\"";
		TransactionParser tp = new TransactionParser(line, DEFAULT_DATE, YEAR);
		String result = tp.parseLine();

		assertNotNull(result);
		assertEquals(expectedResult, result);
		assertNotNull(tp.getDate());
		assertEquals("10/17" + FORWARD_SLASH + YEAR, tp.getDate());
		assertNotNull(tp.getDescription());
		assertEquals("ATM Check Deposit - 10/17 Mach ID 20820 2925 Reynolda Rd", tp.getDescription());
		assertNull(tp.getDebit());
		assertNotNull(tp.getCredit());
		assertEquals("2978.99", tp.getCredit());
	}

	@Test
	public void parseCredit_DepositMadeInBranch() {

		String line = "10724 Deposit Made In A Branch/Store 2,299.21";
		String expectedResult = "\"" + DEFAULT_DATE_WITH_YEAR
				+ "\",\"Deposit Made In A Branch/Store\",\"2299.21\",\"\"";
		TransactionParser tp = new TransactionParser(line, DEFAULT_DATE, YEAR);
		String result = tp.parseLine();

		assertNotNull(result);
		assertEquals(expectedResult, result);
		assertNotNull(tp.getDate());
		assertEquals(DEFAULT_DATE_WITH_YEAR, tp.getDate());
		assertNotNull(tp.getDescription());
		assertEquals("Deposit Made In A Branch/Store", tp.getDescription());
		assertNull(tp.getDebit());
		assertNotNull(tp.getCredit());
		assertEquals("2299.21", tp.getCredit());
	}

	@Test
	public void parseCredit_CreditCardPurchaseRim() {

		String line = "70124 ‘Check Crd Pur Rim 10/21 Bimco Corporation Winston Sale NC 181.53";
		String expectedResult = "\"" + DEFAULT_DATE_WITH_YEAR
				+ "\",\"Check Crd Pur Rim 10/21 Bimco Corporation Winston Sale NC\",\"181.53\",\"\"";
		TransactionParser tp = new TransactionParser(line, DEFAULT_DATE, YEAR);
		String result = tp.parseLine();

		assertNotNull(result);
		assertEquals(expectedResult, result);
		assertNotNull(tp.getDate());
		assertEquals(DEFAULT_DATE_WITH_YEAR, tp.getDate());
		assertNotNull(tp.getDescription());
		assertEquals("Check Crd Pur Rim 10/21 Bimco Corporation Winston Sale NC", tp.getDescription());
		assertNull(tp.getDebit());
		assertNotNull(tp.getCredit());
		assertEquals("181.53", tp.getCredit());
	}

	@Test
	public void parseCredit_OnlineTransfer() {

		String line = "10/26 ‘Online Transfer Ref #Ibetk32S7B From Business Checking 250.00";
		String expectedResult = "\"10/26" + FORWARD_SLASH + YEAR
				+ "\",\"Online Transfer Ref #Ibetk32S7B From Business Checking\",\"250.00\",\"\"";
		TransactionParser tp = new TransactionParser(line, DEFAULT_DATE, YEAR);
		String result = tp.parseLine();

		assertNotNull(result);
		assertEquals(expectedResult, result);
		assertNotNull(tp.getDate());
		assertEquals("10/26" + FORWARD_SLASH + YEAR, tp.getDate());
		assertNotNull(tp.getDescription());
		assertEquals("Online Transfer Ref #Ibetk32S7B From Business Checking", tp.getDescription());
		assertNull(tp.getDebit());
		assertNotNull(tp.getCredit());
		assertEquals("250.00", tp.getCredit());
	}

	@Test
	public void parseCredit_RemoveSingleQuote() {

		String line = "10/26 'Online Transfer Ref #Ibetk32S7B From Business Checking 250.00";
		String expectedResult = "\"10/26" + FORWARD_SLASH + YEAR
				+ "\",\"Online Transfer Ref #Ibetk32S7B From Business Checking\",\"250.00\",\"\"";
		TransactionParser tp = new TransactionParser(line, DEFAULT_DATE, YEAR);
		String result = tp.parseLine();

		assertNotNull(result);
		assertEquals(expectedResult, result);
		assertNotNull(tp.getDate());
		assertEquals("10/26" + FORWARD_SLASH + YEAR, tp.getDate());
		assertNotNull(tp.getDescription());
		assertEquals("Online Transfer Ref #Ibetk32S7B From Business Checking", tp.getDescription());
		assertNull(tp.getDebit());
		assertNotNull(tp.getCredit());
		assertEquals("250.00", tp.getCredit());
	}

	@Test
	public void repairDate_validDate() throws IllegalAccessException, IllegalArgumentException {
		TransactionParser tp = new TransactionParser(null, null, YEAR);
		String testDate = "07/14";
		try {
			String result = (String) methodRepairDate.invoke(tp, testDate, DEFAULT_DATE, YEAR);
			assertNotNull(result);
			assertEquals(testDate + FORWARD_SLASH + YEAR, result);
		} catch (InvocationTargetException e) {
			e.getTargetException().printStackTrace();
			fail("Unexpected Exception");
		}
	}

	@Test
	public void repairDate_validFormatWithLeadingSevenOnMonthAndDay()
			throws IllegalAccessException, IllegalArgumentException {
		TransactionParser tp = new TransactionParser(null, null, null);

		// In this case, the date was actually 10/13. For whatever reason, the ones were
		// read as sevens
		String testDate = "70/73";
		try {
			String result = (String) methodRepairDate.invoke(tp, testDate, DEFAULT_DATE, YEAR);
			assertNotNull(result);
			assertEquals("10/13" + FORWARD_SLASH + YEAR, result);
		} catch (InvocationTargetException e) {
			e.getTargetException().printStackTrace();
			fail("Unexpected Exception");
		}
	}
	
	@Test
	public void repairDate_validFormatWithLeadingSevenOnMonth()
			throws IllegalAccessException, IllegalArgumentException {
		TransactionParser tp = new TransactionParser(null, null, null);

		// In this case, the date was actually 10/13. For whatever reason, the ones were
		// read as sevens
		String testDate = "70/23";
		try {
			String result = (String) methodRepairDate.invoke(tp, testDate, DEFAULT_DATE, YEAR);
			assertNotNull(result);
			assertEquals("10/23" + FORWARD_SLASH + YEAR, result);
		} catch (InvocationTargetException e) {
			e.getTargetException().printStackTrace();
			fail("Unexpected Exception");
		}
	}

	@Test
	public void repairDate_validFormatWithLeadingSevenOnDay() throws IllegalAccessException, IllegalArgumentException {
		TransactionParser tp = new TransactionParser(null, null, null);

		// In this case, the date was actually 10/13. For whatever reason, the ones were
		// read as sevens
		String testDate = "10/73";
		try {
			String result = (String) methodRepairDate.invoke(tp, testDate, DEFAULT_DATE, YEAR);
			assertNotNull(result);
			assertEquals("10/13" + FORWARD_SLASH + YEAR, result);
		} catch (InvocationTargetException e) {
			e.getTargetException().printStackTrace();
			fail("Unexpected Exception");
		}
	}

	@Test
	public void repairDate_fiveDigitWhereSlashWasReadAsAOne() throws IllegalAccessException, IllegalArgumentException {
		TransactionParser tp = new TransactionParser(null, null, null);

		// In this case, the date was actually 10/19
		String testDate = "10119";
		try {
			String result = (String) methodRepairDate.invoke(tp, testDate, DEFAULT_DATE, YEAR);
			assertNotNull(result);
			assertEquals(DEFAULT_DATE_WITH_YEAR, result);
		} catch (InvocationTargetException e) {
			e.getTargetException().printStackTrace();
			fail("Unexpected Exception");
		}
	}

	@Test
	public void repairDate_fiveDigitWhereFirstDigitAndSlashWereReadAsASeven()
			throws IllegalAccessException, IllegalArgumentException {
		TransactionParser tp = new TransactionParser(null, null, null);

		// In this case, the date was actually 10/19
		String testDate = "70719";
		try {
			String result = (String) methodRepairDate.invoke(tp, testDate, DEFAULT_DATE, YEAR);
			assertNotNull(result);
			assertEquals(DEFAULT_DATE_WITH_YEAR, result);
		} catch (InvocationTargetException e) {
			e.getTargetException().printStackTrace();
			fail("Unexpected Exception");
		}
	}

	@Test
	public void repairDate_threeDigitWithoutSlash() throws IllegalAccessException, IllegalArgumentException {
		TransactionParser tp = new TransactionParser(null, null, null);

		String testDate = "107";
		try {
			String result = (String) methodRepairDate.invoke(tp, testDate, DEFAULT_DATE, YEAR);
			assertNotNull(result);
			assertEquals(DEFAULT_DATE_WITH_YEAR, result);
		} catch (InvocationTargetException e) {
			e.getTargetException().printStackTrace();
			fail("Unexpected Exception");
		}
	}

	@Test
	public void repairDate_singleDigitDayOfMonth() throws IllegalAccessException, IllegalArgumentException {
		TransactionParser tp = new TransactionParser(null, null, null);

		// In this case, the date was actually 11/03.
		String testDate = "11/3";
		try {
			String result = (String) methodRepairDate.invoke(tp, testDate, DEFAULT_DATE, YEAR);
			assertNotNull(result);
			assertEquals("11/03" + FORWARD_SLASH + YEAR, result);
		} catch (InvocationTargetException e) {
			e.getTargetException().printStackTrace();
			fail("Unexpected Exception");
		}
	}

	@Test
	public void repairDate_singleDigitMonth() throws IllegalAccessException, IllegalArgumentException {
		TransactionParser tp = new TransactionParser(null, null, null);

		// In this case, the date was actually 05/03
		String testDate = "5/23";
		try {
			String result = (String) methodRepairDate.invoke(tp, testDate, DEFAULT_DATE, YEAR);
			assertNotNull(result);
			assertEquals("5/23" + FORWARD_SLASH + YEAR, result);
		} catch (InvocationTargetException e) {
			e.getTargetException().printStackTrace();
			fail("Unexpected Exception");
		}
	}

	@Test
	public void repairDate_singleDigitMonth_singleDigitDay() throws IllegalAccessException, IllegalArgumentException {
		TransactionParser tp = new TransactionParser(null, null, null);

		// In this case, the date was actually 05/03
		String testDate = "5/3";
		try {
			String result = (String) methodRepairDate.invoke(tp, testDate, DEFAULT_DATE, YEAR);
			assertNotNull(result);
			assertEquals("5/03" + FORWARD_SLASH + YEAR, result);
		} catch (InvocationTargetException e) {
			e.getTargetException().printStackTrace();
			fail("Unexpected Exception");
		}
	}
}