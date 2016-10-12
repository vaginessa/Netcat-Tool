package net.nodomain.chtis.netcattool;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;


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
			try {
				PrintWriter out = new PrintWriter(client.getOutputStream(), true);
				out.println("Hallo!");
				out.close();
			}catch(IOException e){
				// ignore
			}
			try {
				client.close();
			}catch (IOException e){
				// ignore
			}
		}
	}
}

public class ServerService extends Service {
	public class LocalBinder extends Binder {
		ServerService getService() {
			return ServerService.this;
		}
	}

	private final IBinder mBinder = new LocalBinder();
	private NotificationManager nm;
	private ServerNetThread thr = null;

	public ServerService() {
	}

	private void init() {
		nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
	}

	@Override
	public void onCreate() {
		init();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(thr == null){
			Log.i("ServerService", "started listening");
			thr = new ServerNetThread(30003);
			thr.start();
		}
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		if(thr != null && thr.isAlive()) {
			thr.interrupt();
			Log.i("ServerService", "stopped listening");
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
}
