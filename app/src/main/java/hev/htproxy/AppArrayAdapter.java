/*
 * AppArrayAdapter.java
 * Heiher <r@hev.cc>
 */

package hev.htproxy;

import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.ApplicationInfo;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;

public class AppArrayAdapter extends ArrayAdapter<PackageInfo> {
	private final Context context;
	private final PackageInfo[] values;

	public AppArrayAdapter(Context context, PackageInfo[] values) {
		super(context, R.layout.appitem, values);
		this.context = context;
		this.values = values;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.appitem, parent, false);
		ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
		TextView textView = (TextView) rowView.findViewById(R.id.name);
		CheckBox checkBox = (CheckBox) rowView.findViewById(R.id.checked);

		PackageManager pm = context.getPackageManager();
		ApplicationInfo appinfo = values[position].applicationInfo;
		imageView.setImageDrawable(appinfo.loadIcon(pm));
		textView.setText(appinfo.loadLabel(pm).toString());

		ListView listView = (ListView) parent;
		checkBox.setChecked(listView.isItemChecked(position));

		return rowView;
	}
}
