package info.kgeorgiy.ja.kononov.concurrent;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;

/**
 * @author Kononov_Vladimir
 * <p>
 * This class implements {@link ParallelMapper}.
 * Created for processing map for List with multiple Threads
 */
public class ParallelMapperImpl implements ParallelMapper {
    private final List<Thread> threadList;
    private final Queue<Runnable> queue;

    /**
     * Constructor for this class
     *
     * @param threads Takes an int value of the number of threads to process values
     */
    public ParallelMapperImpl(int threads) {
        // :NOTE: лучше выбрасывать исключение
        threads = Math.max(1, threads);
        threadList = new ArrayList<>();
        queue = new ArrayDeque<>();
        for (int i = 0; i < threads; i++) {
            Thread nextThread = new Thread(() -> {
                try {
                    while (!Thread.interrupted()) {
                        runTask();
                    }
                } catch (InterruptedException e) {
                    /* ignored */
                } finally {
                    Thread.currentThread().interrupt();
                }
            });
            threadList.add(nextThread);
            nextThread.start();
        }
    }

    private void runTask() throws InterruptedException {
        Runnable request;
        synchronized (queue) {
            while (queue.isEmpty()) {
                queue.wait();
            }
            request = queue.poll();
            // :NOTE: redundant notify
//            queue.notifyAll();
        }
        request.run();
    }

    /**
     * @param f    Transform function
     * @param args List of elements
     * @param <T>  Object from which to convert
     * @param <R>  The object to convert to
     * @return Transformed list of {@code args} with type T to converted list of Type R
     * @throws InterruptedException if threads can't process
     */
    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) throws
            InterruptedException {
        ResultOfFunction<R> res = new ResultOfFunction<>(args.size());
        for (int i = 0; i < args.size(); i++) {
            int finalI = i;
            Runnable request = () -> {
                try {
                    res.set(finalI, f.apply(args.get(finalI)));
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
            };

            synchronized (queue) {
                queue.add(request);
                // :NOTE: просто notify
//                queue.notifyAll();
                queue.notify();
            }
        }
        return res.get();
    }

    /**
     * Сloses all streams that are being used for processing
     */
    @Override
    public void close() {
        threadList.forEach(Thread::interrupt);

        for (Thread thread : threadList) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static class ResultOfFunction<R> {
        private final List<R> list;
        private int size = 0;

        private ResultOfFunction(int size) {
            list = new ArrayList<>(Collections.nCopies(size, null));
        }

        synchronized void set(int index, R obj) {
            list.set(index, obj);
            size++;
            if (size == list.size()) {
                notify();
            }

        }

        synchronized List<R> get() {
            while (size < list.size()) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return list;
        }
    }
}
