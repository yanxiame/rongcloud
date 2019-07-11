package cn.sa.im.ui.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import cn.sa.im.MainActivity;
import cn.sa.im.R;
import cn.sa.im.server.network.http.HttpException;
import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.UserInfo;

public class LoginActivity extends BaseActivity implements View.OnClickListener {
 private final static String TAG = "LoginActivity";
 private static final int LOGIN = 5;
 private static final int GET_TOKEN = 6;
 private static final int SYNC_USER_INFO = 9;

 private ImageView mImg_Background;
 private EditText mPhoneEdit, mPasswordEdit;
 private String phoneString;
 private String passwordString;
 private String connectResultId;
 private SharedPreferences sp;
 private SharedPreferences.Editor editor;
 private String loginToken;

 @Override
 protected void onCreate(Bundle savedInstanceState) {
  super.onCreate(savedInstanceState);
  setContentView(R.layout.activity_login);
  setHeadVisibility(View.GONE);
  sp = getSharedPreferences("config", MODE_PRIVATE);
  editor = sp.edit();
  initView();
 }

 private void initView() {
  mPhoneEdit = (EditText) findViewById(R.id.de_login_phone);
  mPasswordEdit = (EditText) findViewById(R.id.de_login_password);
  Button mConfirm = (Button) findViewById(R.id.de_login_sign);
  TextView mRegister = (TextView) findViewById(R.id.de_login_register);
  TextView forgetPassword = (TextView) findViewById(R.id.de_login_forgot);
  forgetPassword.setOnClickListener(this);
  mConfirm.setOnClickListener(this);
  mRegister.setOnClickListener(this);
  mImg_Background = (ImageView) findViewById(R.id.de_img_backgroud);


 }

 @Override
 public void onClick(View view) {

 }
}
