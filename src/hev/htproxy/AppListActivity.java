/* MainActivity.java
 * Heiher <r@hev.cc>
 */

package hev.htproxy;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import android.util.SparseBooleanArray;
import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.content.pm.PackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.ApplicationInfo;
import android.view.View;

import hev.htproxy.AppArrayAdapter;

public class AppListActivity extends ListActivity
{
	private Preferences prefs;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

		PackageManager pm = getPackageManager();
		List<PackageInfo> pkginfos = pm.getInstalledPackages(0);
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

		super.onDestroy();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		CheckBox checkbox = (CheckBox) v.findViewById(R.id.checked);
		checkbox.setChecked(l.isItemChecked(position));
	}
}
