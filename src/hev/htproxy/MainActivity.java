/* MainActivity.java
 * Heiher <r@hev.cc>
 */

package hev.htproxy;

import java.util.Set;
import java.util.HashSet;
import android.app.Activity;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.content.Context;
import android.content.Intent;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.widget.EditText;
import android.widget.CheckBox;
import android.widget.Button;
import android.widget.Scroller;
import android.text.method.ScrollingMovementMethod;
import android.view.View;

public class MainActivity extends Activity implements View.OnClickListener
{
	private Preferences prefs;
	private EditText edittext_server_address;
	private EditText edittext_server_port;
	private EditText edittext_bypass_addresses;
	private EditText edittext_password;
	private EditText edittext_extra_configs;
	private CheckBox checkbox_allow_edit;
	private CheckBox checkbox_global_proxy;
	private Button button_applications;
	private Button button_restart;
	private Button button_control;
	private Messenger mTProxyService = null;

	private ServiceConnection mTProxyConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			mTProxyService = new Messenger(service);
		}

		public void onServiceDisconnected(ComponentName className) {
			mTProxyService = null;
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		prefs = new Preferences(this);
		setContentView(R.layout.main);

		edittext_server_address = (EditText) findViewById(R.id.server_address);
		edittext_server_port = (EditText) findViewById(R.id.server_port);
		edittext_bypass_addresses = (EditText) findViewById(R.id.bypass_addresses);
		edittext_password = (EditText) findViewById(R.id.password);
		edittext_extra_configs = (EditText) findViewById(R.id.extra_configs);
		checkbox_allow_edit = (CheckBox) findViewById(R.id.allow_edit);
		checkbox_global_proxy = (CheckBox) findViewById(R.id.global_proxy);
		button_applications = (Button) findViewById(R.id.applications);
		button_restart = (Button) findViewById(R.id.restart);
		button_control = (Button) findViewById(R.id.control);

		edittext_server_address.setText(prefs.getServerAddress());
		edittext_server_port.setText(Integer.toString(prefs.getServerPort()));
		StringBuilder builder = new StringBuilder();
		for (String addr : prefs.getBypassAddresses()) {
			if (0 < builder.length())
			  builder.append("\n");
			builder.append(addr);
		}
		edittext_bypass_addresses.setText(builder.toString());
		edittext_password.setText(prefs.getPassword());
		edittext_extra_configs.setText(prefs.getExtraConfigs());
		checkbox_allow_edit.setOnClickListener(this);
		checkbox_allow_edit.setChecked(false);
		checkbox_global_proxy.setOnClickListener(this);
		checkbox_global_proxy.setChecked(prefs.getGlobalProxy());
		button_applications.setOnClickListener(this);
		button_restart.setOnClickListener(this);
		button_control.setOnClickListener(this);

		/* tproxy service */
		Intent i = new Intent(this, TProxyService.class);
		startService(i);

		/* is Supported */
		button_control.setEnabled(false);
		if (RedirectManager.isSupported())
		  button_control.setEnabled(true);
		else
		  prefs.setHTProxyEnabled(false);
		/* is Enabled */
		boolean redir_enabled = RedirectManager.isEnabled(this);
		if (prefs.getHTProxyEnabled()) {
			if (!redir_enabled) {
				if (RedirectManager.setEnabled(true, this))
				  redir_enabled = true;
			}
		} else {
			if (redir_enabled) {
				if (RedirectManager.setEnabled(false, this))
				  redir_enabled = false;
			}
		}
		lockUI(redir_enabled);
	}

	@Override
	protected void onStart() {
		super.onStart();

		bindService(new Intent(this, TProxyService.class), mTProxyConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onStop() {
		unbindService(mTProxyConnection);

		super.onStop();
	}

	@Override
	protected void onDestroy() {
		savePrefs();

		super.onDestroy();
	}

	public void onClick(View view) {
		if (view == checkbox_allow_edit) {
			boolean allow_edit = checkbox_allow_edit.isChecked();
			edittext_server_address.setEnabled(allow_edit);
			edittext_server_port.setEnabled(allow_edit);
			edittext_bypass_addresses.setEnabled(allow_edit);
			edittext_password.setEnabled(allow_edit);
			edittext_extra_configs.setEnabled(allow_edit);
			checkbox_global_proxy.setEnabled(allow_edit);
			button_applications.setEnabled(!checkbox_global_proxy.isChecked() && allow_edit);
		} else if (view == checkbox_global_proxy) {
			button_applications.setEnabled(!checkbox_global_proxy.isChecked());
		} else if (view == button_applications) {
			startActivity(new Intent(this, AppListActivity.class));
		} else if (view == button_restart) {
			stopTProxyService();
			savePrefs();
			startTProxyService();
		} else if (view == button_control) {
			boolean redir_enabled = RedirectManager.isEnabled(this);
			if (redir_enabled) {
				if (RedirectManager.setEnabled(false, this))
				  redir_enabled = false;
			} else {
				savePrefs();
				if (RedirectManager.setEnabled(true, this))
				  redir_enabled = true;
			}
			lockUI(redir_enabled);
			prefs.setHTProxyEnabled(redir_enabled);
		}
	}

	private void lockUI(boolean lock) {
		boolean allow_edit = checkbox_allow_edit.isChecked();
		if (lock)
		  button_control.setText(R.string.control_disable);
		else
		  button_control.setText(R.string.control_enable);
		checkbox_allow_edit.setEnabled(!lock);
		checkbox_global_proxy.setEnabled(!lock && allow_edit);
		button_applications.setEnabled(!lock && allow_edit &&
						!checkbox_global_proxy.isChecked());
		button_restart.setEnabled(!lock);
		edittext_server_address.setEnabled(!lock && allow_edit);
		edittext_server_port.setEnabled(!lock && allow_edit);
		edittext_bypass_addresses.setEnabled(!lock && allow_edit);
		edittext_password.setEnabled(!lock && allow_edit);
		edittext_extra_configs.setEnabled(!lock && allow_edit);
	}

	private void savePrefs() {
		String[] addrs;
		Set<String> bypass_addresses = new HashSet<String>();

		prefs.setServerAddress(edittext_server_address.getText().toString());
		prefs.setServerPort(Integer.parseInt(edittext_server_port.getText().toString()));

		addrs = edittext_bypass_addresses.getText().toString().split("\n");
		for (String addr : addrs) {
			if (!addr.isEmpty())
			  bypass_addresses.add(addr);
		}
		prefs.setBypassAddresses(bypass_addresses);
		prefs.setPassword(edittext_password.getText().toString());

		prefs.setExtraConfigs(edittext_extra_configs.getText().toString());
		prefs.setGlobalProxy(checkbox_global_proxy.isChecked());
	}

	private void startTProxyService() {
		if (null == mTProxyService)
		  return;

		try {
			Message msg = Message.obtain(null, TProxyService.MessageHandler.TYPE_START);
			mTProxyService.send(msg);
		} catch (RemoteException e) {
		}
	}

	private void stopTProxyService() {
		if (null == mTProxyService)
		  return;

		try {
			Message msg = Message.obtain(null, TProxyService.MessageHandler.TYPE_STOP);
			mTProxyService.send(msg);
		} catch (RemoteException e) {
		}
	}
}
