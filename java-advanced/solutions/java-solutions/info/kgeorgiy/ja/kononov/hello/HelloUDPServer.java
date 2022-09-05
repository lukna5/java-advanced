package info.kgeorgiy.ja.kononov.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HelloUDPServer implements HelloServer {
    DatagramSocket socket;
    ExecutorService converters;
    ExecutorService handler;
    private final static int WAITING_TIME = 200;

    // :NOTE: утечка ресурсов при повторных вызовах
    @Override
    public void start(int port, int threads) {
        if (socket != null) {
            return;
        }
        try {
            socket = new DatagramSocket(port);

            // :NOTE: поменять местами
            handler = Executors.newSingleThreadExecutor();
            converters = Executors.newFixedThreadPool(threads);

            int sizeOfBuffer = socket.getReceiveBufferSize();
            handler.submit(() -> {
                while (!socket.isClosed()) {
                    DatagramPacket nextPacket = new DatagramPacket(new byte[sizeOfBuffer], sizeOfBuffer);
                    try {
                        socket.receive(nextPacket);
                    } catch (IOException e) {
                        continue;
                    }
                    converters.submit(() -> {
                        String res = doConvert(nextPacket);
                        try {
                            socket.send(new DatagramPacket(res.getBytes(StandardCharsets.UTF_8), res.length(),
                                    nextPacket.getSocketAddress()));
                        } catch (IOException e) {
                            /* ignored */
                        }
                    });
                }
            });
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    private String doConvert(DatagramPacket nextPacket) {
        String given = new String(nextPacket.getData(), nextPacket.getOffset(),
                nextPacket.getLength(), StandardCharsets.UTF_8);
        return "Hello, " + given;
    }

    @Override
    public void close() {
        if (socket != null) {
            socket.close();
        }
        if (converters != null) {
            converters.shutdownNow();
            try {
                if (!converters.awaitTermination(WAITING_TIME, TimeUnit.MILLISECONDS)) {
                    converters.shutdown();
                }
            } catch (InterruptedException e) {
                /* ignored */
            }
        }
        if (handler != null){
            handler.shutdownNow();
            try {
                if (!handler.awaitTermination(WAITING_TIME, TimeUnit.MILLISECONDS)) {
                    handler.shutdown();
                }
            } catch (InterruptedException e) {
                /* ignored */

            }
        }
    }
    public static void main(String[] args) {
        if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
            System.out.println("Please enter 2 not null args");
            return;
        }
        try {
            new HelloUDPServer().start(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
        } catch (NumberFormatException e) {
            System.err.println("Can't process in number " + e.getMessage());
        }
    }
}
