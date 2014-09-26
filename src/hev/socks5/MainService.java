/* MainService.java
 * Heiher <r@hev.cc>
 */

package hev.socks5;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;

public class MainService extends Service {
	private static native void NativeStartService(String local_address, int local_port,
			String server_address, int server_port);
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
		System.loadLibrary("hev-socks5-client");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		startService();
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		stopService();
	}

	public IBinder onBind(Intent intent) {
		return mMessenger.getBinder();
	}

	public void startService() {
		if (isRunning)
		  return;

		Preferences prefs = new Preferences(getApplicationContext());
		NativeStartService(prefs.getLocalAddress(),
				prefs.getLocalPort(),
				prefs.getServerAddress(),
				prefs.getServerPort());
		isRunning = true;
	}

	public void stopService() {
		if (!isRunning)
		  return;

		NativeStopService();
		isRunning = false;
	}
}

