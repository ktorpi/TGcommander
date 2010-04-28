package tgcommander;

import java.io.File;
import java.io.IOException;

/**
 * Futattható osztály.
 * @author ktorpi
 */
public class Main {
    public static void main(String[] args) throws IOException {
        File file1 = new File(args[0]);
        File file2 = new File(args[1]);

        EFile wrapped = new EFile(file1);

        wrapped.deleteEntry();
/*
        Operation muv = new Operation();
        muv.copyEntry(file1, file2);
  */      // muv.copyFile(args[0], args[1]);
       /* muv.deleteEntry(file1);
        if (file1.delete()) {
            System.out.println("Sikeres törlés!");
        } else {
            System.out.println("Sikertelen törlés!");
        }
   */ }
}
