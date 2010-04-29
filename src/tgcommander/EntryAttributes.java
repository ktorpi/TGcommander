package tgcommander;

import java.io.File;
import java.text.DateFormat;

/**
 * Egy bejegyzés attribútumait formázó és tároló osztály.
 * @author Kádár István
 */
public class EntryAttributes implements Comparable<EntryAttributes> {

    private String name;                    // név
    private String ext;                     // kiterjesztés, könyvtárnál üres
    private String size;                    // méret, könyvtár esetén "<DIR>"
    private String date;                    // utolsó modosítás dátuma
    private String rights;                  // jogosultságok (rwx)

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
            size = String.valueOf(f.length());
        }
        
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
        date = df.format(f.lastModified());
        rights = (f.canRead()) ? "r" : "-";
        rights += (f.canWrite()) ? "w" : "-";
        rights += (f.canExecute()) ? "x" : "-";
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
    public String toString() {
        return name + "\t" + ext + "\t" + size + "\t" + date + "\t" + rights;
    }

 }
