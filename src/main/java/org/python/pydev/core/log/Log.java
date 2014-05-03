/*
 * Created on 12/06/2005
 */
package org.python.pydev.core.log;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.python.pydev.core.FullRepIterable;


/**
 * @author Fabio
 */
public class Log {


    /**
     * @param errorLevel IStatus.[OK|INFO|WARNING|ERROR]
     */
    public static void log(int errorLevel, String message, Throwable e) {
        System.err.println(message);
    }

    public static void log(Throwable e) {
        log(0x04, e.getMessage() != null ? e.getMessage() : "No message gotten (null message).", e);
    }

    public static void log(String msg) {
        log(0x04, msg, new RuntimeException(msg));
    }

    
    //------------ Log that writes to a new console

    private final static Object lock = new Object(); 
    private final static StringBuffer logIndent = new StringBuffer();
    
    public static void toLogFile(Object obj, String string) {
        synchronized(lock){
            if(obj == null){
                obj = new Object();
            }
            Class<? extends Object> class1 = obj.getClass();
            toLogFile(string, class1);
        }
    }

    public static void toLogFile(String string, Class<? extends Object> class1) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(logIndent);
        buffer.append(FullRepIterable.getLastPart(class1.getName()));
        buffer.append(": ");
        buffer.append(string);
        
        toLogFile(buffer.toString());
    }

    private static void toLogFile(final String buffer) {
        final Runnable r = new Runnable(){

            public void run() {
                synchronized(lock){
                    try{
                        
                        //also print to console
                        System.out.println(buffer);
                        
//                IPath stateLocation = default1.getStateLocation().append("PydevLog.log");
//                String file = stateLocation.toOSString();
//                REF.appendStrToFile(buffer+"\r\n", file);
                    }catch(Throwable e){
                        log(e); //default logging facility
                    }
                }
                
            }
        };
        
    }
    
    
    
    public static void toLogFile(Exception e) {
        String msg = getExceptionStr(e);
        toLogFile(msg);
    }

    public static String getExceptionStr(Exception e) {
        final ByteArrayOutputStream str = new ByteArrayOutputStream();
        final PrintStream prnt = new PrintStream(str);
        e.printStackTrace(prnt);
        prnt.flush();
        String msg = new String(str.toByteArray());
        return msg;
    }

    public static void addLogLevel() {
        synchronized(lock){
            logIndent.append("    ");
        }        
    }

    public static void remLogLevel() {
        synchronized(lock){
            if(logIndent.length() > 3){
                logIndent.delete(0,4);
            }
        }
    }


}
