package iii.com.tw.mytestimg;

import android.Manifest;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import static android.Manifest.permission.*;


import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener {
    private static final int REQUEST_READ_STORAGE = 3;
    private SimpleCursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        int permission = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            //未取得權限，向使用者要求允許權限
            ActivityCompat.requestPermissions( this,
                    new String[]{READ_EXTERNAL_STORAGE},
                    REQUEST_READ_STORAGE );
        }else{
            //已有權限，可進行檔案存取
            readThumbnails();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode) {
            case REQUEST_READ_STORAGE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //取得讀取外部儲存權限，進行存取
                    readThumbnails();
                } else {
                    //使用者拒絕權限，顯示對話框告知
                    new AlertDialog.Builder(this)
                            .setMessage("必須允許讀取外部儲存權限才能顯示圖檔")
                            .setPositiveButton("OK", null)
                            .show();
                }
                return;
        }
    }

    private void readThumbnails() {
        GridView grid = (GridView) findViewById(R.id.grid);
        String[] from = { MediaStore.Images.Thumbnails.DATA,
                MediaStore.Images.Media.DISPLAY_NAME};
        int[] to = new int[] {R.id.thumb_image, R.id.thumb_text};
        adapter = new SimpleCursorAdapter(
                getBaseContext(),
                R.layout.thumb_item,
                null,
                from ,
                to ,
                0);
        grid.setAdapter(adapter);
        grid.setOnItemClickListener(this);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        return new CursorLoader(this, uri, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view,
                            int position, long id) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("POSITION", position);
        startActivity(intent);
    }
}
