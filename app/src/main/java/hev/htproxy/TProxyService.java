/* TProxyService.java
 * Heiher <r@hev.cc>
 */

package hev.htproxy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;
import android.app.Service;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Context;
import android.content.BroadcastReceiver;
import android.os.IBinder;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.Process;
import android.net.ConnectivityManager;

public class TProxyService extends Service {
	private static native void Socks5StartService(String config_path);
	private static native void Socks5StopService();

	private static native void TProxyStartService(String config_path);
	private static native void TProxyStopService();

	private static native void DnsProxyStartService(String socket_path);
	private static native void DnsProxyStopService();
	private static native void DnsProxyResetResolver();
	private static native void DnsProxySetResolverProxy(String dns1, String dns2);
	private static native void DnsProxySetProxyUids(int[] uids, int last_uid);

	private boolean isRunning = false;
	private final Messenger mMessenger = new Messenger(new MessageHandler());

	private final BroadcastReceiver network_state_receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			resetResolver();
		}
	};

	public class MessageHandler extends Handler {
		public static final int TYPE_START = 1;
		public static final int TYPE_STOP = 2;
		public static final int TYPE_RESET_PROXY_UIDS = 3;

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case TYPE_START:
				startService();
				break;
			case TYPE_STOP:
				stopService();
				break;
			case TYPE_RESET_PROXY_UIDS:
				resetProxyUids();
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}

	static {
		System.loadLibrary("hev-socks5-client");
		System.loadLibrary("hev-socks5-tproxy");
		System.loadLibrary("hev-dns-proxy-jni");
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

		Preferences prefs = new Preferences(this);

		/* Socks5 */
		File socks5_file = new File(getCacheDir(), "socks5.conf");
		try {
			socks5_file.createNewFile();
			FileOutputStream fos = new FileOutputStream(socks5_file, false);

			String socks5_conf = "[Main]\n" +
				"Workers=4\n" +
				"Port=" + prefs.getSocks5Port() + "\n" +
				"ListenAddress=" + prefs.getSocks5Address() + "\n" +
				prefs.getServers() + "\n" +
				prefs.getExtraConfigs();

			fos.write (socks5_conf.getBytes());
			fos.close();
		} catch (IOException e) {
			return;
		}
		Socks5StartService(socks5_file.getAbsolutePath());

		/* TProxy */
		File tproxy_file = new File(getCacheDir(), "tproxy.conf");
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

		/* DnsProxy */
		File dns_proxy_file = new File(getFilesDir(), "dnsproxyd");
		String dns_proxy_path = dns_proxy_file.getAbsolutePath();
		prefs.setDnsProxyPath(dns_proxy_path);
		DnsProxySetResolverProxy(prefs.getProxyDns1Address(), prefs.getProxyDns2Address());
		/* Reset proxy uids before startService */
		resetProxyUids();
		DnsProxyStartService(dns_proxy_path);

		IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(network_state_receiver, filter);

		Intent i = new Intent(this, TProxyService.class);
		PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
		Notification notify = new Notification.Builder(this)
			.setContentTitle(getString(R.string.app_name))
			.setSmallIcon(android.R.drawable.sym_def_app_icon)
			.setContentIntent(pi)
			.build();
		startForeground(1, notify);

		/* Disable OOM */
		setOOMAdj(-17);

		isRunning = true;
	}

	public void stopService() {
		if (!isRunning)
		  return;

		stopForeground(true);

		unregisterReceiver(network_state_receiver);

		/* DnsProxy */
		DnsProxyStopService();

		/* TProxy */
		TProxyStopService();

		/* Socks5 */
		Socks5StopService();

		/* Enable OOM */
		setOOMAdj(4);

		isRunning = false;
	}

	public void resetResolver() {
		DnsProxyResetResolver();
	}

	public void resetProxyUids() {
		Preferences prefs = new Preferences(this);
		Set<String> uids = prefs.getUIDs();
		int i = 0, proxy_uids[] = new int[uids.size()];

		for (String uid : uids) {
			proxy_uids[i++] = Integer.parseInt(uid);
		}

		DnsProxySetProxyUids(proxy_uids, Process.LAST_APPLICATION_UID);
	}

	private void setOOMAdj(int value) {
		int pid = Process.myPid();
		String cmd = String.format("echo %d > /proc/%d/oom_adj", value, pid);
		SuperRunner.runCmd(cmd);
	}
}

