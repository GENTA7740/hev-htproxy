/* MainService.java
 * Heiher <r@hev.cc>
 */

package hev.socks5;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class MainService extends Service {

	static {
		System.loadLibrary("hev-socks5-client");
	}

	public IBinder onBind(Intent intent) {
		return null;
	}
}

