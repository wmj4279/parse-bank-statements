package com.home.wendy.parse.bank.statements.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class StatementParserTest {

	private static final String METHOD_NAME_FIND_YEAR = "findYear";
	private static final String METHOD_NAME_PARSE_TRANSACTIONS = "parseTransactions";

	private static Method methodFindYear;
	private static Method methodParseTransactions;

	private StatementParser sp;

	@BeforeClass
	public static void setUpBeforeClass() throws NoSuchMethodException, SecurityException {
		methodFindYear = StatementParser.class.getDeclaredMethod(METHOD_NAME_FIND_YEAR, String.class);
		methodFindYear.setAccessible(true);

		methodParseTransactions = StatementParser.class.getDeclaredMethod(METHOD_NAME_PARSE_TRANSACTIONS, List.class,
				String.class);
		methodParseTransactions.setAccessible(true);
	}

	@Before
	public void setUp() {
		sp = new StatementParser(null, null);
	}

	@Test
	public void parseTransactions() throws IllegalAccessException, IllegalArgumentException {
		List<String> rawTrans = new ArrayList<>();
		rawTrans.add("10/17 ‘ATM Check Deposit - 10/17 Mach ID 20820 2925 Reynolda Rd 2,978.99");
		rawTrans.add("Winston Salem NC 6014 0003291");
		rawTrans.add("T0n7 ‘Check Crd Purchase 10/13 North Point Chrysi 336-7590599 NC 50.73");
		
		String year = "11";
		
		String defaultDate = "10/01";
		
		StatementParser sp = new StatementParser(null, defaultDate);
		
		List<String> results = null;
		try {
			results = (List) methodParseTransactions.invoke(sp, rawTrans, year);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}

		assertNotNull(results);
		assertTrue(2 == results.size());
		assertEquals("\"10/17/11\",\"ATM Check Deposit - 10/17 Mach ID 20820 2925 Reynolda Rd\",\"2978.99\",\"\"",
				results.get(0));
		assertEquals("\"10/17/11\",\"Check Crd Purchase 10/13 North Point Chrysi 336-7590599 NC\",\"\",\"50.73\"",
				results.get(1));
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
