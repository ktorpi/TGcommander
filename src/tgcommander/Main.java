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
        EntryAttributes[] cont = wrapped.getContent(false);
        for (int i = 0; i<cont.length; i++) {
            System.out.println(cont[i]);
        }
    }
}
