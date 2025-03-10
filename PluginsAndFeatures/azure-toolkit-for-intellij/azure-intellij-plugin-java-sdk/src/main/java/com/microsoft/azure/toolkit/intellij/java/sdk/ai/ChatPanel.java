package com.microsoft.azure.toolkit.intellij.java.sdk.ai;

import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public class ChatPanel extends JPanel {
    private final JTextArea chatHistory;
    private final JTextArea inputField;
    private final JButton sendButton;
    private final Project project;
    private final ExecutorService executorService;
    private boolean isResponding = false;
    private final HttpClient client;

    public ChatPanel(Project project) {
        this.executorService = Executors.newSingleThreadExecutor();

        this.project = project;
        setLayout(new BorderLayout());

        // Chat history area
        chatHistory = new JTextArea();
        chatHistory.setEditable(false);
        chatHistory.setLineWrap(true);
        chatHistory.setWrapStyleWord(true);
        JBScrollPane chatScrollPane = new JBScrollPane(chatHistory);
        add(chatScrollPane, BorderLayout.CENTER);

        // Input panel (text field + send button)
        JPanel inputPanel = new JPanel(new BorderLayout());

        // Text input area
        inputField = new JBTextArea(3, 20);
        inputField.setLineWrap(true);
        inputField.setWrapStyleWord(true);
        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && e.isControlDown()) {
                    sendMessage();
                    e.consume();
                }
            }
        });
        JBScrollPane inputScrollPane = new JBScrollPane(inputField);
        inputPanel.add(inputScrollPane, BorderLayout.CENTER);

        // Send button
        sendButton = new JButton("Send");
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        inputPanel.add(sendButton, BorderLayout.EAST);

        add(inputPanel, BorderLayout.SOUTH);

        // Welcome message
        chatHistory.append("Chat initialized. Type a message and press Send or Ctrl+Enter.\n\n");

        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(20))
                .build();
    }

    private void sendMessage() {
        String message = inputField.getText().trim();
        if (!message.isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            String timestamp = sdf.format(new Date());

            chatHistory.append("[" + timestamp + "] You: " + message + "\n\n");

            // Clear input field
            inputField.setText("");

            // Start streaming response
            connectToRealChatService(message);

            // Scroll to bottom of chat history
            scrollToBottom();
        }
    }

    private void streamResponse(String userMessage) {
        // Disable the input field and send button during response
        setInputEnabled(false);
        isResponding = true;

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String timestamp = sdf.format(new Date());

        // Add initial response indicator
        chatHistory.append("[" + timestamp + "] Assistant: ");

        // This would be where you connect to your actual chat service
        // For demonstration, we'll simulate streaming with a sample response

        executorService.submit(() -> {
            try {
                // Example response to stream
                String fullResponse = "I am an Azure assistant that can answer your queries on Azure. I can help with recommending Azure services, providing product catalog, writing code to integrate with Azure using Azure SDKs and troubleshoot issues with Azure.";

                // Stream the response character by character with random delays
                for (char c : fullResponse.toCharArray()) {
                    final char currentChar = c;
                    SwingUtilities.invokeLater(() -> {
                        chatHistory.append(String.valueOf(currentChar));
                        scrollToBottom();
                    });

                    // Random delay between 10-50ms to simulate streaming
                    Thread.sleep((long) (Math.random() * 40 + 10));
                }

                // Add a line break at the end
                SwingUtilities.invokeLater(() -> {
                    chatHistory.append("\n\n");
                    scrollToBottom();
                    setInputEnabled(true);
                    isResponding = false;
                });

            } catch (InterruptedException e) {
                SwingUtilities.invokeLater(() -> {
                    chatHistory.append("\n\n[Error: Response interrupted]\n\n");
                    scrollToBottom();
                    setInputEnabled(true);
                    isResponding = false;
                });
            }
        });
    }

    private void setInputEnabled(boolean enabled) {
        inputField.setEnabled(enabled);
        sendButton.setEnabled(enabled);
    }

    private void scrollToBottom() {
        chatHistory.setCaretPosition(chatHistory.getDocument().getLength());
    }

    // Method to implement real chat service integration with streaming
    public void connectToRealChatService(String userMessage) {
        // This is where you would implement your actual chat service integration
        // The implementation would depend on the specific API you're using

        // Example implementation structure:
        /*
        ChatServiceClient client = new ChatServiceClient();
        client.sendMessageWithStreaming(userMessage, new ChatServiceCallback() {
            @Override
            public void onToken(String token) {
                SwingUtilities.invokeLater(() -> {
                    handler.onToken(token);
                });
            }

            @Override
            public void onComplete() {
                SwingUtilities.invokeLater(() -> {
                    handler.onComplete();
                });
            }

            @Override
            public void onError(Exception e) {
                SwingUtilities.invokeLater(() -> {
                    handler.onError(e);
                });
            }
        });
        */



            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(createMessage("system", "You are a helpful assistant."));
            messages.add(createMessage("user", userMessage));


            String requestBody = buildRequestBody("llama3.2", messages, true);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:11434/api/chat"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            StringBuilder fullResponse = new StringBuilder();
            System.out.println("Starting to receive streaming response from Ollama...\n");

            // Use the BodyHandlers.ofLines() to process the response line by line
        executorService.submit(() -> {
            try {
                HttpResponse<Stream<String>> response = client.send(
                        request,
                        HttpResponse.BodyHandlers.ofLines());

                // Process each line in the stream
                response.body().forEach(line -> {
                    if (line.contains("\"done\":true")) {
                        SwingUtilities.invokeLater(() -> {
                            chatHistory.append("\n\n");
                            scrollToBottom();
                            setInputEnabled(true);
                            isResponding = false;
                        });
                        return;
                    }

                    if (line.contains("\"content\":")) {
                        String content = extractContent(line);
                        SwingUtilities.invokeLater(() -> {
                            chatHistory.append(content);
                            scrollToBottom();
                        });
                        fullResponse.append(content);
                    }
                });
                System.out.println("\n\nFull response received:");
                System.out.println(fullResponse.toString());
            } catch (Exception exception) {
                streamResponse(userMessage);
            }
        });
    }

    /**
     * Create a message map for the conversation history
     */
    private static Map<String, String> createMessage(String role, String content) {
        Map<String, String> message = new HashMap<>();
        message.put("role", role);
        message.put("content", content);
        return message;
    }

    private static String extractContent(String jsonLine) {
        int startIndex = jsonLine.indexOf("\"content\":") + 11;
        int endIndex = jsonLine.indexOf("\"", startIndex);
        if (startIndex >= 11 && endIndex > startIndex) {
            return jsonLine.substring(startIndex, endIndex);
        }
        return "";
    }

    private static String buildRequestBody(String model, List<Map<String, String>> messages, boolean stream) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"model\": \"").append(model).append("\",\n");
        json.append("  \"messages\": [\n");

        for (int i = 0; i < messages.size(); i++) {
            Map<String, String> message = messages.get(i);
            json.append("    {\n");
            json.append("      \"role\": \"").append(message.get("role")).append("\",\n");
            json.append("      \"content\": \"").append(message.get("content")).append("\"\n");
            json.append("    }");

            if (i < messages.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }

        json.append("  ],\n");
        json.append("  \"stream\": ").append(stream).append("\n");
        json.append("}");

        return json.toString();
    }

    // Interface for handling streaming responses
    public interface StreamingResponseHandler {
        void onToken(String token);
        void onComplete();
        void onError(Exception e);
    }

    // Clean up resources when panel is disposed
    public void dispose() {
        executorService.shutdown();
    }
}
