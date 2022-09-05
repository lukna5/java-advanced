package info.kgeorgiy.ja.kononov.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

public class WebCrawler implements Crawler {
    private final ExecutorService downloaderService;
    private final ExecutorService extractorService;
    private final Downloader downloader;

    /**
     * Main function for downloading url
     *
     * @param args should be: url [depth [downloads [extractors [perHost]]]]
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        if (args == null || args.length != 5) {
            System.out.println("Invalid args, should be: url [depth [downloads [extractors [perHost]]]]");
            return;
        }
        String url = args[0];
        int depth = Integer.parseInt(args[1]);
        int downloads = Math.max(Integer.parseInt(args[2]), 1);
        int extractors = Math.max(Integer.parseInt(args[3]), 1);
        new WebCrawler(new CachingDownloader(), downloads, extractors, 0).download(url, depth);

    }

    /**
     * Constructor that specifies the Downloader and the number of threads to work with
     *
     * @param downloader  Downloader documents of web-page
     * @param downloaders number of threads for downloading page
     * @param extractors  number of threads for extracting links from page
     * @param perHost     Unused variable
     */
    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        downloaderService = Executors.newFixedThreadPool(downloaders);
        extractorService = Executors.newFixedThreadPool(extractors);
        this.downloader = downloader;
    }

    /**
     * Downloads pages from url to depth
     *
     * @param url   Page where we start
     * @param depth How deep to go down
     * @return a list of downloaded pages and a map of urls and errors
     */
    @Override
    public Result download(String url, int depth) {
        Map<String, IOException> exceptions = new ConcurrentHashMap<>();
        List<String> visited = new ArrayList<>(bfsDownload(depth, url, exceptions));
        visited.removeAll(exceptions.keySet());
        return new Result(visited, exceptions);
    }

    private Set<String> bfsDownload(int depth, String url, Map<String, IOException> exceptions) {
        Set<String> visited = ConcurrentHashMap.newKeySet();
        Phaser phaser = new Phaser(1);
        Set<String> urls = ConcurrentHashMap.newKeySet();
        urls.add(url);
        for (int i = 0; i < depth; i++) {
            boolean needExctract = i != depth - 1;
            urls = downloadLevel(visited, exceptions, urls, phaser, needExctract);
            phaser.arriveAndAwaitAdvance();
        }
        return visited;
    }

    private Set<String> downloadLevel(Set<String> visited, Map<String, IOException> exceptions,
                                      Set<String> urls, Phaser phaser, boolean needExctract) {
        Set<String> nextUrls = ConcurrentHashMap.newKeySet();
        urls.forEach(link -> downloadURL(link, visited, phaser, exceptions, nextUrls, needExctract));
        return nextUrls;
    }

    private void downloadURL(String url, Set<String> visited, Phaser phaser, Map<String,
            IOException> exceptions, Set<String> nextUrls, boolean needExctract) {
        if (visited.contains(url)) {
            return;
        }
        visited.add(url);
        synchronized (url) {
            Runnable downloadOperation = () -> {
                try {
                    Document document = downloader.download(url);
                    if (needExctract) {
                        Runnable extractOperation = () -> {
                            try {
                                nextUrls.addAll(document.extractLinks());
                            } catch (IOException e) {
                                exceptions.put(url, e);
                            } finally {
                                phaser.arriveAndDeregister();
                            }
                        };
                        phaser.register();
                        extractorService.submit(extractOperation);
                    }
                } catch (IOException e) {
                    exceptions.put(url, e);
                } finally {
                    phaser.arriveAndDeregister();
                }
            };
            phaser.register();
            downloaderService.submit(downloadOperation);
        }
    }

    /**
     * Stopped all threads
     */
    @Override
    public void close() {
        downloaderService.shutdown();
        extractorService.shutdown();
        try {
            if (!downloaderService.awaitTermination(1000, TimeUnit.MILLISECONDS)) {
                downloaderService.shutdownNow();
            }
            if (!extractorService.awaitTermination(1000, TimeUnit.MILLISECONDS)) {
                extractorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            /* ignored */
        } finally {
            downloaderService.shutdown();
            extractorService.shutdownNow();
        }
    }
}
