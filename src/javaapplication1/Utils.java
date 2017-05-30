package javaapplication1;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
//import org.apache.commons.io.IOUtils;
import java.nio.charset.StandardCharsets;
import java.lang.String;
import java.io.InputStream;
import java.io.IOException;
import java.io.DataInputStream;
import java.util.Scanner;

public class Utils {
    public static byte[] InputStreamToBytesArray(InputStream is) {
        try{
            int length = is.available();
            byte[] data = new byte[length];
            is.read(data);
            return data;
        } catch(IOException e) {
            return new byte[16];
        }
        
        
    }

    public static String InputStreamToString(InputStream is){
        String result;
        try(Scanner s = new Scanner(is).useDelimiter("\\A")) {            
            result = s.hasNext() ? s.next() : "";
        }
        return result;
    }
}
