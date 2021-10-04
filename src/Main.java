import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.Timer;
import javax.swing.*;
import com.fazecast.jSerialComm.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;


import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class Main {
    private static final String XLM_FILE = "./config.xml";
    public static Label lblValue, lblStatus, lblSetSpeed;
    public static JSlider sliderSpeed;
    public static int limitThreshold, sliderSize;
    private static TrayIcon trayIcon = null;
    static SystemTray tray = SystemTray.getSystemTray();
    private static JFrame window;
    private static  Element eElement;
    private static JComboBox<Object> comboPorts;

    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException {
        //Create window
        window = new JFrame();
        window.setSize(400, 250);
        window.setLayout(null);
        window.setLocationRelativeTo(null);
        window.setResizable(false);
        window.setTitle("Configuração do encoder");
        Image icon = Toolkit.getDefaultToolkit().getImage("./img/icon.png");
        window.setIconImage(icon);
        window.addWindowListener(new WindowAdapter() {// Window close event
            public void windowClosing(WindowEvent e) {
                window.setVisible(false);
                miniTray();
            }

            public void windowIconified(WindowEvent e) {// Window minimized event
                window.setVisible(false);
                miniTray();
            }
        });

        InputStream file = new FileInputStream(XLM_FILE);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(file);
        doc.getDocumentElement().normalize();
        eElement = doc.getDocumentElement();

        limitThreshold = Integer.parseInt(eElement.getElementsByTagName("limitThreshold").item(0).getTextContent());

        String defaultPort = eElement.getElementsByTagName("port").item(0).getTextContent();
        int defaultSpeed = Integer.parseInt(eElement.getElementsByTagName("speed").item(0).getTextContent());
        sliderSize = Integer.parseInt(eElement.getElementsByTagName("sliderSize").item(0).getTextContent());

        doc.cloneNode(true);

        //List of ports
        SerialPort[] ports = SerialPort.getCommPorts();

        JLabel lblSelectPort = new JLabel("Selecione a porta: ");
        lblSelectPort.setBounds(20, 20, 150, 20);
        window.add(lblSelectPort);

        comboPorts = new JComboBox<>();
        comboPorts.setBounds(20,40,150,20);
        // Populates combo
        for (SerialPort port : ports) {
            comboPorts.addItem(port.getSystemPortName());
        }
        comboPorts.addActionListener(e -> {
            SerialPort port = findPort((String) comboPorts.getSelectedItem(), ports);
            try {
                save(doc);
                start(port);
            } catch (AWTException | FileNotFoundException awtException) {
                awtException.printStackTrace();
            }
        });
        window.add(comboPorts);

        lblStatus = new Label("");
        lblStatus.setBounds(20, 60, 150, 20);
        window.add(lblStatus);

        sliderSpeed = new JSlider();
        sliderSpeed.setBounds(200, 45, 180, 60);
        sliderSpeed.setPaintTrack(true);
        sliderSpeed.setPaintTicks(true);
        sliderSpeed.setMajorTickSpacing(25);
        sliderSpeed.setMinorTickSpacing(10);
        sliderSpeed.setPaintLabels(true);
        sliderSpeed.setValue(defaultSpeed);
        sliderSpeed.setMaximum(sliderSize);
        sliderSpeed.addChangeListener(e -> {
            lblSetSpeed.setText("Velocidade: " + sliderSpeed.getValue());
            save(doc);
        });
        window.add(sliderSpeed);

        lblSetSpeed = new Label("Velocidade: " + sliderSpeed.getValue());
        lblSetSpeed.setBounds(200, 20, 150, 20);
        window.add(lblSetSpeed);

        JLabel lblUpdatedValue = new JLabel("Valor do encoder: ");
        lblUpdatedValue.setBounds(20, 90, 150, 20);
        window.add(lblUpdatedValue);

        lblValue = new Label("0", SwingConstants.CENTER);
        lblValue.setFont(new Font("Arial", Font.BOLD, 50));
        lblValue.setBounds(20, 120, 400, 80);
        window.add(lblValue);



        window.setVisible(true);

        comboPorts.setSelectedItem(defaultPort);

        SerialPort port = findPort((String) comboPorts.getSelectedItem(), ports);
        try {
            save(doc);
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

    private static void save(Document doc){
        eElement.getElementsByTagName("port").item(0).setTextContent((String) comboPorts.getSelectedItem());
        eElement.getElementsByTagName("speed").item(0).setTextContent(String.valueOf(sliderSpeed.getValue()));
        eElement.getElementsByTagName("sliderSize").item(0).setTextContent(String.valueOf(sliderSize));
        eElement.getElementsByTagName("limitThreshold").item(0).setTextContent(String.valueOf(limitThreshold));
        try{
            writeXml(doc);
        } catch (IOException | TransformerException e) {
            e.printStackTrace();
        }
    }

    private static void start(SerialPort serialPort) throws AWTException, FileNotFoundException {
        if(serialPort.openPort()) {
            lblStatus.setForeground(Color.green);
            lblStatus.setText("Porta aberta com sucesso.");
            System.out.println(lblStatus.getText());
        }
        else {
            lblStatus.setForeground(Color.red);
            lblStatus.setText("Não foi possível abrir a porta solicita.");
            System.out.println(lblStatus.getText());
            return;
        }
        serialPort.setComPortParameters(9600, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
       // serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 0, 0);

        //Runtime.getRuntime().addShutdownHook(new Thread(serialPort::closePort));

        //var timer = new Timer();
        var timedSchedule = new TimerScheduleHandler();

        //timer.schedule(timedSchedule, 0, 1);
        serialPort.addDataListener(timedSchedule);
    }

    public static void scroll(boolean up) throws AWTException {
        Robot robot = new Robot();
        robot.mouseWheel(up ? -1 : 1);
    }

    private static void miniTray() {// Minimize the window to the taskbar tray
        ImageIcon trayImg = new ImageIcon("./img/icon.png");// Tray icon

        trayIcon = new TrayIcon(trayImg.getImage(), "Configuração do encoder", new PopupMenu());
        trayIcon.setImageAutoSize(true);
        trayIcon.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    tray.remove(trayIcon);
                    window.setVisible(true);
                    window.setExtendedState(JFrame.NORMAL);
                    window.toFront();
                }
            }
        });

        try {
            tray.add(trayIcon);
        } catch (AWTException e1) {
            e1.printStackTrace();
        }
    }

    // write doc to output stream
    private static void writeXml(Document doc) throws TransformerException, UnsupportedEncodingException {
        Transformer tf = TransformerFactory.newInstance().newTransformer();
        tf.setOutputProperty(OutputKeys.METHOD, "xml");
        tf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        DOMSource domSource = new DOMSource(doc);
        StreamResult sr = new StreamResult(new File(XLM_FILE));
        tf.transform(domSource, sr);
    }
}