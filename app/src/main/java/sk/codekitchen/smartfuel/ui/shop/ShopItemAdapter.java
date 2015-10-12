package sk.codekitchen.smartfuel.ui.shop;


import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import sk.codekitchen.smartfuel.*;
import sk.codekitchen.smartfuel.ui.GUI.*;

/**
 * @author Gabriel Lehocky
 *
 *  Holds the shop items for the ShopActivity
 */
public class ShopItemAdapter extends ArrayAdapter<ShopItem>{

    Context context;
    int layoutId;
    ShopItem[] data = null;

    public ShopItemAdapter(Context c, int l, ShopItem[] d) {
        super(c, l, d);
        context = c;
        data = d;
        layoutId = l;
    }

    @Override
    public View getView(int pos, View view, ViewGroup parent){
        View row = view;
        ItemHolder holder = null;

        if (row == null){
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutId, parent, false);

            holder = new ItemHolder();
            holder.picView = (RoundedImageView) row.findViewById(R.id.item_pic);
            holder.titleView = (SemiboldTextView) row.findViewById(R.id.item_title);
            holder.pointView = (LightTextView) row.findViewById(R.id.item_points);

            row.setTag(holder);
        }
        else {
            holder = (ItemHolder) row.getTag();
        }

        ShopItem item = data[pos];
        holder.titleView.setText(item.title);
        holder.pointView.setText(String.valueOf(item.points) + " " + context.getString(R.string.profile_current_points_text));
        holder.picView.setImageResource(item.icon);

        return row;
    }

    static class ItemHolder{
        RoundedImageView picView;
        SemiboldTextView titleView;
        LightTextView pointView;
    }

}
