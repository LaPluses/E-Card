package com.example.hezhu.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hezhu.MessageUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
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

public class SignUpActivity extends AppCompatActivity {

    @BindView(R.id.input_email) TextView tvEmail;
    @BindView(R.id.input_password) TextView tvPassword;
    @BindView(R.id.btn_submit) TextView btnSubmit;
    private List<Double> face = null;
    private ProgressDialog progressDialog;

    private void showToast(final String text) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(SignUpActivity.this, text, Toast.LENGTH_SHORT).show();
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
                btnSubmit.setText("完成注册");
                if (tvEmail.getText().length() > 0 && tvPassword.getText().length() > 0) {
                    onSubmit();
                }
            }
        }
    }

    public void openCamera() {
        Intent intent = new Intent(this, CameraActivity.class);
        startActivityForResult(intent, 1);
    }

    class SignUpSender implements Runnable {


        public void run() {
            try {
                Socket socket = new Socket();
                socket.setSoTimeout(2000);
                socket.connect(new InetSocketAddress(Settings.SERVER_BLOCKCHAIN, Settings.SERVER_BLOCKCHAIN_PORT), 2000);
                MessageUtil.MessageBox messageBox = MessageUtil.MessageBox.newBuilder().setType(1)
                        .addTransStr(tvEmail.getText().toString())
                        .addTransStr(tvPassword.getText().toString())
                        .addAllTransPhoto(face)
                        .build();
                Log.d("MassegaBox", messageBox.toString());
                putMessageToOutputStream(socket.getOutputStream(), messageBox.toByteArray());
                MessageUtil.MessageBox data = MessageUtil.MessageBox.parseFrom(getMessageFromInputStream(socket.getInputStream()));
                if (data.getType() == -1) {
                    CardPageFragment.username = tvEmail.getText().toString();
                    CardPageFragment.userID = data.getTransStr(0);
                    CardPageFragment.code = data.getTransStr(1);
                    SignUpActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                            startActivity(intent);
                            setResult(RESULT_OK);
                            finish();
                        }
                    });
                } else {
                    showToast("信息错误");
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

    @OnClick(R.id.btn_submit)
    void onSubmit() {
        if (tvEmail.length() == 0 || tvPassword.length() == 0) {
            showToast("请输入账号密码");
            return;
        }
        if (face == null) {
            openCamera();
        } else {
            Log.d("test", "submit");
            progressDialog = ProgressDialog.show(this, "注册中", "请稍候");
            progressDialog.setCancelable(false);
            new Thread(new SignUpSender()).start();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);
    }
}
