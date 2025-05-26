import javax.swing.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class QuickChatTest {
    
    // Test data from requirements
    private static final String TEST_PHONE_1 = "+27718693002";
    private static final String TEST_MESSAGE_1 = "Hi Mike, can you join us for dinner tonight";
    private static final String TEST_PHONE_2 = "08575975889";
    private static final String TEST_MESSAGE_2 = "Hi Keegan, did you receive the payment?";
    private static final String LONG_MESSAGE = "This is a very long message that exceeds the 250 character limit. "
            + "This is a very long message that exceeds the 250 character limit. "
            + "This is a very long message that exceeds the 250 character limit. "
            + "This is a very long message that exceeds the 250 character limit. "
            + "This is a very long message that exceeds the 250 character limit."; // 280 chars
    
    @Test
    public void testMessageLengthValidation_Success() {
        QuickChat.Message message = new QuickChat.Message(TEST_PHONE_1, TEST_MESSAGE_1);
        String result = validateMessageLength(message.getContent());
        assertEquals("Message ready to send.", result);
    }
    
    @Test
    public void testMessageLengthValidation_Failure() {
        String result = validateMessageLength(LONG_MESSAGE);
        assertTrue(result.startsWith("Message exceeds 250 characters by"));
        assertTrue(result.contains("please reduce size."));
    }
    
    private String validateMessageLength(String message) {
        if (message.length() > 250) {
            int excess = message.length() - 250;
            return String.format("Message exceeds 250 characters by %d, please reduce size.", excess);
        }
        return "Message ready to send.";
    }
    
    @Test
    public void testPhoneNumberValidation_Success() {
        QuickChat.Message message1 = new QuickChat.Message(TEST_PHONE_1, TEST_MESSAGE_1);
        QuickChat.Message message2 = new QuickChat.Message(TEST_PHONE_2, TEST_MESSAGE_2);
        
        assertEquals("Cell phone number successfully captured.", validatePhoneNumber(message1.getRecipientNumber()));
        assertEquals("Cell phone number successfully captured.", validatePhoneNumber(message2.getRecipientNumber()));
    }
    
    @Test
    public void testPhoneNumberValidation_Failure() {
        String[] invalidNumbers = {"123", "abcdefghij", "+271", "0821234567890"};
        
        for (String number : invalidNumbers) {
            assertEquals("Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.", 
                        validatePhoneNumber(number));
        }
    }
    
    private String validatePhoneNumber(String number) {
        Pattern pattern = Pattern.compile("^\\+?\\d{10,12}$");
        Matcher matcher = pattern.matcher(number);
        return matcher.matches() 
                ? "Cell phone number successfully captured."
                : "Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.";
    }
    
    @Test
    public void testMessageHashGeneration() {
        QuickChat.Message message = new QuickChat.Message(TEST_PHONE_1, TEST_MESSAGE_1);
        String expectedHash = message.getMessageId().substring(0, 2) + ":" + 
                             message.getMessageId().substring(2) + ":HITONIGHT";
        
        assertEquals(expectedHash.toUpperCase(), message.getMessageHash());
    }
    
    @Test
    public void testMessageIDGeneration() {
        QuickChat.Message message = new QuickChat.Message(TEST_PHONE_1, TEST_MESSAGE_1);
        assertTrue(message.checkMessageID());
        assertEquals(10, message.getMessageId().length());
        System.out.println("Message ID generated: " + message.getMessageId());
    }
    
    @Test
    public void testMessageActions() {
        // Test Send action
        QuickChat.Message sendMessage = new QuickChat.Message(TEST_PHONE_1, TEST_MESSAGE_1);
        assertEquals("sent", simulateUserAction(sendMessage, 0));
        assertEquals("Message successfully sent.", getActionResultMessage("sent"));
        assertTrue(sendMessage.isSent());
        
        // Test Store action
        QuickChat.Message storeMessage = new QuickChat.Message(TEST_PHONE_1, TEST_MESSAGE_1);
        assertEquals("stored", simulateUserAction(storeMessage, 1));
        assertEquals("Message successfully stored.", getActionResultMessage("stored"));
        assertFalse(storeMessage.isSent());
        
        // Test Discard action
        QuickChat.Message discardMessage = new QuickChat.Message(TEST_PHONE_1, TEST_MESSAGE_1);
        assertEquals("discarded", simulateUserAction(discardMessage, 2));
        assertEquals("Press 0 to delete message.", getActionResultMessage("discarded"));
        assertFalse(discardMessage.isSent());
    }
    
    private String simulateUserAction(QuickChat.Message message, int action) {
        // 0 = Send, 1 = Store, 2 = Discard
        switch (action) {
            case 0: 
                message.sent = true;
                return "sent";
            case 1: 
                return "stored";
            case 2: 
                return "discarded";
            default: 
                return "stored";
        }
    }
    
    private String getActionResultMessage(String action) {
        switch (action) {
            case "sent": return "Message successfully sent.";
            case "discarded": return "Press 0 to delete message.";
            case "stored": return "Message successfully stored.";
            default: return "Action not recognized.";
        }
    }
    
    @Test
    public void testMessageStorage() {
        QuickChat quickChat = new QuickChat();
        QuickChat.Message message = new QuickChat.Message(TEST_PHONE_1, TEST_MESSAGE_1);
        
        quickChat.storeMessage(message);
        assertEquals(1, quickChat.returnTotalMessages());
        
        String allMessages = quickChat.printMessages();
        assertTrue(allMessages.contains(message.getMessageId()));
        assertTrue(allMessages.contains(message.getRecipientNumber()));
        assertTrue(allMessages.contains(message.getContent()));
    }
}