/*
 * TProxyService.java
 * Heiher <r@hev.cc>
 */

package hev.htproxy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import android.os.ParcelFileDescriptor;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.VpnService;
import android.content.pm.PackageManager.NameNotFoundException;

public class TProxyService extends VpnService {
	private static native void Socks5StartService(String config_path);
	private static native void Socks5StopService();

	private static native void TProxyStartService(String config_path, int fd);
	private static native void TProxyStopService();

	public static final String ACTION_CONNECT = "hev.htproxy.CONNECT";
	public static final String ACTION_DISCONNECT = "hev.htproxy.DISCONNECT";

	static {
		System.loadLibrary("hev-socks5-client");
		System.loadLibrary("hev-socks5-tunnel");
	}

	private ParcelFileDescriptor tunFd = null;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null && ACTION_DISCONNECT.equals(intent.getAction())) {
			stopService();
			return START_NOT_STICKY;
		}
		startService();
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onRevoke() {
		stopService();
		super.onRevoke();
	}

	public void startService() {
		if (tunFd != null)
		  return;

		Preferences prefs = new Preferences(this);

		/* VPN */
		String session = new String();
		VpnService.Builder builder = new VpnService.Builder();
		builder.setBlocking(false);
		builder.setMtu(prefs.getTunnelMtu());
		if (prefs.getIpv4()) {
			String addr = prefs.getTunnelIpv4Address();
			int prefix = prefs.getTunnelIpv4Prefix();
			builder.addAddress(addr, prefix);
			builder.addRoute("0.0.0.0", 0);
			builder.addDnsServer(prefs.getTunnelIpv4Gateway());
			session += "IPv4";
		}
		if (prefs.getIpv6()) {
			String addr = prefs.getTunnelIpv6Address();
			int prefix = prefs.getTunnelIpv6Prefix();
			builder.addAddress(addr, prefix);
			builder.addRoute("::", 0);
			builder.addDnsServer(prefs.getTunnelIpv6Gateway());
			if (!session.isEmpty())
			  session += " + ";
			session += "IPv6";
		}
		boolean disallowSelf = true;
		if (prefs.getGlobal()) {
			session += "/Global";
		} else {
			for (String appName : prefs.getApplications()) {
				try {
					builder.addAllowedApplication(appName);
					disallowSelf = false;
				} catch (NameNotFoundException e) {
				}
			}
			session += "/per-App";
		}
		if (disallowSelf) {
			String selfName = getApplicationContext().getPackageName();
			try {
				builder.addDisallowedApplication(selfName);
			} catch (NameNotFoundException e) {
			}
		}
		builder.setSession(session);
		tunFd = builder.establish();
		if (tunFd == null) {
			stopSelf();
			return;
		}

		/* Socks5 */
		File socks5_file = new File(getCacheDir(), "socks5.conf");
		try {
			socks5_file.createNewFile();
			FileOutputStream fos = new FileOutputStream(socks5_file, false);

			String socks5_conf = "main:\n" +
				"  workers: 4\n" +
				"  port: " + prefs.getSocks5Port() + "\n" +
				"  listen-address: '" + prefs.getSocks5Address() + "'\n" +
				prefs.getConfigs() + "\n";

			fos.write(socks5_conf.getBytes());
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

			String tproxy_conf = "socks5:\n" +
				"  port: " + prefs.getSocks5Port() + "\n" +
				"  address: '" + prefs.getSocks5Address() + "'\n" +
				"tunnel:\n" +
				"  name: '" + prefs.getTunnelName() + "'\n" +
				"  mtu: " + prefs.getTunnelMtu() + "\n" +
				"  dns:\n" +
				"    port: " + prefs.getTunnelDnsPort() + "\n";

			if (prefs.getIpv4()) {
				tproxy_conf += "  ipv4:\n" +
				"    address: '" + prefs.getTunnelIpv4Address() + "'\n" +
				"    gateway: '" + prefs.getTunnelIpv4Gateway() + "'\n" +
				"    prefix: " + prefs.getTunnelIpv4Prefix() + "\n";
			}
			if (prefs.getIpv6()) {
				tproxy_conf += "  ipv6:\n" +
				"    address: '" + prefs.getTunnelIpv6Address() + "'\n" +
				"    gateway: '" + prefs.getTunnelIpv6Gateway() + "'\n" +
				"    prefix: " + prefs.getTunnelIpv6Prefix() + "\n";
			}

			fos.write(tproxy_conf.getBytes());
			fos.close();
		} catch (IOException e) {
			return;
		}
		TProxyStartService(tproxy_file.getAbsolutePath(), tunFd.getFd());

		Intent i = new Intent(this, TProxyService.class);
		PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
		Notification notify = new Notification.Builder(this)
			.setContentTitle(getString(R.string.app_name))
			.setSmallIcon(android.R.drawable.sym_def_app_icon)
			.setContentIntent(pi)
			.build();
		startForeground(1, notify);
	}

	public void stopService() {
		if (tunFd == null)
		  return;

		stopForeground(true);

		/* TProxy */
		TProxyStopService();

		/* Socks5 */
		Socks5StopService();

		/* VPN */
		try {
			tunFd.close();
		} catch (IOException e) {
		}
		tunFd = null;
	}
}
