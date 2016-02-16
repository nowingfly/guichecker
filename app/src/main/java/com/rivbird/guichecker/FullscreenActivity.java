package com.rivbird.guichecker;

import java.io.IOException;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.rivbird.guichecker.util.Keeper;
import com.rivbird.guichecker.util.SystemUiHider;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class FullscreenActivity extends Activity implements View.OnClickListener, View.OnLongClickListener {
    static final String KEY_ALPHA = "bg_alpha";
    static final int DEFAULT_ALPHA = 222;
    private static final int REQ_PICKER_CODE = 101;
    // private int mShareAreaWidth;
    // private int mShareAreaHeight;
    private Bitmap mBitmap = null;
    private TextView mBackgroundImg = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Keeper.init(this);
        WindowManager wm = getWindowManager();
        Point size = new Point();
        wm.getDefaultDisplay().getSize(size);
        // mShareAreaWidth = size.x;
        // mShareAreaHeight = size.y;
        setContentView(R.layout.activity_fullscreen);

        mBackgroundImg = (TextView) findViewById(R.id.image_holder);
        mBackgroundImg.setOnClickListener(this);
        mBackgroundImg.setOnLongClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.load_img:
                selectImage();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.load_img:
                selectImage();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void selectImage() {
        // Intent intent = new Intent(this, GalleryPickerActivity.class);
        // startActivityForResult(intent, REQ_PICKER_CODE);

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");// 相片类型
        startActivityForResult(intent, REQ_PICKER_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            return;
        }
        switch (requestCode) {
            case REQ_PICKER_CODE:
                if (mBitmap != null) {
                    mBitmap.recycle();
                    mBitmap = null;
                }
                // String fileName =
                // data.getStringExtra(GalleryPickerActivity.EXTRA_DATA);
                // if (fileName == null) {
                // return;
                // }
                // mBitmap = ImageResizer.decodeSampledBitmapFromFile(fileName,
                // mShareAreaWidth, mShareAreaHeight);

                ContentResolver resolver = getContentResolver();
                Uri originalUri = data.getData();
                try {
                    mBitmap = MediaStore.Images.Media.getBitmap(resolver, originalUri);
                    if (mBitmap == null)
                        return;
                    mBackgroundImg.setText("");
                    mBackgroundImg.setBackground(new BitmapDrawable(getResources(), mBitmap));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                break;

            default:
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        int alpha = Keeper.readInt(KEY_ALPHA, DEFAULT_ALPHA);
        Drawable drawable = mBackgroundImg.getBackground();
        if (drawable == null)
            return;
        drawable.setAlpha(alpha);
    }

    @Override
    public void onClick(View view) {
        selectImage();
    }

    @Override
    public boolean onLongClick(View view) {
        Intent intent = new Intent(this, AndroidSeekBar.class);
        startActivity(intent);
        return true;
    }
}
