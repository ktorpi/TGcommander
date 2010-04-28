package tgcommander;

/**
 * Kivételosztály annak kezelésére, ha fájl másolásakor, vagy
 * áthelyezésekor a célfájl már létezik.
 *
 * @author Kádád István
 */
public class OverwritingExeption extends Exception {

    public OverwritingExeption(String msg) {
        super(msg);
    }

}
