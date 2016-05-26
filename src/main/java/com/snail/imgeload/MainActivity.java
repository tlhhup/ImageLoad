package com.snail.imgeload;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private EditText mImage_url;
    private ImageView mIv_show;
    private ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mImage_url = (EditText) findViewById(R.id.et_image_url);
        mIv_show = (ImageView) findViewById(R.id.iv_show);

        //初始化进度框
        mDialog = new ProgressDialog(this);
        mDialog.setTitle("提示");
        mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);//设置为水平
        mDialog.setCancelable(false);//设置为不可关闭
    }

    public void loadImage(View view) {
        String url = this.mImage_url.getText().toString();
        if (!TextUtils.isEmpty(url)) {
            new AsyncTask<String, Integer, Bitmap>() {

                @Override
                protected void onPreExecute() {//执行前
                    if (mDialog != null)
                        mDialog.show();
                }

                @Override
                protected Bitmap doInBackground(String... params) {//子线程
                    String url = params[0];
                    try {
                        URL netUrl=new URL(url);
                        HttpURLConnection connection= (HttpURLConnection) netUrl.openConnection();
                        connection.setRequestMethod("GET");//设置请求方式
                        connection.setReadTimeout(5000);
                        connection.setConnectTimeout(5000);
                        //获取请求码
                        int responseCode = connection.getResponseCode();
                        if(responseCode==200){
                            //设置进度条的长度
                            mDialog.setMax(connection.getContentLength());
                            //获取数据
                            InputStream is = connection.getInputStream();
                            //输出流
                            ByteArrayOutputStream bos=new ByteArrayOutputStream();
                            byte[] buffer=new byte[1024];
                            int len=0;
                            while((len=is.read(buffer))!=-1){
                                SystemClock.sleep(1000);
                                //更新下载进度
                                publishProgress(len);
                                //存储数据
                                bos.write(buffer,0,len);
                            }
                            bos.close();
                            is.close();
                            byte[] datas = bos.toByteArray();
                            return BitmapFactory.decodeByteArray(datas,0,datas.length);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onProgressUpdate(Integer... values) {//更新进度
                    int len=values[0];
                    mDialog.setProgress(mDialog.getProgress()+len);
                }

                @Override
                protected void onPostExecute(Bitmap result) {//执行完成
                    if (mDialog != null && mDialog.isShowing()) {
                        mDialog.dismiss();
                        if(result!=null) {
                            mIv_show.setImageBitmap(result);
                        }
                    }
                }
            }.execute(url);
        } else {
            Toast.makeText(this, "图片地址不能为空", Toast.LENGTH_LONG).show();
        }
    }

}
