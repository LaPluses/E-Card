package com.example.hezhu.myapplication;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import butterknife.OnClick;

import static android.app.Activity.RESULT_OK;
import static com.example.hezhu.myapplication.TCPUtil.getMessageFromInputStream;
import static com.example.hezhu.myapplication.TCPUtil.putMessageToOutputStream;

public class PersonDetail extends Fragment {

    private void showToast(final String text) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    List<Double> face;
    ProgressDialog progressDialog;

    class LoginSender implements Runnable {


        public void run() {
            try {
                Socket socket = new Socket();
                socket.setSoTimeout(2000);
                socket.connect(new InetSocketAddress(Settings.SERVER_BLOCKCHAIN, Settings.SERVER_BLOCKCHAIN_PORT), 2000);
                MessageUtil.MessageBox messageBox;
                messageBox = MessageUtil.MessageBox.newBuilder().setType(11)
                        .addTransStr(CardPageFragment.username)
                        .addAllTransPhoto(face)
                        .build();
                Log.d("test", messageBox.toString());
                putMessageToOutputStream(socket.getOutputStream(), messageBox.toByteArray());
                MessageUtil.MessageBox data = MessageUtil.MessageBox.parseFrom(getMessageFromInputStream(socket.getInputStream()));
                if (data.getType() == -1) {
                    showToast("更新成功");
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                double[] tmp = data.getDoubleArrayExtra("face");
                face = new ArrayList<>(tmp.length);
                for (double x : tmp) {
                    face.add(x);
                }
                Log.d("face", face.size() + "");
                progressDialog = ProgressDialog.show(getActivity(), "登录中", "请稍候");
                progressDialog.setCancelable(false);
                new Thread(new LoginSender()).start();
            }
        }
    }

    public void openCamera() {
        Intent intent = new Intent(getActivity(), CameraActivity.class);
        startActivityForResult(intent, 1);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_person_detail, container, false);
        TextView usernameTextView = (TextView) view.findViewById(R.id.username);
        TextView userIDTextView = (TextView) view.findViewById(R.id.id);
        TextView codeTextView = (TextView) view.findViewById(R.id.code);
        usernameTextView.setText(CardPageFragment.username);
        userIDTextView.setText(CardPageFragment.userID);
        codeTextView.setText(CardPageFragment.code);
        Button refaceButton = (Button) view.findViewById(R.id.reface);
        refaceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCamera();
            }
        });
        return view;
    }

}
