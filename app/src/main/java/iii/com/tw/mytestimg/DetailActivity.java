package iii.com.tw.mytestimg;

import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

import cz.msebera.android.httpclient.Header;

//記得開網路權限
//client.post方法要設失敗的回傳訊息


public class DetailActivity extends AppCompatActivity {
    private final int REQUEST_PICK_IMAGE = 1;
    private int REQUEST_READ_STORAGE = 3;
//
    private int position;
    private ImageView image;
    private Cursor cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        position = getIntent().getIntExtra("POSITION", 0);
        image = (ImageView) findViewById(R.id.imageView);
        CursorLoader loader = new CursorLoader(this,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, null, null, null);
        cursor = loader.loadInBackground();
        cursor.moveToPosition(position);
        updateImage();
    }

    private void getSelectImage(Intent data){
        //從 onActivityResult 傳入的data中，取得圖檔路徑
        Uri selectedImage = data.getData();
        String[] filePathColumn = { MediaStore.Images.Media.DATA };

        Cursor cursor = getContentResolver().query(selectedImage,
                filePathColumn, null, null, null);
        if(cursor==null){ return; }
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String imagePath = cursor.getString(columnIndex);
        cursor.close();
        //Log.d("editor","image:"+imagePath);




        //Toast.makeText(MainActivity.this,imagePath,Toast.LENGTH_LONG).show();
        //使用圖檔路徑產生調整過大小的Bitmap

        Bitmap bitmap = getResizedBitmap(imagePath); //程式寫在後面

        //將 Bitmap 轉為 base64 字串
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        byte[] bitmapData = bos.toByteArray();
        String imageBase64 = Base64.encodeToString(bitmapData, Base64.DEFAULT);
        //Log.d("editor",imageBase64);

        //將圖檔上傳至 Imgur，將取得的圖檔網址插入文字輸入框
        imgurUpload(imageBase64); //程式寫在後面
        //Toast.makeText(DetailActivity.this,imageBase64,Toast.LENGTH_SHORT).show();

    }

    private void imgurUpload(final String image){ //插入圖片
        Toast.makeText(DetailActivity.this,"in Imgur Upload", Toast.LENGTH_SHORT).show();
        //String urlString = "https://imgur-apiv3.p.mashape.com/3/image/";
        String urlString = "https://imgur-apiv3.p.mashape.com/3/image";
        String mashapeKey = ""; //設定自己的 Mashape Key
        String clientId = ""; //設定自己的 Clinet ID
        String titleString = "hihi45454545"; //設定圖片的標題
        //showLoadingDialog();



        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("X-Mashape-Key", mashapeKey);
        client.addHeader("Authorization", "Client-ID "+clientId);
        client.addHeader("Content-Type", "application/x-www-form-urlencoded");

        RequestParams params = new RequestParams();
        params.put("title", titleString);
        params.put("image", image);


        client.post(urlString, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                //dismissLoadingDialog();
                if (!response.optBoolean("success") || !response.has("data")) {
                    Log.d("editor", "response: "+response.toString());
                    Toast.makeText(DetailActivity.this,"fail", Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(DetailActivity.this,"in On Success", Toast.LENGTH_SHORT).show();

                JSONObject data = response.optJSONObject("data");
                //Log.d("editor","link: "+data.optString("link"));
                String link = data.optString("link","");
                int width = data.optInt("width",0);
                int height = data.optInt("height",0);
                String bbcode = "[img="+width+"x"+height+"]"+link+"[/img]";
                textView = (TextView)findViewById(R.id.text);
                textView.setText(bbcode);
               // textInsertString(bbcode); //將文字插入輸入框的程式 寫在後面

            }


            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject error) {
                //dismissLoadingDialog();
                //Log.d("editor","error: "+error.toString());


                Toast.makeText(DetailActivity.this,"Error: " + statusCode + " " + e.getMessage(), Toast.LENGTH_SHORT).show();

                /*
                if (error.has("data")) {
                    JSONObject data = error.optJSONObject("data");
                    AlertDialog dialog = new AlertDialog.Builder(DetailActivity.this)
                            .setTitle("Error: " + statusCode + " " + e.getMessage())
                            .setMessage(data.optString("error",""))
                            .setPositiveButton("確定", null)
                            .create();
                    dialog.show();
                }
                */
            }
        });

    }

    private void updateImage() {
        String imagePath = cursor.getString(cursor.getColumnIndex(
                MediaStore.Images.Media.DATA ));
        textView = (TextView)findViewById(R.id.text);
        if (textView != null) {
           // textView.setText(imagePath);
        }
       // Toast.makeText(DetailActivity.this,imagePath,Toast.LENGTH_LONG).show();
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        image.setImageBitmap(bitmap);

        //******************

        //使用圖檔路徑產生調整過大小的Bitmap

        Bitmap bitmap1 = getResizedBitmap(imagePath); //程式寫在後面
        ImageView imageView = (ImageView)findViewById(R.id.resizeImage);
        imageView.setImageBitmap(bitmap1);

        //將 Bitmap 轉為 base64 字串
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        bitmap1.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        byte[] bitmapData = bos.toByteArray();
        String imageBase64 = Base64.encodeToString(bitmapData, Base64.DEFAULT);
        //Log.d("editor",imageBase64);

        //將圖檔上傳至 Imgur，將取得的圖檔網址插入文字輸入框

        imgurUpload(imageBase64); //程式寫在後面


        Toast.makeText(DetailActivity.this,"inImgur", Toast.LENGTH_SHORT).show();


        //*********************
    }

    private Bitmap getResizedBitmap(String imagePath) {
        // 取得原始圖檔的bitmap與寬高
        BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);
        int width = options.outWidth, height = options.outHeight;


        // 將圖檔等比例縮小至寬度為1024
        final int MAX_WIDTH = 1024;
        float resize = 1; // 縮小值 resize 可為任意小數
        if(width>MAX_WIDTH){
            resize = ((float) MAX_WIDTH) / width;
        }

        // 產生縮圖需要的參數 matrix
        Matrix matrix = new Matrix();
        matrix.postScale(resize, resize); // 設定寬高的縮放比例

        // 產生縮小後的圖
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        return resizedBitmap;


    }
    TextView textView;
}
