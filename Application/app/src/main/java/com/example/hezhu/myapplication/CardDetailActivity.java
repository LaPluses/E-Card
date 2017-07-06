package com.example.hezhu.myapplication;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hezhu.CardUtil;
import com.example.hezhu.MessageUtil;
import com.google.protobuf.ByteString;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.example.hezhu.myapplication.TCPUtil.getMessageFromInputStream;
import static com.example.hezhu.myapplication.TCPUtil.putMessageToOutputStream;

public class CardDetailActivity extends AppCompatActivity {

    private void showToast(final String text) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(CardDetailActivity.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        onBackPressed();
        return true;
    }

    @BindView(R.id.cardImageView) ImageView imageView;
    @BindView(R.id.line1) LinearLayout linearLayout;
    private ProgressDialog progressDialog;
    public static EcardListAdapter ecardListAdapter;
    int position;

    @OnClick(R.id.generate)
    void generate() {
        List<String> list = ecardListAdapter.cards.getCards(position).getFieldsList();
        String str = "";
        for (int i = 0; i < list.size(); ++i) {
            String value = list.get(i);
            String key = CardPageFragment.FIELDS[ecardListAdapter.cards.getCards(position).getCardType().getNumber()][i];
            str += key + ": " + value + "\n";
        }
        Intent intent = new Intent(this, QRCodeActivity.class);
        intent.putExtra("str", str);
        startActivity(intent);
    }

    @OnClick(R.id.authorize)
    void authorize() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("是否授权?");
        builder.setPositiveButton("授权", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                progressDialog = ProgressDialog.show(CardDetailActivity.this, "授权中", "请稍候");
                progressDialog.setCancelable(false);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Socket socket = new Socket();
                            socket.setSoTimeout(2000);
                            socket.connect(new InetSocketAddress(Settings.SERVER_BLOCKCHAIN, Settings.SERVER_BLOCKCHAIN_PORT), 2000);
                            MessageUtil.MessageBox messageBox = MessageUtil.MessageBox.newBuilder().setType(7)
                                    .addTransStr(CardPageFragment.username)
                                    .addTransStr(Integer.toString(ecardListAdapter.cards.getCards(position).getCardID()))
                                    .build();
                            Log.d("test", messageBox.toString());
                            putMessageToOutputStream(socket.getOutputStream(), messageBox.toByteArray());
                            MessageUtil.MessageBox data = MessageUtil.MessageBox.parseFrom(getMessageFromInputStream(socket.getInputStream()));
                            Log.d("test", data.getType() + "");
                            if (data.getType() == -1) {
                                CardDetailActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        showToast("授权成功");
                                        Intent intent = new Intent(CardDetailActivity.this, MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);
                                    }
                                });
                            } else {
                                showToast("授权失败");
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
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    @OnClick(R.id.delete)
    void delete() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("是否删除?");
        builder.setPositiveButton("删除", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                progressDialog = ProgressDialog.show(CardDetailActivity.this, "删除中", "请稍候");
                progressDialog.setCancelable(false);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Socket socket = new Socket();
                            socket.setSoTimeout(2000);
                            socket.connect(new InetSocketAddress(Settings.SERVER_BLOCKCHAIN, Settings.SERVER_BLOCKCHAIN_PORT), 2000);
                            MessageUtil.MessageBox messageBox = MessageUtil.MessageBox.newBuilder().setType(6)
                                    .addTransStr(CardPageFragment.username)
                                    .addTransStr(Integer.toString(ecardListAdapter.cards.getCards(position).getCardID()))
                                    .build();
                            Log.d("test", messageBox.toString());
                            putMessageToOutputStream(socket.getOutputStream(), messageBox.toByteArray());
                            MessageUtil.MessageBox data = MessageUtil.MessageBox.parseFrom(getMessageFromInputStream(socket.getInputStream()));
                            Log.d("test", data.getType() + "");
                            if (data.getType() == -1) {
                                CardDetailActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        showToast("删除成功");
                                        Intent intent = new Intent(CardDetailActivity.this, MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);
                                    }
                                });
                            } else {
                                showToast("未知错误");
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
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    void hasAuthorize() {
        Button button = (Button) findViewById(R.id.authorize);
        button.setClickable(false);
        button.setText("已授权");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_detail);
        position = getIntent().getIntExtra("position", 0);
        boolean self = getIntent().getBooleanExtra("self", false);
        if (!self) {
            Button deleteButton = (Button) findViewById(R.id.delete);
            deleteButton.setVisibility(View.GONE);
            Button authorizeButton = (Button) findViewById(R.id.authorize);
            authorizeButton.setVisibility(View.GONE);
        }
        ButterKnife.bind(this);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("详细信息");
        actionBar.setDisplayHomeAsUpEnabled(true);
        imageView.setImageBitmap(ecardListAdapter.bitmaps[position]);
        linearLayout.removeAllViews();
        if (ecardListAdapter.cards.getCards(position).getCheckflag() == 1) {
            hasAuthorize();
        }
        List<String> list = ecardListAdapter.cards.getCards(position).getFieldsList();
        LayoutInflater inflater = getLayoutInflater();
        for (int i = 0; i < list.size(); ++i) {
            String value = list.get(i);
            String key = CardPageFragment.FIELDS[ecardListAdapter.cards.getCards(position).getCardType().getNumber()][i];
            View view = inflater.inflate(R.layout.fragment_card_field, linearLayout, false);
            TextView keyField = (TextView) view.findViewById(R.id.keyField);
            TextView valueField = (TextView) view.findViewById(R.id.valueField);
            keyField.setText(key);
            valueField.setText(value);
            linearLayout.addView(view);
        }
    }
}
