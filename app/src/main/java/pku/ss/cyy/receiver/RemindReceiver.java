package pku.ss.cyy.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;

import pku.ss.cyy.myweather.CityList;
import pku.ss.cyy.myweather.MainActivity;
import pku.ss.cyy.myweather.R;

public class RemindReceiver extends BroadcastReceiver {
    public RemindReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        String remind = sp.getString("remind","出行前请注意查看天气哦~");

        NotificationManager manager = (NotificationManager)context.getSystemService(android.content.Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification(R.drawable.ic_launcher,"显示通知",System.currentTimeMillis());
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        notification.defaults = Notification.DEFAULT_ALL;
        Intent i = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0,i,0);
        notification.setLatestEventInfo(context,"天气提醒",remind,pendingIntent);
        manager.notify(1,notification);
    }
}
