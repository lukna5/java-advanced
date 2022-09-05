package info.kgeorgiy.ja.kononov.i18n;

import java.util.ListResourceBundle;

public class ResourceBundle_en extends ListResourceBundle {
    private static final Object[][] WORDS = {
            {"AnalysedFile", "Analyzed file"},
            {"CheckStatistic",  "Statistic"},
            {"SentenceStatistic" , "Sentence statistic"},
            {"WordStatistic" , "Word Statistic"},
            {"NumStatistic" , "Num Statistic"},
            {"SumStatistic" , "Sum money Statistic"},
            {"DateStatistic" , "Date Statistic"},
            {"Different", "different"},

            {"NumOfSentence" , "Num of sentences"},
            {"MinSentence" , "Min sentence"},
            {"MaxSentence" , "Max sentence"},
            {"MinLengthSentence" , "Min length sentence"},
            {"MaxLengthSentence" , "Max length sentence"},
            {"AverageLengthSentence" , "Average length sentence"},

            {"NumOfWords" , "Num of words"},
            {"MinWord" , "Min word"},
            {"MaxWord" , "Max word"},
            {"MinLengthWord" , "Min length word"},
            {"MaxLengthWord" , "Max length word"},
            {"AverageLengthWord" , "Average length words"},

            {"NumOfNums" , "Num of numbers"},
            {"MinNum" , "Min num"},
            {"MaxNum" , "Max sentence num"},
            {"AverageLengthNum" , "Average num"},

            {"NumOfMoneySum" , "Num of MoneySums"},
            {"MinMoneySum" , "Min MoneySum"},
            {"MaxMoneySum" , "Max MoneySum"},
            {"AverageSum" , "Average MoneySum"},

            {"NumOfData" , "Num of Dates"},
            {"MinData" , "Min Data"},
            {"MaxData" , "Max sentence Data"},
            {"AverageData" , "Average Data"}
    };
    @Override
    protected Object[][] getContents() {
        return WORDS;
    }
}
