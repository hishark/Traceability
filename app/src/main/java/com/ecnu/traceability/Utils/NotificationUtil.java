package com.ecnu.traceability.Utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.ecnu.traceability.R;
import com.ecnu.traceability.judge.JudgeActivity;

public class NotificationUtil {
//    private int id = 1;
    private Context context;

//    public NotificationUtil(Context context){
//        this.context=context;
//    }

    /**
     * 出现风险时发出通知
     * @param context
     * @param title 风险警告
     * @param msg 存在风险，请检查
     */
    public static void notification(Context context, String title, String msg) {
        int id = 1;
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 创建
            NotificationChannel channel = new NotificationChannel(String.valueOf(id), "通知", NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableLights(true);
            channel.setLightColor(ContextCompat.getColor(context, R.color.c_ff6633));
            channel.setShowBadge(true);
            channel.setDescription("风险警告");
            manager.createNotificationChannel(channel);
        }

        Intent clickIntent = new Intent(context, JudgeActivity.class);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, String.valueOf(id));
        mBuilder.setContentTitle(title)
                .setContentText(msg)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.trace))
                .setOnlyAlertOnce(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.trace)
                .setColor(ContextCompat.getColor(context, R.color.c_ff6633));
        clickIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent clickPendingIntent = PendingIntent.getActivity(context, 0, clickIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        // 点击删除
        Intent cancelIntent = new Intent(context, NoticeCancelBroadcastReceiver.class);
        cancelIntent.setAction("notice_cancel");
        cancelIntent.putExtra("id", id);
        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(context, 0, cancelIntent, PendingIntent.FLAG_ONE_SHOT);
        mBuilder.setContentIntent(clickPendingIntent);
        mBuilder.setDeleteIntent(cancelPendingIntent);
        mBuilder.setAutoCancel(true);
        manager.notify(id, mBuilder.build());

        playNotificationRing(context);
        playNotificationVibrate(context);
    }

    /**
     * 播放通知声音
     */
    private static void playNotificationRing(Context context) {
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone rt = RingtoneManager.getRingtone(context, uri);
        rt.play();
    }

    /**
     * 手机震动一下
     */
    private static void playNotificationVibrate(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
        long[] vibrationPattern = new long[]{0, 1000, 1000, 1000};
        // 第一个参数为开关开关的时间，第二个参数是重复次数，振动需要添加权限
        vibrator.vibrate(vibrationPattern, -1);
    }


//    public void notification() {
//        NotificationManager manager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            // 创建
//            NotificationChannel channel = new NotificationChannel(String.valueOf(id), "通知", NotificationManager.IMPORTANCE_DEFAULT);
//            channel.enableLights(true);
//            channel.setLightColor(ContextCompat.getColor(this, R.color.c_ff6633));
//            channel.setShowBadge(true);
//            channel.setDescription("风险警告");
//            manager.createNotificationChannel(channel);
//        }
//
//        Intent clickIntent = new Intent(this, JudgeActivity.class);
//        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, String.valueOf(id));
//        mBuilder.setContentTitle("风险警告")
//                .setContentText("存在风险，请检查")
//                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.drawable.trace))
//                .setOnlyAlertOnce(true)
//                .setDefaults(Notification.DEFAULT_ALL)
//                .setWhen(System.currentTimeMillis())
//                .setDefaults(NotificationCompat.DEFAULT_ALL)
//                .setVibrate(new long[]{0, 1000, 1000, 1000})//震动
//                .setSmallIcon(R.drawable.trace)
//                .setColor(ContextCompat.getColor(this, R.color.c_ff6633));
//        clickIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        PendingIntent clickPendingIntent = PendingIntent.getActivity(this, 0, clickIntent, PendingIntent.FLAG_CANCEL_CURRENT);
//        // 点击删除
//        Intent cancelIntent = new Intent(this, NoticeCancelBroadcastReceiver.class);
//        cancelIntent.setAction("notice_cancel");
//        cancelIntent.putExtra("id", id);
//        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(this, 0, cancelIntent, PendingIntent.FLAG_ONE_SHOT);
//        mBuilder.setContentIntent(clickPendingIntent);
//        mBuilder.setDeleteIntent(cancelPendingIntent);
//        mBuilder.setAutoCancel(true);
//        manager.notify(id, mBuilder.build());
//        playNotificationRing(this);
//        playNotificationVibrate(this);
//    }
}
class NoticeCancelBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String acton = intent.getAction();
        if (acton.equals("notice_cancel")) {
            int id = intent.getIntExtra("id", -1);
            if (id != -1) {
                NotificationManager notificationManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(id);
            }
        }
    }
}