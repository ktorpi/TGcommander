package tgcommander;

import java.io.File;
import java.text.DateFormat;

/**
 * Egy bejegyzés attribútumait formázó és tároló osztály.
 * @author Kádár István
 */
public class EntryAttributes implements Comparable<EntryAttributes> {
    /** bejegyzés neve */
    private String name;
    /** kiterjesztés */
    private String ext;
    /** méret */
    private String size;
    /** utolsó módosítás dátuma */
    private String date;
    /** rxw jogosultságok */
    private String rights;

    /**
     * Konstruktor.
     * A paraméterül kapott bejegyzés atribútumainak
     * formázása és eltárolása történik itt.
     * @param f File, melynek attribútumain dolgozuk.
     */
    public EntryAttributes(File f) {
        if (f.isDirectory()) {
            name = f.getName();
            ext = "";
            size = "<DIR>";
        } else {
            int index;
            if ((index = f.getName().lastIndexOf(".")) > 0) {
                name = f.getName().substring(0, index);
                ext = f.getName().substring(index + 1);
            } else {
                name = f.getName();
                ext = "";
            }
            size = formatSize(f.length());
        }
        
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
        date = df.format(f.lastModified());
        rights = (f.canRead()) ? "r" : "-";
        rights += (f.canWrite()) ? "w" : "-";
        rights += (f.canExecute()) ? "x" : "-";
    }

    /** A bejegyzés méreteinek lehetséges mértékegységei. */
    private enum unit {
        B, KB, MB, GB, TB, PB, EB
    }

    /**
     * A bájtokban kapott méret átváltása és kiegészítése a mertékegységel.
     * @param size A formázandó méret bájtokban.
     * @return A formázott méret.
     */
    private String formatSize(long length) {
        int i = 0;
        double s = (double)length;
        while (s >= 1024 && i <= unit.EB.ordinal()) {
            s /= 1024.0;
            i++;
        }
        // kerekítés 2 tizedsjegyre
        s *= 100.0;
        s = Math.rint(s);
        s /=100;

        return s + " " + unit.values()[i].toString();
    }
    
    // Getterek
    
    public String getDate() {
        return date;
    }

    public String getExt() {
        return ext;
    }

    public String getName() {
        return name;
    }

    public String getRights() {
        return rights;
    }

    public String getSize() {
        return size;
    }


    /**
     * Az összehasonlítás úgy történik, hogy a könyvtárakat rendezzük előre, azon
     * belül pedig név szerint abc-sorrendben.
     * @param o 
     * @return
     */
    public int compareTo(EntryAttributes o) {
        if (size.equals("<DIR>") && !o.getSize().equals("<DIR>")) {
            return -1;
        } else if (!size.equals("<DIR>") && o.getSize().equals("<DIR>")) {
            return 1;
        }
        return name.compareTo(o.getName());             // ha mind2 könyvtár vagy mind2 file, a név dönt
    }


    /**
     * Az osztály szöveges reprezentációja.
     * @return Az attribútumok tabulátorral elválasztva.
     */
    @Override
    public String toString() {
        return name + "\t" + ext + "\t" + size + "\t" + date + "\t" + rights;
    }

 }
