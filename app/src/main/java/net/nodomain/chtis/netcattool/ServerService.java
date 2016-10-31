package net.nodomain.chtis.netcattool;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


class ServerNetThread extends Thread{
	private final int port;
	private ServerSocket sock;

	ServerNetThread(int port){
		this.port = port;
	}

	@Override
	public void interrupt(){
		super.interrupt();
		try {
			sock.close();
		}catch(IOException e){
			// we can ignore
		}
	}

	@Override
	public void run(){
		Socket client;
		ArrayList<ReceiveThread> child_threads = new ArrayList<>();
		try {
			sock = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
			Log.i("ServerService", "didn't get socket: "+e.getMessage());
			return;
		}
		while (!this.isInterrupted()) {
			try {
				client = sock.accept();
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
			Log.i("ServerService", "accepted connection");
			ReceiveThread thr = new ReceiveThread(client);
			child_threads.add(thr);
			thr.start();
		}
		for(ReceiveThread thr : child_threads) {
			thr.interrupt();
		}
	}
}

public class ServerService extends Service {
	public final int PORT = 30003;

	public class LocalBinder extends Binder {
		ServerService getService() {
			return ServerService.this;
		}
	}

	private final IBinder mBinder = new LocalBinder();
	private NotificationManager nm;
	private ServerNetThread thr = null;
	private ConnectionManager cm = null;

	public ServerService() {
	}

	@Override
	public void onCreate() {
		nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		cm = new ConnectionManager();
	}

	private int notification_count = 1;
	private synchronized Notification showNotification(String what) {
		// The PendingIntent to launch our activity if the user selects this notification
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
			new Intent(this, MainActivity.class), 0);

		// Set the info for the views that show in the notification panel.
		Notification notification = new NotificationCompat.Builder(this)
			.setSmallIcon(R.drawable.ic_cloud_download)
			.setTicker("What shall I do?")
			.setWhen(System.currentTimeMillis())
			.setContentTitle("Incoming Connection")
			.setContentText("What shall I do?")
			.setContentIntent(contentIntent)
			.setLocalOnly(true)
			.build();

		// Send the notification.
		nm.notify(notification_count++, notification);

		return notification;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(thr == null){
			Log.i("ServerService", "started listening");
			thr = new ServerNetThread(PORT);
			thr.start();
			//showNotification("");
		}
		return START_STICKY;
	}

	@Override
	public void onDestroy(){
		Log.i("ServerService", "Destroy");
		if(thr != null && thr.isAlive()) {
			thr.interrupt();
			cm.destroy();
			Log.i("ServerService", "stopped listening");
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
}
