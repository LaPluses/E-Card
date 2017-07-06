package com.example.hezhu.myapplication;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v13.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hezhu.MessageUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.example.hezhu.myapplication.TCPUtil.getMessageFromInputStream;
import static com.example.hezhu.myapplication.TCPUtil.putMessageToOutputStream;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.input_email) TextView tvEmail;
    @BindView(R.id.input_password) TextView tvPassword;
    private List<Double> face = null;
    ProgressDialog progressDialog;

    private void showToast(final String text) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(LoginActivity.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                double[] tmp = data.getDoubleArrayExtra("face");
                face = new ArrayList<>(tmp.length);
                for (double x : tmp) {
                    face.add(x);
                }
                Log.d("face", face.size() + "");
                tvPassword.setText("****************");
                tvPassword.setEnabled(false);
                if (tvEmail.getText().length() > 0) {
                    login();
                }
            }
        } else if (requestCode == 2) {
            if (resultCode == RESULT_OK) {
                finish();
            }
        }
    }

    @OnClick(R.id.btn_camera)
    public void openCamera() {
        Intent intent = new Intent(this, CameraActivity.class);
        startActivityForResult(intent, 1);
    }

    @BindView(R.id.link_signup) Button btnSignUp;

    @OnClick(R.id.link_signup)
    public void signUp() {
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivityForResult(intent, 2);
    }

    class LoginSender implements Runnable {


        public void run() {
            try {
                Socket socket = new Socket();
                socket.setSoTimeout(2000);
                socket.connect(new InetSocketAddress(Settings.SERVER_BLOCKCHAIN, Settings.SERVER_BLOCKCHAIN_PORT), 2000);
                MessageUtil.MessageBox messageBox;
                if (face == null) {
                    messageBox = MessageUtil.MessageBox.newBuilder().setType(2)
                            .addTransStr(tvEmail.getText().toString())
                            .addTransStr(tvPassword.getText().toString())
                            .build();
                } else {
                    messageBox = MessageUtil.MessageBox.newBuilder().setType(3)
                            .addTransStr(tvEmail.getText().toString())
                            .addAllTransPhoto(face)
                            .build();
                }
                Log.d("test", messageBox.toString());
                putMessageToOutputStream(socket.getOutputStream(), messageBox.toByteArray());
                MessageUtil.MessageBox data = MessageUtil.MessageBox.parseFrom(getMessageFromInputStream(socket.getInputStream()));
                if (data.getType() == -1) {
                    CardPageFragment.username = tvEmail.getText().toString();
                    CardPageFragment.userID = data.getTransStr(0);
                    CardPageFragment.code = data.getTransStr(1);
                    Log.d("test", CardPageFragment.code.length() + " " + CardPageFragment.code);
                    Log.d("test", data.getTransStr(0));
                    LoginActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            setResult(RESULT_OK);
                            finish();
                        }
                    });
                } else {
                    showToast("密码错误");
                }
                socket.close();
            } catch (UnknownHostException e) {
                showToast("网络错误");
                e.printStackTrace();
            } catch (IOException e) {
                showToast("网络错误");
                e.printStackTrace();
            } finally {
                progressDialog.dismiss();
            }
        }
    }

    @OnClick(R.id.btn_login)
    void login() {
        if (tvEmail.length() == 0 || tvPassword.length() == 0) {
            showToast("请输入账号密码");
            return;
        }
        progressDialog = ProgressDialog.show(this, "登录中", "请稍候");
        progressDialog.setCancelable(false);
        new Thread(new LoginSender()).start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
//        LoginActivity.this.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//                startActivity(intent);
//                setResult(RESULT_OK);
//                finish();
//            }
//        });
    }

}
