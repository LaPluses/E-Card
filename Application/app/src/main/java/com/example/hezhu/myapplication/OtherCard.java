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

import com.example.hezhu.CardUtil;
import com.example.hezhu.MessageUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import static com.example.hezhu.myapplication.TCPUtil.getMessageFromInputStream;
import static com.example.hezhu.myapplication.TCPUtil.putMessageToOutputStream;

public class OtherCard extends Fragment {

    TextView usernameTextView;
    TextView userIDTextView;
    TextView codeTextView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void showToast(final String text) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    void check() {
        final ProgressDialog progressDialog = ProgressDialog.show(getActivity(), "确认中", "请稍候");
        progressDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket();
                    socket.setSoTimeout(2000);
                    socket.connect(new InetSocketAddress(Settings.SERVER_BLOCKCHAIN, Settings.SERVER_BLOCKCHAIN_PORT), 2000);
                    MessageUtil.MessageBox messageBox = MessageUtil.MessageBox.newBuilder().setType(9)
                            .addTransStr(usernameTextView.getText().toString())
                            .addTransStr(userIDTextView.getText().toString())
                            .addTransStr(codeTextView.getText().toString())
                            .build();
                    Log.d("test", messageBox.toString());
                    putMessageToOutputStream(socket.getOutputStream(), messageBox.toByteArray());
                    final MessageUtil.MessageBox data = MessageUtil.MessageBox.parseFrom(getMessageFromInputStream(socket.getInputStream()));
                    if (data.getType() == -1) {
                        CardActivity.username = usernameTextView.getText().toString();
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(getActivity(), CardActivity.class);
                                startActivity(intent);
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
        }).start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_other_card, container, false);
        Button checkButton = (Button) view.findViewById(R.id.check);
        checkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                check();
            }
        });
        usernameTextView = (TextView) view.findViewById(R.id.username);
        userIDTextView = (TextView) view.findViewById(R.id.id);
        codeTextView = (TextView) view.findViewById(R.id.code);
        return view;
    }

}
