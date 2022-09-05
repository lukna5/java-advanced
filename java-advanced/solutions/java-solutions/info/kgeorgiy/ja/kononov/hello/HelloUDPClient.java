package info.kgeorgiy.ja.kononov.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HelloUDPClient implements HelloClient {
    private final static int SOCKET_TIMEOUT = 200;

    private void functionForThread(int requests, int threadNum, String prefix, SocketAddress socketAddress) {
        for (int reguestNum = 0; reguestNum < requests; reguestNum++) {
            try (DatagramSocket socket = new DatagramSocket()) {
                socket.setSoTimeout(SOCKET_TIMEOUT);
                String outMessage = prefix + threadNum + "_" + reguestNum;
                DatagramPacket outPacket = new DatagramPacket(outMessage.getBytes(StandardCharsets.UTF_8),
                        outMessage.getBytes().length, socketAddress);
                DatagramPacket inPacket = new DatagramPacket(new byte[socket.getReceiveBufferSize()],
                        socket.getReceiveBufferSize());
                System.out.println(outMessage);

                String inMessage = "";
                while (!socket.isClosed() && !inMessage.contains(outMessage)) {
                    try {
                        socket.send(outPacket);
                        System.out.println(": " + outMessage);
                        socket.receive(inPacket);
                        inMessage = new String(inPacket.getData(), inPacket.getOffset(),
                                inPacket.getLength(), StandardCharsets.UTF_8);
                    } catch (IOException e) {
                        System.out.println("Can't process with socket " + e.getMessage());
                    }
                }
                System.out.println(inMessage);
            } catch (SocketException e) {
                System.out.println("Beda with socket" + e.getMessage());
            }
        }
    }

    @Override
    public void run(String host, int port, String prefix, int threads, int requests) {
        SocketAddress socketAddress = new InetSocketAddress(host, port);
        ExecutorService handlers = Executors.newFixedThreadPool(threads);
        for (int threadNum = 0; threadNum < threads; threadNum++) {
            int finalThreadNum = threadNum;
            handlers.submit(() -> functionForThread(requests, finalThreadNum, prefix, socketAddress));
        }
        handlers.shutdown();
        try {
            if (!handlers.awaitTermination(threads * requests, TimeUnit.SECONDS)) {
                System.out.println("Pechalno");
            }
        } catch (InterruptedException e) {
            /* ignored */
        }

    }

    public static void main(String[] args) {
        if (args == null || args.length != 5) {
            System.err.println("Need 5 args");
            return;
        }
        try {
            new HelloUDPClient().run(args[0], Integer.parseInt(args[1]), args[2], Integer.parseInt(args[3]), Integer.parseInt(args[4]));
        } catch (NumberFormatException e) {
            System.err.println("Can't process in number");
        }
    }
}
