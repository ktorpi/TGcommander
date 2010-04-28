package tgcommander;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Csomagoló osztály.
 * @author Kádár István
 */
public class EFile {

    File file;

    /**
     * Konstruktor.
     * @param f
     */
    public EFile(File f) {
        file = f;
    }


    /**
     * A csomagolt file másolása a megadott fájlba.
     * @param dest A célfájl bejegyzés. Fontos: Nem a célkönyvtár!
     * @throws IOExceptionhiba lépett fel a másolás közben
     */
    private void copyFile(File dest) throws IOException {
        int len;                                        // a tényleges kiolvasott bájtok száma
        byte buffer[] = new byte[2048];                 // 2 KB-os puffer
        FileInputStream in = null;
        FileOutputStream out = null;

        try {
            in = new FileInputStream(file);
            out = new FileOutputStream(dest);
            while ((len = in.read(buffer)) != -1) {     // beolvasás a pufferbe, majd a beolvasott bájtok kiírása
                out.write(buffer, 0, len);
            }
        } catch (IOException e) {
            IOException ex = new IOException("A fájl másolása nem lehetséges innen: "
                                                + file.getAbsolutePath() + " ide: "
                                                + dest.getAbsolutePath());
            ex.initCause(e);
            throw ex;
        } finally {                                     // állományok lezárása
            if (in != null) in.close();
            if (out != null) out.close();
        }

    }

    /**
     * A konstruktorban megadott 'file' fájl vagy mappa másolása. Ha file mappa
     * és nem üres, a másolás rekurzivan történik.
     * 
     * FIXME: Ha egy könyvtárból valmelyik saját alkönyvtárába másolunk, akkor
     * ez az algoritmus megfekszik, mint a büdösbogár, de legalábbis teleírja a lemezt.
     *
     * @param dest Ebbe a könyvtárba másolunk. Fontos, hogy ez csakis könyvtár.
     * @throws IOException Hiba lépett fel a másolás közben.
     */
    void copyEntry(File dest) throws IOException {
        // a forrás rendben van-e?
        if (! file.exists()) {
            throw new IOException("A forrás nem található: " + file.getAbsolutePath());
        } else if (! file.canRead()) {
            throw new IOException("Nincs megfelelő jogosutságod a másoláshoz: " + file.getAbsolutePath());
        }

        // a tényleges cél, ebbe már nem a file, hanem maga a file tartalma másolódik
        dest = new File(dest, file.getName());

        if (file.isDirectory()) {                       // a file könyvtár
            if (! dest.exists()) {
                if (! dest.mkdir()) {
                    throw new IOException("Nem hozható létre a könyvtár: " + dest.getAbsolutePath());
                }
            }
            String[] list = file.list();
            for (String i : list) {
                new EFile(new File(file, i)).copyEntry(dest);
            }
        } else {                                        // ha a file fájl, másoljuk is
            try {
                if (dest.exists()) throw new OverwritingExeption("A fájl már létezik: " + dest.getAbsolutePath());
                copyFile(dest);
            } catch (OverwritingExeption e) {
                /*
                 * És akkor itt fel kéne dobni egy panelt, hogy akor felülírjuk,
                 * átugorjuk vagy mit csináljunk...
                 */
            } catch (IOException e) {
                /*
                 * A másolás nem sikerült, megint kéne egy feldobott ablak, bár lehet
                 * hogy ilyenkor nem szokott lenni.
                 */
            }
        }
    }


    /**
     * A csomagolt 'file'-hoz tartozó bejegyzést törlése. Amennyiben a file mappa
     * és nem üres, rekurzívan töröljük.
     * @return false Ha a törlés nemsikerült (pl.: jogosultságok miatt).
     */
    boolean deleteEntry() throws IOException {
        if (file.isDirectory()) {
            File[] fList = file.listFiles();
            for (File i : fList) {
                if (! new EFile(i).deleteEntry()) {
                    /*
                     * FIXME: nemtudom menynire akarunk belemenni, de ha nagyon
                     * akkor itt is lehetne dobni egy ablkot, hogy kihagyjukjuk, ujrapróbáljuk meg ilyenek...
                     * az "itt"-et ugyértem hogy a kivétel elkapásákor
                     * Ha meg nem akor nem is kell kivételt dobni szerintem itt.
                     */
                    if (i.isFile()) {
                        throw new IOException("Sikertelen törlés: " + i.getAbsolutePath());
                    }
                    return false;
                }
            }
        }

        return file.delete();                              // ha f mappa, mostmár nem üres, törölhetjük
    }

}
