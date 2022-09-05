package info.kgeorgiy.ja.kononov.i18n;

import java.util.ListResourceBundle;

public class ResourceBundle_ru extends ListResourceBundle {
    private static final Object[][] WORDS = {
            {"AnalysedFile", "Анализируемый файл"},
            {"CheckStatistic", "Сводная статистика"},
            {"SentenceStatistic", "Число предложений"},
            {"WordStatistic", "Число слов"},
            {"NumStatistic", "Число чисел"},
            {"SumStatistic", "Число сумм"},
            {"DateStatistic", "Число дат"},
            {"Different", "различных"},

            {"NumOfSentence", "Число предложений"},
            {"MinSentence", "Минимальное предложение"},
            {"MaxSentence", "Максимальное предложение"},
            {"MinLengthSentence", "Минимальная длина предложения"},
            {"MaxLengthSentence", "Максимальная длина предложения"},
            {"AverageLengthSentence", "Средняя длина предложения"},

            {"NumOfWords", "Число слов"},
            {"MinWord", "Минимальное слово"},
            {"MaxWord", "Максимальное слово"},
            {"MinLengthWord", "Минимальная длина слова"},
            {"MaxLengthWord", "Максимальная длина слова"},
            {"AverageLengthWord", "Средняя длина слова"},

            {"NumOfNums", "Число чисел"},
            {"MinNum", "Минимальное число"},
            {"MaxNum", "Максимальное число"},
            {"AverageLengthNum", "Среднее число"},

            {"NumOfMoneySum", "Число сумм"},
            {"MinMoneySum", "Минимальная сумма"},
            {"MaxMoneySum", "Максимальная сумма"},
            {"AverageSum", "Среднее сумма"},

            {"NumOfData", "Число дат"},
            {"MinData", "Минимальная дата"},
            {"MaxData", "Максимальная дата"},
            {"AverageData", "Средняя дата"}
    };

    @Override
    protected Object[][] getContents() {
        return WORDS;
    }
}
