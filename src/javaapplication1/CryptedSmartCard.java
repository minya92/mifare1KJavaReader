package javaapplication1;

import javax.crypto.Cipher;

public abstract class CryptedSmartCard implements SmartCard{
    
    private Cipher encryptor;
    private Cipher decryptor;
   
    @Override
    public Cipher getEncrypter() {
        return encryptor;
    }
    
    @Override
    public void setEncrypter(Cipher encryptor) {
        this.encryptor = encryptor;
    }
    
    @Override
    public boolean hasEncryptor() {
        return encryptor != null;
    }
    
    @Override
    public Cipher getDecrypter() {
        return decryptor;
    }

    @Override
    public void setDecrypter(Cipher decryptor) {
        this.decryptor = decryptor;
    }
    
    @Override
    public boolean hasDecryptor() {
        return decryptor != null;
    }
}
