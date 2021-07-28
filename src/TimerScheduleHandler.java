import java.awt.*;
import java.nio.charset.StandardCharsets;
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
        if (serialPortEvent.getEventType() == SerialPort.LISTENING_EVENT_DATA_RECEIVED) {
            boolean go = false;

            byte[] initialData = serialPortEvent.getReceivedData();
            byte[] data;

            try {
                if(initialData.length > 4){
                    data = getSliceOfArray(initialData, 0, 4);
                }else{
                    data = initialData;
                }

                intValue = Integer.parseInt(new String(data, StandardCharsets.UTF_8).replace("\r\n", ""));
                go = true;
            } catch (NumberFormatException ignored) {
            }

            if (go) {
                int speedValue = Main.sliderSpeed.getValue() - 100;
                int limit = speedValue * 100 / 100;

                Main.lblValue.setText((intValue > 0 ? count++ : count--) + "");
                if (( Math.abs(baseValue - count) >= Math.abs(limit))) {
                    baseValue = count;
                    try {
                        Main.scroll(intValue > 0);
                    } catch (AWTException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static byte[] getSliceOfArray(byte[] arr, int start, int end)
    {
        // Get the slice of the Array
        byte[] slice = new byte[end - start];

        // Copy elements of arr to slice
        System.arraycopy(arr, start, slice, 0, slice.length);

        // return the slice
        return slice;
    }
}