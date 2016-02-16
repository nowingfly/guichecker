package com.rivbird.guichecker;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.rivbird.guichecker.R;
import com.rivbird.guichecker.util.Keeper;

public class AndroidSeekBar extends Activity implements View.OnClickListener {
    private SeekBar mSeekBar = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seekbar);
        findViewById(R.id.OK).setOnClickListener(this);
        findViewById(R.id.cancel).setOnClickListener(this);
        mSeekBar = (SeekBar) findViewById(R.id.seekbar);
        final TextView currAlpha = (TextView) findViewById(R.id.current_alpha);

        int alpha = Keeper.readInt(FullscreenActivity.KEY_ALPHA, FullscreenActivity.DEFAULT_ALPHA);
        mSeekBar.setProgress(alpha);
        currAlpha.setText("当前透明度：" + mSeekBar.getProgress());

        // 设置拖动条改变监听器
        OnSeekBarChangeListener osbcl = new OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currAlpha.setText("当前透明度：" + mSeekBar.getProgress());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}

        };

        // 为拖动条绑定监听器
        mSeekBar.setOnSeekBarChangeListener(osbcl);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.OK) {
            Keeper.keepInt(FullscreenActivity.KEY_ALPHA, mSeekBar.getProgress());
            finish();
            return;
        }
        if (view.getId() == R.id.cancel) {
            finish();
            return;
        }
    }
}
