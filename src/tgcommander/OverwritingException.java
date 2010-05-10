/*
 * SZTE-TTIK
 * Programozás I.
 * Tehtességgondozós projektfeladat: Total Commander szerű fájkezelő
 *
 * Készítette: Bán Dénes
 *             Bíró Tímea
 *             Kádár István
 *
 * Weboldal: http://github.com/ktorpi/TGcommander
 *
 * 2010. május 10.
 */

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
