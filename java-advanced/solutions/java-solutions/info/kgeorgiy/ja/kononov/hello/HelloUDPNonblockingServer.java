package info.kgeorgiy.ja.kononov.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HelloUDPNonblockingServer implements HelloServer {
    Selector selector;
    DatagramChannel channel;
    ExecutorService thread;
    int socketBufferSize;

    private class Data {
        private ByteBuffer buffer;
        private SocketAddress address;
        private Data(ByteBuffer buffer) {
            this.buffer = buffer;
        }
    }

    private boolean init(int port, int threads) {
        try {
            selector = Selector.open();
        } catch (IOException e) {
            /* ignored */
        }
        if (selector == null) {
            return false;
        }
        if ((channel = createDatagramChannel(selector, port)) == null) {
            return false;
        }
        if (thread != null) {
            thread = Executors.newSingleThreadExecutor();
        }
        try {
            socketBufferSize = channel.socket().getReceiveBufferSize();
        } catch (SocketException e) {
            return false;
        }
        return true;
    }

    private DatagramChannel createDatagramChannel(Selector selector, int port) {
        try {
            DatagramChannel channel = DatagramChannel.open();
            channel.configureBlocking(false);
            InetSocketAddress socketAddress = new InetSocketAddress(port);
            channel.bind(socketAddress);
            channel.register(selector, SelectionKey.OP_READ);
            return channel;
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public void start(int port, int threads) {
        if (!init(port, threads)) {
            close();
            return;
        }
        ByteBuffer buffer = ByteBuffer.allocate(1000);
        while (!Thread.interrupted() && !selector.keys().isEmpty() && channel.isOpen()) {
            try {
                selector.select(1000);
                selector.selectedKeys().forEach(selectionKey -> getAndSend(selectionKey, buffer));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }

    private void getAndSend(SelectionKey key, ByteBuffer buffer) {
        if (key == null || !key.isValid()) {
            return;
        }

        buffer.clear();
        if (key.isReadable()) {
            try {
                Data data = new Data(buffer);
                SocketAddress address = channel.receive(buffer);
                buffer.flip();
                String outMessage = "Hello, " + StandardCharsets.UTF_8.decode(buffer);
                //Data data = (Data) key.attachment();
                data.address = address;
                buffer.flip();
                data.buffer = buffer.put(outMessage.getBytes(StandardCharsets.UTF_8));
                key.interestOps(SelectionKey.OP_WRITE);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (key.isWritable()){
            Data data = (Data) key.attachment();
            data.buffer.flip();
            try {
                channel.send(data.buffer, data.address);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            key.interestOps(SelectionKey.OP_READ);
        }
    }

    @Override
    public void close() {
        if (selector != null){
            try {
                selector.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (channel != null) {
            try {
                channel.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (thread != null){
            thread.shutdown();
            try {
                thread.awaitTermination(1000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
