package com.example.android.BluetoothChat;

import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class DeviceListActivity extends Activity {
	//Log Cat
	private static final String TAG = "DeviceListActivity";
	private static final boolean D = true;

	//Return Intent extra for new device
	public static String EXTRA_DEVICE_ADDRESS = "device_address";

	// Bluetooth Fields
	private BluetoothAdapter  BtAdapter;
	private ArrayAdapter<String> PairedDevicesArrayAdapter;
	private ArrayAdapter<String> NewDevicesArrayAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Setup the layout
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.device_list);

		// Set result CANCELED in case the user backs out
		setResult(Activity.RESULT_CANCELED);

		Button scanButton = (Button) findViewById(R.id.button_scan);
		scanButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				doDiscovery();
				v.setVisibility(View.GONE);
			}
		});

		// Initialize array adapters for new and paired devices 
		PairedDevicesArrayAdapter = new ArrayAdapter<String>(this,
				R.layout.device_name);
		NewDevicesArrayAdapter = new ArrayAdapter<String>(this,
				R.layout.device_name);

		ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
		pairedListView.setAdapter(PairedDevicesArrayAdapter);
		pairedListView.setOnItemClickListener(mDeviceClickListener);

		ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
		newDevicesListView.setAdapter(NewDevicesArrayAdapter);
		newDevicesListView.setOnItemClickListener(mDeviceClickListener);

		//Registering Broadcast reciever for new devices
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		this.registerReceiver(mReceiver, filter);

		// Register for broadcasts when discovery has finished
		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		this.registerReceiver(mReceiver, filter);

		//bluetooth adapter
		BtAdapter = BluetoothAdapter.getDefaultAdapter();

		// setting paired devices
		Set<BluetoothDevice> pairedDevices = BtAdapter.getBondedDevices();

		//adding paired devices to adapter
		if (pairedDevices.size() > 0) {
			findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
			for (BluetoothDevice device : pairedDevices) {
				PairedDevicesArrayAdapter.add(device.getName() + "\n"
						+ device.getAddress());
			}
		} else {
			String noDevices = getResources().getText(R.string.none_paired)
					.toString();
			PairedDevicesArrayAdapter.add(noDevices);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		// cancelling discovery
		if (BtAdapter != null) {
			BtAdapter.cancelDiscovery();
		}

		// Unregister broadcast reciever
		this.unregisterReceiver(mReceiver);
	}

	private void doDiscovery() {
		if (D)
			Log.d(TAG, "doDiscovery()");

		setProgressBarIndeterminateVisibility(true);
		setTitle(R.string.scanning);

		// Turn on sub-title for new devices
		findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);

		// If already discovering, stop it
		if (BtAdapter.isDiscovering()) {
			BtAdapter.cancelDiscovery();
		}
		
		//Request discover from BluetoothAdapter
		BtAdapter.startDiscovery();
	}

	private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
			BtAdapter.cancelDiscovery();

			//Getting device name and address
			String info = ((TextView) v).getText().toString();
			String address = info.substring(info.length() - 17);

			//intent for passing address
			Intent intent = new Intent();
			intent.putExtra(EXTRA_DEVICE_ADDRESS, address);

			//setting result
			setResult(Activity.RESULT_OK, intent);
			finish();
		}
	};

		private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			//finding device
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
					NewDevicesArrayAdapter.add(device.getName() + "\n"
							+ device.getAddress());
				}
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
					.equals(action)) {
				setProgressBarIndeterminateVisibility(false);
				setTitle(R.string.select_device);
				if (NewDevicesArrayAdapter.getCount() == 0) {
					String noDevices = getResources().getText(
							R.string.none_found).toString();
					NewDevicesArrayAdapter.add(noDevices);
				}
			}
		}
	};

}
