package com.example.hezhu.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v13.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;


import com.example.hezhu.CardUtil;

import java.io.ByteArrayInputStream;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EcardListAdapter extends RecyclerView.Adapter<EcardListAdapter.CardViewHolder> {

    private LayoutInflater layoutInflater;
    public CardUtil.Cards cards = null;
    private Context context;
    public Bitmap[] bitmaps;
    private String transitionName;
    private boolean self;

    public void setCards(CardUtil.Cards cards) {
        this.cards = cards;
        bitmaps = new Bitmap[cards.getCardsCount()];
        for (int i = 0; i < cards.getCardsCount(); ++i) {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(cards.getCards(i).getImage().toByteArray());
            bitmaps[i] = BitmapUtil.resize(BitmapFactory.decodeStream(inputStream), 1000, 1000);
        }
    }

    public EcardListAdapter(Context context, boolean self) {
        layoutInflater = LayoutInflater.from(context);
        cards = null;
        bitmaps = null;
        this.context = context;
        this.self = self;
        transitionName = context.getString(R.string.transition_card);
    }

    @Override
    public CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CardViewHolder(layoutInflater.inflate(R.layout.fragment_card, parent, false));
    }

    @Override
    public void onBindViewHolder(CardViewHolder holder, int position) {
        holder.imageView.setImageBitmap(bitmaps[position]);
    }

    @Override
    public int getItemCount() {
        if (cards == null) {
            return 0;
        }
        return cards.getCardsCount();
    }

    public class CardViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.cardImageView) ImageView imageView;
        @BindView(R.id.cardView) CardView cardView;

        CardViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("NormalTextViewHolder", "onClick--> position = " + getAdapterPosition());
                    Intent intent = new Intent(context, CardDetailActivity.class);
                    intent.putExtra("position", getAdapterPosition());
                    intent.putExtra("self", self);
                    ActivityOptionsCompat options =
                            ActivityOptionsCompat.makeSceneTransitionAnimation((Activity)context,
                                    cardView,
                                    transitionName
                            );
                    CardDetailActivity.ecardListAdapter = EcardListAdapter.this;
                    ActivityCompat.startActivity(context, intent, options.toBundle());
                }
            });
        }
    }

}
