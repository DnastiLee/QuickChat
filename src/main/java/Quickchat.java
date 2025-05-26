import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class QuickChat {
    
    // Track all messages in the system
    private static List<ChatMessage> messageHistory = new ArrayList<>();
    private static int nextMessageId = 1; // Start counting messages from 1

    // Represents a single chat message with all its details
    static class ChatMessage {
        private final String id;
        private final String recipientPhone;
        private final String textContent;
        private final String uniqueHash;
        private boolean isDelivered;

        public ChatMessage(String phoneNumber, String messageText) {
            this.id = generateUniqueId();
            this.recipientPhone = phoneNumber;
            this.textContent = messageText;
            this.uniqueHash = generateMessageHash();
            this.isDelivered = false;
        }

        // Validates the message ID format
        public boolean hasValidId() {
            return this.id != null && this.id.length() == 10;
        }

        // Checks if phone number is properly formatted
        public boolean hasValidPhoneNumber() {
            Pattern phonePattern = Pattern.compile("^\\+?\\d{10,12}$");
            return phonePattern.matcher(this.recipientPhone).matches();
        }

        // Creates a unique fingerprint for the message
        private String generateMessageHash() {
            String[] words = this.textContent.split("\\s+");
            String firstWord = words.length > 0 ? words[0] : "";
            String lastWord = words.length > 1 ? words[words.length - 1] : firstWord;
            
            return String.format("%s:%s:%s%s", 
                   this.id.substring(0, 2), 
                   this.id.substring(2),
                   firstWord, 
                   lastWord).toUpperCase();
        }

        // Handles user choice for message disposition
        public String processUserChoice() {
            String[] options = {"Send Now", "Save for Later", "Cancel Message"};
            int choice = JOptionPane.showOptionDialog(null,
                "What would you like to do with this message?",
                "Message Options",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

            switch (choice) {
                case 0: // Send Now
                    this.isDelivered = true;
                    return "sent";
                case 1: // Save for Later
                    return "saved";
                case 2: // Cancel Message
                    return "canceled";
                default:
                    return "saved";
            }
        }

        // Formats message details for display
        public String getFormattedDetails() {
            return String.format(
                "Message ID: %s\n" +
                "Unique Hash: %s\n" +
                "To: %s\n" +
                "Content: %s\n" +
                "Status: %s",
                this.id, this.uniqueHash, this.recipientPhone, 
                this.textContent, this.isDelivered ? "Delivered" : "Pending");
        }

        // Standard getters
        public String getId() { return id; }
        public String getPhoneNumber() { return recipientPhone; }
        public String getContent() { return textContent; }
        public String getHash() { return uniqueHash; }
        public boolean isSent() { return isDelivered; }
    }

    // Application workflow methods
    public static void main(String[] args) {
        welcomeUser();
        
        if (!authenticateUser()) {
            showErrorMessage("Login failed. Exiting application.");
            return;
        }

        runMainMenu();
    }

    private static void welcomeUser() {
        JOptionPane.showMessageDialog(null, 
            "Welcome to QuickChat Messenger!", 
            "QuickChat", 
            JOptionPane.INFORMATION_MESSAGE);
    }

    private static boolean authenticateUser() {
        JTextField usernameInput = new JTextField();
        JPasswordField passwordInput = new JPasswordField();
        
        Object[] loginFields = {
            "Username:", usernameInput,
            "Password:", passwordInput
        };

        int response = JOptionPane.showConfirmDialog(null, 
            loginFields, 
            "Login to QuickChat", 
            JOptionPane.OK_CANCEL_OPTION);
        
        return response == JOptionPane.OK_OPTION && 
               !usernameInput.getText().isEmpty() && 
               passwordInput.getPassword().length > 0;
    }

    private static void runMainMenu() {
        while (true) {
            String[] menuOptions = {
                "Compose New Message", 
                "View Message History", 
                "Exit QuickChat"
            };
            
            int selection = JOptionPane.showOptionDialog(null,
                "What would you like to do?",
                "Main Menu",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                menuOptions,
                menuOptions[0]);

            switch (selection) {
                case 0:
                    createNewMessage();
                    break;
                case 1:
                    displayMessageHistory();
                    break;
                case 2:
                    exitApplication();
                    return;
                default:
                    break;
            }
        }
    }

    private static void createNewMessage() {
        // Get recipient details
        String phoneNumber = JOptionPane.showInputDialog(
            "Enter recipient's phone number (10-12 digits, + optional):");
        
        if (phoneNumber == null) return; // User canceled

        // Get message content
        String messageText = JOptionPane.showInputDialog(
            "Type your message (250 characters max):");
        
        if (messageText == null || messageText.length() > 250) {
            showErrorMessage("Messages must be 250 characters or less.");
            return;
        }

        // Create and validate message
        ChatMessage newMessage = new ChatMessage(phoneNumber, messageText);
        
        if (!newMessage.hasValidPhoneNumber()) {
            showErrorMessage("Please enter a valid 10-12 digit phone number.");
            return;
        }

        // Process user's choice
        String actionResult = newMessage.processUserChoice();
        
        if (actionResult.equals("canceled")) {
            return;
        }

        // Save and notify
        saveMessage(newMessage);
        
        if (newMessage.isSent()) {
            showSuccessMessage("Message sent successfully!\n\n" + 
                newMessage.getFormattedDetails());
        }

        showMessageStats();
    }

    private static void displayMessageHistory() {
        if (messageHistory.isEmpty()) {
            showInformationMessage("No messages in your history yet.");
            return;
        }

        StringBuilder history = new StringBuilder();
        for (ChatMessage msg : messageHistory) {
            history.append(msg.getFormattedDetails()).append("\n\n");
        }

        JOptionPane.showMessageDialog(null, 
            history.toString(), 
            "Your Message History", 
            JOptionPane.INFORMATION_MESSAGE);
    }

    private static void saveMessage(ChatMessage message) {
        messageHistory.add(message);
        // In a real app, we'd also save to database/file here
    }

    private static void showMessageStats() {
        long sentCount = messageHistory.stream().filter(ChatMessage::isSent).count();
        
        JOptionPane.showMessageDialog(null, 
            String.format(
                "Message Statistics:\n\n" +
                "Total Messages: %d\n" +
                "Sent Messages: %d",
                messageHistory.size(), sentCount),
            "QuickChat Stats", 
            JOptionPane.INFORMATION_MESSAGE);
    }

    private static void exitApplication() {
        JOptionPane.showMessageDialog(null, 
            "Thank you for using QuickChat. Have a great day!", 
            "Goodbye", 
            JOptionPane.INFORMATION_MESSAGE);
    }

    // Helper methods for consistent messaging
    private static void showErrorMessage(String text) {
        JOptionPane.showMessageDialog(null, text, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private static void showSuccessMessage(String text) {
        JOptionPane.showMessageDialog(null, text, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private static void showInformationMessage(String text) {
        JOptionPane.showMessageDialog(null, text, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    // Generates a unique 10-digit ID for each message
    private static String generateUniqueId() {
        return String.format("%010d", nextMessageId++);
    }
}