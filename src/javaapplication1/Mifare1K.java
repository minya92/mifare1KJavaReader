/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javaapplication1;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.Long;
import java.lang.String;
import java.math.BigInteger;
import java.util.Arrays;
import javax.smartcardio.ATR;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.Cipher;

public class Mifare1K extends CryptedSmartCard {

    private final int BLOCK_LENGTH = 16;

    private final CardTerminal reader;
    private Card card;
    private CardChannel chan;
    private final Protocol protocol;
    private int authKey;
    private int authBlock;
    private static final byte[] KEY_BYTES = initKey();

    private static byte[] initKey() {
        byte[] key = new byte[16];
        Arrays.fill(key, (byte) 0xFF);
        return key;
    }

    public Mifare1K(CardTerminal reader, Protocol protocol) {
        this.reader = reader;
        this.protocol = protocol;
    }

    /**
     *
     * @return @throws CardException
     */
    @Override
    public boolean connect() throws CardException {
        card = reader.connect(protocol.getName());
        if (card != null) {
            chan = card.getBasicChannel();
            if (chan != null) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isConnected() {
        return card != null;
    }

    /**
     *
     * @throws CardException
     */
    @Override
    public void disconnect() throws CardException {
        if (card != null) {
            card.disconnect(true);
            card = null;
        }
    }

    /**
     *
     * @return byte array with ATR bytes
     */
    @Override
    public byte[] getATR() {
        ATR atr = card.getATR();
        return atr.getBytes();
    }

    /**
     *
     * @return @throws CardException
     */
    @Override
    public long getUID() throws CardException {
        CommandAPDU apdu = new CommandAPDU(new byte[]{(byte) 0xFF, (byte) 0xCA, (byte) 0x00, (byte) 0x00, (byte) 0x00});
        ResponseAPDU res = chan.transmit(apdu);
        ISO7816Response isoResp = new ISO7816Response(res.getSW1(), res.getSW2());
        if (isoResp.isGood()) {
            long result = 0L;
            result = result | (res.getData()[0] << 24);
            result = result | (res.getData()[1] << 16);
            result = result | (res.getData()[2] << 8);
            result = result | res.getData()[3];
            return result;
        }
        return -1L;
    }

    @Override
    public BigInteger getUIDStr() throws CardException {
        CommandAPDU apdu = new CommandAPDU(new byte[]{(byte) 0xFF, (byte) 0xCA, (byte) 0x00, (byte) 0x00, (byte) 0x00});
        ResponseAPDU res = chan.transmit(apdu);
        byte[] data = res.getData();
        byte[] buff = new byte[data.length + 1];
        buff[0] = 0x00;
        for (int i = 1; i < buff.length; i++) {
            buff[i] = data[i - 1];
        }
        //di.read(buff, 1, length);
        BigInteger id = new BigInteger(buff);
        return id;

    }

    /**
     *
     * @param block
     * @param key
     * @return
     * @throws Exception
     */
    @Override
    public InputStream readBlock(int block, int key) throws Exception {
        return read(block, key, BLOCK_LENGTH);
    }

    public Cipher getDecrypt() throws Exception {
        byte[] keyBytes = {(byte) 0xAF, (byte) 0xAF, (byte) 0xAF, (byte) 0xAF,
            (byte) 0xAF, (byte) 0xAF, (byte) 0xAF, (byte) 0xAF,
            (byte) 0xAF, (byte) 0xAF, (byte) 0xAF, (byte) 0xAF,
            (byte) 0xAF, (byte) 0xAF, (byte) 0xAF, (byte) 0xAF};
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        // Create Cipher instance and initialize it to encrytion mode
        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");

        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        return cipher;
    }

    public Cipher getEncrypt() throws Exception {
        byte[] keyBytes = {(byte) 0xAF, (byte) 0xAF, (byte) 0xAF, (byte) 0xAF,
            (byte) 0xAF, (byte) 0xAF, (byte) 0xAF, (byte) 0xAF,
            (byte) 0xAF, (byte) 0xAF, (byte) 0xAF, (byte) 0xAF,
            (byte) 0xAF, (byte) 0xAF, (byte) 0xAF, (byte) 0xAF};
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        // Create Cipher instance and initialize it to encrytion mode
        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");

        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        return cipher;
    }

    public Cipher getCipher(String key) throws Exception {
        String[] splited = key.split("\\s+");
        byte[] keyBytes = new byte[splited.length];
        for (int i = 0; i < splited.length; i++) {
            keyBytes[i] = (byte) Integer.parseInt(splited[i]);
        }
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        // Create Cipher instance and initialize it to encrytion mode
        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");

        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        return cipher;
    }

    /**
     *
     * @param startBlock
     * @param key
     * @param numberOfBytes
     * @return
     * @throws Exception
     */
    @Override
    public InputStream read(int startBlock, int key, int numberOfBytes) throws Exception {
        if (numberOfBytes < 0 || numberOfBytes > 48) {
            throw new IllegalArgumentException("Number of bytes should be greater than 0 and less than 49.");
        }
        if (checkBlock(startBlock)) {
            if (authenticate(key, startBlock)) {
                ByteArrayOutputStream bytes = new ByteArrayOutputStream(5);
                bytes.write(0xFF);
                bytes.write(0xB0);
                bytes.write(0x00);
                bytes.write(startBlock);
                bytes.write(numberOfBytes);
                CommandAPDU apdu = new CommandAPDU(bytes.toByteArray());
                ResponseAPDU res = chan.transmit(apdu);
                ISO7816Response isoResp = new ISO7816Response(res.getSW1(), res.getSW2());
                byte[] data = res.getData();
                if (hasDecryptor()) {
                    data = getDecrypter().doFinal(data);
                }
                if (isoResp.isGood()) {
                    return new ByteArrayInputStream(data);
                }
            }
        }
        return new ByteArrayInputStream(new byte[]{});
    }

    /**
     *
     * @param startBlock
     * @param key
     * @param data
     * @return
     * @throws Exception
     */
    @Override
    public boolean write(int startBlock, int key, byte[] data) throws Exception {
        if (data == null || data.length < 0 || data.length > 48) {
            throw new IllegalArgumentException("Data should has lenght greater then 0 and less than 49.");
        }
        byte[] innerData = addAdditionalBytes(data);
        if (hasEncryptor()) {
            innerData = getEncrypter().doFinal(innerData);
        }
        if (checkBlock(startBlock)) {
            if (authenticate(key, startBlock)) {
                ByteArrayOutputStream bytes = new ByteArrayOutputStream(5 + innerData.length);
                bytes.write(0xFF);
                bytes.write(0xD6);
                bytes.write(0x00);
                bytes.write(startBlock);
                bytes.write(innerData.length);
                bytes.write(innerData, 0, innerData.length);
                CommandAPDU apdu = new CommandAPDU(bytes.toByteArray());
                ResponseAPDU res = chan.transmit(apdu);
                ISO7816Response isoResp = new ISO7816Response(res.getSW1(), res.getSW2());
                return isoResp.isGood();
            }
        }
        return false;
    }

    /**
     *
     * @param keyNumber
     * @param key
     * @return
     * @throws CardException
     */
    @Override
    public boolean updateKey(int keyNumber, byte[] key) throws CardException {
        if (checkKey(keyNumber)) {
            if (key == null || key.length != 6) {
                throw new IllegalArgumentException("Key should has length equals 6.");
            }
            ByteArrayOutputStream bytes = new ByteArrayOutputStream(11);
            bytes.write(0xFF);
            bytes.write(0x82);
            bytes.write(0X00);
            bytes.write(keyNumber);
            bytes.write(0x06);
            bytes.write(key, 0, key.length);
            CommandAPDU apdu = new CommandAPDU(bytes.toByteArray());
            ResponseAPDU res = chan.transmit(apdu);
            ISO7816Response isoResp = new ISO7816Response(res.getSW1(), res.getSW2());
            return isoResp.isGood();
        }
        return false;
    }

    protected byte[] addAdditionalBytes(byte[] data) {
        byte[] result;
        if (data.length % 16 > 0) {
            result = Arrays.copyOf(data, (data.length / 16 + 1) * 16);
            Arrays.fill(result, data.length, result.length, (byte) 0x00);
        } else if (data.length == 0) {
            result = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        } else {
            result = Arrays.copyOf(data, data.length);
        }
        return result;
    }

    private boolean authenticate(int key, int block) throws CardException {
        if (checkKey(key)) {
            if (authKey == key && authBlock == block) {
                return true;
            } else {
                ByteArrayOutputStream bytes = new ByteArrayOutputStream(10);
                bytes.write(0xFF);
                bytes.write(0x86);
                bytes.write(0x00);
                bytes.write(0x00);
                bytes.write(0x05);
                bytes.write(0x01);
                bytes.write(0x00);
                bytes.write(block);
                bytes.write(0x60);
                bytes.write(key);
                CommandAPDU apdu = new CommandAPDU(bytes.toByteArray());
                ResponseAPDU res = chan.transmit(apdu);
                ISO7816Response isoResp = new ISO7816Response(res.getSW1(), res.getSW2());
                if (isoResp.isGood()) {
                    authBlock = block;
                    authKey = key;
                }
                return isoResp.isGood();
            }
        }
        return false;
    }

    private boolean checkKey(int key) {
//        if (key < 0 || key > 0x1F) {
//            throw new IllegalArgumentException("Key number is wrong. It can be greater than 0 and less than 33");
//        }
        return true;
    }

    private boolean checkBlock(int block) {
        // if (block < 1 || block > 0x1F) {
        //     throw new IllegalArgumentException("Block number is wrong. It can be greater than 1 and less than 33");
        // }
        return true;
    }

}
