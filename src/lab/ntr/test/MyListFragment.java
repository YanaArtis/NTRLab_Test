package lab.ntr.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import lab.ntr.test.R;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MyListFragment extends ListFragment implements
		LoaderCallbacks<Void> {

	public static final String TAG = "Test";

	private MyAdapter mAdapter;
	private ArrayList<MyData> mItems;
	private LayoutInflater mInflater;
	private Button mBtnReload;

	private boolean mFirstRun = true;
	private final Handler mHandler = new Handler();
	private Resources mRes;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setRetainInstance(true);

		// LoaderManager.enableDebugLogging(true);

		mRes = getResources();
		mInflater = LayoutInflater.from(getActivity());

		mBtnReload = (Button) getActivity().findViewById(R.id.btn);
		mBtnReload.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mFirstRun) {
					mFirstRun = false;
					mBtnReload.setText(mRes.getString(R.string.restart));
					startLoading();
				} else {
					restartLoading();
				}
			}
		});
		
		if (mAdapter == null) {
			mItems = new ArrayList<MyData>();
			mAdapter = new MyAdapter(getActivity(), mItems);
		}
		getListView().setAdapter(mAdapter);

		LoaderManager lm = getLoaderManager();
		if (lm.getLoader(0) != null) {
			lm.initLoader(0, null, this);
		}

		if (!mFirstRun) {
			mBtnReload.setText(mRes.getString(R.string.restart));
		}
	}

	protected void startLoading() {
		showDialog();
		getLoaderManager().initLoader(0, null, this);
	}

	protected void restartLoading() {
		showDialog();

		mItems.clear();
		mAdapter.notifyDataSetChanged();
		getListView().invalidateViews();

		Log.d(TAG, "restartLoading(): re-starting loader");
		getLoaderManager().restartLoader(0, null, this);
	}
	
	private String getJsonString (JSONObject jo, String tag) throws JSONException {
		return jo.isNull(tag) ? null : jo.getString(tag);
	}

	@Override
	public Loader<Void> onCreateLoader(int id, Bundle args) {
		AsyncTaskLoader<Void> loader = new AsyncTaskLoader<Void>(getActivity()) {

			@Override
			public Void loadInBackground() {
	            HttpURLConnection urlConnection = null;
	            BufferedReader reader = null;
	            String jsonStr = null;

	            try {
					URL url = new URL("https://dl.dropboxusercontent.com/s/f3x990vmoxp5gis/test.json");
	                urlConnection = (HttpURLConnection) url.openConnection();
	                urlConnection.setRequestMethod("GET");
	                urlConnection.connect();

	                InputStream inputStream = urlConnection.getInputStream();
	                if (inputStream == null) {
	                    return null;
	                }
	                StringBuffer buffer = new StringBuffer();
	                reader = new BufferedReader(new InputStreamReader(inputStream));

	                String line;
	                while ((line = reader.readLine()) != null) {
	                    buffer.append(line+"\n");
	                }

	                if (buffer.length() < 1) {
	                    return null;
	                }
	                jsonStr = buffer.toString();
	            } catch (IOException e) {
	                e.printStackTrace();
	                getActivity().runOnUiThread(new Runnable () {
						@Override
						public void run() {
							Toast.makeText(getActivity(), getActivity().getString(R.string.io_error_msg), Toast.LENGTH_LONG).show();
						}
	                });
	                return null;
	            } finally {
	                if (urlConnection != null) {
	                    urlConnection.disconnect();
	                }
	                if (reader != null) {
	                    try {
	                        reader.close();
	                    } catch (final IOException e) {
	                        e.printStackTrace();
	                    }
	                }
	            }

//jsonStr = "{\"id\":\"365828\",\"name\":\"Object 218\",\"type\":\"OWN\",\"date_opened\":\"2014-07-01 00:00:00\",\"date_closed\":null,\"address\":{\"additional_information\":null,\"zip\":null,\"country\":\"RUS\",\"region\":\"22\",\"area\":null,\"city\":\"Some city\",\"town\":null,\"street\":\"Lenina st\",\"house\":null,\"case\":null,\"apartment\":null,\"gps_lat\":56.233969,\"gps_lng\":43.388959},\"phone\":\"84991234567\",\"status\":\"258\",\"products\":[\"SHOP\",\"CAFE\"]}";
	    		Log.d(TAG, "jsonStr: "+jsonStr);

	            try {
	                JSONObject jObj = new JSONObject(jsonStr);
	    			mItems.clear();
	    			String sName = getJsonString(jObj, "name");
	    			JSONObject joAddr = jObj.getJSONObject("address");
	    			String sZip = getJsonString(joAddr, "zip");
	    			String sCity = getJsonString(joAddr, "city");
	    			String sTown = getJsonString(joAddr, "town");
	    			String sStreet = getJsonString(joAddr, "street");
	    			String sHouse = getJsonString(joAddr, "house");
	    			String sCase = getJsonString(joAddr, "case");
	    			String sApartment = getJsonString(joAddr, "apartment");
	    			StringBuilder sbAddr = new StringBuilder
	    					( ((sStreet == null) ? "" : sStreet + " ")
	    					+ ((sHouse == null) ? "" : (sHouse + " "))
	    					+ ((sCase == null) ? "" : (getActivity().getString(R.string.addr_case)+" "+sCase+" "))
	    					+ ((sApartment == null) ? "" : (getActivity().getString(R.string.addr_apt)+" "+sApartment+" "))
	    					);
	    			StringBuilder sbAddr2 = new StringBuilder();
	    			if (sTown != null) {
	    				if (sbAddr2.length() > 0) { sbAddr2.append(", "); }
	    				sbAddr2.append(sTown);
	    			}
	    			if (sCity != null) {
	    				if (sbAddr2.length() > 0) { sbAddr2.append(", "); }
	    				sbAddr2.append(sCity);
	    			}
	    			if (sZip != null) {
	    				if (sbAddr2.length() > 0) { sbAddr2.append(", "); }
	    				sbAddr2.append(sZip);
	    			}
	    			if (sbAddr2.length() > 0) {
	    				if (sbAddr.length() > 0) { sbAddr.append("; "); }
	    				sbAddr.append(sbAddr2);
	    			}
	    			
	    			String sPhone = getJsonString(jObj, "phone");
	    			String sDateOpened = getJsonString(jObj, "date_opened");
	    			String sDateClosed = getJsonString(jObj, "date_closed");
	    			
	    			mItems.add(new MyData(getActivity().getString(R.string.title_name), sName));
	    			mItems.add(new MyData(getActivity().getString(R.string.title_address), sbAddr.toString()));
	    			mItems.add(new MyData(getActivity().getString(R.string.title_phone), sPhone));
	    			mItems.add(new MyData(getActivity().getString(R.string.title_date_opened), sDateOpened));
	    			mItems.add(new MyData(getActivity().getString(R.string.title_date_closed), sDateClosed));
	            } catch (JSONException e) {
	                e.printStackTrace();
	            }
	            return null;
			}
		};
		loader.forceLoad();
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Void> loader, Void result) {
		mAdapter.notifyDataSetChanged();
		hideDialog();
		Log.d(TAG, "onLoadFinished(): done loading!");
	}

	@Override
	public void onLoaderReset(Loader<Void> loader) {}
	
	private class MyData {
		public String name;
		public String value;
		
		public MyData (String name, String value) {
			this.name = name;
			this.value = value;
		}
	}
	
	private class MyAdapter extends BaseAdapter {
		private ArrayList<MyData> list;
		
		public MyAdapter (Context context, ArrayList<MyData> list) {
			this.list = list;
		}

		@Override
		public int getCount() {
			return (list == null) ? -1 : list.size();
		}

		@Override
		public Object getItem(int n) {
			return (list == null) ? null : list.get(n);
		}

		@Override
		public long getItemId(int n) {
			return n;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			MyDataViewHolder vh;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.list_item, parent, false);
				vh = new MyDataViewHolder(convertView);
				convertView.setTag(vh);
			} else {
				vh = (MyDataViewHolder) convertView.getTag();
			}
			MyData data = (MyData) getItem(position);
			vh.getNameView().setText(data.name);
			vh.getValueView().setText(data.value);
			
			return convertView;
		}
	}

	private static class MyDataViewHolder {
		private final View mRoot;
		private TextView mName;
		private TextView mValue;

		public MyDataViewHolder(View root) {
			mRoot = root;
		}

		public TextView getNameView() {
			if (mName == null) {
				mName = (TextView) mRoot.findViewById(R.id.name);
			}
			return mName;
		}

		public TextView getValueView() {
			if (mValue == null) {
				mValue = (TextView) mRoot.findViewById(R.id.value);
			}
			return mValue;
		}
	}

	public static class MyAlertDialog extends DialogFragment {
		public MyAlertDialog() {}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			ProgressDialog progress = new ProgressDialog(getActivity());
			progress.setMessage(getString(R.string.loading));
			return progress;
		}
	}

	private void showDialog() {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		Fragment prev = getFragmentManager().findFragmentByTag("dialog");
		if (prev != null) {
			ft.remove(prev);
		}

		// Create and show the dialog.
		DialogFragment newFragment = new MyAlertDialog();
		newFragment.show(ft, "dialog");
	}

	private void hideDialog() {
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				FragmentTransaction ft = getFragmentManager()
						.beginTransaction();
				Fragment prev = getFragmentManager()
						.findFragmentByTag("dialog");
				if (prev != null) {
					ft.remove(prev).commit();
				}
			}
		});
	}
}