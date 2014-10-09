/* ServiceReceiver.java
 * Heiher <r@hev.cc>
 */

package hev.htproxy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ServiceReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			/* socks5 service */
			Intent i = new Intent(context, Socks5Service.class);
			context.startService(i);
			/* tproxy service */
			i = new Intent(context, TProxyService.class);
			context.startService(i);
			/* dns fwd service */
			i = new Intent(context, DNSFwdService.class);
			context.startService(i);

			Context app_context = context.getApplicationContext();
			boolean redir_enabled = RedirectManager.isEnabled(app_context);
			Preferences prefs = new Preferences(app_context);
			if (prefs.getHTProxyEnabled()) {
				if (!redir_enabled)
				  RedirectManager.setEnabled(true, app_context);
			} else {
				if (redir_enabled)
				  RedirectManager.setEnabled(false, app_context);
			}
		}
	}
}

