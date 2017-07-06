package com.example.hezhu.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.hezhu.CardUtil;
import com.example.hezhu.MessageUtil;
import com.google.protobuf.ByteString;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
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

public class CardAddDetailActivity extends AppCompatActivity {

    private void showToast(final String text) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(CardAddDetailActivity.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @BindView(R.id.preview) ImageView imageView;
    private Bitmap bitmap = null;
    private List<EditText> editTexts = new ArrayList<>();

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                bitmap = CardCameraActivity.bitmap;
                bitmap = BitmapUtil.resize(bitmap, 1000, 1000);
                imageView.setImageBitmap(bitmap);
            } else {
                finish();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        finish();
        return true;
    }

    private int mType;

    @OnClick(R.id.btn_submit)
    void submit() {
        for (EditText editText : editTexts) {
            if (editText.length() == 0) {
                showToast("请填写所有字段");
                return;
            }
        }
        final ProgressDialog progressDialog = ProgressDialog.show(this, "上传中", "请稍候");
        progressDialog.setCancelable(false);
        progressDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket();
                    socket.setSoTimeout(10000);
                    socket.connect(new InetSocketAddress(Settings.SERVER_BLOCKCHAIN, Settings.SERVER_BLOCKCHAIN_PORT), 2000);
                    MessageUtil.MessageBox messageBox = MessageUtil.MessageBox.newBuilder().setType(4)
                            .addTransStr(CardPageFragment.username)
                            .build();
                    Log.d("test", messageBox.toString());
                    putMessageToOutputStream(socket.getOutputStream(), messageBox.toByteArray());
                    CardUtil.Card.Builder cardBuilder = CardUtil.Card.newBuilder().setCardType((CardUtil.CardType.forNumber(mType)));
                    for (EditText editText : editTexts) {
                        cardBuilder.addFields(editText.getText().toString());
                    }
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
                    Log.d("test", byteArrayOutputStream.size() + "");
                    cardBuilder.setImage(ByteString.copyFrom(byteArrayOutputStream.toByteArray()));
                    CardUtil.Card card = cardBuilder.build();
                    putMessageToOutputStream(socket.getOutputStream(), card.toByteArray());
                    Log.d("test", card.getSerializedSize() + "");
                    CardUtil.Cards data = CardUtil.Cards.parseFrom(getMessageFromInputStream(socket.getInputStream()));
                    Log.d("test", data.toString());
                    CardAddDetailActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(CardAddDetailActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }
                    });
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_add_detail);
        ButterKnife.bind(this);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("填写证件信息");
        actionBar.setDisplayHomeAsUpEnabled(true);
        mType = getIntent().getIntExtra("type", 0);
        Log.d("type", mType + "");
        if (mType == 0) {
            finish();
        }
        int px = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, this.getResources().getDisplayMetrics());
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.line1);
        for (String s : CardPageFragment.FIELDS[mType]) {
            TextInputLayout textInputLayout = new TextInputLayout(this);
            LinearLayout.LayoutParams layoutParams1 = new AppBarLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams1.setMargins(0, px, 0, px);
            textInputLayout.setLayoutParams(layoutParams1);
            LinearLayout.LayoutParams layoutParams = new AppBarLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            EditText editText = new EditText(this);
            editText.setLayoutParams(layoutParams);
            editText.setHint(s);
            textInputLayout.addView(editText);
            editTexts.add(editText);
            linearLayout.addView(textInputLayout);
        }
        if (bitmap == null) {
            Intent intent = new Intent(this, CardCameraActivity.class);
            startActivityForResult(intent, 1);
        }
    }
}
