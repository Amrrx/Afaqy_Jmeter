/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * Name: BCETCPSampler
 * <p>
 * Description:
 * <p>
 * Date: 11/8/2021
 * <p>
 * Author: Amr Aly
 * <p>
 * Contributers: Hisham Ismail, Hossam Mohamed
 * <p>
 * Mail: amr.ali@afaqy.com
 **/

package org.apache.jmeter.protocol.tcp.sampler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.util.JOrphanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TCPClient implementation.
 * Reads data until the defined EOM byte is reached.
 * If there is no EOM byte defined, then reads until
 * the end of the stream is reached.
 * The EOM byte is defined by the property "tcp.BinaryTCPClient.eomByte".
 *
 * <p>Input/Output strings are passed as hex-encoded binary strings.</p>
 */
public class BCETCPSampler extends AbstractTCPClient {
    private static final Logger log = LoggerFactory.getLogger(BCETCPSampler.class);

    private static final int EOM_INT = JMeterUtils.getPropDefault("tcp.BinaryTCPClient.eomByte", 1000); // $NON_NLS-1$
    private int index = 0;
    public BCETCPSampler() {
        super();
        setEolByte(EOM_INT);
        if (useEolByte) {
            log.info("Using eomByte={}", eolByte);
        }
    }

    /**
     * Convert hex string to binary byte array.
     *
     * @param hexEncodedBinary - hex-encoded binary string
     * @return Byte array containing binary representation of input hex-encoded string
     * @throws IllegalArgumentException if string is not an even number of hex digits
     */
    public static byte[] hexStringToByteArray(String hexEncodedBinary) {
        if (hexEncodedBinary.length() % 2 == 0) {
            char[] sc = hexEncodedBinary.toCharArray();
            byte[] ba = new byte[sc.length / 2];

            for (int i = 0; i < ba.length; i++) {
                int nibble0 = Character.digit(sc[i * 2], 16);
                int nibble1 = Character.digit(sc[i * 2 + 1], 16);
                if (nibble0 == -1 || nibble1 == -1){
                    throw new IllegalArgumentException(
                            "Hex-encoded binary string contains an invalid hex digit in '"+sc[i * 2]+sc[i * 2 + 1]+"'");
                }
                ba[i] = (byte) ((nibble0 << 4) | nibble1);
            }

            return ba;
        } else {
            throw new IllegalArgumentException(
                    "Hex-encoded binary string contains an uneven no. of digits");
        }
    }

    /**
     * Input (hex) string is converted to binary and written to the output stream.
     * @param os output stream
     * @param hexEncodedBinary hex-encoded binary
     */
    @Override
    public void write(OutputStream os, String hexEncodedBinary) throws IOException{
        log.info(String.valueOf(index));
        String message = buildTheSignalMessage(Long.parseLong(hexEncodedBinary));
        if (index == 0){
            message = "23424345230d0a" + message;
        }
        os.write(hexStringToByteArray(message));
        os.flush();
        index++;
        if(log.isDebugEnabled()) {
            log.debug("Wrote: " + hexEncodedBinary);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(OutputStream os, InputStream is) {
        throw new UnsupportedOperationException(
                "Method not supported for Length-Prefixed data.");
    }

    @Deprecated
    public String read(InputStream is) throws ReadException {
        log.warn("Deprecated method, use read(is, sampleResult) instead");
        return read(is, new SampleResult());
    }

    /**
     * Reads data until the defined EOM byte is reached.
     * If there is no EOM byte defined, then reads until
     * the end of the stream is reached.
     * Response data is converted to hex-encoded binary
     * @return hex-encoded binary string
     * @throws ReadException when reading fails
     */
    @Override
    public String read(InputStream is, SampleResult sampleResult) throws ReadException {
        ByteArrayOutputStream w = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[4096];
            int x = 0;
            boolean first = true;
            while ((x = is.read(buffer)) > -1) {
                if (first) {
                    sampleResult.latencyEnd();
                    first = false;
                }
                w.write(buffer, 0, x);
                if (useEolByte && (buffer[x - 1] == eolByte)) {
                    break;
                }
            }
        } catch (SocketTimeoutException e) {
            if (useEolByte) {
                throw new ReadException("Socket timed out while looking for EOM", e,
                        JOrphanUtils.baToHexString(w.toByteArray()));
            }
            log.debug("Ignoring SocketTimeoutException, as we are not looking for EOM", e);
        } catch (IOException e) {
            throw new ReadException("Problems while trying to read", e, JOrphanUtils.baToHexString(w.toByteArray()));
        }
        final String hexString = JOrphanUtils.baToHexString(w.toByteArray());
        if(log.isDebugEnabled()) {
            log.debug("Read: {}\n{}", w.size(), hexString);
        }
        return hexString;
    }

    private static String buildTheSignalMessage(Long unitImei) {
        log.info("IMEI----> " + unitImei);
        String newImei = hexEncodedIMEI(unitImei);
        String newTime = getCurrTimeMessage();
        String newMessage = generateNewMessage(newImei, newTime);
        byte[] bytes = hexStringToByteArray(newMessage);
        ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(bytes);
        buffer.flip();
        String checkSum = hexEncodedIMEI(checksum(buffer, bytes.length - 2)).substring(0, 2);
        return newMessage.substring(0, newMessage.length() - 2) + checkSum;
    }

    private static String generateNewMessage(String heximei, String hextime) {
        //IMEI 2b00a56628  TIME 478c21cf ab4072f41c429076aa41001c2c1a00d30c764cd3805e6e930b3110a401039a0865a70037d6
        // "%s 2b00a56628 %s ab4072f41c429076aa41001c2c1a00d30c764cd3805e6e930b3110a401039a0865a70037d6";
        String rawMessage = "%s5b01a55344%sffc302a000804000d74f3b4299ddc541001875540200000000d78000009a375100e8481b005a1000000000a401046856f55b002600000000000000000000000044f798b9bfffc302a000804000d74f3b4299ddc541001875540200000000d6800000a2375e00114936005a1000000000a401046856f55b0026000000000000000000000000440799b9bfffc302a000804000d74f3b4299ddc541001975540200000000d6800000bd375e002c4936005a1000000000a401046856f55b0026000000000000000000000000442799b9bfffc302a000804000d74f3b4299ddc541001775540200000000d7800000fa365e00394836005a1000000000a401046856f55b0026000000000000000000000000443799b9bfffc302a000804000d74f3b4299ddc541001875540200000000d7800000533743008a4828005a1000000000a401046856f55b00260000000000000000000000005b";
        return String.format(rawMessage, heximei, hextime);
    }

    private static String hexEncodedIMEI(long Imei) {
        StringBuilder buf = new StringBuilder();
        ByteBuffer byteBuffer = ByteBuffer.allocate(8);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.putLong(Imei);
        int i = 0;
        for (byte b : byteBuffer.array()) {
            String hex = Integer.toHexString((int) (0xff & (Imei >> i)));
            if (hex.length() == 1) {
                buf.append('0');
            }
            buf.append(hex);
            i += 8;
        }
        return buf.toString().toUpperCase();
    }

    private static String getCurrTimeMessage() {
        long currUnixTime = System.currentTimeMillis() / 1000L;
        long unixToTime = (currUnixTime - 0x47798280) / 2;
        String binaryTime = Long.toBinaryString(unixToTime) + "0111";
        Long decimalTime = toUnsignedInt(binaryToDecimal(binaryTime));
        String hexTime = hexEncodedIMEI(decimalTime).substring(0, 8);
        return hexTime;
    }

    private static int checksum(ByteBuffer buf, int end) {
        byte result = 0;
        for (int i = 0; i < end; i++) {
            result += buf.get(i);
        }
        return result;
    }

    public static long toUnsignedInt(int x) {
        return (x & 0xFFFFFFFFL);
    }

    static int binaryToDecimal(String n) {
        String num = n;
        int dec_value = 0;

        // Initializing base value to 1,
        // i.e 2^0
        int base = 1;

        int len = num.length();
        for (int i = len - 1; i >= 0; i--) {
            if (num.charAt(i) == '1')
                dec_value += base;
            base = base * 2;
        }

        return dec_value;
    }
}
