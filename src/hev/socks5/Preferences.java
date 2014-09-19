package hev.socks5;

import android.content.Context;
import android.content.SharedPreferences;

public class Preferences
{
	public static final String PREFS_NAME = "Socks5Prefs";
	public static final String LOCAL_ADDRESS = "LocalAddress";
	public static final String LOCAL_PORT = "LocalPort";
	public static final String SERVER_ADDRESS = "ServerAddress";
	public static final String SERVER_PORT = "ServerPort";

	private SharedPreferences prefs;

	public Preferences(Context context) {
		prefs = context.getSharedPreferences(PREFS_NAME, 0);
	}

	public String getLocalAddress() {
		return prefs.getString(LOCAL_ADDRESS, "127.0.0.1");
	}

	public void setLocalAddress(String address) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(LOCAL_ADDRESS, address);
		editor.commit();
	}

	public int getLocalPort() {
		return prefs.getInt(LOCAL_PORT, 1080);
	}

	public void setLocalPort(int port) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(LOCAL_PORT, port);
		editor.commit();
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
}
