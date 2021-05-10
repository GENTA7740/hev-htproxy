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
	public static final String CONFIG_URL = "ConfigUrl";
	public static final String USERNAME = "Username";
	public static final String PASSWORD = "Password";
	public static final String IPV4 = "Ipv4";
	public static final String IPV6 = "Ipv6";
	public static final String GLOBAL = "Global";
	public static final String APPS = "Apps";
	public static final String ENABLE = "Enable";

	private SharedPreferences prefs;

	public Preferences(Context context) {
		prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_MULTI_PROCESS);
	}

	public String getConfigs() {
		return prefs.getString(CONFIGS, "");
	}

	public void setConfigs(String extra_configs) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(CONFIGS, extra_configs);
		editor.commit();
	}

	public String getConfigUrl() {
		return prefs.getString(CONFIG_URL, "https://hev.cc/htp/htp.yml");
	}

	public void setConfigUrl(String extra_configs) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(CONFIG_URL, extra_configs);
		editor.commit();
	}

	public String getUsername() {
		return prefs.getString(USERNAME, "");
	}

	public void setUsername(String username) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(USERNAME, username);
		editor.commit();
	}

	public String getPassword() {
		return prefs.getString(PASSWORD, "");
	}

	public void setPassword(String password) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PASSWORD, password);
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

	public boolean getGlobal() {
		return prefs.getBoolean(GLOBAL, false);
	}

	public void setGlobal(boolean enable) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(GLOBAL, enable);
		editor.commit();
	}

	public Set<String> getApps() {
		return prefs.getStringSet(APPS, new HashSet<String>());
	}

	public void setApps(Set<String> apps) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putStringSet(APPS, apps);
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
		return 10496;
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

	public String getTunnelDnsIpv4Address() {
		return "8.8.8.8";
	}

	public String getTunnelDnsIpv6Address() {
		return "2001:4860:4860::8888";
	}

	public String getSocks5Address() {
		return "127.0.0.1";
	}

	public int getSocks5Port() {
		return 1080;
	}
}
