package cn.edu.gdmec.s07150850.mymediaplayer;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.sql.Time;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {
    private Display currDisplay;
    private SurfaceView surfaceView;
    private SurfaceHolder holder;
    private MediaPlayer player;
    private int wWidth ,wHeight;
    private Timer timer;
    private ImageButton rew,pause,start,ff;
    private TextView play_time,all_time,title;
    private SeekBar seekBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        final Intent intent=getIntent();
        Uri uri=intent.getData();
        String mPath="";
        if (uri!=null){
            mPath=uri.getPath();
        }else {
            Bundle localBundle=getIntent().getExtras();
            if (localBundle!=null){
                String t_path=localBundle.getString("path");
                if (t_path!=null&&!"".equals(t_path)){
                    mPath=t_path;
                }
            }
        }
        title= (TextView) findViewById(R.id.title);
        surfaceView= (SurfaceView) findViewById(R.id.surfaceview);
        rew= (ImageButton) findViewById(R.id.rew);
        pause= (ImageButton) findViewById(R.id.pause);
        start= (ImageButton) findViewById(R.id.start);
        ff= (ImageButton) findViewById(R.id.ff);
        play_time= (TextView) findViewById(R.id.play_time);
        all_time= (TextView) findViewById(R.id.all_time);
        seekBar= (SeekBar) findViewById(R.id.seekbar);

        holder=surfaceView.getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                player.setDisplay(holder);
                player.prepareAsync();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });

        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        player=new MediaPlayer();
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (timer!=null){
                    timer.cancel();
                    timer=null;
                }
            }
        });

        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                wWidth=player.getVideoWidth();
                wHeight=player.getVideoHeight();

                if (wWidth>currDisplay.getWidth()||wHeight>currDisplay.getHeight()){
                    float wRatio=(float) wWidth/(float)currDisplay.getWidth();
                    float hRatio=(float) wHeight/(float)currDisplay.getHeight();
                    float ratio=Math.max(wRatio,hRatio);
                    wWidth= (int) Math.ceil((float)wWidth/ratio);
                    wHeight= (int) Math.ceil((float)wHeight/ratio);

                    surfaceView.setLayoutParams(new LinearLayout.LayoutParams(wWidth,wHeight));
                    player.start();
                }else {
                    player.start();
                }
                if (timer!=null){
                    timer.cancel();
                    timer=null;
                }

                timer=new Timer();
                timer.schedule(new MyTask(),50,500);
            }
        });
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try{
            if (!mPath.equals("")){
                title.setText(mPath.substring(mPath.lastIndexOf("/")+1));
                player.setDataSource(mPath);
            }else {
                AssetFileDescriptor afd=this.getResources().openRawResourceFd(R.raw.exodus);
                player.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getDeclaredLength());

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pause.setVisibility(View.GONE);
                start.setVisibility(View.VISIBLE);
                player.pause();
                if (timer!=null){
                    timer.cancel();
                    timer=null;
                }
            }
        });
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start.setVisibility(View.GONE);
                pause.setVisibility(View.VISIBLE);
                player.start();
                if (timer!=null){
                    timer.cancel();
                    timer=null;
                }
                timer=new Timer();
                timer.schedule(new MyTask(),50,500);
            }
        });

        rew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player.isPlaying()){
                    int currentPosition=player.getCurrentPosition();
                    if (currentPosition-10000>0){
                        player.seekTo(currentPosition-10000);
                    }
                }
            }
        });
        ff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player.isPlaying()) {
                    int currentPosition = player.getCurrentPosition();
                    if (currentPosition + 10000 > 0) {
                        player.seekTo(currentPosition + 10000);
                    }
                }
            }
        });
        currDisplay=this.getWindowManager().getDefaultDisplay();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0,1,0,"文件夹");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==1){
            Intent intent=new Intent(this,MyFileActivity.class);
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public class MyTask extends TimerTask{

        @Override
        public void run() {
            Message message=new Message();
            message.what=1;
            handler.sendMessage(message);
        }
    }
    private final Handler handler=new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case 1:
                    Time progress=new Time(player.getCurrentPosition());
                    Time allTime=new Time(player.getDuration());
                    String timestr=progress.toString();
                    String timrstr2=allTime.toString();
                    play_time.setText(timestr.substring(timestr.indexOf(":")+1));
                    all_time.setText(timrstr2.substring(timestr.indexOf(":")+1));
                    int progressValue=0;
                    if (player.getDuration()>0){
                        progressValue=seekBar.getMax()*
                                player.getCurrentPosition()/player.getDuration();
                    }
                    seekBar.setProgress(progressValue);
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player!=null){
            player.stop();
        }
    }
}
