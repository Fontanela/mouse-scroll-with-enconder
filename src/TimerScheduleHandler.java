import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.TimerTask;
import com.fazecast.jSerialComm.*;

public class TimerScheduleHandler extends TimerTask implements SerialPortDataListener{

    private int count = 0, baseValue = 0,  intValue = 0;

    // Constructor
    public TimerScheduleHandler() {
    }

    // Override run method on TimerTask
    @Override
    public void run(){
    }

    @Override
    public int getListeningEvents(){
        return SerialPort.LISTENING_EVENT_DATA_RECEIVED;
    }

    @Override
    public void serialEvent(SerialPortEvent serialPortEvent) {
        boolean go = false;
        byte[] initialData = serialPortEvent.getReceivedData();
        byte[] data;
        int bInt = intValue;

        data = initialData;
        try {
            intValue = Integer.parseInt(new String(data, StandardCharsets.UTF_8).replace("\r\n", ""));

            go = true;
        } catch (NumberFormatException ignored) {
        }

        if (go) {
            int speedValue = Main.sliderSpeed.getValue() - Main.sliderSize;
            int limit = speedValue * Main.limitThreshold / Main.sliderSize;

            Main.lblValue.setText((intValue > 0 ? count-- : count++) + "");
            if (( Math.abs(baseValue - count) >= Math.abs(limit))) {
                baseValue = count;
                try {
                    Main.scroll(!(intValue > 0));
                } catch (AWTException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}