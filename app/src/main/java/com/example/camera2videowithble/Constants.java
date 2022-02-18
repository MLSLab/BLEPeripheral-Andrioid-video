package com.example.camera2videowithble;

import java.util.UUID;

public class Constants {
    // used to identify adding bluetooth names
    public final static int REQUEST_ENABLE_BT = 3054;
    // used to request fine location permission
    public final static int REQUEST_FINE_LOCATION = 3055;
    // scan period in milliseconds
    public final static int SCAN_PERIOD = 5000;

//    public static String SERVICE_STRING = "CB660002-4339-FF22-A1ED-DEBFED27BDB4";
//    public static final UUID SERVICE_UUID = UUID.fromString(SERVICE_STRING);
//    public static String CHARACTERISTIC_UUID = "CB660004-4339-FF22-A1ED-DEBFED27BDB4";
//    public static String CONFIG_UUID = "00005609-0000-1001-8080-00705c9b34cb";

    //    //Service UUID to expose our UART characteristics
    public static String UART_SERVICE = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";
    public static final UUID UART_SERVICE_UUID = UUID.fromString(UART_SERVICE);

    //RX, Write characteristic
    public static String RX_WRITE_CHAR = "6e400002-b5a3-f393-e0a9-e50e24dcca9e";

    //TX READ Notify
    public static String TX_READ_CHAR = "6e400003-b5a3-f393-e0a9-e50e24dcca9e";
    public static String TX_READ_CHAR_DESC = "00002902-0000-1000-8000-00805f9b34fb";
//
//    static {
//        // Services.
//        attributes.put(UART_SERVICE, "Uart Service");
//        // Characteristics.
//        attributes.put(RX_WRITE_CHAR, "RX_WRITE_CHAR");
//        attributes.put(TX_READ_CHAR, "TX_READ_CHAR_DESC");
//        attributes.put(TX_READ_CHAR_DESC, "TX_READ_CHAR_DESC");
//    }
//
//    public static String lookup(String uuid, String defaultName) {
//        String name = attributes.get(uuid);
//        return name == null ? defaultName : name;
//    }





}