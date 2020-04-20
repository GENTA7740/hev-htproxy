/*
 * MainActivity.java
 * Heiher <r@hev.cc>
 */

package hev.htproxy;

import java.net.URL;
import java.net.Authenticator;
import java.net.URLConnection;
import java.net.PasswordAuthentication;
import java.net.MalformedURLException;
import java.io.InputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import android.os.Bundle;
import android.os.AsyncTask;
import android.app.Activity;
import android.content.Intent;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import android.net.VpnService;

public class MainActivity extends Activity implements View.OnClickListener {
	private Preferences prefs;
	private EditText edittext_config_url;
	private EditText edittext_username;
	private EditText edittext_password;
	private CheckBox checkbox_global;
	private CheckBox checkbox_ipv4;
	private CheckBox checkbox_ipv6;
	private Button button_apps;
	private Button button_update;
	private Button button_control;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		prefs = new Preferences(this);
		setContentView(R.layout.main);

		edittext_config_url = (EditText) findViewById(R.id.config_url);
		edittext_username = (EditText) findViewById(R.id.username);
		edittext_password = (EditText) findViewById(R.id.password);
		checkbox_ipv4 = (CheckBox) findViewById(R.id.ipv4);
		checkbox_ipv6 = (CheckBox) findViewById(R.id.ipv6);
		checkbox_global = (CheckBox) findViewById(R.id.global);
		button_apps = (Button) findViewById(R.id.apps);
		button_update = (Button) findViewById(R.id.update);
		button_control = (Button) findViewById(R.id.control);

		checkbox_global.setOnClickListener(this);
		button_apps.setOnClickListener(this);
		button_update.setOnClickListener(this);
		button_control.setOnClickListener(this);
		updateUI();

		/* Request VPN permission */
		Intent intent = VpnService.prepare(MainActivity.this);
		if (intent != null)
		  startActivityForResult(intent, 0);
		else
		  onActivityResult(0, RESULT_OK, null);
	}

	@Override
	protected void onActivityResult(int request, int result, Intent data) {
		if ((result == RESULT_OK) && prefs.getEnable()) {
			Intent intent = new Intent(this, TProxyService.class);
			startService(intent.setAction(TProxyService.ACTION_CONNECT));
		}
	}

	@Override
	public void onClick(View view) {
		if (view == checkbox_global) {
			savePrefs();
			updateUI();
		} else if (view == button_apps) {
			startActivity(new Intent(this, AppListActivity.class));
		} else if (view == button_update) {
			savePrefs();
			updateConfigs();
		} else if (view == button_control) {
			boolean isEnable = prefs.getEnable();
			prefs.setEnable(!isEnable);
			savePrefs();
			updateUI();
			Intent intent = new Intent(this, TProxyService.class);
			if (isEnable)
			  startService(intent.setAction(TProxyService.ACTION_DISCONNECT));
			else
			  startService(intent.setAction(TProxyService.ACTION_CONNECT));
		}
	}

	private void updateUI() {
		edittext_config_url.setText(prefs.getConfigUrl());
		edittext_username.setText(prefs.getUsername());
		edittext_password.setText(prefs.getPassword());
		checkbox_ipv4.setChecked(prefs.getIpv4());
		checkbox_ipv6.setChecked(prefs.getIpv6());
		checkbox_global.setChecked(prefs.getGlobal());

		boolean editable = !prefs.getEnable();
		edittext_config_url.setEnabled(editable);
		edittext_username.setEnabled(editable);
		edittext_password.setEnabled(editable);
		checkbox_global.setEnabled(editable);
		checkbox_ipv4.setEnabled(editable);
		checkbox_ipv6.setEnabled(editable);
		button_apps.setEnabled(editable && !prefs.getGlobal());
		button_update.setEnabled(editable);

		if (editable)
		  button_control.setText(R.string.control_enable);
		else
		  button_control.setText(R.string.control_disable);
		button_control.setEnabled(!prefs.getConfigs().isEmpty());
	}

	private void savePrefs() {
		prefs.setConfigUrl(edittext_config_url.getText().toString());
		prefs.setUsername(edittext_username.getText().toString());
		prefs.setPassword(edittext_password.getText().toString());
		if (!checkbox_ipv4.isChecked() && !checkbox_ipv4.isChecked())
		  checkbox_ipv4.setChecked(prefs.getIpv4());
		prefs.setIpv4(checkbox_ipv4.isChecked());
		prefs.setIpv6(checkbox_ipv6.isChecked());
		prefs.setGlobal(checkbox_global.isChecked());
	}

	private void updateConfigs() {
		Authenticator.setDefault(new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				String user = prefs.getUsername();
				String pass = prefs.getPassword();
				return new PasswordAuthentication(user, pass.toCharArray());
			}
		});

		AsyncTaskRunner task = new AsyncTaskRunner();
		task.execute();
		Toast.makeText(this, "Updating configs...", Toast.LENGTH_LONG).show();
	}

	private class AsyncTaskRunner extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... params) {
			Preferences prefs = new Preferences(getApplicationContext());
			boolean res = false;
			URL url;

			try {
				url = new URL(prefs.getConfigUrl());
			} catch (MalformedURLException e) {
				return false;
			}

			try {
				URLConnection conn = url.openConnection();
				conn.setConnectTimeout(5000);
				conn.setReadTimeout(5000);
				int s, size = 0, total = 16384;
				byte[] content = new byte[total + 1];
				InputStream istream = conn.getInputStream();
				for (; (total - size) > 0; size += s) {
					s = istream.read(content, size, total - size);
					if (s <= 0)
					  break;
				}
				if (size > 0) {
					prefs.setConfigs(new String(content, 0, size, StandardCharsets.UTF_8));
					res = true;
				}
				istream.close();
			} catch (IOException e) {
				return false;
			}

			return res;
		}

		@Override
		protected void onPostExecute (Boolean success) {
			Context context = getApplicationContext();
			if (!success) {
				Toast.makeText(context, "Update configs failed!", Toast.LENGTH_SHORT).show();
				return;
			}
			updateUI();
			Toast.makeText(context, "Update configs successful!", Toast.LENGTH_SHORT).show();
		}
	}
}
