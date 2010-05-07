package tgcommander;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;


/**
 * Egy File típusú objektumot becsomagoló osztály, ami
 * könyvtár törléssel, másolással, stb. bővíti a csomagolt File-t.
 *
 * @author Kádár István
 */
public class EFile {

    /** A becsomagolt File. */
    private File file;

    /**
     * Ha a bejegyzés könyvtár ez az adattag tárolja a benen lévő
     * bejegyzések atrribútumait, formázott formában, ha a bejgyzés fájl,
     * értéke null.
     */
    private EntryAttributes[] content = null;


    /**
     * Konstruktor
     * @param f A csomagolt File, amin a műveleteket végezzük.
     */
    public EFile(File f) {
        file = f;
    }

    /**
     * A csomagolt file másolása a megadott fájlba.
     * @param dest A célfájl bejegyzés. Fontos: Nem a célkönyvtár!
     * @throws IOException hiba lépett fel a másolás közben
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
     * @param dest A cél bejegyzés. Ebbe kerül a a forrás tartalma.
     * Fájl esetén ebbe az új fájba kerül a másolandó fáj tartalma, ha
     * pedig könyvtárról van szó, akkor a forráskönyvtár alatti bejegyzések
     * másolódnak a dest alá.
     * @param forced Ha ture és már létezik a célfájl, nem kérdezünk, hanem felülírjuk.
     * 
     * @throws IOException Hiba lépett fel a másolás közben.
     * @throws OverwritingException Már létezika  célfájl.
     */
    public void copyEntry(File dest, boolean forced) throws IOException, OverwritingException {
        // a forrás rendben van-e?
        if (! file.exists()) {
            throw new IOException("A forrás nem található: " + file.getAbsolutePath());
        } else if (! file.canRead()) {
            throw new IOException("Hozzáférés megtagadva " + file.getAbsolutePath()
                                    + " Ellenőrizd a jogosultságokat!");
        }

        if (file.isDirectory()) {                       // a file könyvtár
            if (dest.getAbsolutePath().startsWith(file.getAbsolutePath())) {
                throw new IOException
                            ("Nem másolható/helyezhető át: "
                                + file.getAbsolutePath() + " a saját alkönyvtárába: "
                                + dest.getParent());
            }
            if (! dest.exists()) {
                if (! dest.mkdir()) {
                    throw new IOException("Nem hozható létre a könyvtár: " + dest.getAbsolutePath());
                }
            }
            String[] list = file.list();
            for (String i : list) {
                new EFile(new File(file, i)).copyEntry(new File(dest, i), false);
            }
        } else {                                        // ha a file fájl, másoljuk is
            if (dest.exists() && !forced) {
                throw new OverwritingException("A fájl már létezik: " + dest.getAbsolutePath());
            }
            copyFile(dest);
        }
    }

    /**
     * A csomagolt 'file'-hoz tartozó bejegyzést törlése. Amennyiben a file mappa
     * és nem üres, rekurzívan töröljük.
     * @return false Ha a törlés nemsikerült (pl.: jogosultságok miatt).
     * @throws IOException
     */
    public boolean deleteEntry() throws IOException {
        if (! file.exists()) {
            throw new IOException("Nem található: " + file.getAbsolutePath());
        } else if (! file.canWrite()) {
            throw new IOException("Hozzáférés megtagadva " + file.getAbsolutePath()
                                    + " Ellenőrizd a jogosultságokat!");
        }

        if (file.isDirectory()) {
            File[] fList = file.listFiles();
            for (File i : fList) {
                if (! new EFile(i).deleteEntry()) {
                    /*
                     * TODO: nemtudom menynire akarunk belemenni, de ha nagyon
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

    /**
     * Átnevezés/áthelyezés. Ha a "gyári" renameTo() metódusnak nem sikerül
     * a művelet (pl. mert a cél egy másik fájlrendszeren van), akkor a
     * másolás-törlés kombinációval probálkozunk.
     * @param dest Amire át akarjuk nevezni, ahova átakarjuk helyezni.
     * @throws IOException Sikertelen átnevezés/áthelyezés
     */
    public void renameEntry(File dest) throws IOException, OverwritingException {
        
            if (!dest.exists()) {
                if (!file.renameTo(dest)) {             // ha nem sikerült átnevezéssel, akkor másolás-törlés
                    copyEntry(dest, false);
                    if (!deleteEntry()) {
                        throw new IOException("Nem sikerült törölni a következőt: "
                                + file.getAbsolutePath());
                    }
                }
            } else {
                throw new OverwritingException("Már létezik: " + dest.getAbsolutePath());
            }
        
    }


    /**
     * Getter a file-hoz.
     * @return A file mező.
     */
    public File getFile() {
        return this.file;
    }


    /**
     * A becsomagolt file tartalmának visszadása.
     * @param showHidden listázzuk-e a rejtett fájlokat
     * @return A tartalmom, mint EntryAttributes tömb. Null ha nem sikerült
     * a tartalmat összeállítani, pl. azért mert fájl tartalmát kértük le.
     */
    public EntryAttributes[] getContent(boolean showHidden) {
        if (content == null) {                          // ha null, a content mező inicializálása
            File[] fileList;
            if (showHidden) {
                fileList = file.listFiles();
            } else {
                // implementáljuk a FilenameFilter interfészt
                FilenameFilter filter = new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        // rejtett fájlokat nemkérünk
                        return !(new File(dir, name).isHidden());
                    }
                };
                fileList = file.listFiles(filter);
            }
            if (fileList != null) {
                content = new EntryAttributes[fileList.length];

                for (int i = 0; i < content.length; i++) {
                    content[i] = new EntryAttributes(fileList[i]);
                }

                // a tartalom rendezése: a könyvtárakat előre, azon belöl abc-rendbe
                Arrays.sort(content);
            }
        }

        return content;
    }

}
