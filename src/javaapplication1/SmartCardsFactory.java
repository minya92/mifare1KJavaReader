package javaapplication1;

import javax.smartcardio.ATR;
import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;

public class SmartCardsFactory {
    
    private final static int MIFARE_1K = 0x01;
    private final static int MIFARE_4K = 0x02;
    private final static int MIFARE_ULTRALIGHT = 0x03;
    private final static int MIFARE_MINI = 0X26;

    public static SmartCard getCard(CardTerminal reader, Protocol protocol) throws CardException {
        Card card = reader.connect(protocol.getName());
        ATR atr = card.getATR();
        
        switch (getCardType(atr)) {
            case MIFARE_1K: {
                return new Mifare1K(reader, protocol);
            }
            case MIFARE_4K: {
            }
            case MIFARE_ULTRALIGHT: {
            }
            case MIFARE_MINI: {
            }
        }
        
        return null;
    }
    
    private static int getCardType(ATR atr) {
        int result = 0;
        result = result | (atr.getBytes()[13] << 8);
        result = result | atr.getBytes()[14];
        return result;
    }
}
