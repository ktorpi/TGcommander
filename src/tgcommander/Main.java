package tgcommander;

import java.io.File;
/**
 * Futattható osztály.
 * @author ktorpi
 */
public class Main {
    public static void main(String[] args) {
        File file = new File(args[0]);
        Operation muv = new Operation();
        muv.listDir(file);
    }
}
