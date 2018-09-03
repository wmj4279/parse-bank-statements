package com.home.wendy.parse.bank.statements.parser;

import org.apache.commons.lang3.StringUtils;

public class TransactionParser {

	private static final String DOUBLE_QUOTE = "\"";
	private static final String SEPARATOR = ",";

	private String rawTransData;
	private String date = null;
	private String description = null;
	private String amount = null;
	// private String

	public TransactionParser(String rawTransData) {
		this.rawTransData = rawTransData;
	}

	/**
	 * Returns comma separated values for date, description, credit, debit
	 */
	public String parseLine() {
		String[] firstTwoPieces = StringUtils.split(rawTransData, StringUtils.SPACE, 2);
		if (firstTwoPieces.length < 2) {
			// Continue on to the next line because this is not a line that has the start of
			// a new transaction on it
			return null;
		}
		// String date = firstTwoPieces[0];
		// String description = null;
		// String amount = null;
		String descriptionAndAmts = firstTwoPieces[1];

		// Get the last value from the right side of the line. If this is a line that a
		// new transaction started on, this will be a decimal value
		// representing either the transaction amount or the ending daily balance
		String lastLineValue = StringUtils.substringAfterLast(descriptionAndAmts, StringUtils.SPACE);

		if (!StringUtils.contains(lastLineValue, ".") && (lastLineValue.length() != 3)) {
			// The last value on the line doesn't contain a decimal. This value cannot be an
			// amount, therefore this line cannot be the first line of a new transaction
			// We check the length for three digits because for whatever reason, the OCR
			// always misses the decimal point on any amount less than 10.00. These will be
			// valid transactions, but will have a three digit number without a decimal
			// point
			return null;
		}

		// The description is everything before the lastLineValue
		description = StringUtils.substringBefore(descriptionAndAmts, lastLineValue).trim();

		boolean isLastLineValueADecimal = false;
		try {
			// Is this a double.
			Double.valueOf(lastLineValue);
			amount = lastLineValue;

			// It's safe to set the date now because we know we have line that is the start
			// of a new transaction
			date = firstTwoPieces[0];

			isLastLineValueADecimal = true;

			// That must have been a double if we are here. So lastLineValue is either the
			// transaction amount or the daily ending balance
			String lineWithoutLastValue = StringUtils.substringBefore(descriptionAndAmts, lastLineValue).trim();

			// Get the next value from the right side of the line
			String secondToLastLineValue = StringUtils.substringAfterLast(lineWithoutLastValue, StringUtils.SPACE);

			try {
				Double.valueOf(secondToLastLineValue);
				amount = secondToLastLineValue;

				// Since this line has two amounts, update the description to only the portion
				// prior to the second amount
				description = StringUtils.substringBefore(lineWithoutLastValue, secondToLastLineValue).trim();

			} catch (NumberFormatException e) {
				// The second to last value does not represent a decimal amt,
				// Do nothing
			}
		} catch (NumberFormatException e) {
			// Do nothing
		}

		if (!isLastLineValueADecimal) {
			// There was no amount on this line, so this cannot be a line with a new
			// transaction
			return null;
		}

		System.out.println("\tRaw Trans: " + rawTransData);
		return formatTransactionData(date, description, amount);
	}

	private String formatTransactionData(String date, String description, String amount) {
		StringBuilder sb = new StringBuilder();
		sb.append(DOUBLE_QUOTE).append(date).append(DOUBLE_QUOTE).append(SEPARATOR);
		sb.append(DOUBLE_QUOTE).append(description).append(DOUBLE_QUOTE).append(SEPARATOR);
		sb.append(DOUBLE_QUOTE).append(amount).append(DOUBLE_QUOTE).append(SEPARATOR);
		System.out.println("\t\tParsed Trans: " + sb.toString());
		return sb.toString();
	}

	public String getDate() {
		return date;
	}

	public String getAmount() {
		return amount;
	}

	public String getDescription() {
		return description;
	}
}
