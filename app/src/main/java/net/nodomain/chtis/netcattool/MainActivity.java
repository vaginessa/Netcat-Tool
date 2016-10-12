package net.nodomain.chtis.netcattool;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

	public ServerService serv = null;
	private ServiceConnection serv_conn = null;
	private Intent starter;

	private void serverStarted(){
		if (serv == null) {
			switch_listen.setChecked(false);
		}else{
			Toast.makeText(this, "Listening started", Toast.LENGTH_SHORT).show();
		}
	}
	private void serverStopped(){
		Toast.makeText(this, "Listening stopped", Toast.LENGTH_SHORT).show();
		switch_listen.setChecked(false);
	}
	private boolean startServer() {
		startService(starter);
		serv_conn = new ServiceConnection() {
			public void onServiceConnected(ComponentName className, IBinder service) {
				serv = ((ServerService.LocalBinder) service).getService();
				serverStarted();
			}
			public void onServiceDisconnected(ComponentName className) {
				serv = null;
			}
		};
		return bindService(starter, serv_conn, BIND_AUTO_CREATE|BIND_NOT_FOREGROUND);
	}

	private boolean stopServer() {
		if (serv_conn != null) {
			// Detach our existing connection.
			unbindService(serv_conn);
			serv_conn = null;
			stopService(starter);
			serv = null;
			serverStopped();
		}
		return false;
	}

	private Switch switch_listen;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		starter = new Intent(MainActivity.this, ServerService.class);

		switch_listen = (Switch) findViewById(R.id.switch_listen);
		switch_listen.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (switch_listen.isChecked()) {
					switch_listen.setChecked(startServer());
				} else {
					switch_listen.setChecked(stopServer());
				}
			}
		});

		//
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		stopServer();
	}

}
