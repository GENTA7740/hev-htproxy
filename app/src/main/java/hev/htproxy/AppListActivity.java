/* MainActivity.java
 * Heiher <r@hev.cc>
 */

package hev.htproxy;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.Iterator;
import java.util.Comparator;
import java.util.Collections;
import android.util.SparseBooleanArray;
import android.app.ListActivity;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.content.Context;
import android.content.Intent;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.ApplicationInfo;
import android.view.View;

import hev.htproxy.AppArrayAdapter;

public class AppListActivity extends ListActivity
{
	private Preferences prefs;
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

		bindService(new Intent(this, TProxyService.class),
				mTProxyConnection, Context.BIND_AUTO_CREATE);

		getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

		PackageManager pm = getPackageManager();
		List<PackageInfo> pkginfos = pm.getInstalledPackages(0);
		Iterator<PackageInfo> iter = pkginfos.iterator();
		while (iter.hasNext()) {
			if (iter.next().packageName.equals(getPackageName())) {
				iter.remove();
				break;
			}
		}
		Collections.sort(pkginfos, new Comparator<PackageInfo>() {
			public int compare(PackageInfo a, PackageInfo b) {
				String aLabel, bLabel;
				PackageManager pm = getPackageManager();
				aLabel = a.applicationInfo.loadLabel(pm).toString();
				bLabel = b.applicationInfo.loadLabel(pm).toString();
				if (aLabel.equals(bLabel))
				  return 0;
				return aLabel.compareTo(bLabel);
			}
		});

		AppArrayAdapter adapter = new AppArrayAdapter(this, pkginfos.toArray(new PackageInfo[0]));
		setListAdapter(adapter);

		prefs = new Preferences(this);
		Set<String> applications = prefs.getApplications();
		for (int i = 0; i < pkginfos.size(); i++) {
			PackageInfo pkginfo = adapter.getItem(i);
			if (applications.contains(pkginfo.packageName))
			  getListView().setItemChecked(i, true);
		}
	}

	@Override
	protected void onDestroy() {
		ListView list_view = getListView();
		AppArrayAdapter adapter = (AppArrayAdapter) list_view.getAdapter();
		int count = list_view.getCount();
		SparseBooleanArray sb_array = list_view.getCheckedItemPositions();
		Set<String> applications = new HashSet<String>();
		Set<String> uids = new HashSet<String>();
		for (int i = 0; i < count; i++) {
			if (!sb_array.get(i))
			  continue;

			PackageInfo pkginfo = adapter.getItem(i);
			ApplicationInfo appinfo = pkginfo.applicationInfo;

			applications.add(pkginfo.packageName);
			String uid = Integer.toString(appinfo.uid);
			if (!uids.contains(uid))
			  uids.add(uid);
		}

		prefs.setApplications(applications);
		prefs.setUIDs(uids);

		if (mTProxyService != null) {
			Message msg = Message.obtain(null,
				TProxyService.MessageHandler.TYPE_RESET_PROXY_UIDS);
			try {
				mTProxyService.send(msg);
			} catch (RemoteException e) {
			}
		}

		unbindService(mTProxyConnection);

		super.onDestroy();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		CheckBox checkbox = (CheckBox) v.findViewById(R.id.checked);
		checkbox.setChecked(l.isItemChecked(position));
	}
}
