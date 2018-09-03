package com.home.wendy.parse.bank.statements;

import java.io.IOException;
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

	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println(
					"To parse files, you must enter the full path to the directory.  Example: C:\\Users\\Orval\\myBankStaments");
			System.exit(0);
		}

		String fullPathToDirectory = args[0];

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
					StatementParser sp = new StatementParser(path);
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
	}
}