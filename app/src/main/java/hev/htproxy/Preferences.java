/*
 * Perferences.java
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
	public static final String CONFIGS = "Configs";
	public static final String GLOBAL = "Global";
	public static final String IPV4 = "Ipv4";
	public static final String IPV6 = "Ipv6";
	public static final String APPLICATIONS = "Applications";
	public static final String ENABLE = "Enable";

	private static final String CONFIGS_DEFAULT = "protocols:\n" +
		"  generic: &generic\n" +
		"    request: 'POST /zh-cn HTTP/1.1\\r\\n" +
				"Host: www.microsoft.com\\r\\n" +
				"Content-Type: application/octet-stream\\r\\n" +
				"Connection: keep-alive\\r\\n\\r\\n'\n" +
		"    response: 'HTTP/1.1 200 OK\\r\\n" +
				"Server: Microsoft-IIS/8.5\\r\\n" +
				"Content-Type: application/octet-stream\\r\\n" +
				"Connection: keep-alive\\r\\n\\r\\n'\n" +
		"\n" +
		"servers:\n" +
		"  usa:\n" +
		"    port: 80\n" +
		"    address: 10.0.0.1\n" +
		"    password:\n" +
		"    protocol: *generic\n" +
		"    weight: 1\n";

	private SharedPreferences prefs;

	public Preferences(Context context) {
		prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_MULTI_PROCESS);
	}

	public String getConfigs() {
		return prefs.getString(CONFIGS, CONFIGS_DEFAULT);
	}

	public void setConfigs(String extra_configs) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(CONFIGS, extra_configs);
		editor.commit();
	}

	public boolean getGlobal() {
		return prefs.getBoolean(GLOBAL, false);
	}

	public void setGlobal(boolean enable) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(GLOBAL, enable);
		editor.commit();
	}

	public boolean getIpv4() {
		return prefs.getBoolean(IPV4, true);
	}

	public void setIpv4(boolean enable) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(IPV4, enable);
		editor.commit();
	}

	public boolean getIpv6() {
		return prefs.getBoolean(IPV6, true);
	}

	public void setIpv6(boolean enable) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(IPV6, enable);
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

	public boolean getEnable() {
		return prefs.getBoolean(ENABLE, false);
	}

	public void setEnable(boolean enable) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(ENABLE, enable);
		editor.commit();
	}

	public String getTunnelName() {
		return "tun0";
	}

	public int getTunnelMtu() {
		return 8192;
	}

	public String getTunnelIpv4Address() {
		return "10.0.0.2";
	}

	public String getTunnelIpv4Gateway() {
		return "10.0.0.1";
	}

	public int getTunnelIpv4Prefix() {
		return 30;
	}

	public String getTunnelIpv6Address() {
		return "fc00::2";
	}

	public String getTunnelIpv6Gateway() {
		return "fc00::1";
	}

	public int getTunnelIpv6Prefix() {
		return 126;
	}

	public int getTunnelDnsPort() {
		return 53;
	}

	public String getSocks5Address() {
		return "127.0.0.1";
	}

	public int getSocks5Port() {
		return 1080;
	}
}
