package com.example.android.BluetoothChat;

import java.io.File;

import android.R.string;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class BluetoothChat extends Activity {
	TextView songs_list_title;
	ListView songs_list;
	MediaPlayer mediaPlayer;
	String path;
	String[] songs;
	int song_number;
	private static final String TAG = "BluetoothChat";
	private static final boolean D = true;

	// Message types sent states
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";

	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE = 2;
	private static final int REQUEST_ENABLE_BT = 3;

	private ListView SongView;
	private EditText mOutEditText;
	private Button mSendButton;

	// Name of the connected device
	private String ConnectedDeviceName = null;
	
	private ArrayAdapter<String> SongArrayAdapter;
	
	private StringBuffer OutStringBuffer;
	
	private BluetoothAdapter BluetoothAdapter = null;

	// Member object for sending services
	private BluetoothChatService SongService = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (D)
			Log.e(TAG, "+++ ON CREATE +++");

		setContentView(R.layout.main);

		BluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		if (BluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available",
					Toast.LENGTH_LONG).show();
			finish();
			return;
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		if (D)
			Log.e(TAG, "++ ON START ++");

			if (!BluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			} else {
			if (SongService == null)
				setupChat();
		}
	}

	@Override
	public synchronized void onResume() {
		super.onResume();
		if (D)
			Log.e(TAG, "+ ON RESUME +");

				if (SongService != null) {
					if (SongService.getState() == BluetoothChatService.STATE_NONE) {
						SongService.start();
			}
		}
	}

	private void setupChat() {
		Log.d(TAG, "setupChat()");

			SongArrayAdapter = new ArrayAdapter<String>(this,
				R.layout.message);
		SongView = (ListView) findViewById(R.id.songs);
		SongView.setAdapter(SongArrayAdapter);
		songs_list_title=(TextView)findViewById(R.id.songs_list_title);
		songs_list=(ListView)findViewById(R.id.songs);
		path = Environment.getExternalStorageDirectory().toString()+"/music";
        Log.d("Files", "Path: " + path);
        File f = new File(path);
        File file[]=f.listFiles();
        songs=new String[file.length];
        for(int i=0;i<file.length;i++)
        {
        	songs[i]=file[i].getName();
        }
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, songs);
        songs_list.setAdapter(adapter);
        songs_list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, final int arg2,
					long arg3) {
				// TODO Auto-generated method stub
			//	Toast.makeText(getApplicationContext(), arg2, 2000).show();
				new AlertDialog.Builder(BluetoothChat.this)
				.setTitle("Dialog Box")
				.setMessage("Options Available")
				.setPositiveButton("Play", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						Intent intent=new Intent(BluetoothChat.this,Play_Activity.class);
						intent.putExtra("Song Number",songs[arg2]);
						startActivity(intent);
						//mediaPlayer.setDataSource()
					}
				})
				.setNegativeButton("Share", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						song_number=arg2;
						sendMessage(songs[arg2]);
					}
				}).show();
			}
		});


		// Initialize the BluetoothChatService
		SongService = new BluetoothChatService(this, mHandler);

		// Initialize the buffer
		OutStringBuffer = new StringBuffer("");
	}

	@Override
	public synchronized void onPause() {
		super.onPause();
		if (D)
			Log.e(TAG, "- ON PAUSE -");
	}

	@Override
	public void onStop() {
		super.onStop();
		if (D)
			Log.e(TAG, "-- ON STOP --");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Stop Bluetooth chat services
		if (SongService != null)
			SongService.stop();
		if (D)
			Log.e(TAG, "--- ON DESTROY ---");
	}

	private void ensureDiscoverable() {
		if (D)
			Log.d(TAG, "ensure discoverable");
		if (BluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent discoverableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(
					BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivity(discoverableIntent);
		}
	}

		public void sendMessage(String message) {
		if (SongService.getState() != BluetoothChatService.STATE_CONNECTED) {
			Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
					.show();
			return;
		}

		if (message.length() > 0) {
			// Get the message and then BluetoothChatService to write
			byte[] send = message.getBytes();
			SongService.write(send);

			// Reset out string buffer to 0
			OutStringBuffer.setLength(0);
		}
	}

	/*public void setname(String name)
	{
		song_name=name;
	}
	
	public String getname()
	{
		Toast.makeText(getApplicationContext(), song_name, 2000).show();
		return song_name;
	}*/
		
	private final void setStatus(int resId) {
		final ActionBar actionBar = getActionBar();
		actionBar.setSubtitle(resId);
	}

	private final void setStatus(CharSequence subTitle) {
		final ActionBar actionBar = getActionBar();
		actionBar.setSubtitle(subTitle);
	}

	//The Handler that gets information back from the BluetoothChatService
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				if (D)
					Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
				switch (msg.arg1) {
				case BluetoothChatService.STATE_CONNECTED:
					setStatus(getString(R.string.title_connected_to,
							ConnectedDeviceName));
					SongArrayAdapter.clear();
					break;
				case BluetoothChatService.STATE_CONNECTING:
					setStatus(R.string.title_connecting);
					break;
				case BluetoothChatService.STATE_LISTEN:
				case BluetoothChatService.STATE_NONE:
					setStatus(R.string.title_not_connected);
					break;
				}
				break;
			case MESSAGE_WRITE:
				byte[] writeBuf = (byte[]) msg.obj;
				// construct a string from the buffer
				String writeMessage = new String(writeBuf);
				SongArrayAdapter.add("Me:  " + writeMessage);
				break;
			case MESSAGE_READ:
				byte[] readBuf = (byte[]) msg.obj;
				final String readMessage = new String(readBuf, 0, msg.arg1);
				SongArrayAdapter.add(ConnectedDeviceName + ":  "
						+ readMessage);
				Toast.makeText(getApplicationContext(), readMessage, 2000).show();
				//group play code
				int check=check_song(readMessage);
				if(check==1)
				{
					new AlertDialog.Builder(BluetoothChat.this)
					.setTitle("Group Play Request")
					.setMessage("Start Group Play?")
					.setPositiveButton("START",new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
						BluetoothChat.this.sendMessage("Commencing");
						final Handler handler = new Handler();
						handler.postDelayed(new Runnable() {
						    @Override
						    public void run() {
						        // Do something after 5s = 5000ms
						        //buttons[inew][jnew].setBackgroundColor(Color.BLACK);
						    }
						}, 1000);
						group_play(readMessage);
						//setname(readMessage);
						}
					}).show();
					}
				else if(check==2)
				{
					
					group_play(songs[song_number]);
				}
				break;
			case MESSAGE_DEVICE_NAME:
				// save the connected device's name
				ConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				Toast.makeText(getApplicationContext(),
						"Connected to " + ConnectedDeviceName,
						Toast.LENGTH_SHORT).show();
				break;
			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(),
						msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
						.show();
				break;
			}
		}
	};
//group play function
	public void group_play(String string)
	{
		Toast.makeText(getApplicationContext(), string, 3500).show();
		Intent intent=new Intent(BluetoothChat.this,Play_Activity.class);
		intent.putExtra("Song Number",string);
		startActivity(intent);
		
	}
	//code for getting result
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (D)
			Log.d(TAG, "onActivityResult " + resultCode);
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE:
			if (resultCode == Activity.RESULT_OK) {
				connectDevice(data);
			}
			break;
		case REQUEST_ENABLE_BT:
			if (resultCode == Activity.RESULT_OK) {
				setupChat();
			} else {
				Log.d(TAG, "BT not enabled");
				Toast.makeText(this, R.string.bt_not_enabled_leaving,
						Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}

	private void connectDevice(Intent data) {
		// Get the device MAC address
		String address = data.getExtras().getString(
				DeviceListActivity.EXTRA_DEVICE_ADDRESS);
		// Get the BluetoothDevice object
		BluetoothDevice device = BluetoothAdapter.getRemoteDevice(address);
		SongService.connect(device);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.option_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent serverIntent = null;
		switch (item.getItemId()) {
		case R.id.connect_scan:
			serverIntent = new Intent(this, DeviceListActivity.class);
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
			return true;
		case R.id.discoverable:
			ensureDiscoverable();
			return true;
		}
		return false;
	}
	//Checking song availability function
	public int check_song(String string)
	{
		int c=0;
		if(string.equals("Commencing"))
		{
			c=2;
			return c;
		}
		for(int i=0;i<songs.length;i++)
		{
			if(string.equals(songs[i]))
			{
			Toast.makeText(getApplicationContext(), "Song is Available",2000).show();
			c=1;
			}
		}
		if(c==0)
		Toast.makeText(getApplicationContext(), "Song Unavailable", 2000).show();
		return c;
		
	}
}
