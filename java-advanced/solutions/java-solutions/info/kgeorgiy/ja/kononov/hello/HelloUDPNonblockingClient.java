package info.kgeorgiy.ja.kononov.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;

public class HelloUDPNonblockingClient implements HelloClient {
    private class Data {
        private final ByteBuffer buffer;
        private int threadNum;
        private int requests = 1;

        private Data(ByteBuffer buffer, int threadNum) {
            this.buffer = buffer;
            this.threadNum = threadNum;
        }
    }

    @Override
    public void run(String host, int port, String prefix, int threads, int requests) {
        SocketAddress socketAddress = new InetSocketAddress(host, port);
        ByteBuffer buffer = ByteBuffer.allocate(1000);
        try (Selector selector = Selector.open()) {
            createThreadChannels(threads, socketAddress, selector);
            while (!Thread.interrupted() && !selector.keys().isEmpty()) {
                selector.select(1000);
                selector.selectedKeys().forEach(selectionKey -> {
                    if (selectionKey.isValid()) {
                        Data data = (Data) selectionKey.attachment();
                        DatagramChannel channel = (DatagramChannel) selectionKey.channel();
                        if (selectionKey.isReadable()) {
                            /*String outMessage = prefix + data.threadNum + "_" + data.requests;
                            buffer.clear().put(outMessage.getBytes(StandardCharsets.UTF_8));
                            buffer.flip();

                             */
                            buffer.clear();
                            try {
                                channel.receive(buffer);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            buffer.flip();
                            String inMessage = convertBufferToString(buffer);
                            String outMessage = prefix + data.threadNum + "_" + data.requests;
                            buffer.clear().put(outMessage.getBytes(StandardCharsets.UTF_8));
                            if (inMessage.contains(outMessage)) {
                                System.out.println(inMessage);
                                data.requests++;
                            }
                            if (data.requests == requests) {
                                try {
                                    selectionKey.channel().close();
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            } else{
                                selectionKey.interestOps(SelectionKey.OP_WRITE);
                            }

                        } if (selectionKey.isWritable()){
                            String outMessage = prefix + data.threadNum + "_" + data.requests;
                            buffer.clear().put(outMessage.getBytes(StandardCharsets.UTF_8)).flip();
                            try {
                                if (channel.send(buffer, socketAddress) != 0){
                                    System.out.println(outMessage);
                                    selectionKey.interestOps(SelectionKey.OP_READ);
                                }
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                });
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private String convertBufferToString(ByteBuffer buffer) {
        return StandardCharsets.UTF_8.decode(buffer).toString();
    }

    private void createThreadChannels(int threads, SocketAddress socketAddress, Selector selector) throws IOException {
        for (int i = 0; i < threads; i++) {
            DatagramChannel channel = DatagramChannel.open();
            channel.configureBlocking(false);
            channel.connect(socketAddress);
            channel.register(selector, SelectionKey.OP_WRITE, new Data(ByteBuffer.allocate(1024), i));

        }
    }
}
