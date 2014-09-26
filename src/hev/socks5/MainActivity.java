package hev.socks5;

import android.app.Activity;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.content.Context;
import android.content.Intent;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.widget.EditText;
import android.widget.Button;
import android.view.View;

public class MainActivity extends Activity implements View.OnClickListener
{
	private Preferences prefs;
	private EditText edittext_local_address;
	private EditText edittext_local_port;
	private EditText edittext_server_address;
	private EditText edittext_server_port;
	private Button button_restart;
	private Messenger mService = null;

	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			mService = new Messenger(service);
		}

		public void onServiceDisconnected(ComponentName className) {
			mService = null;
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		prefs = new Preferences(getApplicationContext());
		setContentView(R.layout.main);

		edittext_local_address = (EditText) findViewById(R.id.local_address);
		edittext_local_port = (EditText) findViewById(R.id.local_port);
		edittext_server_address = (EditText) findViewById(R.id.server_address);
		edittext_server_port = (EditText) findViewById(R.id.server_port);
		button_restart = (Button) findViewById(R.id.restart);

		edittext_local_address.setText(prefs.getLocalAddress());
		edittext_local_port.setText(Integer.toString(prefs.getLocalPort()));
		edittext_server_address.setText(prefs.getServerAddress());
		edittext_server_port.setText(Integer.toString(prefs.getServerPort()));
		button_restart.setOnClickListener(this);

		Intent i = new Intent(getApplicationContext(), MainService.class);
		getApplicationContext().startService(i);
		bindService(new Intent(this, MainService.class), mConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onDestroy() {
		savePrefs();
		unbindService(mConnection);
	}

	public void onClick(View view) {
		stopSocks5Service();
		savePrefs();
		startSocks5Service();
	}

	private void savePrefs() {
		prefs.setLocalAddress(edittext_local_address.getText().toString());
		prefs.setLocalPort(Integer.parseInt(edittext_local_port.getText().toString()));
		prefs.setServerAddress(edittext_server_address.getText().toString());
		prefs.setServerPort(Integer.parseInt(edittext_server_port.getText().toString()));
	}

	private void startSocks5Service() {
		if (null == mService)
		  return;

		try {
			Message msg = Message.obtain(null, MainService.MessageHandler.TYPE_START);
			mService.send(msg);
		} catch (RemoteException e) {
		}
	}

	private void stopSocks5Service() {
		if (null == mService)
		  return;

		try {
			Message msg = Message.obtain(null, MainService.MessageHandler.TYPE_STOP);
			mService.send(msg);
		} catch (RemoteException e) {
		}
	}
}
