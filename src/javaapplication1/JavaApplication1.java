/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javaapplication1;

import java.math.BigInteger;
import java.util.Timer;
import java.util.TimerTask;
import javax.smartcardio.TerminalFactory;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.CardTerminal;
import java.lang.Thread;
/**
 *
 * @author lapsh
 */
public class JavaApplication1 {
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        BigInteger prevUid = BigInteger.valueOf(0);
        while(true){
            try {
                Thread.sleep(1000);
                TerminalFactory terminalFactory = TerminalFactory.getDefault();
                CardTerminals terminals = terminalFactory.terminals();
                CardTerminal term = terminals.list().get(1);
                SmartCard CARD = SmartCardsFactory.getCard(term, Protocol.AUTO);
                CARD.connect();
                BigInteger uid = CARD.getUIDStr();
                
                //System.out.println("prev: " + prevUid.toString() + " cur: " + uid.toString());
                
                if(uid.compareTo(prevUid) != 0){
                    prevUid = uid;
                    System.out.println("NEW CARD READED");
                    new ProcessBuilder("keys.exe", uid.toString()).start();
                }
                
            } catch (Exception e) {
                BigInteger uid = BigInteger.valueOf(0);
                prevUid = uid;
                System.out.println("Error! " + e.toString());
            }
        }
        
        
//        Timer timer = new java.util.Timer();
//        timer.schedule(tt, 1000);
        //System.out.println("Hello world");
    }
    
    static TimerTask tt = new TimerTask() { 

        @Override
        public void run() {
            try {
                TerminalFactory terminalFactory = TerminalFactory.getDefault();
                CardTerminals terminals = terminalFactory.terminals();
                CardTerminal term = terminals.list().get(1);
                SmartCard CARD = SmartCardsFactory.getCard(term, Protocol.AUTO);
               // BigInteger uid = CARD.getUIDStr();
                CARD.connect();
                System.out.println("CARD READED: " + CARD.getUIDStr().toString());
            } catch (Exception e) {
                System.out.println("Error! " + e.toString());
            }
            System.out.println("Hello world");
        }
    };
    
}
