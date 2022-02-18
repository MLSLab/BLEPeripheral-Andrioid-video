/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.camera2videowithble;

import java.util.HashMap;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class UartGattAttributes {
    private static HashMap<String, String> attributes = new HashMap();

    //Service UUID to expose our UART characteristics
    public static String UART_SERVICE = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";

    //RX, Write characteristic
    public static String RX_WRITE_CHAR = "6e400002-b5a3-f393-e0a9-e50e24dcca9e";

    //TX READ Notify
    public static String TX_READ_CHAR = "6e400003-b5a3-f393-e0a9-e50e24dcca9e";
    public static String TX_READ_CHAR_DESC = "00002902-0000-1000-8000-00805f9b34fb";

    static {
        // Services.
        attributes.put(UART_SERVICE, "Uart Service");
        // Characteristics.
        attributes.put(RX_WRITE_CHAR, "RX_WRITE_CHAR");
        attributes.put(TX_READ_CHAR, "TX_READ_CHAR_DESC");
        attributes.put(TX_READ_CHAR_DESC, "TX_READ_CHAR_DESC");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
