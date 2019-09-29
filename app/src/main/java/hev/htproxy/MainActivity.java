/*
 * MainActivity.java
 * Heiher <r@hev.cc>
 */

package hev.htproxy;

import java.util.Set;
import java.util.HashSet;
import android.app.Activity;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import android.widget.CheckBox;
import android.widget.Button;
import android.widget.Scroller;
import android.text.method.ScrollingMovementMethod;
import android.net.VpnService;

public class MainActivity extends Activity implements View.OnClickListener {
	private Preferences prefs;
	private EditText edittext_configs;
	private CheckBox checkbox_editable;
	private CheckBox checkbox_global;
	private CheckBox checkbox_ipv4;
	private CheckBox checkbox_ipv6;
	private Button button_applications;
	private Button button_control;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);
		prefs = new Preferences(this);

		Intent intent = VpnService.prepare(MainActivity.this);
		if (intent != null)
		  startActivityForResult(intent, 0);
		else
		  onActivityResult(0, RESULT_OK, null);

		edittext_configs = (EditText) findViewById(R.id.configs);
		checkbox_editable = (CheckBox) findViewById(R.id.editable);
		checkbox_global = (CheckBox) findViewById(R.id.global);
		checkbox_ipv4 = (CheckBox) findViewById(R.id.ipv4);
		checkbox_ipv6 = (CheckBox) findViewById(R.id.ipv6);
		button_applications = (Button) findViewById(R.id.applications);
		button_control = (Button) findViewById(R.id.control);

		edittext_configs.setText(prefs.getConfigs());
		checkbox_editable.setOnClickListener(this);
		checkbox_editable.setChecked(false);
		checkbox_global.setOnClickListener(this);
		checkbox_global.setChecked(prefs.getGlobal());
		checkbox_ipv4.setChecked(prefs.getIpv4());
		checkbox_ipv6.setChecked(prefs.getIpv6());
		button_applications.setOnClickListener(this);
		button_control.setOnClickListener(this);
		if (prefs.getEnable())
		  button_control.setText(R.string.control_disable);

		setEditable();
	}

	@Override
	protected void onDestroy() {
		savePrefs();
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int request, int result, Intent data) {
		if ((result == RESULT_OK) && prefs.getEnable()) {
			Intent intent = new Intent(this, TProxyService.class);
			startService(intent.setAction(TProxyService.ACTION_CONNECT));
		}
	}

	public void onClick(View view) {
		if (view == checkbox_editable) {
			setEditable();
		} else if (view == checkbox_global) {
			button_applications.setEnabled(!checkbox_global.isChecked());
		} else if (view == button_applications) {
			startActivity(new Intent(this, AppListActivity.class));
		} else if (view == button_control) {
			boolean isEnable = prefs.getEnable();
			Intent intent = new Intent(this, TProxyService.class);
			if (isEnable) {
				button_control.setText(R.string.control_enable);
				startService(intent.setAction(TProxyService.ACTION_DISCONNECT));
			} else {
				button_control.setText(R.string.control_disable);
				savePrefs();
				startService(intent.setAction(TProxyService.ACTION_CONNECT));
			}
			prefs.setEnable(!isEnable);
			setEditable();
		}
	}

	private void setEditable() {
		boolean editable = checkbox_editable.isChecked();
		edittext_configs.setEnabled(editable);
		checkbox_global.setEnabled(editable);
		checkbox_ipv4.setEnabled(editable);
		checkbox_ipv6.setEnabled(editable);
		button_applications.setEnabled(!checkbox_global.isChecked() && editable);
		checkbox_editable.setEnabled(!prefs.getEnable());
	}

	private void savePrefs() {
		prefs.setConfigs(edittext_configs.getText().toString());
		prefs.setGlobal(checkbox_global.isChecked());
		if (!checkbox_ipv4.isChecked() && !checkbox_ipv4.isChecked())
		  checkbox_ipv4.setChecked(prefs.getIpv4());
		prefs.setIpv4(checkbox_ipv4.isChecked());
		prefs.setIpv6(checkbox_ipv6.isChecked());
	}
}
