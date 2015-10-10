package sk.codekitchen.smartfuel.ui.shop;

/**
 * @author Gabriel Lehocky
 *
 *  Holds the data of single item for the ShopActivity
 */
public class ShopItem {
    public int icon;
    public String title;
    public int points;

    public ShopItem(int i, String t, int p){
        super();
        icon = i;
        title = t;
        points = p;
    }

}
