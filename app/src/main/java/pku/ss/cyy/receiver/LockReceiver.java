package pku.ss.cyy.receiver;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.AndroidCharacter;
import android.util.Log;

import pku.ss.cyy.myweather.LockScreen;
import pku.ss.cyy.myweather.R;

public class LockReceiver extends BroadcastReceiver {
    KeyguardManager keyguardManager;
    KeyguardManager.KeyguardLock keyguardLock;
    static final String TAG = "app";

    public LockReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
//        throw new supportedOperationException("Not yet implemented");
        if (intent.getAction().equals(Intent.ACTION_SCREEN_ON) ||
                intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            Intent toMainIntent = new Intent(context, LockScreen.class);
            toMainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//这个flags表示如果已经有这个activity，则将已有的提到栈顶，否则新建一个activity。
            keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
            keyguardLock = keyguardManager.newKeyguardLock("my lock");
            keyguardLock.disableKeyguard();//解锁系统锁屏
            context.startActivity(toMainIntent);//跳转到主界面
        }
    }
}
