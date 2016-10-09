/* TProxyService.java
 * Heiher <r@hev.cc>
 */

package hev.htproxy;

import android.app.Service;
import android.app.Notification;
import android.app.Notification.Builder;
import android.content.Intent;
import android.os.IBinder;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;

public class TProxyService extends Service {
	private static native void NativeStartService(String local_address, int local_port,
			String socks5_address, int socks5_port);
	private static native void NativeStopService();

	private boolean isRunning = false;
	private final Messenger mMessenger = new Messenger(new MessageHandler());

	public class MessageHandler extends Handler {
		public static final int TYPE_START = 1;
		public static final int TYPE_STOP = 2;

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case TYPE_START:
				startService();
				break;
			case TYPE_STOP:
				stopService();
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}

	static {
		System.loadLibrary("hev-socks5-tproxy");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);

		startService();
		Notification notify = new Notification.Builder(this)
			.setContentTitle(getString(R.string.app_name))
			.setSmallIcon(android.R.drawable.sym_def_app_icon)
			.build();
		startForeground(1, notify);
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		stopForeground(true);
		stopService();

		super.onDestroy();
	}

	public IBinder onBind(Intent intent) {
		return mMessenger.getBinder();
	}

	public void startService() {
		if (isRunning)
		  return;

		Preferences prefs = new Preferences(getApplicationContext());
		NativeStartService(prefs.getTProxyAddress(),
				prefs.getTProxyPort(),
				prefs.getSocks5Address(),
				prefs.getSocks5Port());
		isRunning = true;
	}

	public void stopService() {
		if (!isRunning)
		  return;

		NativeStopService();
		isRunning = false;
	}
}

