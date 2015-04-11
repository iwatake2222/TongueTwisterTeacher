/**
 * DebugUtility
 * @author take.iwiw
 * @version 1.0.0
 */

package com.take_iwiw.tonguetwisterteacher;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;


public class DebugUtility {
    static final public boolean DEBUG = true;

    public static void logDebug(String msg){
        if(DebugUtility.DEBUG){
            StackTraceElement callStack = Thread.currentThread().getStackTrace()[3];
            Log.d("@] "
                     + callStack.getFileName() + "#" + callStack.getMethodName() + ":"+ callStack.getLineNumber(), msg);
        }
    }

    public static void logError(String msg){
        StackTraceElement callStack = Thread.currentThread().getStackTrace()[3];
        Log.e("@] "
                + callStack.getFileName() + "#" + callStack.getMethodName() + ":"+ callStack.getLineNumber(), msg);
    }

    public static void showToast(Context context, String msg){
        if(DebugUtility.DEBUG){
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
        }
    }

    public static void showToastDetail(Context context, String msg){
        if(DebugUtility.DEBUG){
            final String BR = System.getProperty("line.separator");
            StackTraceElement callStack = Thread.currentThread().getStackTrace()[3];
            Toast.makeText(context, callStack.getFileName() + "#" + callStack.getMethodName() + ":"+ callStack.getLineNumber()
                    + BR + msg, Toast.LENGTH_LONG).show();
        }
    }

}

