package tgcommander;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;

/**
 *
 * @author ktorpi
 */
public class Operation {

    /**
     * Konstruktor.
     */
    public Operation() {

    }

    /**
     * Fájl másolása.
     * @param source A forrásfájl elérési útvonala.
     * @param dest A célfájl elérési útvonala. Egyenlőre fájl!
     */
    void copyFile(String source, String dest) {
        int b;
        FileInputStream in = null;
        FileOutputStream out = null;

        try {
            in = new FileInputStream(source);
            out = new FileOutputStream(dest);
            while ((b = in.read()) != -1) {             // olvasás-írás bájtonként
                out.write(b);
            }
        } catch (IOException e){
            // FIXME: lehet hogy nem a System.err-re kéne irni ha már desktop application
            System.err.println(e.getMessage());
        } finally {                                     // állományok lezárása
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                // FIXME:
                System.err.println(e.getMessage());
            }
        }
    }

    /**
     * Fájl másolása.
     * @param source A forrsáfájl bejegyzés.
     * @param dest Célfájl bejegyzés. Egyenlőre fájl!
     */
    void copyFile(File source, File dest) {
        int b;
        FileInputStream in = null;
        FileOutputStream out = null;

        try {
            in = new FileInputStream(source);
            out = new FileOutputStream(dest);
            while ((b = in.read()) != -1) {             // olvasás-írás bájtonként
                out.write(b);
            }
        } catch (IOException e){
            // FIXME: lehet hogy nem a System.err-re kéne irni ha már desktop application
            System.err.println(e.getMessage());
        } finally {                                     // állományok lezárása
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                // FIXME:
                System.err.println(e.getMessage());
            }
        }
    }

    /**
     * Könyvtár tartalmának listázása.
     * @param Dir A könyvtárbejegyzés, aminek a tartalmát listázzuk.
     */
    void listDir(File dir) {
        File[] files = dir.listFiles();
        String name, ext, size, date, attr;

        // először a könyvtárak

        for (File i : files) {
            if (i.isDirectory()) {
                DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
                date = df.format(i.lastModified());
                attr = (i.canRead()) ? "r" : "-";
                attr += (i.canWrite()) ? "w" : "-";

                name = i.getName();
                ext = "";
                size = "<DIR>";
                System.out.println(name + "\t" + ext + "\t" + size + "\t" + date + "\t" + attr);
            }
        }

        // majd a fájlok
        for (File i : files) {
            if (i.isFile()) {
                DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
                date = df.format(i.lastModified());
                attr = (i.canRead()) ? "r" : "-";
                attr += (i.canWrite()) ? "w" : "-";
                int index = i.getName().lastIndexOf(".");
                name = i.getName().substring(0, index);
                ext = i.getName().substring(index + 1);
                size = new Long(i.length()).toString();
                System.out.println(name + "\t" + ext + "\t" + size + "\t" + date + "\t" + attr);
            }
        }
    }
    
}
