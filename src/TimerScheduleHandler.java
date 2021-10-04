import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import com.fazecast.jSerialComm.*;

public class TimerScheduleHandler implements SerialPortDataListener{
    private int count = 0, baseValue = 0,  intValue = 0, beforeIntValue = 0;
    private String messages = "";

    // Constructor
    public TimerScheduleHandler() {
    }

    @Override
    public int getListeningEvents(){
        return SerialPort.LISTENING_EVENT_DATA_RECEIVED;
    }

    @Override
    public void serialEvent(SerialPortEvent serialPortEvent) {
        byte[] initialData = serialPortEvent.getReceivedData();
        String cValue = "";

        try {
            messages += new String(initialData);
            while (messages.contains("\n")) {
                String[] message = messages.split("\\n", 2);
                messages = (message.length > 1) ? message[1] : "";
                cValue = message[0];
                //System.out.println("Message: " + message[0]);
            }
            intValue = Integer.parseInt(new String(cValue.trim()));
            System.out.println("Valor: " + intValue);
            /*System.out.println("Antes: " + beforeIntValue);
            System.out.println("Conta: " + Math.abs(beforeIntValue - intValue));
            System.out.println();*/

            for(int i = 0; i < Math.abs(beforeIntValue - intValue); i++){
                int speedValue = Main.sliderSpeed.getValue() - Main.sliderSize;
                int limit = speedValue * Main.limitThreshold / Main.sliderSize;

                Main.lblValue.setText(((beforeIntValue - intValue) > 0 ? count-- : count++) + "");
                if (( Math.abs(baseValue - count) >= Math.abs(limit))) {
                    baseValue = count;
                    try {
                        Main.scroll(!((beforeIntValue - intValue) > 0));
                    } catch (AWTException e) {
                        e.printStackTrace();
                    }
                }
            }
            beforeIntValue = intValue;
            //intValue = Integer.parseInt(new String(data, StandardCharsets.UTF_8).replace("\r\n", ""));
        } catch (NumberFormatException ignored) {
        }
    }
}