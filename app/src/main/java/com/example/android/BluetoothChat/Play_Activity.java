package com.example.android.BluetoothChat;

import java.io.File;

import android.app.Activity;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;


public class Play_Activity extends Activity {
	Button leave_button;
	private MediaPlayer mediaPlayer;
	Bundle bundle;
	String song_name;
	String path;
	String[] songs;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_play_);
		leave_button=(Button)findViewById(R.id.Stop_button);
		mediaPlayer=new MediaPlayer();
		bundle=getIntent().getExtras();
		song_name=bundle.getString("Song Number");
		path = Environment.getExternalStorageDirectory().toString()+"/music";
		File f = new File(path);
        File file[]=f.listFiles();
        songs=new String[file.length];
        for(int i=0;i<file.length;i++)
        {
        	songs[i]=file[i].getName();
        }
        mediaPlayer= MediaPlayer.create(this, Uri.parse(Environment.getExternalStorageDirectory().getPath()+ "/Music/"+song_name));
        //mediaPlayer.setLooping(true);
        mediaPlayer.start();
        leave_button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
			mediaPlayer.stop();
			finish();
			}

			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
			}
		});
		
	}
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		mediaPlayer.stop();
		finish();
		super.onBackPressed();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.play_, menu);
		return true;
	}

}
