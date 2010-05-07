package tgcommander;

import java.io.File;
import java.io.IOException;

/**
 * Futattható osztály.
 * @author ktorpi
 */
public class Main {
    public static void main(String[] args) throws Exception {
       /* File file1 = new File(args[0]);
        File file2 = new File(args[1]);
        EFile wrapper = new EFile(file1);*/

        System.out.println(EntryAttributes.formatSize(345435));

        // wrapper.renameEntry(file2);
    }
}
