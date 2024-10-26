import com.DylanPerez.www.ims.presentation.Store;

public class Main {

    /*
    AI instructions for generating initData test data

    Alright new plan, let's say I am a retailer that needs a Java text block, where each line contains data regarding one specific item.

    Each line contains the item's SKU, name, name of manufacturer, category, price, a number representing a low quantity in the inventory, and a number representing the number if this item to reorder to restock our inventory of this item when we have reach a low quantity in the inventory.

    For the SKU, I want it to be a 10 character long string such that the first two characters of the string represent the category. The category will be represented by a letter from the alphabet, not including X

    This data, within a single line, will be separated by a hyphen symbol (-).

     */

    private static String initData =
            """
            A1XAPPXIPH-iPhone 16-Apple-SPECIALTY-1129.0-0-10
            """;

    public static void main(String[] args) {

        Store store = new Store(initData);

        /*
        ~30,000,000 customers every 7 days
        (target has a 2% chance every second someone will shop--assuming either in store or online)
        https://www.contimod.com/target-statistics/

        store.simulate(604_800, .2);
        */
    }

}
