package sk.codekitchen.smartfuel.ui;


import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import sk.codekitchen.smartfuel.ui.GUI.*;
import sk.codekitchen.smartfuel.ui.shop.*;
import sk.codekitchen.smartfuel.R;


public class ShopActivity extends Activity implements View.OnClickListener {

    private MainMenu menu;

    private final static int TAB_PRODUCT = 1;
    private final static int TAB_PROMO = 2;
    private final static int TAB_CONTEST = 3;
    private int tab = TAB_PROMO;
    private LightTextView tabProduct;
    private LightTextView tabPromo;
    private LightTextView tabContest;

    private ListView shopList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);

        menu = new MainMenu(this, MainMenu.SHOP_ID);

        setView();
        loadItems();
    }

    private void setView(){
        tabProduct = (LightTextView) findViewById(R.id.shop_product);
        tabPromo = (LightTextView) findViewById(R.id.shop_promo);
        tabContest = (LightTextView) findViewById(R.id.shop_contest);
        tabProduct.setOnClickListener(this);
        tabPromo.setOnClickListener(this);
        tabContest.setOnClickListener(this);

        shopList = (ListView) findViewById(R.id.shop_list_view);

    }

    @Override
    public void onBackPressed() {
        menu.goToActivity(menu.RECORDER_ID, RecorderActivity.class);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.shop_product:
                if (tab != TAB_PRODUCT) changeTab(TAB_PRODUCT);
                break;
            case R.id.shop_promo:
                if (tab != TAB_PROMO) changeTab(TAB_PROMO);
                break;
            case R.id.shop_contest:
                if (tab != TAB_CONTEST) changeTab(TAB_CONTEST);
                break;
        }
    }

    private void changeTab(int newTab){
        switch (newTab){
            case TAB_PRODUCT:
                Utils.setBackgroundOfView(this, tabProduct, R.drawable.border_bottom_selected_good);
                tabProduct.setTextColor(Colors.HIGHIGHT);
                break;
            case TAB_PROMO:
                Utils.setBackgroundOfView(this, tabPromo, R.drawable.border_bottom_selected_good);
                tabPromo.setTextColor(Colors.HIGHIGHT);
                break;
            case TAB_CONTEST:
                Utils.setBackgroundOfView(this, tabContest, R.drawable.border_bottom_selected_good);
                tabContest.setTextColor(Colors.HIGHIGHT);
                break;
        }
        switch (tab){
            case TAB_PRODUCT:
                Utils.setBackgroundOfView(this, tabProduct, R.drawable.border_bottom);
                tabProduct.setTextColor(Colors.GRAY);
                break;
            case TAB_PROMO:
                Utils.setBackgroundOfView(this, tabPromo, R.drawable.border_bottom);
                tabPromo.setTextColor(Colors.GRAY);
                break;
            case TAB_CONTEST:
                Utils.setBackgroundOfView(this, tabContest, R.drawable.border_bottom);
                tabContest.setTextColor(Colors.GRAY);
                break;
        }
        tab = newTab;

        loadItems();
    }

    private void loadItems(){

        switch (tab){
            case TAB_PRODUCT:
                ShopItem data[] = new ShopItem[]{
                        new ShopItem(R.mipmap.product_01, "Bazant Radler Citron - 500ml", 205),
                        new ShopItem(R.mipmap.product_02, "Nescafe Original - 250ml", 190),
                        new ShopItem(R.mipmap.product_03, "Max Atlas - Europa 2016/2017", 270),
                        new ShopItem(R.mipmap.product_04, "Sheron Nano Plus - Ostrekovac - 5l", 450),
                        new ShopItem(R.mipmap.product_05, "Total Quarz 5W40 - 5l", 600)
                };
                ShopItemAdapter adapter = new ShopItemAdapter(this, R.layout.shop_item_with_picture, data);
                shopList.setAdapter(adapter);
                break;
            case TAB_PROMO:
                break;
            case TAB_CONTEST:
                break;
        }


    }
}
