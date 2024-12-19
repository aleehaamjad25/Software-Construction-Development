import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

class Message {
    private String sender;
    private String recipient;
    private String text;
    private Date timestamp;

    public Message(String sender, String recipient, String text) {
        this.sender = sender;
        this.recipient = recipient;
        this.text = text;
        this.timestamp = new Date();
    }

    @Override
    public String toString() {
        return "[" + timestamp + "] " + sender + ": " + text;
    }
}

public class ChatAppGUI {
    private static List<String> usernames = new ArrayList<>();
    private static List<String> passwords = new ArrayList<>();
    private static Map<String, List<Message>> chats = new HashMap<>();
    private static Set<String> onlineUsers = new HashSet<>();

    private JFrame frame;
    private JTextArea chatArea;
    private JTextField inputField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JList<String> userList;
    private DefaultListModel<String> userListModel;
    private JPanel cardPanel;
    private CardLayout cardLayout;
    private String currentUser;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChatAppGUI::new);
    }

    public ChatAppGUI() {
        createAndShowGUI();
    }

    private void createAndShowGUI() {
        frame = new JFrame("Chat App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setMinimumSize(new Dimension(800, 600));

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        JPanel loginPanel = createLoginPanel();
        JPanel chatPanel = createChatPanel();

        cardPanel.add(loginPanel, "Login");
        cardPanel.add(chatPanel, "Chat");

        frame.getContentPane().add(cardPanel);
        frame.setVisible(true);
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel titleLabel = new JLabel("Welcome to Chat App");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Username:"), gbc);

        usernameField = new JTextField(15);
        gbc.gridx = 1;
        panel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Password:"), gbc);

        passwordField = new JPasswordField(15);
        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(e -> login());
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(loginButton, gbc);

        JButton signUpButton = new JButton("Sign Up");
        signUpButton.addActionListener(e -> signUp());
        gbc.gridx = 1;
        panel.add(signUpButton, gbc);

        return panel;
    }

    private JPanel createChatPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        inputField = new JTextField();
        inputField.setFont(new Font("Arial", Font.PLAIN, 14));
        inputField.addActionListener(e -> sendMessage());
        panel.add(inputField, BorderLayout.SOUTH);

        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userList.setFont(new Font("Arial", Font.PLAIN, 14));
        userList.setBorder(BorderFactory.createTitledBorder("Users"));
        userList.addListSelectionListener(e -> displayUserStatus());
        JScrollPane userScrollPane = new JScrollPane(userList);
        userScrollPane.setPreferredSize(new Dimension(150, 0));
        panel.add(userScrollPane, BorderLayout.EAST);

        JLabel headerLabel = new JLabel("Chat Room", JLabel.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(headerLabel, BorderLayout.NORTH);

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> logout());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(logoutButton);
        panel.add(buttonPanel, BorderLayout.NORTH);

        return panel;
    }

    private void login() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        int index = usernames.indexOf(username);
        if (index != -1 && passwords.get(index).equals(password)) {
            if (onlineUsers.contains(username)) {
                JOptionPane.showMessageDialog(frame, "User is already logged in.", "Login Failed", JOptionPane.WARNING_MESSAGE);
                return;
            }
            currentUser = username;
            onlineUsers.add(username);
            refreshUserList();
            switchToChatPanel();
        } else {
            JOptionPane.showMessageDialog(frame, "Invalid username or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void signUp() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (usernames.contains(username)) {
            JOptionPane.showMessageDialog(frame, "Username already exists.", "Sign Up Failed", JOptionPane.ERROR_MESSAGE);
        } else {
            usernames.add(username);
            passwords.add(password);
            chats.put(username, new ArrayList<>());
            JOptionPane.showMessageDialog(frame, "Sign-up successful. Please log in.");
        }
    }

    private void sendMessage() {
        String messageText = inputField.getText().trim();
        if (messageText.isEmpty()) return;

        String recipient = userList.getSelectedValue();
        if (recipient == null) {
            recipient = JOptionPane.showInputDialog(
                frame,
                "Enter the username of the recipient:",
                "Select Recipient",
                JOptionPane.QUESTION_MESSAGE
            );

            if (recipient == null || recipient.trim().isEmpty()) {
                JOptionPane.showMessageDialog(frame, "No recipient selected.", "No Recipient", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (!usernames.contains(recipient)) {
                JOptionPane.showMessageDialog(frame, "The username does not exist.", "Invalid Recipient", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        Message message = new Message(currentUser, recipient, messageText);
        chats.get(currentUser).add(message);
        chats.get(recipient).add(message);

        chatArea.append(message + "\n");
        inputField.setText("");
    }

    private void displayUserStatus() {
        String username = userList.getSelectedValue();
        if (username != null) {
            String status = onlineUsers.contains(username) ? "Online" : "Offline";
            JOptionPane.showMessageDialog(frame, username + " is " + status, "User Status", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void refreshUserList() {
        userListModel.clear();
        for (String username : usernames) {
            userListModel.addElement(username);
        }
    }

    private void switchToChatPanel() {
        refreshUserList();
        chatArea.setText("");
        List<Message> userChats = chats.get(currentUser);
        for (Message message : userChats) {
            chatArea.append(message + "\n");
        }
        cardLayout.show(cardPanel, "Chat");
    }

    private void logout() {
        if (currentUser != null) {
            onlineUsers.remove(currentUser);
            currentUser = null;
        }
        chatArea.setText("");
        inputField.setText("");
        cardLayout.show(cardPanel, "Login");
    }
}
