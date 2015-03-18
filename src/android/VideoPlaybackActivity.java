package nl.miraclethings.videoplayback;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.VideoView;


public class VideoPlaybackActivity extends Activity implements View.OnClickListener {

    private VideoView videoView;
    private MediaController mMediaController;
    private View emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        videoView = new VideoView(this);

        setContentView(this.videoView);

        getWindow().getDecorView().setBackgroundColor(Color.BLACK);

        emptyView = new View(this);
        addContentView(emptyView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        emptyView.setOnClickListener(this);

        videoView.setVideoURI(getIntent().getData());
    }

    @Override
    protected void onResume() {
        super.onResume();
        videoView.start();
    }

    @Override
    public void onClick(View v) {
        if (mMediaController == null) {
            mMediaController = new MediaController(this);
            mMediaController.setAnchorView(emptyView);
            this.videoView.setMediaController(mMediaController);
        }

        if (mMediaController.isShowing()) {
            mMediaController.hide();
        } else {
            mMediaController.show();
        }
    }

}
