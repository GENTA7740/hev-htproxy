/* MainService.java
 * Heiher <r@hev.cc>
 */

package hev.socks5;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Bundle;

public class MainService extends Service {
	private static native void NativeStartService(String local_address, int local_port,
			String server_address, int server_port);
	private static native void NativeStopService();

	static {
		System.loadLibrary("hev-socks5-client");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Bundle extras = intent.getExtras();
		NativeStartService(extras.getString(Preferences.LOCAL_ADDRESS),
				extras.getInt(Preferences.LOCAL_PORT),
				extras.getString(Preferences.SERVER_ADDRESS),
				extras.getInt(Preferences.SERVER_PORT));
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		NativeStopService();
	}

	public IBinder onBind(Intent intent) {
		return null;
	}
}

