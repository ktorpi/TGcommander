package tgcommander;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;

/**
 * Ez csak ilyen kókány osztály, majd máshogy kell ezt megirni, metódusok
 * se a legelegánsabbak néhol...
 * @author ktorpi
 */
public class Operation {

    /**
     * Konstruktor.
     */
    public Operation() {

    }


    /**
     * Fájl másolása a megadott fájlba.
     * FIXME: még majd kezelni kell a felülírást, meg az arra rákérdezést
     * @param source A forrsáfájl bejegyzés.
     * @param dest A célfájl bejegyzés. Fontos: Nem a célkönyvtár!
     */
    void copyFile(File src, File dest) throws IOException {
        int len;                                        // a tényleges kiolvasott bájtok száma
        byte buffer[] = new byte[1024];                 // 1 KB-os puffer
        FileInputStream in = null;
        FileOutputStream out = null;

        try {
            in = new FileInputStream(src);
            out = new FileOutputStream(dest);
            while ((len = in.read(buffer)) != -1) {     // beolvasás a pufferbe, majd a beolvasott bájtok kiírása
                out.write(buffer, 0, len);
            }
        } finally {                                     // állományok lezárása
            if (in != null) in.close();
            if (out != null) out.close();
        }
    }


    /**
     * Fájl vagy mappa másolása. Ha a mappa nem üres, a másolás
     * rekurzivan történik.
     * FIXME: Ha egy könyvtárból valmelyik saját alkönyvtárába másolunk, akkor
     * ez az algoritmus megfekszik, mint a büdösbogár, de legalábbis teleírja a lemezt.
     *
     * @param src E könyvtár alatti bejegyzéseket másoljuk, ill ezt a fájlt. Fontos, hogy
     * maga a mappa nem másolódik, csak a tartalma.
     * @param dest E könyvtárba másolunk, ill ebbe a fájlba.
     * @throws IOException Hiba lépett fel a másolás közben.
     */
    void copyEntry(File src, File dest) throws IOException {
        if (src.isDirectory()) {
            if (!dest.exists()) {                       // ha a célkönyvtár nemlétezett létrehozzuk
                dest.mkdir();
            }

            String[] list = src.list();
            for (String i : list) {                     // a könyvtár tartalmát is másoljuk
                copyEntry(new File(src, i), new File(dest, i));
            }
        } else {
            copyFile(src, dest);
        }
    }


    /**
     * Fájl vagy mappa törlése. Amennyiben a mappa nem üres,
     * rekurzívan töröljük.
     * @param f A törlendő bejegyzés.
     * @return false Ha a törlés nemsikerült (pl.: jogosultságok miatt).
     */
    boolean deleteEntry(File f) {
        if (f.isDirectory()) {
            File[] fList = f.listFiles();
            for (File i : fList) {
                if (! deleteEntry(i)) {
                    return false;
                }
            }
        }

        return f.delete();                              // ha f mappa, mostmár nem üres, törölhetjük
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
