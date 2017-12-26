package doff.bt;

import android.util.Log;

import doff.chimney.Actuators;
import doff.chimney.Sensors;

public class Protocol {
    private Bluetooth bt = null;
    private Sensors sensors = null;
    private Actuators actuators = null;

    public Protocol(Bluetooth bt, Sensors sensors, Actuators actuators) {
        this.bt=bt;
        this.sensors=sensors;
        this.actuators=actuators;
    }

    public  final String Tx_Q = "Q"; //Quit operation, idle state"Q\n"

    public  final String Tx_B = "B"; //Battery test start: "B\n"
    public  final String Tx_b = "b"; //Battery test, request actual status  "b\n"
    public final String Rx_b = "b";  // "b <value>\n", 0 ladda batteri, 1 batteri ok

    public  final String Tx_L = "L"; //Start leakage measure transmitted: "L <value>\n"
    public  final String Tx_P = "P"; //Desired pressure transmitted: "P <value>\n"
    public  final String Tx_F = "F"; //Set fan motor signal transmitted: "F <value>\n"

    public  final String Tx_S = "S"; //Request all sensor signals values: "M\n"
    public  final String Rx_s = "s"; //Sensor signals received: "s <pressure><blank><flow>\n"
    public  final String Rx_a = "a"; //Lll signals received : "a <motor><blank><pressure><blank><flow>\n"

    public  void txQuit() {
        bt.send(Tx_Q + "\n");
    }

    public  boolean txBatteryTest() {
        return bt.send(Tx_B + "\n");
    }
    public  boolean txBatteryStatus() {
        return bt.send(Tx_b + "\n");
    }

    public  boolean txLeakageMeasure(int pressure) {
        return bt.send(Tx_L + " " + pressure + "\n");
    }
    public  boolean txGetSensorSignals() {
        return bt.send(Tx_S +"\n");
    }
    public  boolean txSetFanMotorSignal(int percent) {
        return bt.send(Tx_F + " " + percent + "\n" );
    }
    public  boolean txDesiredPressure(int pressure) {
        return bt.send(Tx_P + " " + pressure + "\n" );
    }

    public  void decode(String response) {
        String[] arr = response.split("\\s+");

        switch(arr[0])
        {
            case Rx_b:
                if ( arr.length == 2)
                {
                    Log.d("doff-bt", "Battery Test="+arr[1]);
                    sensors.battery=Integer.parseInt(arr[1]);
                    sensors.batteryCounter++;
                }
            case Rx_s:
                if ( arr.length == 3)
                {
                    Log.d("doff-bt", "Pressure="+arr[1]);
                    sensors.pressure=Double.parseDouble(arr[1]);
                    sensors.pressureCounter++;
                    Log.d("doff-bt", "Flow="+arr[2]);
                    sensors.flow = Double.parseDouble(arr[2]);
                    sensors.flowCounter++;
                }
                break;
        }
    }

}
