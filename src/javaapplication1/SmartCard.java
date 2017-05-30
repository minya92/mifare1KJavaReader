package javaapplication1;

import java.io.InputStream;
import java.lang.String;
import javax.crypto.Cipher;
import javax.smartcardio.CardException;
import java.math.BigInteger;

public interface SmartCard {
    
    public boolean connect() throws CardException;
    
    public boolean isConnected();
    
    public void disconnect() throws CardException;
    
    public byte[] getATR();
    
    public long getUID() throws CardException;

    public BigInteger getUIDStr() throws CardException;

    public InputStream readBlock(int block, int key) throws Exception;
    
    public InputStream read(int startBlock, int key, int numberOfBytes) throws Exception;
    
    public boolean write(int startBlock, int key, byte[] data) throws Exception;
    
    public boolean updateKey(int block, byte[] key) throws CardException;
    
    public Cipher getEncrypter();
    
    public void setEncrypter(Cipher encryptor); 
    
    public boolean hasEncryptor();
    
    public Cipher getDecrypter();
    
    public void setDecrypter(Cipher decryptor); 
    
    public boolean hasDecryptor();
}