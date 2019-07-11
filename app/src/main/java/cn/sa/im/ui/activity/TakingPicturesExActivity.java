package cn.sa.im.ui.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import cn.sa.im.R;
import io.rong.common.RLog;
import io.rong.message.utils.BitmapUtil;

public class TakingPicturesExActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "TakingPicturesActivity";
    private static final int REQUEST_CAMERA = 2;
    private ImageView mImage;
    private Uri mSavedPicUri;

    public TakingPicturesExActivity() {
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(1);
        this.setContentView(R.layout.rc_ac_camera);
        Button cancel = (Button)this.findViewById(R.id.rc_back);
        Button send = (Button)this.findViewById(R.id.rc_send);
        this.mImage = (ImageView)this.findViewById(R.id.rc_img);
        cancel.setOnClickListener(this);
        send.setOnClickListener(this);
        RLog.d("TakingPicturesActivity", "onCreate savedInstanceState : " + savedInstanceState);
        if (savedInstanceState == null) {
            this.startCamera();
        } else {
            String str = savedInstanceState.getString("photo_uri");
            if (str != null) {
                this.mSavedPicUri = Uri.parse(str);

                try {
                    this.mImage.setImageBitmap(BitmapUtil.getResizedBitmap(this, this.mSavedPicUri, 960, 960));
                } catch (IOException var6) {
                    RLog.e("TakingPicturesActivity", "onCreate", var6);
                }
            }
        }

    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public void onClick(View v) {
        File file = new File(this.mSavedPicUri.getPath());
        if (!file.exists()) {
            this.finish();
        }

        if (v.getId() == R.id.rc_send) {
            if (this.mSavedPicUri != null) {
                Intent data = new Intent();
                data.setData(this.mSavedPicUri);
                this.setResult(-1, data);
            }

            this.finish();
        } else if (v.getId() == R.id.rc_back) {
            this.finish();
        }

    }

    @SuppressLint("WrongConstant")
    private void startCamera() {
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if (!path.exists()) {
            path.mkdirs();
        }

        String name = System.currentTimeMillis() + ".jpg";
        File file = new File(path, name);
        this.mSavedPicUri = Uri.fromFile(file);
        RLog.d("TakingPicturesActivity", "startCamera output pic uri =" + this.mSavedPicUri);
        Intent intent = new Intent();
        intent.setAction("android.media.action.IMAGE_CAPTURE");
        List<ResolveInfo> resInfoList = this.getPackageManager().queryIntentActivities(intent, 65536);
        Uri uri = null;

        try {
            uri = FileProvider.getUriForFile(this, this.getPackageName() + this.getString(R.string.rc_authorities_fileprovider), file);
        } catch (Exception var11) {
            throw new RuntimeException("Please check IMKit Manifest FileProvider config.");
        }

        Iterator var7 = resInfoList.iterator();

        while(var7.hasNext()) {
            ResolveInfo resolveInfo = (ResolveInfo)var7.next();
            String packageName = resolveInfo.activityInfo.packageName;
            this.grantUriPermission(packageName, uri, 2);
            this.grantUriPermission(packageName, uri, 1);
        }

        intent.putExtra("output", uri);
        intent.addCategory("android.intent.category.DEFAULT");

        try {
            this.startActivityForResult(intent, 2);
        } catch (SecurityException var10) {
            Log.e("TakingPicturesActivity", "REQUEST_CAMERA SecurityException!!!");
        }

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        RLog.d("TakingPicturesActivity", "onActivityResult resultCode = " + resultCode + ", intent=" + data);
        if (resultCode != -1) {
            this.finish();
        } else {
            switch(requestCode) {
                case 2:
                    if (resultCode == 0) {
                        this.finish();
                        Log.e("TakingPicturesActivity", "RESULT_CANCELED");
                    }

                    if (this.mSavedPicUri != null && resultCode == -1) {
                        try {
                            this.mImage.setImageBitmap(BitmapUtil.getResizedBitmap(this, this.mSavedPicUri, 960, 960));
                        } catch (IOException var5) {
                            RLog.e("TakingPicturesActivity", "onActivityResult", var5);
                        }
                    }

                    super.onActivityResult(requestCode, resultCode, data);
                    return;
                default:
            }
        }
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.e("TakingPicturesActivity", "onRestoreInstanceState");
        this.mSavedPicUri = Uri.parse(savedInstanceState.getString("photo_uri"));
        super.onRestoreInstanceState(savedInstanceState);
    }

    public void onSaveInstanceState(Bundle outState) {
        Log.e("TakingPicturesActivity", "onSaveInstanceState");
        outState.putString("photo_uri", this.mSavedPicUri.toString());
        super.onSaveInstanceState(outState);
    }
}