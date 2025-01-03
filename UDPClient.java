import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPClient {
    private static final int SERVER_PORT = 12345;
    private static final String SERVER_HOST = "localhost";
    private JTextField inputField;
    private JTextArea textArea;
    private volatile boolean running = true;
    private DatagramSocket socket;
    private String username;

    public UDPClient() {
        JFrame frame = new JFrame("UDP Client");
        textArea = new JTextArea(20, 40);
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);

        inputField = new JTextField(30);

        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(this::sendMessage);

        JPanel inputPanel = new JPanel();
        inputPanel.add(inputField);
        inputPanel.add(sendButton);

        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(inputPanel, BorderLayout.SOUTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        username = JOptionPane.showInputDialog("Enter your username:");
        connectToServer();
        startReceiving();
    }

    private void connectToServer() {
        try {
            socket = new DatagramSocket();
            InetAddress serverAddress = InetAddress.getByName(SERVER_HOST);
            byte[] buffer = (username + " has joined the chat.").getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, serverAddress, SERVER_PORT);
            socket.send(packet);
        } catch (Exception e) {
            textArea.append("Error connecting to server: " + e.getMessage() + "\n");
        }
    }

    private void sendMessage(ActionEvent event) {
        String message = inputField.getText().trim();
        if (!message.isEmpty()) {
            try {
                String fullMessage = username + ": " + message;
                byte[] buffer = fullMessage.getBytes();
                InetAddress serverAddress = InetAddress.getByName(SERVER_HOST);
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, serverAddress, SERVER_PORT);
                socket.send(packet);
                textArea.append("You: " + message + "\n");
                inputField.setText("");
            } catch (Exception e) {
                textArea.append("Error: " + e.getMessage() + "\n");
            }
        }
    }

    private void startReceiving() {
        new Thread(() -> {
            try {
                byte[] buffer = new byte[1024];
                while (running) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    String message = new String(packet.getData(), 0, packet.getLength());
                    SwingUtilities.invokeLater(() -> textArea.append(message + "\n"));
                }
            } catch (Exception e) {
                if (running) {
                    SwingUtilities.invokeLater(() -> textArea.append("Error receiving: " + e.getMessage() + "\n"));
                }
            }
        }).start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(UDPClient::new);
    }
}