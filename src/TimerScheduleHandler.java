import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import com.fazecast.jSerialComm.*;

public class TimerScheduleHandler implements SerialPortDataListener{
    private int count = 0, baseValue = 0,  intValue = 0, beforeIntValue = 0;
    private String values = "";
    int speedValue = Main.sliderSpeed.getValue() - Main.sliderSize;
    int limit = speedValue * Main.limitThreshold / Main.sliderSize;

    // Constructor
    public TimerScheduleHandler() {
    }

    @Override
    public int getListeningEvents(){
        return SerialPort.LISTENING_EVENT_DATA_RECEIVED;
    }

    @Override
    public void serialEvent(SerialPortEvent serialPortEvent) {
        byte[] initialData = serialPortEvent.getReceivedData();// Data that comes from serial event
        String temporaryValue = "";// Temporary treated value

        try {
            values += new String(initialData);
            while (values.contains("\n")) {
                String[] treatedValue = values.split("\\n", 2);
                values = (treatedValue.length > 1) ? treatedValue[1] : "";
                temporaryValue = treatedValue[0];
            }
            intValue = Integer.parseInt(new String(temporaryValue.trim()));
            int dif = beforeIntValue - intValue;

            for(int i = 0; i < Math.abs(dif); i++){
                if (( Math.abs(baseValue - count) >= Math.abs(limit))) {
                    baseValue = count;
                    try {
                        Main.scroll(!(dif > 0));
                    } catch (AWTException e) {
                        e.printStackTrace();
                    }
                }
            }
            beforeIntValue = intValue;
            Main.lblValue.setText(((beforeIntValue - intValue) > 0 ? count-- : count++) + "");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}