package tgcommander;

import java.io.File;
import javax.swing.text.DefaultEditorKit.CopyAction;
/**
 * Futattható osztály.
 * @author ktorpi
 */
public class Main {
    public static void main(String[] args) {
        File file1 = new File(args[0]);
        File file2 = new File(args[1]);

        Operation muv = new Operation();
        // muv.copyFile(file1, file2);
        muv.copyFile(args[0], args[1]);
    }
}
