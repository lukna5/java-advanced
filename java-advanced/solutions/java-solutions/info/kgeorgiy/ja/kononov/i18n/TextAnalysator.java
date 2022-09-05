package info.kgeorgiy.ja.kononov.i18n;

import java.text.*;
import java.util.*;

public class TextAnalysator {
    String text;
    Locale locale;
    private final String TAB = "\t";
    private final String SKIP = "\n";
    private final String QUOTE = "\"";
    private final String SKIP_TAB = SKIP + TAB;
    final List<String> sentenceKeys = Arrays.asList("NumOfSentence", "MinSentence", "MaxSentence",
            "AverageLengthSentence", "MinLengthSentence", "MaxLengthSentence");
    final List<String> wordsKeys = Arrays.asList("NumOfWords", "MinWord", "MaxWord", "AverageLengthWord",
            "MinLengthWord", "MaxLengthWord");
    final List<String> numberKeys = Arrays.asList("NumOfNums", "MinNum", "MaxNum",
            "AverageLengthNum");
    final List<String> moneyKeys = Arrays.asList("NumOfMoneySum", "MinMoneySum", "MaxMoneySum",
            "AverageSum");
    final List<String> dataKeys = Arrays.asList("NumOfData", "MinData", "MaxData",
            "AverageData");
    String basicForm = "{0} : {1} ";
    String wordForm = "{0} : " + QUOTE + "{1}" + QUOTE + "";
    String moneyForm = "{0} : {1, number, currency} ";
    String dataForm = "{0} : {1, date} ";
    List<DateFormat> dateFormatList;

    public TextAnalysator(String text, Locale locale) {
        this.text = text;
        this.locale = locale;
        dateFormatList = List.of(
                DateFormat.getDateInstance(DateFormat.SHORT, locale),
                DateFormat.getDateInstance(DateFormat.FULL, locale),
                DateFormat.getDateInstance(DateFormat.DEFAULT, locale),
                DateFormat.getDateInstance(DateFormat.MEDIUM, locale),
                DateFormat.getDateInstance(DateFormat.LONG, locale)
        );
    }

    public Category<String> analyzeWords(BreakIterator breakIterator) {
        breakIterator.setText(text);
        int left = breakIterator.first();
        int right = breakIterator.next();
        Category<String> res = new Category<>(Collator.getInstance(locale));
        while (right != BreakIterator.DONE) {
            String next = text.substring(left, right).trim();
            left = right;
            right = breakIterator.next();
            if (next.codePoints().anyMatch(Character::isLetter)) {
                setCategory(res, next, next);
                res.addAverageSum(next.length());
            }
        }
        res.setAverage();
        return res;
    }

    public Category<Date> analyzeData() {
        final BreakIterator breakIterator = BreakIterator.getLineInstance(locale);
        breakIterator.setText(text);
        int left = breakIterator.first();
        int right = breakIterator.next();
        Category<Date> res = new Category<>((o1, o2) -> {
            Date date1 = (Date) o1;
            Date date2 = (Date) o2;
            if (date1.before(date2)) return -1;
            else if (date2.before(date1)) return 1;
            return 0;
        });
        res.isDate = true;
        ArrayList<Date> was = new ArrayList<>();
        while (right != BreakIterator.DONE) {
            String next = text.substring(left, right).trim();
            left = right;
            right = breakIterator.next();
            Date num = null;
            for (DateFormat dateFormat : dateFormatList) {
                try {
                    if (dateFormat.parse(next) != null) {
                        num = dateFormat.parse(next);
                        break;
                    }
                } catch (ParseException ignored) {
                }
            }
            if (num == null) {
                continue;
            }
            was.add(num);
            setCategory(res, num, next);
        }
        if (res.getNumOfIn() > 0) {
            res.averageSum = (long) (was
                    .stream()
                    .mapToLong(e -> e.getTime() / (1000L))
                    .average()
                    .getAsDouble() * 1000L);
        }
        return res;
    }

    private Category<Number> analyzeNumOrMoney(NumberFormat numberFormat, BreakIterator breakIterator) {
        breakIterator.setText(text);
        int left = breakIterator.first();
        int right = breakIterator.next();
        Category<Number> res = new Category<>((o1, o2) -> {
            Number number1 = (Number) o1;
            Number number2 = (Number) o2;
            return Double.compare(number1.doubleValue(), number2.doubleValue());
        });
        while (right != BreakIterator.DONE) {
            String next = text.substring(left, right).trim();
            left = right;
            right = breakIterator.next();
            Number num;
            try {
                num = numberFormat.parse(next);
            } catch (ParseException e) {
                continue;
            }
            if (num == null) {
                continue;
            }
            setCategory(res, num, next);
            res.addAverageSum(num.doubleValue());
        }
        res.setAverage();
        return res;
    }

    private <T> void setCategory(Category<T> res, T num, String next) {
        res.incNumOfAll();
        if (res.addElement(num)) {
            res.incNumOfEach();
        }
        res.setMin(num);
        res.setMax(num);
        res.setMaxLength(next);
        res.setMinLength(next);
    }

    public Category<Number> analyzeNumber() {
        return analyzeNumOrMoney(NumberFormat.getNumberInstance(locale), BreakIterator.getWordInstance(locale));
    }

    public Category<Number> analyzeMoney() {
        return analyzeNumOrMoney(NumberFormat.getCurrencyInstance(locale), BreakIterator.getWordInstance(locale));
    }

    public Category<String> analyzeSentence() {
        return analyzeWords(BreakIterator.getSentenceInstance(locale));
    }

    public String getAllInfo(Category<String> sentenceCat, Category<String> wordCat, Category<Number> numCat,
                             Category<Number> moneyCat, Category<Date> dataCat, Locale outLocale, String inFile) {
        ResourceBundle bundle;
        if (outLocale.getLanguage().equals("en")) {
            Locale.setDefault(new Locale ("en", "US"));
            bundle = ResourceBundle.getBundle("info.kgeorgiy.ja.kononov.i18n.ResourceBundle_en");
        } else if (outLocale.getLanguage().equals("ru")) {
            Locale.setDefault(new Locale("ru", "RU"));
            bundle = ResourceBundle.getBundle("info.kgeorgiy.ja.kononov.i18n.ResourceBundle_ru");
        } else {
            System.out.println("What are you given me???");
            return "Don't have this local";
        }

        String basicInfo = bundle.getString("AnalysedFile") + ": " + QUOTE + inFile + QUOTE + SKIP +
                bundle.getString("CheckStatistic") + SKIP_TAB +
                bundle.getString("NumOfSentence") + ": " + sentenceCat.getNumOfIn() + SKIP_TAB +
                bundle.getString("NumOfWords") + ": " + wordCat.getNumOfIn() + SKIP_TAB +
                bundle.getString("NumOfNums") + ": " + numCat.getNumOfIn() + SKIP_TAB +
                bundle.getString("NumOfMoneySum") + ": " + moneyCat.getNumOfIn() + SKIP_TAB +
                bundle.getString("NumOfData") + ": " + dataCat.getNumOfIn() + SKIP + SKIP;
        String builder = bundle.getString("SentenceStatistic") + SKIP_TAB + getCategoryInfo(wordForm, sentenceCat, bundle, sentenceKeys, true) +
                bundle.getString("WordStatistic") + SKIP_TAB + getCategoryInfo(wordForm, wordCat, bundle, wordsKeys, true) +
                bundle.getString("NumStatistic") + SKIP_TAB + getCategoryInfo(basicForm, numCat, bundle, numberKeys, false) +
                bundle.getString("SumStatistic") + SKIP_TAB + getCategoryInfo(moneyForm, moneyCat, bundle, moneyKeys, false) +
                bundle.getString("DateStatistic") + SKIP_TAB + getCategoryInfo(dataForm, dataCat, bundle, dataKeys, false);
        return basicInfo + builder;
    }

    private <T> String getCategoryInfo(String smallForm, Category<T> category, ResourceBundle bundle,
                                       List<String> keys, boolean isWords) {
        String average;
        String pattern;
        String minLength = "";
        String maxLength = "";
        if (isWords) {
            pattern = "{0} ({1}) \n\t{2} \n\t{3} \n\t{4} \n\t{5} \t{6}\n";
            String patternLength = "{0} ({1})\n";
            minLength = MessageFormat.format(patternLength, getLine(basicForm, bundle,
                    category.getMinLength().length(), 4, keys, category), category.getMinLength());
            maxLength = MessageFormat.format(patternLength, getLine(basicForm, bundle,
                    category.getMaxLength().length(), 5, keys, category), category.getMaxLength());
        } else {
            pattern = "{0} ({1}) \n\t{2} \n\t{3} \n\t{4}\n\n";
        }
        if (category.isDate) {
            average = getLine(basicForm, bundle, new Date(category.getAverageSum()), 3, keys, category);
        } else {
            average = getLine(smallForm, bundle, category.getAverage(), 3, keys, category);
        }
        return MessageFormat.format(pattern,
                getLine(basicForm, bundle, category.getNumOfIn(), 0, keys, category),
                category.getNumOfEach() + " " + bundle.getString("Different"),
                getLine(smallForm, bundle, category.getMin(), 1, keys, category),
                getLine(smallForm, bundle, category.getMax(), 2, keys, category),
                average,
                minLength,
                maxLength
        );
    }

    private <T, R> String getLine(String patSmall, ResourceBundle bundle, T info,
                                  int ind, List<String> keys, Category<R> category) {
        if (keys.size() <= ind || info == null || category.getNumOfIn() == 0) {
            return "";
        }

        return getLineOfStatistic(bundle, patSmall, keys.get(ind), info);
    }

    private <T> String getLineOfStatistic(ResourceBundle bundle, String pat, String key, T info) {
        return MessageFormat.format(pat, bundle.getString(key), info);
    }

}


