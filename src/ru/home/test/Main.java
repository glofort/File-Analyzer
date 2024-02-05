package ru.home.test;

import ru.home.test.analyzer.AbstractFileAnalyzer;
import ru.home.test.analyzer.FileAnalyzer;
import ru.home.test.analyzer.FileAnalyzerRecursive;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        final Configuration configuration = new Configuration(args);

        final String validateText = configuration.validateText();
        if (validateText != null && !validateText.isEmpty()) {
            System.out.println(validateText);
        } else {
            final AbstractFileAnalyzer analyzer;
            if (configuration.isRecursive()) {
                analyzer = new FileAnalyzerRecursive(configuration);
            } else {
                analyzer = new FileAnalyzer(configuration);
            }
            analyzer.search();
        }
    }
}
