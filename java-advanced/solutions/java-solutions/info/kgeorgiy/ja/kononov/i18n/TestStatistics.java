package info.kgeorgiy.ja.kononov.i18n;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.*;
import java.util.*;

public class TestStatistics {
    public static void main(String[] args) throws ParseException {
        if (args == null || args.length < 4 || args[0] == null || args[1] == null || args[2] == null || args[3] == null) {
            System.err.println("Insert: <inLocale> <outLocale> <inText> <outText>");
            return;
        }
        Locale inLocale = giveLocale(args[0]);
        Locale outLocale = new Locale(args[1]);
        String inFile = args[2];
        String outFile = args[3];
        if (!outLocale.getLanguage().equals("ru") && !outLocale.getLanguage().equals("en")) {
            System.out.println("haven't so outLocale language (have <ru> and <en>)");
            return;
        }
        try {
            writeStatistic(inFile, outFile, inLocale, outLocale);
        } catch (IOException e) {
            System.out.println("Can't open files " + e);
        }


    }

    private static Locale giveLocale(String inLocale){
        String[] params = inLocale.split("_");
        if (params.length == 1) {
            return new Locale(inLocale);
        }
        return new Locale(params[0], params[1]);
    }

    private static void writeStatistic(String inFile, String outFile, Locale inLocale, Locale outLocale) throws IOException {
        try {
            String text = Files.readString(Path.of(inFile));
            try (BufferedWriter writer = Files.newBufferedWriter(Path.of(outFile), StandardCharsets.UTF_8)) {
                writer.write(getCategories(text, inLocale, outLocale, inFile));
            }
        } catch (IOException e) {
            throw new IOException(e);
        }
    }

    private static String getCategories(String text, Locale inLocale, Locale outLocale, String inFile) {
        TextAnalysator textAnalysator = new TextAnalysator(text, inLocale);
        Category<String> sentenceCat = textAnalysator.analyzeSentence();
        Category<String> wordCat = textAnalysator.analyzeWords(BreakIterator.getWordInstance(inLocale));
        Category<Number> numCat = textAnalysator.analyzeNumber();
        Category<Number> moneyCat = textAnalysator.analyzeMoney();
        Category<Date> dataCat = textAnalysator.analyzeData();
        return textAnalysator.getAllInfo(sentenceCat, wordCat, numCat, moneyCat, dataCat, outLocale, inFile);
    }

}
