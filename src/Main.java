import java.awt.*;
import java.util.Scanner;
import javax.swing.*;
import com.fazecast.jSerialComm.*;

public class Main {
    private static Label lblValue, lblStatus;
    private static JSlider sliderSpeed;

    public static void main(String[] args) {
        //Create window
        JFrame window = new JFrame();
        window.setSize(400, 250);
        window.setLayout(null);
        window.setLocationRelativeTo(null);
        window.setResizable(false);

        //List of ports
        SerialPort[] ports = SerialPort.getCommPorts();

        JLabel lblSelectPort = new JLabel("Select encoder port: ");
        lblSelectPort.setBounds(20, 20, 150, 20);
        window.add(lblSelectPort);

        JComboBox<Object> comboPorts = new JComboBox<>();
        comboPorts.setBounds(20,40,150,20);
        //Populates combo
        for (SerialPort port : ports) {
            comboPorts.addItem(port.getSystemPortName());
        }
        comboPorts.addActionListener(e -> {
            SerialPort port = findPort((String) comboPorts.getSelectedItem(), ports);
            try {
                start(port);
            } catch (AWTException awtException) {
                awtException.printStackTrace();
            }
        });
        window.add(comboPorts);

        lblStatus = new Label("Select encoder port: ");
        lblStatus.setBounds(20, 60, 150, 20);
        window.add(lblStatus);

        JLabel lblSetSpeed = new JLabel("Set encoder speed: ");
        lblSetSpeed.setBounds(200, 20, 150, 20);
        window.add(lblSetSpeed);

        sliderSpeed = new JSlider();
        sliderSpeed.setBounds(200, 45, 180, 60);
        sliderSpeed.setPaintTrack(true);
        sliderSpeed.setPaintTicks(true);
        sliderSpeed.setMajorTickSpacing(25);
        sliderSpeed.setMinorTickSpacing(10);
        sliderSpeed.setPaintLabels(true);
        window.add(sliderSpeed);

        JLabel lblUpdatedValue = new JLabel("Encoder value: ");
        lblUpdatedValue.setBounds(20, 90, 150, 20);
        window.add(lblUpdatedValue);

        lblValue = new Label("0", SwingConstants.CENTER);
        lblValue.setFont(new Font("Arial", Font.BOLD, 50));
        lblValue.setBounds(20, 120, 400, 80);
        window.add(lblValue);

        window.setVisible(true);

        comboPorts.setSelectedItem("COM3");

        SerialPort port = findPort((String) comboPorts.getSelectedItem(), ports);
        try {
            start(port);
        } catch (AWTException awtException) {
            awtException.printStackTrace();
        }
    }

    public static SerialPort findPort(String selectedPort, SerialPort[] ports){
        int i = 0;
        for (SerialPort port : ports) {
            if(port.getSystemPortName().equals(selectedPort)) break;
            i++;
        }
        return ports[i];
    }

    private static void start(SerialPort serialPort) throws AWTException {
        if(serialPort.openPort()) {
            lblStatus.setForeground(Color.green);
            lblStatus.setText("Port opened successfully.");
            System.out.println(lblStatus.getText());
        }
        else {
            lblStatus.setForeground(Color.red);
            lblStatus.setText("Unable to open the port.");
            System.out.println(lblStatus.getText());
            return;
        }
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 0, 0);

        Robot robot = new Robot();
        Scanner data = new Scanner(serialPort.getInputStream());
        int value = 0;

        int baseValue = 0;
        while(true){
            int beforeValue = value;

            try{value = Integer.parseInt(data.nextLine());}catch(Exception e){
                System.out.println(e.getMessage());
            }

            int speedValue = sliderSpeed.getValue() - 50;
            int limit = speedValue*6000/50;

            if(beforeValue != value && ((baseValue - value) >= limit)){
                baseValue = value;
                robot.mouseWheel(beforeValue > value ? -1 : 1);
            }
            lblValue.setText(String.valueOf(value));
        }
    }
}