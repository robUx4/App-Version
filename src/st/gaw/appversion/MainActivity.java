package st.gaw.appversion;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends ListActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setListAdapter(new AppsAdapter(this));
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		final ResolveInfo info = (ResolveInfo) l.getItemAtPosition(position);
		AlertDialog.Builder d = new AlertDialog.Builder(this);
		d.setTitle(info.loadLabel(getPackageManager()))
		.setItems(R.array.context_choice, new DialogInterface.OnClickListener() {
			@SuppressLint("NewApi")
			@SuppressWarnings("deprecation")
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (0==which) {
					StringBuilder sb = new StringBuilder(64);
					sb.append(info.loadLabel(getPackageManager()));
					try {
						PackageInfo pInfo = getPackageManager().getPackageInfo(info.activityInfo.packageName, 0);
						sb.append(", ")
						.append(String.format(getString(R.string.versionCode), pInfo.versionCode))
						.append(", ")
						.append(String.format(getString(R.string.versionName), pInfo.versionName));
					} catch (NameNotFoundException e) {
					}
					try {
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
							ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
							clipboard.setPrimaryClip(ClipData.newPlainText("AppVersion", sb));
						} else {
							android.text.ClipboardManager clipboardDeprecated = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
							clipboardDeprecated.setText(sb); 
						}
					} catch (Throwable e) {
					}
				} else if (1==which) {
					Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND); 
					emailIntent.setType("text/plain"); 
					emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, String.format(getString(R.string.shareTitle), info.loadLabel(getPackageManager())));
					final StringBuilder sb = new StringBuilder(64);
					sb.append(info.loadLabel(getPackageManager()));
					try {
						PackageInfo pInfo = getPackageManager().getPackageInfo(info.activityInfo.packageName, 0);
						sb.append('\n')
						.append(String.format(getString(R.string.versionCode), pInfo.versionCode))
						.append('\n')
						.append(String.format(getString(R.string.versionName), pInfo.versionName));
					} catch (NameNotFoundException e) {
					}
					emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, sb.toString());
					if (null!=getPackageManager().resolveActivity(emailIntent, PackageManager.MATCH_DEFAULT_ONLY)) {
						startActivity(Intent.createChooser(emailIntent, getString(R.string.menu_share)));
					}
				}
			}
		})
		.show();
	}

	private class AppsAdapter extends BaseAdapter {

		private final List<ResolveInfo> pkgAppsList;
		private final LayoutInflater mInflater;

		AppsAdapter(Activity context) {
			final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
			mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			this.pkgAppsList = context.getPackageManager().queryIntentActivities( mainIntent, 0);
			this.mInflater = context.getLayoutInflater();

			Collections.sort(pkgAppsList, new Comparator<ResolveInfo>() {
				@Override
				public int compare(ResolveInfo lhs, ResolveInfo rhs) {
					return lhs.loadLabel(mInflater.getContext().getPackageManager()).toString().compareToIgnoreCase(rhs.loadLabel(mInflater.getContext().getPackageManager()).toString());
				}
			});
		}

		@Override
		public int getCount() {
			return pkgAppsList.size();
		}

		@Override
		public Object getItem(int position) {
			return pkgAppsList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (null==convertView) {
				//convertView = mInflater.inflate(android.R.layout.simple_expandable_list_item_2, parent, false);
				convertView = mInflater.inflate(R.layout.activity_main, parent, false);
			}
			ResolveInfo r = pkgAppsList.get(position);
			TextView t1 = (TextView) convertView.findViewById(android.R.id.text1);
			t1.setText(r.loadLabel(mInflater.getContext().getPackageManager()));

			try {
				PackageInfo pInfo = getPackageManager().getPackageInfo(r.activityInfo.packageName, 0);
				TextView t2 = (TextView) convertView.findViewById(android.R.id.text2);
				StringBuilder sb = new StringBuilder(32);
				sb.append('v');
				sb.append(pInfo.versionCode);
				sb.append(" - \"");
				sb.append(pInfo.versionName);
				sb.append('\"');
				t2.setText(sb);
			} catch (NameNotFoundException e) {
			}

			ImageView icon = (ImageView) convertView.findViewById(android.R.id.icon);
			icon.setImageDrawable(r.loadIcon(mInflater.getContext().getPackageManager()));

			return convertView;
		}

	}

}
