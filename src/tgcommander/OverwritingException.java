package tgcommander;

/**
 * Kivételosztály annak kezelésére, ha fájl másolásakor, vagy
 * áthelyezésekor a célfájl már létezik.
 *
 * @author Kádád István
 */
public class OverwritingException extends Exception {

    public OverwritingException(String msg) {
        super(msg);
    }

}
