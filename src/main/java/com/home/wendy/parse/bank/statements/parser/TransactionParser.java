package com.home.wendy.parse.bank.statements.parser;

import java.util.regex.Pattern;

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;

public class TransactionParser {

	// TODO: Make this configurable
	private static final boolean enableLogging = true;

	private static final String DOUBLE_QUOTE = "\"";
	private static final String SEPARATOR = ",";
	private static final String SEVEN = "7";
	private static final String FORWARD_SLASH = "/";

	private String rawTransData;
	private String defaultDate;
	private String date = null;
	private String description = null;
	private String credit = null;
	private String debit = null;
	private String year = null;

	public TransactionParser(String rawTransData, String defaultDate, String year) {
		this.rawTransData = rawTransData;
		this.defaultDate = defaultDate;
		this.year = year;

		if (enableLogging) {
			System.out.println("Created new instance of TransactionParser.");
			System.out.println("\trawTransData: " + rawTransData);
			System.out.println("\tdefaultDate: " + defaultDate);
			System.out.println("\tyear: " + year);
		}
	}

	/**
	 * Returns comma separated values for date, description, credit, debit
	 */
	public String parseLine() {
		// System.out.println("Parsing line: " + rawTransData);
		String[] firstTwoPieces = StringUtils.split(rawTransData, StringUtils.SPACE, 2);
		if (firstTwoPieces.length < 2) {
			// Continue on to the next line because this is not a line that has the start of
			// a new transaction on it
			return null;
		}

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

		amount = addDecimalToThreeDigitAmt(amount);

		if (isCredit(description)) {
			credit = amount;
		} else {
			debit = amount;
		}

		// System.out.println("\tRaw Trans: " + rawTransData);
		date = repairDate(date, defaultDate, year);
		description = repairDescription(description);
		return formatTransactionData(date, description, credit, debit);
	}

	private String repairDescription(String desc) {
		desc = StringUtils.remove(desc, "‘");
		desc = StringUtils.remove(desc, "'");
		desc = StringUtils.remove(desc, "\"");
		return desc;
	}

	private String addDecimalToThreeDigitAmt(String amt) {
		if (!StringUtils.contains(amt, ".") && amt.length() == 3) {
			// This is a three digit amount and the decimal wasn't read in, so add it
			String decimalAmt = StringUtils.substring(amt, 0, 1).concat(".").concat(StringUtils.substring(amt, 1));
			amt = decimalAmt;
		}

		return amt;
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

	private String formatTransactionData(String date, String description, String credit, String debit) {
		StringBuilder sb = new StringBuilder();
		sb.append(DOUBLE_QUOTE).append(date).append(DOUBLE_QUOTE).append(SEPARATOR);
		sb.append(DOUBLE_QUOTE).append(description).append(DOUBLE_QUOTE).append(SEPARATOR);
		if (credit != null) {
			sb.append(DOUBLE_QUOTE).append(credit).append(DOUBLE_QUOTE).append(SEPARATOR);
		} else {
			sb.append(DOUBLE_QUOTE).append(DOUBLE_QUOTE).append(SEPARATOR);
		}

		if (debit != null) {
			sb.append(DOUBLE_QUOTE).append(debit).append(DOUBLE_QUOTE);
		} else {
			sb.append(DOUBLE_QUOTE).append(DOUBLE_QUOTE);
		}

		// System.out.println("\t\tParsed Trans: " + sb.toString());
		return sb.toString();
	}

	private String repairDate(String origDate, String lastResortDate, String year) {
		String result = null;
		
		// Look for a date with a single digit for day of the month. For consistency,
		// prefix that day
		// with a 0 before going forward
		Pattern singleDatePattern = Pattern.compile("\\d{1,2}/\\d");
		if (singleDatePattern.matcher(origDate).matches()) {
			String[] dateParts = StringUtils.split(origDate, "/");
			origDate = dateParts[0].concat("/").concat("0").concat(dateParts[1]);
		}

		// any digit = /d
		// forward slash = /
		Pattern pattern = Pattern.compile("\\d{1,2}/\\d\\d");
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

				// Since we have found a valid date, update the defaultDate to this valid value
				defaultDate = result;
			}
		} else {
			// If this value isn't in the right format, just return the default because it's
			// too difficult to tell what the OCR did
			result = lastResortDate;
		}
		
		// Append year
		String dateWithSlash = result.concat(FORWARD_SLASH);
		String dateWithSlashAndYear = dateWithSlash.concat(year);

		// return result;
		return dateWithSlashAndYear;
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

	public String getDefaultDate() {
		return defaultDate;
	}
}
