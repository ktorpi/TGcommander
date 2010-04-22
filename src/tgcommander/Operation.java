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
     * FIXME: még majd kezelni kell a felülírást, meg az arra rákérdezést
     * @param source A forrásfájl elérési útvonala.
     * @param dest A célkönyvtár elérési útvonala.
     */
    void copyFile(String source, String dest) {
        int b;
        FileInputStream in = null;
        FileOutputStream out = null;

        try {
            in = new FileInputStream(source);
            out = new FileOutputStream(dest + source.substring(source.lastIndexOf("/")));
            while ((b = in.read()) != -1) {             // olvasás-írás bájtonként
                out.write(b);
            }
        } catch (IOException e){
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
                System.err.println(e.getMessage());
            }
        }
    }

    /**
     * Fájl másolása.
     * FIXME: még majd kezelni kell a felülírást, meg az arra rákérdezést
     * @param source A forrsáfájl bejegyzés.
     * @param dest Célkönyvtár bejegyzés.
     */
    void copyFile(File source, File dest) {
        int b;
        FileInputStream in = null;
        FileOutputStream out = null;

        try {
            in = new FileInputStream(source);
            out = new FileOutputStream(dest.getPath() + "/" + source.getName());
            while ((b = in.read()) != -1) {             // olvasás-írás bájtonként
                out.write(b);
            }
        } catch (IOException e){
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
        String name,                                    // név
               ext,                                     // kiterjesztés ha nem mappa
               size,                                    // méret (egyenlőre bájtokban)
               date,                                    // utolsó módosítás dátuma
               attr;                                    // olvasható-írható attribútumok

        // először a könyvtárak
        for (File i : files) {
            if (i.isDirectory()) {
                DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
                date = df.format(i.lastModified());     // utolsó módosítás dátumának formázása
                attr = (i.canRead()) ? "r" : "-";
                attr += (i.canWrite()) ? "w" : "-";
                name = i.getName();
                // könyvtár esetén nincs kiterjesztés, a méret: "<DIR>"
                ext = "";
                size = "<DIR>";
                System.out.println(name + "\t" + ext + "\t" + size + "\t" + date + "\t" + attr);
            }
        }

        // majd a fájlok listázása
        for (File i : files) {
            if (i.isFile()) {
                DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
                date = df.format(i.lastModified());     // utolsó módosítás dátumának formázása
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
