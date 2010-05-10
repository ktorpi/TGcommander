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

import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;

/**
 * The main class of the application.
 */
public class TGcommanderApp extends SingleFrameApplication {

    /**
     * At startup create and show the main frame of the application.
     */
    @Override protected void startup() {
        TGcommanderView tgc = new TGcommanderView(this);
        show(tgc);
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override protected void configureWindow(java.awt.Window root) {
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of TGcommanderApp
     */
    public static TGcommanderApp getApplication() {
        return Application.getInstance(TGcommanderApp.class);
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {
        launch(TGcommanderApp.class, args);
    }
}
