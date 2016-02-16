package com.rivbird.guichecker;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Checkable;
import android.widget.CursorAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.rivbird.guichecker.R;
import com.rivbird.guichecker.bitmapfun.ImageCache;
import com.rivbird.guichecker.bitmapfun.ImageFetcher;
import com.rivbird.guichecker.bitmapfun.Utils;
import com.rivbird.guichecker.view.RecyclingImageView;

@SuppressLint("NewApi")
public class GalleryPickerActivity extends Activity implements LoaderCallbacks<Cursor>,
        OnItemClickListener, MultiChoiceModeListener {
    private static final String TAG = "GalleryPickerActivity";

    public static final String ACTION_PICK = "cn.com.smartdevices.bracelet.action.PICK";
    public static final String ACTION_MULTIPLE_PICK = "cn.com.smartdevices.bracelet.action.MULTIPLE_PICK";
    public static final String EXTRA_DATA = "cn.com.smartdevices.bracelet.extra.DATA";
    public static final String EXTRA_CROP_DATA = "cn.com.smartdevices.bracelet.extra.CROP";

    final static Uri URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    final static String[] PROJECTION = { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID };
    final static String SORTORDER = MediaStore.Images.Media._ID + " DESC";

    private static final int LOADER_ID = 0;
    public static final int ID_INDEX = 1;
    public static final int DATA_INDEX = 0;

    private GridView mGridView;
    private GalleryAdapter mAdapter;
    private boolean mIsMultiple;

    private int mImageThumbSize;
    private int mImageThumbSpacing;
    private ImageFetcher mImageFetcher;
    private static final String IMAGE_CACHE_DIR = "thumbs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_picker);
        mIsMultiple = ACTION_MULTIPLE_PICK.equals(getIntent().getAction());
        mImageThumbSize = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
        mImageThumbSpacing = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_spacing);

        cacheConfig();
        initView();
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    private void cacheConfig() {
        ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams(this, IMAGE_CACHE_DIR);
        // Set memory cache to 25% of app memory
        cacheParams.setMemCacheSizePercent(0.25f);

        // The ImageFetcher takes care of loading images into our ImageView
        // children asynchronously
        mImageFetcher = new ImageFetcher(this, mImageThumbSize);
        mImageFetcher.setLoadingImage(R.drawable.picker_empty_photo);
        mImageFetcher.addImageCache(getFragmentManager(), cacheParams);
    }

    private void initView() {
        setTitleBack();
        mAdapter = new GalleryAdapter(this, mImageFetcher);
        mGridView = (GridView) findViewById(R.id.picker_grid);
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(this);
        mGridView.setMultiChoiceModeListener(this);
        mGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                // Pause fetcher to ensure smoother scrolling when flinging
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                    // Before Honeycomb pause image loading on scroll to help
                    // with performance
                    if (!Utils.hasHoneycomb()) {
                        mImageFetcher.setPauseWork(true);
                    }
                } else {
                    mImageFetcher.setPauseWork(false);
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {}
        });
        mGridView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @TargetApi(VERSION_CODES.JELLY_BEAN)
            @Override
            public void onGlobalLayout() {
                if (mAdapter.getNumColumns() == 0) {
                    final int numColumns = (int) Math.floor(mGridView.getWidth()
                            / (mImageThumbSize + mImageThumbSpacing));
                    if (numColumns > 0) {
                        final int columnWidth = (mGridView.getWidth() / numColumns) - mImageThumbSpacing;
                        mAdapter.setNumColumns(numColumns);
                        mAdapter.setItemHeight(columnWidth);

                        if (Utils.hasJellyBean()) {
                            mGridView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        } else {
                            mGridView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        }
                    }
                }
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, URI, PROJECTION, null, null, SORTORDER);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mIsMultiple) {
            if (view instanceof Checkable) {
                Checkable c = (Checkable) view;
                boolean checked = !c.isChecked();
                mGridView.setItemChecked(position, checked);
            }
        } else {
            GalleryAdapter.ViewHolder vh = (GalleryAdapter.ViewHolder) view.getTag();
            String path = vh.path;
            Log.i(TAG, "path:" + path);
            Intent data = new Intent();
            data.putExtra(EXTRA_DATA, path);
            setResult(Activity.RESULT_OK, data);
            finish();
        }
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        if (item.getItemId() == R.id.menu_done) {

            long[] ids = mGridView.getCheckedItemIds();

            Intent data = new Intent();
            if (mIsMultiple) {
                Uri[] uris = new Uri[ids.length];
                for (int i = 0; i < ids.length; i++) {
                    Uri uri = ContentUris.withAppendedId(URI, ids[i]);
                    uris[i] = uri;
                }
                data.putExtra(EXTRA_DATA, uris);
            } else {
                if (ids.length > 0) {
                    Uri uri = ContentUris.withAppendedId(URI, ids[0]);
                    data.putExtra(EXTRA_DATA, uri);
                } else {
                    return true;
                }
            }

            setResult(Activity.RESULT_OK, data);
            finish();
            return true;
        }
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {}

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
        int selectCount = mGridView.getCheckedItemCount();
        String title = getResources().getQuantityString(R.plurals.picker_number_of_items_selected, selectCount,
                        selectCount);
        mode.setTitle(title);
    }

    public static class GalleryAdapter extends CursorAdapter {

        private int mItemHeight = 0;
        private int mNumColumns = 0;
        private final ImageFetcher mImageFetcher;
        private GridView.LayoutParams mImageViewLayoutParams;

        public static class ViewHolder {
            RecyclingImageView imageView;
            String path;
        }

        public GalleryAdapter(Context context, ImageFetcher imageFetcher) {
            super(context, null, false);
            mImageFetcher = imageFetcher;
            mImageViewLayoutParams = new GridView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        }

        /**
         * Sets the item height. Useful for when we know the column width so the height can be set
         * to match.
         *
         * @param height
         */
        public void setItemHeight(int height) {
            if (height == mItemHeight) {
                return;
            }
            mItemHeight = height;
            mImageViewLayoutParams = new GridView.LayoutParams(LayoutParams.MATCH_PARENT, mItemHeight);
            mImageFetcher.setImageSize(height);
            notifyDataSetChanged();
        }

        public void setNumColumns(int numColumns) {
            mNumColumns = numColumns;
        }

        public int getNumColumns() {
            return mNumColumns;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {

            ViewHolder holder = new ViewHolder();

            RecyclingImageView riv = new RecyclingImageView(context);
            riv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            riv.setLayoutParams(mImageViewLayoutParams);
            holder.imageView = riv;

            riv.setTag(holder);
            return riv;
        }

        @Override
        public long getItemId(int position) {
            return super.getItemId(position);
        }

        @Override
        public boolean hasStableIds() {
            return super.hasStableIds();
        }

        @Override
        public void bindView(View view, final Context context, Cursor cursor) {
            final ViewHolder holder = (ViewHolder) view.getTag();
            final int dataColumn = cursor.getColumnIndex(Media.DATA);
            final String path = cursor.getString(dataColumn);
            holder.path = path;
            final ImageView imageView = holder.imageView;

            // Check the height matches our calculated column width
            if (imageView.getLayoutParams().height != mItemHeight) {
                imageView.setLayoutParams(mImageViewLayoutParams);
            }

            // Finally load the image asynchronously into the ImageView, this
            // also takes care of
            // setting a placeholder image while the background thread runs
            mImageFetcher.loadImageFromFile(path, imageView);

        }
    }

    private void setTitleBack() {
//        view.setText(getString(R.string.sport_running_gallery));
    }

    @Override
    public void onResume() {
        super.onResume();
        mImageFetcher.setExitTasksEarly(false);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
        mImageFetcher.setPauseWork(false);
        mImageFetcher.setExitTasksEarly(true);
        mImageFetcher.flushCache();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mImageFetcher.closeCache();
    }
}
