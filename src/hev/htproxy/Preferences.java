/* Perferences.java
 * Heiher <r@hev.cc>
 */

package hev.htproxy;

import java.util.Set;
import java.util.HashSet;
import android.content.Context;
import android.content.SharedPreferences;

public class Preferences
{
	public static final String PREFS_NAME = "Socks5Prefs";
	public static final String SERVER_ADDRESS = "ServerAddress";
	public static final String SERVER_PORT = "ServerPort";
	public static final String BYPASS_ASSRESSES = "BypassAddresses";
	public static final String APPLICATIONS = "Applications";
	public static final String ENABLE_HTPROXY = "HTProxyEnabled";

	private SharedPreferences prefs;

	public Preferences(Context context) {
		prefs = context.getSharedPreferences(PREFS_NAME, 0);
	}

	public String getServerAddress() {
		return prefs.getString(SERVER_ADDRESS, "10.0.0.1");
	}

	public void setServerAddress(String address) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(SERVER_ADDRESS, address);
		editor.commit();
	}

	public int getServerPort() {
		return prefs.getInt(SERVER_PORT, 80);
	}

	public void setServerPort(int port) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(SERVER_PORT, port);
		editor.commit();
	}

	public Set<String> getBypassAddresses() {
		return prefs.getStringSet(BYPASS_ASSRESSES, new HashSet<String>());
	}

	public void setBypassAddresses(Set<String> addresses) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putStringSet(BYPASS_ASSRESSES, addresses);
		editor.commit();
	}

	public Set<String> getApplications() {
		return prefs.getStringSet(APPLICATIONS, new HashSet<String>());
	}

	public void setApplications(Set<String> applications) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putStringSet(APPLICATIONS, applications);
		editor.commit();
	}

	public boolean getHTProxyEnabled() {
		return prefs.getBoolean(ENABLE_HTPROXY, false);
	}

	public void setHTProxyEnabled(boolean enable) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(ENABLE_HTPROXY, enable);
		editor.commit();
	}

	public String getSocks5Address() {
		return "127.0.0.1";
	}

	public int getSocks5Port() {
		return 1080;
	}

	public String getTProxyAddress() {
		return "127.0.0.1";
	}

	public int getTProxyPort() {
		return 10800;
	}

	public String getDNSFwdAddress() {
		return "127.0.0.1";
	}

	public int getDNSFwdPort() {
		return 5300;
	}

	public String getDNSUpstreamAddress() {
		return "8.8.8.8";
	}
}
