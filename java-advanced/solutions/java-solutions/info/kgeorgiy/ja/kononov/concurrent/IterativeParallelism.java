package info.kgeorgiy.ja.kononov.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ScalarIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;

/**
 * @author Kononov_Vladimir
 * <p>
 * This class implements {@link ScalarIP}.
 * Created for processing List with multiple Threads
 */
public class IterativeParallelism implements ScalarIP {
    private final ParallelMapper parallelMapper;

    public IterativeParallelism(ParallelMapper parallelMapper) {
        this.parallelMapper = parallelMapper;
    }

    public IterativeParallelism() {
        this.parallelMapper = null;
    }

    private <T> List<List<? extends T>> splitIntoThreads(int threads, final List<? extends T> values) {
        List<List<? extends T>> lists = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            lists.add(sublist(threads, values, i));
        }
        return lists;
    }

    private <T> List<? extends T> sublist(int threads, final List<? extends T> values, int from) {
        final int valuesInThread = values.size() / threads;
        int addElemsForThread;
        if (values.size() % threads > 0) {
            addElemsForThread = 1;
        } else addElemsForThread = 0;

        int left = Math.min(from * (valuesInThread + addElemsForThread), values.size() - 1);
        int right = Math.min((from + 1) * (valuesInThread + addElemsForThread), values.size());
        return values.subList(left, right);
    }

    private <T, R> List<R> startParallel(int threads, final List<? extends T> values,
                                         final Function<List<? extends T>, R> function) throws InterruptedException {

        if (threads < 1 || values == null || values.size() < 1) {
            throw new InterruptedException("Threads should be >= 1 and (List != null and > 0)");
        }
        threads = Math.min(threads, values.size());

        // :NOTE: Не стоило использовать массив
        if (parallelMapper != null) {
            return parallelMapper.map(function, splitIntoThreads(threads, values));
        }
        final List<Thread> threadArray = new ArrayList<>(threads);
        final List<R> resList = new ArrayList<>(Collections.nCopies(threads, null));

        // :NOTE: IntStream
        int finalThreads = threads;
        IntStream.range(0, threads).forEach(i -> {
            final int finalI = i;
            threadArray.add(i, new Thread(() -> resList.set(finalI, function.apply(sublist(finalThreads, values, i)))));
            threadArray.get(i).start();
        });

        waitThreads(threadArray);
        return resList;
    }

    private void waitThreads(final List<Thread> threadsAr) throws InterruptedException {
        for (final Thread thread : threadsAr) {
            thread.join();
        }
    }

    /**
     * Returns maximum element
     *
     * @param threads    number or concurrent threads.
     * @param values     List of elements.
     * @param comparator value comparator.
     * @param <T>        List element type
     * @return minimum element from a list with type T
     * @throws InterruptedException if 1 or mode of these reasons:
     *                              <ul>
     *                                  <li>1) threads less than 1</li>
     *                                  <li>2) if any thread is interrupted</li>
     *                                  <li>3) {@code  values} == null</li>
     *                              </ul>
     */
    @Override
    public <T> T maximum(final int threads, final List<? extends T> values, final Comparator<? super T> comparator) throws InterruptedException {
        return minimum(threads, values, comparator.reversed());
    }

    /**
     * Returns minimum element
     *
     * @param threads    number or concurrent threads.
     * @param values     List of elements.
     * @param comparator value comparator.
     * @param <T>        List element type
     * @return maximum element from a list with type T
     * @throws InterruptedException if 1 or mode of these reasons:
     *                              <ul>
     *                                  <li>1) threads less than 1</li>
     *                                  <li>2) if any thread is interrupted</li>
     *                                  <li>3) {@code  values} == null</li>
     *                              </ul>
     */
    @Override
    public <T> T minimum(final int threads, final List<? extends T> values, final Comparator<? super T> comparator) throws InterruptedException {
        return startParallel(threads, values, (list) -> list.stream().min(comparator).orElseThrow())
                .stream()
                .min(comparator)
                .orElseThrow();
    }

    /**
     * Checks that one element match the given predicate
     *
     * @param threads   number or concurrent threads.
     * @param values    List of elements to test.
     * @param predicate test predicate.
     * @param <T>       List element type
     * @return True (if all List elements approach for predicate) | False (else)
     * @throws InterruptedException if 1 or mode of these reasons:
     *                              <ul>
     *                                  <li>1) threads less than 1</li>
     *                                  <li>2) if any thread is interrupted</li>
     *                                  <li>3) {@code  values} == null</li>
     *                              </ul>
     */
    @Override
    public <T> boolean all(final int threads, final List<? extends T> values, final Predicate<? super T> predicate) throws InterruptedException {
        return startParallel(threads, values, stream -> stream.stream().allMatch(predicate))
                .stream()
                .allMatch(element -> element);
    }

    /**
     * Checks that all elements match the given predicate
     *
     * @param threads   number or concurrent threads.
     * @param values    List of elements to test.
     * @param predicate test predicate.
     * @param <T>       List element type
     * @return True (if one List element approach for predicate) | False (else)
     * @throws InterruptedException if 1 or mode of these reasons:
     *                              <ul>
     *                                  <li>1) threads less than 1</li>
     *                                  <li>2) if any thread is interrupted</li>
     *                                  <li>3) {@code  values} == null</li>
     *                              </ul>
     */
    @Override
    public <T> boolean any(final int threads, final List<? extends T> values, final Predicate<? super T> predicate) throws InterruptedException {
        return !all(threads, values, predicate.negate());
    }
}
