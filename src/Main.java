// @todo Add package-level documentation after completing the IMS project

import com.DylanPerez.www.ims.presentation.IMS;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        boolean testPassed = test();
        if(!testPassed) throw new RuntimeException("Failed test.");

//        Cart c1 = ims.withdrawCart();
//        System.out.println(c1.getId());

//        IMS ims = new IMS(100);

//        IMS IMS = new IMS(initData, 100);
//        manualTest(IMS, new Scanner(System.in));
        /*
        ~30,000,000 customers every 7 days
        (target has a 2% chance every second someone will shop--assuming either in store or online)
        https://www.contimod.com/target-statistics/

        store.simulate(604_800, .2);
        */
    }

    private static boolean test() {
        try {
            boolean testAdmin = true; ///< If true, calls testAdmin, else testCustomer
            if (testAdmin)
                return testAdmin();
            return testCustomer();
        } catch(IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean testAdmin() throws IOException {
        Scanner scanner = new Scanner(System.in);
        String buffer;

        boolean autoRestocks = false;

        System.out.println("[?] Automatic Restocks (y/n) : ");
        buffer = scanner.nextLine();
        autoRestocks = buffer.charAt(0) == 'y';

        IMS ims = new IMS(new File("src/resources/inventory.json"), autoRestocks);
        ims.run();

        return true;
    }

    private static boolean testCustomer() {
//        boolean endProgram = false;
//
//        Cart cart = null;
//        int option = -1;
//        while(!endProgram) {
//            if(cart != null) {
//                System.out.println("Cart : ");
//                cart.getProducts().forEach((k, v) -> {
//                    InventoryItemAccessor item = ims.search(k);
//                    System.out.println("* [" + v  + "] " + item.getName() + " by " + item.getManufacturer() + " | " + item.getPrice() * v);
//                });
//                System.out.println();
//            }
//
//            System.out.println("[Customer Panel] Options ----" +
//                    "\n1. View products" +
//                    "\n2. Grab new cart" +
//                    "\n3. Add item to cart" +
//                    "\n4. Remove item from cart" +
//                    "\n5. Check out cart" +
//                    "\n6. Exit Store");
//            System.out.print("> ");
//            if(scanner.hasNextLine())
//                option = Integer.parseInt(scanner.nextLine());
//
//            String productSku = "";
//            switch(option) {
//                case 1:
//                    ims.listProductsByCategory(customerType);
//                    break;
//                case 2:
//                    cart = ims.withdrawCart(customerType);
//                    if(cart == null) {
//                        System.out.println("[!] Unable to withdraw cart at this time!");
//                        break;
//                    }
//                    System.out.println("[*] Grabbed cart.");
//                    break;
//                case 3:
//                    if(cart == null) {
//                        cart = ims.withdrawCart(customerType);
//                        if(cart == null) {
//                            System.out.println("[!] Unable to withdraw cart at this time!");
//                            break;
//                        }
//                        System.out.println("[*] Grabbed cart.");
//                    }
//                    productSku = "";
//                    System.out.print("Enter the name of the item: ");
//                    if(scanner.hasNextLine())
//                        productSku = ims.searchProduct(scanner.nextLine());
//                    // TODO: Add check in case item can't be found
//                    System.out.println("Item : " + ims.searchItem(productSku).getProduct());
//                    System.out.print("Enter quantity: ");
//                    if(scanner.hasNextLine())
//                        cart.addItem(productSku,Integer.parseInt(scanner.nextLine()));
//                    break;
//                case 4:
//                    if(cart == null) {
//                        cart = ims.withdrawCart(customerType);
//                        if(cart == null) {
//                            System.out.println("[!] Unable to withdraw cart at this time!");
//                            break;
//                        }
//                        System.out.println("[*] Grabbed cart.");
//                    }
//                    productSku = "";
//                    System.out.print("Enter the name of the item: ");
//                    if(scanner.hasNextLine())
//                        productSku = ims.searchProduct(scanner.nextLine());
//                    System.out.println("Item : " + ims.searchItem(productSku).getProduct());
//                    System.out.print("Enter quantity: ");
//                    if(scanner.hasNextLine())
//                        cart.removeItem(productSku,Integer.parseInt(scanner.nextLine()));
//                    break;
//                case 5:
//                    if(cart == null) {
//                        System.out.println("[!] No cart to check out!");
//                        break;
//                    }
//                    ims.checkOutCart(cart);
//                    ims.depositCart(cart);
//                    cart = null;
//                    break;
//                case 6:
//                    break;
//                default:
//            }
//        }

        return true;
    }

}
