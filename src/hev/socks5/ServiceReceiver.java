/* ServiceReceiver.java
 * Heiher <r@hev.cc>
 */

package hev.socks5;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ServiceReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			Intent i = new Intent(context, MainService.class);
			Preferences prefs = new Preferences(context);
			i.putExtra(Preferences.LOCAL_ADDRESS, prefs.getLocalAddress());
			i.putExtra(Preferences.LOCAL_PORT, prefs.getLocalPort());
			i.putExtra(Preferences.SERVER_ADDRESS, prefs.getServerAddress());
			i.putExtra(Preferences.SERVER_PORT, prefs.getServerPort());
			context.startService(i);
		}
	}
}

