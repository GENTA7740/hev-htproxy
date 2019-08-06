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
	public static final String SERVERS = "Servers";
	public static final String BYPASS_ASSRESSES = "BypassAddresses";
	public static final String GLOBAL_PROXY = "GlobalProxy";
	public static final String APPLICATIONS = "Applications";
	public static final String UIDS = "UIDs";
	public static final String EXTRA_CONFIGS = "ExtraConfigs";
	public static final String DNS_PROXY_PATH = "DnsProxyPath";
	public static final String ENABLE_HTPROXY = "HTProxyEnabled";

	private static final String SERVERS_DEFAULT = "[Srv]\n" +
		"Port=80\n" +
		"Address=10.0.0.1\n" +
		"Password=";
	private static final String EXTRA_CONFIGS_DEFAULT = "[HTTP]\n" +
		"Request=POST /zh-cn HTTP/1.1\\r\\n" +
			"Host: www.microsoft.com\\r\\n" +
			"Content-Type: application/octet-stream\\r\\n" +
			"Connection: keep-alive\\r\\n\\r\\n\n" +
		"Response=HTTP/1.1 200 OK\\r\\n" +
			"Server: Microsoft-IIS/8.5\\r\\n" +
			"Content-Type: application/octet-stream\\r\\n" +
			"Connection: keep-alive\\r\\n\\r\\n";

	private SharedPreferences prefs;

	public Preferences(Context context) {
		prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_MULTI_PROCESS);
	}

	public String getServers() {
		return prefs.getString(SERVERS, SERVERS_DEFAULT);
	}

	public void setServers(String servers) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(SERVERS, servers);
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

	public boolean getGlobalProxy() {
		return prefs.getBoolean(GLOBAL_PROXY, false);
	}

	public void setGlobalProxy(boolean enable) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(GLOBAL_PROXY, enable);
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

	public Set<String> getUIDs() {
		return prefs.getStringSet(UIDS, new HashSet<String>());
	}

	public void setUIDs(Set<String> uids) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putStringSet(UIDS, uids);
		editor.commit();
	}

	public String getExtraConfigs() {
		return prefs.getString(EXTRA_CONFIGS, EXTRA_CONFIGS_DEFAULT);
	}

	public void setExtraConfigs(String extra_configs) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(EXTRA_CONFIGS, extra_configs);
		editor.commit();
	}

	public String getDnsProxyPath() {
		return prefs.getString(DNS_PROXY_PATH, "");
	}

	public void setDnsProxyPath(String path) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(DNS_PROXY_PATH, path);
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
		return "::";
	}

	public int getTProxyPort() {
		return 1088;
	}

	public String getDNSFwdAddress() {
		return "127.0.0.1";
	}

	public int getDNSFwdPort() {
		return 5300;
	}

	public String getProxyDns1Address() {
		return "8.8.8.8";
	}

	public String getProxyDns2Address() {
		return "8.8.4.4";
	}
}
