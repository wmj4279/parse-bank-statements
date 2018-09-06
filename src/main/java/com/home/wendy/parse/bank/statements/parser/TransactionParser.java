package com.home.wendy.parse.bank.statements.parser;

import java.util.regex.Pattern;

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;

public class TransactionParser {

	private static final String DOUBLE_QUOTE = "\"";
	private static final String SEPARATOR = ",";
	private static final String SEVEN = "7";
	private static final String FORWARD_SLASH = "/";

	private String rawTransData;
	private String defaultDate;
	private String date = null;
	private String description = null;
	// private String amount = null;
	private String credit = null;
	private String debit = null;

	public TransactionParser(String rawTransData, String defaultDate) {
		this.rawTransData = rawTransData;
		this.defaultDate = defaultDate;
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
		String amount = null;

		boolean isLastLineValueADecimal = false;

		// Remove any commas that may be in a value like 2,458.54 because that will make
		// the next set of checks fail
		String lastLineValueMinusCommas = StringUtils.remove(lastLineValue, ",");
		try {
			// Is this a double.
			Double.valueOf(lastLineValueMinusCommas);
			amount = lastLineValueMinusCommas;

			// It's safe to set the date now because we know we have line that is the start
			// of a new transaction
			date = firstTwoPieces[0];

			isLastLineValueADecimal = true;

			// That must have been a double if we are here. So lastLineValue is either the
			// transaction amount or the daily ending balance
			String lineWithoutLastValue = StringUtils.substringBefore(descriptionAndAmts, lastLineValue).trim();

			// Get the next value from the right side of the line
			String secondToLastLineValue = StringUtils.substringAfterLast(lineWithoutLastValue, StringUtils.SPACE);

			// Remove any commas that may be in a value like 2,458.54 because that will make
			// the next set of checks fail
			String secondToLastLineValueMinusCommas = StringUtils.remove(secondToLastLineValue, ",");

			try {
				Double.valueOf(secondToLastLineValueMinusCommas);
				amount = secondToLastLineValueMinusCommas;

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

		if (isCredit(description)) {
			credit = amount;
		} else {
			debit = amount;
		}

		System.out.println("\tRaw Trans: " + rawTransData);
		date = repairDate(date, defaultDate);
		return formatTransactionData(date, description, amount);
	}

	/**
	 * Based on text in the description, determine if the amount for this line item
	 * is a credit or not
	 * 
	 * @param description
	 * @return
	 */
	private boolean isCredit(String description) {
		boolean result = false;

		if (StringUtils.containsIgnoreCase(description, "Deposit")
				|| StringUtils.containsIgnoreCase(description, "rtrn")
				|| StringUtils.containsIgnoreCase(description, "rim")
				|| (StringUtils.containsIgnoreCase(description, "Transfer")
						&& StringUtils.containsIgnoreCase(description, "From"))) {
			result = true;
		}

		return result;
	}

	private String formatTransactionData(String date, String description, String amount) {
		StringBuilder sb = new StringBuilder();
		sb.append(DOUBLE_QUOTE).append(date).append(DOUBLE_QUOTE).append(SEPARATOR);
		sb.append(DOUBLE_QUOTE).append(description).append(DOUBLE_QUOTE).append(SEPARATOR);
		sb.append(DOUBLE_QUOTE).append(amount).append(DOUBLE_QUOTE);
		System.out.println("\t\tParsed Trans: " + sb.toString());
		return sb.toString();
	}

	private String repairDate(String origDate, String lastResortDate) {
		String result = null;
		
		// any digit = /d
		// forward slash = /
		Pattern pattern = Pattern.compile("\\d\\d/\\d\\d");
		if (pattern.matcher(origDate).matches()) {
			result = origDate;

			// A value in a valid format can have a 7's where there shouldn't be. At least
			// replace the leading 7's if they exist.
			if (StringUtils.contains(result, SEVEN)) {

				String[] dateParts = StringUtils.split(result, FORWARD_SLASH);

				// Replace 7 in month
				if (StringUtils.startsWith(dateParts[0], SEVEN)) {
					dateParts[0] = RegExUtils.replaceFirst(dateParts[0], SEVEN, "1");
				}

				// Replace 7 in day
				if (StringUtils.startsWith(dateParts[1], SEVEN)) {
					dateParts[1] = RegExUtils.replaceFirst(dateParts[1], SEVEN, "1");
				}

				result = StringUtils.join(dateParts, FORWARD_SLASH);
			}
		} else {
			// If this value isn't in the right format, just return the default because it's
			// too difficult to tell what the OCR did
			result = lastResortDate;
		}
		
		return result;
	}

	public String getDate() {
		return date;
	}

	public String getDescription() {
		return description;
	}

	public String getDebit() {
		return debit;
	}

	public String getCredit() {
		return credit;
	}
}
