import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.io.FileWriter;
import java.io.IOException;

public class QuickChat {
    private static int messageCounter = 1;
    private static List<Message> allMessages = new ArrayList<>();

    static class Message {
        private String messageId;
        private String recipientNumber;
        private String content;
        private String messageHash;
        private boolean sent;

        public Message(String recipientNumber, String content) {
            this.messageId = generateMessageId();
            this.recipientNumber = recipientNumber;
            this.content = content;
            this.messageHash = createMessageHash();
            this.sent = false;
        }

        public boolean checkMessageID() {
            return this.messageId != null && this.messageId.length() == 10;
        }

        public int checkRecipientCell() {
            Pattern pattern = Pattern.compile("^\\+?\\d{10,12}$");
            Matcher matcher = pattern.matcher(this.recipientNumber);
            return matcher.matches() ? 1 : 0;
        }

        public String createMessageHash() {
            String[] words = this.content.split("\\s+");
            String firstWord = words.length > 0 ? words[0] : "";
            String lastWord = words.length > 1 ? words[words.length - 1] : firstWord;
            
            return (this.messageId.substring(0, 2) + ":" + 
                   (messageId.substring(2)) + ":" + 
                   firstWord + lastWord).toUpperCase();
        }

        public String sentMessage() {
            Object[] options = {"Send", "Store", "Discard"};
            int choice = JOptionPane.showOptionDialog(null,
                "Choose an action for this message:",
                "Message Options",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

            switch (choice) {
                case 0: // Send
                    this.sent = true;
                    return "sent";
                case 1: // Store
                    return "stored";
                case 2: // Discard
                    return "discarded";
                default:
                    return "stored";
            }
        }

        public String printMessage() {
            return "Message ID: " + this.messageId + "\n" +
                   "Message Hash: " + this.messageHash + "\n" +
                   "Recipient: " + this.recipientNumber + "\n" +
                   "Message: " + this.content + "\n" +
                   "Status: " + (this.sent ? "Sent" : "Stored");
        }

        public String getMessageId() { return messageId; }
        public String getRecipientNumber() { return recipientNumber; }
        public String getContent() { return content; }
        public String getMessageHash() { return messageHash; }
        public boolean isSent() { return sent; }
    }

    public static String printMessages() {
        StringBuilder sb = new StringBuilder();
        for (Message msg : allMessages) {
            sb.append(msg.printMessage()).append("\n\n");
        }
        return sb.toString();
    }

    public static int returnTotalMessages() {
        return allMessages.size();
    }

    public static int returnTotalSentMessages() {
        int count = 0;
        for (Message msg : allMessages) {
            if (msg.isSent()) count++;
        }
        return count;
    }

    public static void storeMessage(Message message) {
        allMessages.add(message);
    }

    public static void storeMessageToJson(Message message) {
        JSONObject messageJson = new JSONObject();
        messageJson.put("messageId", message.getMessageId());
        messageJson.put("recipientNumber", message.getRecipientNumber());
        messageJson.put("content", message.getContent());
        messageJson.put("messageHash", message.getMessageHash());
        messageJson.put("sent", message.isSent());

        JSONArray messagesArray = new JSONArray();
        messagesArray.add(messageJson);

        try (FileWriter file = new FileWriter("messages.json", true)) {
            file.write(messagesArray.toJSONString());
            file.write("\n");
            file.flush();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, 
                "Error saving message to file.", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private static String generateMessageId() {
        return String.format("%010d", messageCounter++);
    }

    private static boolean login() {
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        Object[] message = {
            "Username:", usernameField,
            "Password:", passwordField
        };

        int option = JOptionPane.showConfirmDialog(null, message, "Login", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            return !usernameField.getText().isEmpty() && passwordField.getPassword().length > 0;
        }
        return false;
    }

    private static void sendMessageFlow() {
        String recipientNumber = JOptionPane.showInputDialog("Enter recipient's phone number (10-12 digits with optional +):");
        if (recipientNumber == null) return;

        String content = JOptionPane.showInputDialog("Enter your message (max 250 characters):");
        if (content == null || content.length() > 250) {
            JOptionPane.showMessageDialog(null, 
                "Please enter a message of less than 250 characters.", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        Message message = new Message(recipientNumber, content);

        if (message.checkRecipientCell() == 0) {
            JOptionPane.showMessageDialog(null, 
                "Invalid phone number. Please enter a 10-12 digit number with optional +.", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        String status = message.sentMessage();

        if (status.equals("discarded")) {
            return;
        }

        storeMessage(message);
        storeMessageToJson(message);

        if (message.isSent()) {
            JOptionPane.showMessageDialog(null, 
                message.printMessage(), 
                "Message Sent", 
                JOptionPane.INFORMATION_MESSAGE);
        }

        JOptionPane.showMessageDialog(null, 
            "Total messages: " + returnTotalMessages() + "\n" +
            "Sent messages: " + returnTotalSentMessages(), 
            "Message Count", 
            JOptionPane.INFORMATION_MESSAGE);
    }

    private static void showRecentMessages() {
        if (allMessages.isEmpty()) {
            JOptionPane.showMessageDialog(null, 
                "No messages sent yet.", 
                "Recent Messages", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(null, 
            printMessages(), 
            "Recent Messages", 
            JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        JOptionPane.showMessageDialog(null, 
            "Welcome to QuickChat.", 
            "QuickChat", 
            JOptionPane.INFORMATION_MESSAGE);

        if (!login()) {
            JOptionPane.showMessageDialog(null, 
                "Login failed. Exiting application.", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        while (true) {
            String[] options = {"Send Message", "Show Recent Messages", "Quit"};
            int choice = JOptionPane.showOptionDialog(null,
                "Main Menu:",
                "QuickChat",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

            switch (choice) {
                case 0:
                    sendMessageFlow();
                    break;
                case 1:
                    showRecentMessages();
                    break;
                case 2:
                    JOptionPane.showMessageDialog(null, 
                        "Thank you for using QuickChat. Goodbye!", 
                        "QuickChat", 
                        JOptionPane.INFORMATION_MESSAGE);
                    return;
                default:
                    break;
            }
        }
    }
}

