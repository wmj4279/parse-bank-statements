package com.home.wendy.parse.bank.statements.parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

public class StatementParser {

	// Heading that occurs before transactions are listed on a page
	private static final String BEGIN_TRANS_TEXT = "Date Number Description Credits Debits";
	private static final String END_TRANS_TEXT = "Ending balance on";

	private Path statement;
	private String defaultDate;
	private List<String> transactionLines = new ArrayList<>();

	public StatementParser(Path statement, String defaultDate) {
		this.statement = statement;
		this.defaultDate = defaultDate;
	}

	/**
	 * Will return a list of transactions where each transaction has it's relevant
	 * data in a comma separated list
	 * 
	 */
	public List<String> parse() {
		System.out.println("Reading this file: " + statement.toString());
		Stream<String> s;
		List<String> transList = new ArrayList<>();
		try {
			s = Files.lines(statement);
			Iterator<String> lineIter = s.iterator();
			boolean foundTransactions = false;
			while (lineIter.hasNext()) {
				String line = lineIter.next();
				if (!foundTransactions) {
					if (StringUtils.startsWith(line, BEGIN_TRANS_TEXT)) {
						foundTransactions = true;
					}
				} else {
					if (StringUtils.startsWith(line, END_TRANS_TEXT)) {
						// Exit the iterator because we've read all of the transaction data from this
						// file
						break;
					} else {
						addTransactionLine(line);
					}
				}
			}
		} catch (IOException e) {
			System.out.println("Failure to read file: " + statement.toString());
			e.printStackTrace();
		}

		if (!transactionLines.isEmpty()) {
			transList = parseTransactions(transactionLines);
		} else {
			System.out.println("No transaction data found in file: " + statement.toString());
		}

		return transList;
	}

	private void addTransactionLine(String line) {
		transactionLines.add(line);
	}

	private List<String> parseTransactions(List<String> rawTransactions) {
		List<String> commaSeparatedValueList = new ArrayList<>();
		for (String rawTrans : rawTransactions) {
			TransactionParser lineParser = new TransactionParser(rawTrans, defaultDate);
			defaultDate = lineParser.getDefaultDate();
			String csv = lineParser.parseLine();
			if (!StringUtils.isEmpty(csv)) {
				commaSeparatedValueList.add(csv);
			}
		}

		return commaSeparatedValueList;
	}
}
