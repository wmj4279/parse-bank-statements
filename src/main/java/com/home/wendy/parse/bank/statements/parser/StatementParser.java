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
			String csv = lineParser.parseLine();
			if (!StringUtils.isEmpty(csv)) {
				commaSeparatedValueList.add(csv);
			}
		}

		return commaSeparatedValueList;
	}

	// private void parseLine(String rawTrans) {
	// String[] firstTwoPieces = StringUtils.split(rawTrans, StringUtils.SPACE, 2);
	// if (firstTwoPieces.length < 2) {
	// // Continue on to the next line because this is not a line that has the start
	// of
	// // a new transaction on it
	// return;
	// }
	// // String date = firstTwoPieces[0];
	// // String description = null;
	// // String amount = null;
	// String descriptionAndAmts = firstTwoPieces[1];
	//
	// // Get the last value from the right side of the line. If this is a line that
	// a
	// // new transaction started on, this will be a decimal value
	// // representing either the transaction amount or the ending daily balance
	// String lastLineValue = StringUtils.substringAfterLast(descriptionAndAmts,
	// StringUtils.SPACE);
	//
	// if (!StringUtils.contains(lastLineValue, ".") && (lastLineValue.length() !=
	// 3)) {
	// // The last value on the line doesn't contain a decimal. This value cannot be
	// an
	// // amount, therefore this line cannot be the first line of a new transaction
	// // We check the length for three digits because for whatever reason, the OCR
	// // always misses the decimal point on any amount less than 10.00. These will
	// be
	// // valid transactions, but will have a three digit number without a decimal
	// // point
	// return;
	// }
	//
	// // The description is everything before the lastLineValue
	// description = StringUtils.substringBefore(descriptionAndAmts,
	// lastLineValue).trim();
	//
	// boolean isLastLineValueADecimal = false;
	// try {
	// // Is this a double.
	// Double.valueOf(lastLineValue);
	// amount = lastLineValue;
	//
	// // It's safe to set the date now because we know we have line that is the
	// start
	// // of a new transaction
	// date = firstTwoPieces[0];
	//
	// isLastLineValueADecimal = true;
	//
	// // That must have been a double if we are here. So lastLineValue is either
	// the
	// // transaction amount or the daily ending balance
	// String lineWithoutLastValue = StringUtils.substringBefore(descriptionAndAmts,
	// lastLineValue).trim();
	//
	// // Get the next value from the right side of the line
	// String secondToLastLineValue =
	// StringUtils.substringAfterLast(lineWithoutLastValue, StringUtils.SPACE);
	//
	// try {
	// Double.valueOf(secondToLastLineValue);
	// amount = secondToLastLineValue;
	//
	// // Since this line has two amounts, update the description to only the
	// portion
	// // prior to the second amount
	// description = StringUtils.substringBefore(lineWithoutLastValue,
	// secondToLastLineValue).trim();
	//
	// } catch (NumberFormatException e) {
	// // The second to last value does not represent a decimal amt,
	// // Do nothing
	// }
	// } catch (NumberFormatException e) {
	// // Do nothing
	// }
	//
	// if (!isLastLineValueADecimal) {
	// // There was no amount on this line, so this cannot be a line with a new
	// // transaction
	// return;
	// }
	//
	// System.out.println("\tRaw Trans: " + rawTrans);
	// writeTransactionDataToFile(date, description, amount);
	// }

	// private void writeTransactionDataToFile(String date, String description,
	// String amount) {
	// StringBuilder sb = new StringBuilder();
	// sb.append(DOUBLE_QUOTE).append(date).append(DOUBLE_QUOTE).append(SEPARATOR);
	// sb.append(DOUBLE_QUOTE).append(description).append(DOUBLE_QUOTE).append(SEPARATOR);
	// sb.append(DOUBLE_QUOTE).append(amount).append(DOUBLE_QUOTE).append(SEPARATOR);
	// System.out.println("\t\tParsed Trans: " + sb.toString());
	// }

	// public String getDate() {
	// return date;
	// }
	//
	// public String getDescription() {
	// return description;
	// }
	//
	// public String getAmount() {
	// return amount;
	// }
}
