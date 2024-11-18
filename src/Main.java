import com.DylanPerez.www.ims.application.itemtype.InventoryItem;
import com.DylanPerez.www.ims.application.itemtype.Product;
import com.DylanPerez.www.ims.presentation.Store;
import com.DylanPerez.www.ims.presentation.util.Cart;

import java.util.Scanner;

public class Main {

    private static String initData =
            """
iPhone 16/Apple/SPECIALTY/999.99/30/100
4K TV/Sony/SHOPPING/799.00/20/40
Switch/Nintendo/CONVENIENCE/299.99/25/50
Tablet/Microsoft/SPECIALTY/599.99/10/20
Drill/DeWalt/SHOPPING/199.99/15/30
Inspire/Fitbit/SPECIALTY/179.95/20/40
Galaxy/Samsung/SHOPPING/799.99/30/50
Mixer/KitchenAid/SPECIALTY/499.99/12/25
Headphones/Sony/CONVENIENCE/349.99/25/60
Laptop/HP/SHOPPING/699.99/15/30
Chromecast/Google/SPECIALTY/39.99/30/60
QuietComfort 35/Bose/SHOPPING/299.99/10/20
Kitchen Set/Kitchenware/CONVENIENCE/49.99/25/50
XPS 15/Dell/SPECIALTY/1499.99/5/10
Flip 5 Speaker/JBL/SHOPPING/79.99/20/40
GPS/Garmin/SPECIALTY/249.99/15/30
Coffee Maker/CoffeeCo/CONVENIENCE/89.99/25/50
DSLR/Nikon/SHOPPING/799.99/10/20
Watch/Apple/SPECIALTY/399.99/10/20
Blu-ray/Sony/SHOPPING/199.99/15/30
Printer/HP/SPECIALTY/129.99/25/50
Versa/Fitbit/CONVENIENCE/199.95/20/40
Mouse/Logitech/SHOPPING/49.99/30/50
3/Roku/SPECIALTY/99.99/20/40
Xbox One/Microsoft/SHOPPING/299.99/15/30
Kindle/Amazon/SPECIALTY/89.99/20/40
Philips-Head/Philips/SHOPPING/149.99/30/50
TV/Samsung/SPECIALTY/999.99/10/20
Garlic Press/KitchenTools/CONVENIENCE/19.99/25/50
Blender/KitchenAid/SHOPPING/299.99/15/30
Microphone/Sony/SPECIALTY/129.99/10/20
Charge/JBL/SHOPPING/149.99/20/40
D750/Nikon/SPECIALTY/1999.99/5/10
SD Card/SanDisk/CONVENIENCE/29.99/30/50
Fan/Corsair/SHOPPING/89.99/20/40
Key/Microsoft/SPECIALTY/49.99/25/50
Envy 15/HP/CONVENIENCE/1199.99/10/20
iPad/Apple/SHOPPING/329.99/15/30
Soundbar/Bose/SPECIALTY/699.99/10/20
RTX 5090/Nvidia/SPECIALTY/1999.99/0/100/false
            """;

    public static void main(String[] args) {

        Store store = new Store(initData, 100);
        manualTest(store, new Scanner(System.in));
        /*
        ~30,000,000 customers every 7 days
        (target has a 2% chance every second someone will shop--assuming either in store or online)
        https://www.contimod.com/target-statistics/

        store.simulate(604_800, .2);
        */
    }

    private static boolean manualTest(Store store, Scanner scanner) {
        boolean endProgram = false;

        Cart.CartType customerType = null;
        System.out.print("Are you an in-store shopper, or online shopper? (1/2): ");
        if(scanner.hasNextLine())
            customerType = Cart.CartType.values()[Integer.parseInt(scanner.nextLine()) - 1];

        Cart cart = null;
        int option = -1;
        while(!endProgram) {
            if(cart != null) {
                System.out.println("Cart : ");
                cart.getProducts().forEach((k, v) -> {
                    InventoryItem item = store.searchItem(k);
                    Product p = item.getProduct();
                    System.out.println("* [" + v  + "] " + p.getName() + " by " + p.getManufacturer() + " | " + item.getSalesPrice() * v);
                });
                System.out.println();
            }

            System.out.println("[" + customerType.toString()  + "] Options ----" +
                    "\n1. View products" +
                    "\n2. Grab new cart" +
                    "\n3. Add item to cart" +
                    "\n4. Remove item from cart" +
                    "\n5. Check out cart" +
                    "\n6. Exit Store");
            System.out.print("> ");
            if(scanner.hasNextLine())
                option = Integer.parseInt(scanner.nextLine());

            String productSku = "";
            switch(option) {
                case 1:
                    store.listProductsByCategory(customerType);
                    break;
                case 2:
                    cart = store.withdrawCart(customerType);
                    if(cart == null) {
                        System.out.println("[!] Unable to withdraw cart at this time!");
                        break;
                    }
                    System.out.println("[*] Grabbed cart.");
                    break;
                case 3:
                    if(cart == null) {
                        cart = store.withdrawCart(customerType);
                        if(cart == null) {
                            System.out.println("[!] Unable to withdraw cart at this time!");
                            break;
                        }
                        System.out.println("[*] Grabbed cart.");
                    }
                    productSku = "";
                    System.out.print("Enter the name of the item: ");
                    if(scanner.hasNextLine())
                        productSku = store.searchProduct(scanner.nextLine());
                    // TODO: Add check in case item can't be found
                    System.out.println("Item : " + store.searchItem(productSku).getProduct());
                    System.out.print("Enter quantity: ");
                    if(scanner.hasNextLine())
                        cart.addItem(productSku,Integer.parseInt(scanner.nextLine()));
                    break;
                case 4:
                    if(cart == null) {
                        cart = store.withdrawCart(customerType);
                        if(cart == null) {
                            System.out.println("[!] Unable to withdraw cart at this time!");
                            break;
                        }
                        System.out.println("[*] Grabbed cart.");
                    }
                    productSku = "";
                    System.out.print("Enter the name of the item: ");
                    if(scanner.hasNextLine())
                        productSku = store.searchProduct(scanner.nextLine());
                    System.out.println("Item : " + store.searchItem(productSku).getProduct());
                    System.out.print("Enter quantity: ");
                    if(scanner.hasNextLine())
                        cart.removeItem(productSku,Integer.parseInt(scanner.nextLine()));
                    break;
                case 5:
                    if(cart == null) {
                        System.out.println("[!] No cart to check out!");
                        break;
                    }
                    store.checkOutCart(cart);
                    store.depositCart(cart);
                    cart = null;
                    break;
                case 6:
                    break;
                default:
            }
        }

        return true;
    }

}
