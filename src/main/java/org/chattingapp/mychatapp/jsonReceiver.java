package org.chattingapp.mychatapp;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;

/**
 * NIO-based client for sending/receiving encrypted JSON
 * Uses non-blocking I/O with Selector (same as server)
 *
 * Protocol:
 * - Send: [4 bytes length][8 bytes groupId][encrypted data]
 * - Receive: Same format or ACK message
 *
 * Usage: java NIOClient <clientName>
 */
public class jsonReceiver {

    private static final int HEADER_SIZE = 4;

    private SocketChannel socketChannel;
    private Selector selector;
    private String clientName;
    private volatile boolean running = true;
    private MessageBuffer messageBuffer;

    public jsonReceiver(String clientName) {
        this.clientName = clientName;
        this.messageBuffer = new MessageBuffer();
    }

    /**
     * Connect to server using NIO
     */
    public void connect(String host, int port) throws IOException {
        // Open selector and socket channel
        selector = Selector.open();
        socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);

        // Register for CONNECT event
        socketChannel.register(selector, SelectionKey.OP_CONNECT);

        // Initiate connection
        socketChannel.connect(new InetSocketAddress(host, port));

        printConnectionBanner();

        // Start event loop in separate thread
        Thread eventLoopThread = new Thread(this::eventLoop);
        eventLoopThread.setDaemon(true);
        eventLoopThread.start();
    }

    /**
     * NIO event loop - handles all I/O operations
     */
    private void eventLoop() {
        try {
            while (running) {
                // Wait for events (timeout 1 second)
                selector.select(1000);

                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();

                    if (!key.isValid()) continue;

                    try {
                        if (key.isConnectable()) {
                            handleConnect(key);
                        } else if (key.isReadable()) {
                            handleRead(key);
                        } else if (key.isWritable()) {
                            handleWrite(key);
                        }
                    } catch (IOException e) {
                        System.err.println("❌ [" + clientName + "] I/O error: " + e.getMessage());
                        key.cancel();
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("❌ [" + clientName + "] Event loop error: " + e.getMessage());
        }
    }

    /**
     * Handle connection completion
     */
    private void handleConnect(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();

        if (channel.finishConnect()) {
            System.out.println("✓ [" + clientName + "] Connected to server\n");

            // Switch to read mode
            key.interestOps(SelectionKey.OP_READ);
        }
    }

    /**
     * Handle incoming data from server
     */
    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(8192);

        int bytesRead = channel.read(buffer);

        if (bytesRead == -1) {
            // Server closed connection
            System.out.println("[" + clientName + "] Server closed connection\n");
            close();
            return;
        }

        if (bytesRead > 0) {
            buffer.flip();
            messageBuffer.append(buffer);

            // Extract and process complete messages
            byte[] completeMessage;
            while ((completeMessage = messageBuffer.extractMessage()) != null) {
                handleReceivedMessage(completeMessage);
            }
        }
    }

    /**
     * Handle write events (currently unused, writes are immediate)
     */
    private void handleWrite(SelectionKey key) throws IOException {
        // For future use if implementing write queue
    }

    /**
     * Send encrypted group JSON to server (non-blocking)
     */
    public void sendEncryptedGroup(byte[] encryptedJson, long groupId) throws IOException {
        System.out.println("📤 [" + clientName + "] Sending encrypted group: " + groupId);
        System.out.println("   Encrypted size: " + encryptedJson.length + " bytes");

        // Build message: [length][groupId][encrypted data]
        int messageSize = 8 + encryptedJson.length;
        ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE + messageSize);

        // Write: [4 bytes length][8 bytes groupId][encrypted data]
        buffer.putInt(messageSize);
        buffer.putLong(groupId);
        buffer.put(encryptedJson);
        buffer.flip();

        // Write to channel (non-blocking)
        while (buffer.hasRemaining()) {
            socketChannel.write(buffer);
        }

        System.out.println("   ✓ Sent successfully\n");
    }

    /**
     * Handle different types of received messages
     */
    private void handleReceivedMessage(byte[] messageData) {
        try {
            // Extract groupId (first 8 bytes)
            ByteBuffer bb = ByteBuffer.wrap(messageData);
            long groupId = bb.getLong();

            if (groupId == -1L) {
                // ACK message
                long ackedGroupId = bb.getLong();
                System.out.println("✓ [" + clientName + "] ACK received for group: " + ackedGroupId + "\n");
            } else {
                // Encrypted group data
                byte[] encryptedJson = Arrays.copyOfRange(messageData, 8, messageData.length);

                System.out.println("📥 [" + clientName + "] Received encrypted group: " + groupId);
                System.out.println("   Size: " + encryptedJson.length + " bytes");
            }
        } catch (Exception e) {
            System.err.println("❌ [" + clientName + "] Error handling message: " + e.getMessage() + "\n");
        }
    }

    /**
     * Close connection and cleanup
     */
    public void close() {
        running = false;
        try {
            if (socketChannel != null && socketChannel.isOpen()) {
                socketChannel.close();
                System.out.println("[" + clientName + "] Disconnected\n");
            }
            if (selector != null && selector.isOpen()) {
                selector.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }

    /**
     * Format JSON for display (truncate if too long)
     */
    private String formatJson(String json) {
        if (json.length() > 300) {
            return json.substring(0, 300) + "\n   ... (truncated)";
        }
        return json;
    }

    private void printConnectionBanner() {
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║   " + String.format("%-35s", clientName + " Connecting...") + "    ║");
        System.out.println("║   Using NIO (Non-blocking I/O)        ║");
        System.out.println("╚════════════════════════════════════════╝\n");
    }

    /**
     * Helper class to buffer and extract complete messages
     */
    static class MessageBuffer {
        private ByteBuffer buffer = ByteBuffer.allocate(16384);

        public void append(ByteBuffer data) {
            // Expand buffer if needed
            if (buffer.remaining() < data.remaining()) {
                ByteBuffer newBuffer = ByteBuffer.allocate(buffer.capacity() * 2);
                buffer.flip();
                newBuffer.put(buffer);
                buffer = newBuffer;
            }
            buffer.put(data);
        }

        public byte[] extractMessage() {
            buffer.flip();

            // Need at least header (4 bytes)
            if (buffer.remaining() < HEADER_SIZE) {
                buffer.compact();
                return null;
            }

            // Read message length
            int messageLength = buffer.getInt();

            // Check if complete message available
            if (buffer.remaining() < messageLength) {
                // Incomplete - reset and wait for more data
                buffer.position(buffer.position() - HEADER_SIZE);
                buffer.compact();
                return null;
            }

            // Extract complete message
            byte[] message = new byte[messageLength];
            buffer.get(message);

            buffer.compact();
            return message;
        }
    }

    /**
     * Demo usage with NIO client
     */
    public void receiveJsonData(String code) {
        try {
            String clientName = "Client-1";

            long groupId = 8487619624L;

            // Create NIO client and connect
            jsonReceiver client = new jsonReceiver(clientName);
            client.connect("localhost", 12345);

            // Wait for connection to complete
            Thread.sleep(2000);

            // Encrypt and send
            byte[] bytes = code.getBytes(StandardCharsets.UTF_8);
            client.sendEncryptedGroup(bytes, groupId);

            // Keep client running to receive messages
            System.out.println("Listening for messages... (Press Ctrl+C to exit)\n");

            // Add shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nShutting down...");
                client.close();
            }));

            // Keep main thread alive
            Thread.sleep(Long.MAX_VALUE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        msgEncodeDecode obj2 = new msgEncodeDecode();
        obj2.setUpCode("12345678");
        String code = obj2.symmetricEncrypt("Tanishq");
        byte[] bytes = code.getBytes(StandardCharsets.UTF_8);
        jsonReceiver obj = new jsonReceiver("client-A");
        obj.receiveJsonData(Arrays.toString(bytes));
    }
}
