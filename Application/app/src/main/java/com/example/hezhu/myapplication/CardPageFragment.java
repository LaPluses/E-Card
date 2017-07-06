package com.example.hezhu.myapplication;


import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.azoft.carousellayoutmanager.CarouselLayoutManager;
import com.azoft.carousellayoutmanager.CarouselZoomPostLayoutListener;
import com.azoft.carousellayoutmanager.CenterScrollListener;
import com.example.hezhu.CardUtil;
import com.example.hezhu.MessageUtil;
import com.google.android.gms.vision.text.Text;
import com.google.protobuf.ByteString;

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

public class CardPageFragment extends Fragment {

    TextView noCardTextView;

    public static final String[][] FIELDS = {
            {},
            {"姓名", "性别", "民族", "出生", "住址", "公民身份号码", "签发机关", "有效期限"},
            {"姓名", "性别", "国籍", "住址", "出生日期", "初次领证日期", "准驾车型", "有效期限"},
            {"姓名", "性别", "民族", "出生日期", "学院", "班次", "学号", "签发日期"},
            {"姓名", "卡号"},
            {"姓名", "证件号", "签发日期"},
    };

    public static String username;
    public static String userID;
    public static String code;

    private EcardListAdapter ecardListAdapter;
    private Button refreshButton;
    private RecyclerView cardStackLayout;

    void add() {
        Intent intent = new Intent(getActivity(), CardAddActivity.class);
        startActivityForResult(intent, 1);
    }

    private void showToast(final String text) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void refresh() {
        cardStackLayout.setVisibility(View.INVISIBLE);
        final ProgressDialog progressDialog = ProgressDialog.show(getActivity(), "刷新中", "请稍候");
        progressDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket();
                    socket.setSoTimeout(2000);
                    socket.connect(new InetSocketAddress(Settings.SERVER_BLOCKCHAIN, Settings.SERVER_BLOCKCHAIN_PORT), 2000);
                    MessageUtil.MessageBox messageBox = MessageUtil.MessageBox.newBuilder().setType(5)
                            .addTransStr(username)
                            .build();
                    Log.d("test", messageBox.toString());
                    putMessageToOutputStream(socket.getOutputStream(), messageBox.toByteArray());
                    final CardUtil.Cards data = CardUtil.Cards.parseFrom(getMessageFromInputStream(socket.getInputStream()));
                    ecardListAdapter.setCards(data);
                    for (CardUtil.Card card : data.getCardsList()) {
                        Log.d("test", card.getCardID() + "");
                    }
                    getActivity().runOnUiThread(new Runnable() {
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_card, container, false);
        cardStackLayout = (RecyclerView) view.findViewById(R.id.cardStack);
        noCardTextView = (TextView) view.findViewById(R.id.noCard);
        FloatingActionButton floatingActionButton = (FloatingActionButton) view.findViewById(R.id.floatingActionButton);
        refreshButton = (Button) view.findViewById(R.id.refresh);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh();
            }
        });
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                add();
            }
        });
        final CarouselLayoutManager layoutManager = new CarouselLayoutManager(CarouselLayoutManager.VERTICAL);
        layoutManager.setMaxVisibleItems(10);
        ecardListAdapter = new EcardListAdapter(getContext(), true);
        cardStackLayout.setHasFixedSize(false);
        cardStackLayout.setLayoutManager(layoutManager);
        cardStackLayout.addOnScrollListener(new CenterScrollListener());
        cardStackLayout.setAdapter(ecardListAdapter);
        layoutManager.setPostLayoutListener(new CarouselZoomPostLayoutListener());
        refresh();
        return view;
    }
}
