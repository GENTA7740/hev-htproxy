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
			/* tproxy service */
			Intent i = new Intent(context, TProxyService.class);
			context.startService(i);

			boolean redir_enabled = RedirectManager.isEnabled(context);
			Preferences prefs = new Preferences(context);
			if (prefs.getHTProxyEnabled()) {
				if (!redir_enabled)
				  RedirectManager.setEnabled(true, context);
			} else {
				if (redir_enabled)
				  RedirectManager.setEnabled(false, context);
			}
		}
	}
}

