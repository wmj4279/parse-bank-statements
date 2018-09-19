package com.home.wendy.parse.bank.statements;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.home.wendy.parse.bank.statements.parser.StatementParser;

public class ParseBankStatements {

	private static final String FILE_EXTENSION = ".txt";
	private static final String FILE_NAME_CSV = "//Summary.csv";
	private static final String HEADER = "Date,Description,Credit,Debit";
	private static final String NEW_LINE = "\n";
	private static String defaultDate;

	public static void main(String[] args) {
		if (args.length < 2) {
			System.out.println(
					"To parse files, you must enter the full path to the directory.  Example: C:\\Users\\Orval\\myBankStaments");
			System.exit(0);
		}

		String fullPathToDirectory = args[0];
		setDefaultDate(args[1]);

		System.out.println("Attempting to read " + FILE_EXTENSION + " files in directory " + fullPathToDirectory);

		Path source = Paths.get(fullPathToDirectory);
		Stream<Path> pathStream = null;
		List<String> masterTransList = new ArrayList<>();
		try {
			pathStream = Files.walk(source);
			List<Path> pathList = pathStream.sorted().collect(Collectors.toList());
			pathList.forEach(path -> {
				if (path.toString().endsWith(FILE_EXTENSION)) {
					System.out.println("Exporting data to .csv for file: " + path.toString());
					StatementParser sp = new StatementParser(path, getDefaultDate());
					setDefaultDate(sp.getDefaultDate());
					// this.defaultDate = sp.getDefaultDate();
					List<String> transList = sp.parse();
					if (!transList.isEmpty()) {
						masterTransList.addAll(transList);
					}
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (pathStream != null) {
				pathStream.close();
			}
		}

		// Write the csv transaction data to a file in this directory
		writeData(Paths.get(fullPathToDirectory + FILE_NAME_CSV), masterTransList);
	}

	private static void setDefaultDate(String dd) {
		defaultDate = dd;
	}

	private static String getDefaultDate() {
		return defaultDate;
	}

	private static void writeData(Path path, List<String> transactions) {

		try (BufferedWriter writer = Files.newBufferedWriter(path, Charset.forName("UTF-8"))) {
			// Write the header (or column names to the file)
			writer.write(HEADER);
			writer.write(NEW_LINE);
			transactions.forEach(trans -> {
				try {
					writer.write(trans);
					writer.write(NEW_LINE);
				} catch (IOException e) {
					System.out
							.println(
									"Error while writing a transaction to the csv file.  Aborting export. Error transaction: '"
											+ trans + "'");
					System.exit(0);
				}
			});
		} catch (IOException e) {
			System.out.println("Error while creating csv file.  Aborting export.");
			e.printStackTrace();
			System.exit(0);
		}
	}
}