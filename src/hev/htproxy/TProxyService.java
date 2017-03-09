/* TProxyService.java
 * Heiher <r@hev.cc>
 */

package hev.htproxy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import android.app.Service;
import android.app.Notification;
import android.app.Notification.Builder;
import android.content.Intent;
import android.content.Context;
import android.os.IBinder;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;

public class TProxyService extends Service {
	private static native void Socks5StartService(String config_path);
	private static native void Socks5StopService();

	private static native void TProxyStartService(String config_path);
	private static native void TProxyStopService();

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
		System.loadLibrary("hev-socks5-tproxy");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);

		startService();
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		stopService();

		super.onDestroy();
	}

	public IBinder onBind(Intent intent) {
		return mMessenger.getBinder();
	}

	public void startService() {
		if (isRunning)
		  return;

		Context app_context = getApplicationContext();
		Preferences prefs = new Preferences(app_context);

		/* Socks5 */
		File socks5_file = new File(app_context.getCacheDir(), "socks5.conf");
		try {
			socks5_file.createNewFile();
			FileOutputStream fos = new FileOutputStream(socks5_file, false);

			String socks5_conf = "[Main]\n" +
				"Workers=4\n" +
				"Port=" + prefs.getSocks5Port() + "\n" +
				"ListenAddress=" + prefs.getSocks5Address() + "\n" +
				"[Srv1]\n" +
				"Port=" + prefs.getServerPort() + "\n" +
				"Address=" + prefs.getServerAddress() + "\n" +
				"Password=" + prefs.getPassword() + "\n" +
				prefs.getExtraConfigs();

			fos.write (socks5_conf.getBytes());
			fos.close();
		} catch (IOException e) {
			return;
		}
		Socks5StartService(socks5_file.getAbsolutePath());

		/* TProxy */
		File tproxy_file = new File(app_context.getCacheDir(), "tproxy.conf");
		try {
			tproxy_file.createNewFile();
			FileOutputStream fos = new FileOutputStream(tproxy_file, false);

			String tproxy_conf = "[Main]\n" +
				"Workers=4\n" +
				"[Socks5]\n" +
				"Port=" + prefs.getSocks5Port() + "\n" +
				"Address=" + prefs.getSocks5Address() + "\n" +
				"[TCP]\n" +
				"Port=" + prefs.getTProxyPort() + "\n" +
				"ListenAddress=" + prefs.getTProxyAddress() + "\n" +
				"[DNS]\n" +
				"Port=" + prefs.getDNSFwdPort() + "\n" +
				"ListenAddress=" + prefs.getDNSFwdAddress() + "\n";

			fos.write (tproxy_conf.getBytes());
			fos.close();
		} catch (IOException e) {
			return;
		}
		TProxyStartService(tproxy_file.getAbsolutePath());

		Notification notify = new Notification.Builder(this)
			.setContentTitle(getString(R.string.app_name))
			.setSmallIcon(android.R.drawable.sym_def_app_icon)
			.build();
		startForeground(1, notify);

		isRunning = true;
	}

	public void stopService() {
		if (!isRunning)
		  return;

		stopForeground(true);

		/* TProxy */
		TProxyStopService();

		/* Socks5 */
		Socks5StopService();

		isRunning = false;
	}
}

