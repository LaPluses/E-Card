package com.example.hezhu.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.ListViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.azoft.carousellayoutmanager.CarouselLayoutManager;
import com.azoft.carousellayoutmanager.CarouselZoomPostLayoutListener;
import com.azoft.carousellayoutmanager.CenterScrollListener;
import com.example.hezhu.CardUtil;
import com.example.hezhu.MessageUtil;
import com.google.protobuf.ByteString;
import com.mutualmobile.cardstack.CardStackAdapter;
import com.mutualmobile.cardstack.CardStackLayout;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.example.hezhu.myapplication.TCPUtil.getMessageFromInputStream;
import static com.example.hezhu.myapplication.TCPUtil.putMessageToOutputStream;

public class CardActivity extends AppCompatActivity {

    TextView noCardTextView;

    public static String username;

    private EcardListAdapter ecardListAdapter;
    private Button refreshButton;
    private RecyclerView cardStackLayout;

    private void showToast(final String text) {
        CardActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(CardActivity.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void refresh() {
        cardStackLayout.setVisibility(View.INVISIBLE);
        final ProgressDialog progressDialog = ProgressDialog.show(CardActivity.this, "刷新中", "请稍候");
        progressDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket();
                    socket.setSoTimeout(2000);
                    socket.connect(new InetSocketAddress(Settings.SERVER_BLOCKCHAIN, Settings.SERVER_BLOCKCHAIN_PORT), 2000);
                    MessageUtil.MessageBox messageBox = MessageUtil.MessageBox.newBuilder().setType(10)
                            .addTransStr(username)
                            .build();
                    Log.d("test", messageBox.toString());
                    putMessageToOutputStream(socket.getOutputStream(), messageBox.toByteArray());
                    final CardUtil.Cards data = CardUtil.Cards.parseFrom(getMessageFromInputStream(socket.getInputStream()));
                    ecardListAdapter.setCards(data);
                    for (CardUtil.Card card : data.getCardsList()) {
                        Log.d("test", card.getCardID() + "");
                    }
                    CardActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            refreshButton.setVisibility(View.INVISIBLE);
                            if (data.getCardsCount() == 0) {
                                noCardTextView.setVisibility(View.VISIBLE);
                            } else {
                                noCardTextView.setVisibility(View.INVISIBLE);
                                cardStackLayout.setVisibility(View.VISIBLE);
                            }
                            ecardListAdapter.notifyDataSetChanged();
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
    public boolean onOptionsItemSelected(MenuItem item){
        onBackPressed();
        return true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(username + "的证件");
        actionBar.setDisplayHomeAsUpEnabled(true);
        cardStackLayout = (RecyclerView) findViewById(R.id.cardStack);
        noCardTextView = (TextView) findViewById(R.id.noCard);
        noCardTextView.setText("对方没有已授权的证件");
        FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        floatingActionButton.setVisibility(View.GONE);
        refreshButton = (Button) findViewById(R.id.refresh);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh();
            }
        });
        final CarouselLayoutManager layoutManager = new CarouselLayoutManager(CarouselLayoutManager.VERTICAL);
        layoutManager.setMaxVisibleItems(10);
        ecardListAdapter = new EcardListAdapter(this, false);
        cardStackLayout.setHasFixedSize(false);
        cardStackLayout.setLayoutManager(layoutManager);
        cardStackLayout.addOnScrollListener(new CenterScrollListener());
        cardStackLayout.setAdapter(ecardListAdapter);
        layoutManager.setPostLayoutListener(new CarouselZoomPostLayoutListener());
        refresh();
    }
}
