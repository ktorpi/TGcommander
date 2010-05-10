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

import java.awt.Color;
import java.awt.Cursor;
import org.jdesktop.application.Action;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import java.io.*;

/**
 * The application's main frame.
 */
public class TGcommanderView extends FrameView implements MouseListener {

    //az oldal tartalma
    private EFile bal;
    private EFile jobb;

    //melyik oldalon van a fókusz
    private boolean focus = false;

    //jelenjenek-e meg a rejtett file-ok
    private boolean showHidden = false;

    //hol volt
    private int holVoltBal;
    private int holVoltJobb;

    /**
     * az egér lekezelése
     * @author Bán Dénes
     * @param e - az esemény
     */
    public void mouseClicked(MouseEvent e){

        //ha a másik oldalra kattintottunk, mint ahol a focus van, akkor change
        JTable target = (JTable)e.getSource();
        int selection = target.getSelectedRow();
        if ((target == balLista && focus) || (target == jobbLista && !focus)) {
            new ChangeFocusAction().actionPerformed(null);
            // a selection megmarad
            target.getSelectionModel().setSelectionInterval(selection, selection);
        }

        //ha duplakatt, akkor listazas
        if (e.getClickCount() == 2) {
            listazasAction();
        }
    }

    /**
     * a focus oldalra listázza a kijelölt file-t, ha directory
     * @author Bán Dénes
     */
    public void listazasAction() {
        JTable t = null;
        EFile oldal = null;
        if (focus) {
            oldal = jobb;
            t = jobbLista;
        } else {
            oldal = bal;
            t = balLista;
        }
        int id = t.getSelectedRow();
        String uj = oldal.getFile().getAbsolutePath();
        if (id == 0) {
            uj = oldal.getFile().getParent();
        } else if (id > 0) {
            uj = oldal.getFile().getAbsolutePath() +
                    File.separator + t.getValueAt(id, 0);
        }

        File temp = oldal.getFile();
        try {
            temp = new File(uj);
        } catch (NullPointerException exc) {
            //már root voltunk, nincs parent
        }

        if (temp.isDirectory()) {
            if (temp.canExecute()) {
                listDir(focus,new EFile(temp),showHidden);
                try {
                if (temp.getParent().equals("/")) {                
                    if (focus) jobbKonyvtar.setText("/"+temp.getName());
                    else balKonyvtar.setText("/"+temp.getName());
                } else {
                    if (focus) jobbKonyvtar.setText(uj);
                    else balKonyvtar.setText(uj);
                }
                } catch (NullPointerException n) {
                    if (focus) jobbKonyvtar.setText("/");
                    else balKonyvtar.setText("/");
                }
                if (focus) jobbFajlokSzama.setText(fajlOsszesites(new EFile(temp),showHidden));
                else balFajlokSzama.setText(fajlOsszesites(new EFile(temp),showHidden));
            } else {
                JOptionPane.showMessageDialog(mainPanel, "Nincs jogosultságod megnyitni!");
            }
        }
    }

    /**
     * "hova" oldalra kilistázza "mit" könyvtárat, ha "hidden", akkor a rejtetteket is
     * @author Bán Dénes
     * @author Módosította: Bíró Tímea
     * @param hova
     * @param mit
     * @param hidden
     */
    public void listDir(boolean hova, EFile mit, boolean hidden) {
        EntryAttributes[] ea = mit.getContent(hidden);
        JTable target;

        if (hova) {
            target = jobbLista;
            holVoltJobb = 0;
            jobb = mit;
        } else {
            target = balLista;
            holVoltBal = 0;
            bal = mit;
        }
        DefaultTableModel dtm = (DefaultTableModel)target.getModel();
        dtm.getDataVector().removeAllElements();
        Object[][] tomb = null;
        int i = 1;
        if (!(mit.getFile().getAbsolutePath().equals("/"))) {
            tomb = new Object[ea.length+1][5];
            tomb[0][0] = "..";
            for (int j=1; j<5; j++) {tomb[0][j] = ""; }
        } else {
            i=0;
            tomb = new Object[ea.length][5];
        }
        for (EntryAttributes e : ea) {
            tomb[i][0] = e.getName();
            tomb[i][1] = e.getExt();
            tomb[i][2] = e.getSize();
            tomb[i][3] = e.getDate();
            tomb[i][4] = e.getRights();
            i++;
        }
        target.setModel(new javax.swing.table.DefaultTableModel(
            tomb,
            new String [] {
                "Név", "Kiterjesztés", "Méret", "Utoljára módosítva", "Jogosultságok"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        TableColumn column = null;
        column = target.getColumnModel().getColumn(0);
        column.setPreferredWidth(170);
        column = target.getColumnModel().getColumn(1);
        column.setPreferredWidth(40);
        column = target.getColumnModel().getColumn(2);
        column.setPreferredWidth(80);
        column = target.getColumnModel().getColumn(3);
        column.setPreferredWidth(100);
        column = target.getColumnModel().getColumn(4);
        column.setPreferredWidth(30);
        if (focus == hova) {
            target.getSelectionModel().setSelectionInterval(0, 0);
        }
    }

    /**
     * debug segédfv
     * @author Bán Dénes
     * @param m - plusz message
     */
    public void __dump(String m) {
        String msg = m + "\n\njobb: "+ jobb.getFile().getAbsolutePath()
                + "\nbal: "+ bal.getFile().getAbsolutePath()
                + "\nfocus: "+focus;
        JOptionPane.showMessageDialog(menuBar, msg);
    }

    /**
     * oldalak frissítése selection megtartásával, ellenőrzésekkel
     * @author Bán Dénes
     */
    public void refresh() {
        int id;
        if (focus) {
            id = jobbLista.getSelectedRow();
        } else {
            id = balLista.getSelectedRow();
        }
        try {
            bal = new EFile(bal.getFile());
            listDir(false,bal,showHidden);
            balFajlokSzama.setText(fajlOsszesites(bal,showHidden));
        } catch (NullPointerException ex) {
            bal = new EFile(new File("/"));
            listDir(false,bal,showHidden);
            balFajlokSzama.setText(fajlOsszesites(bal,showHidden));
        }
        try {
            jobb = new EFile(jobb.getFile());
            listDir(true,jobb,showHidden);
            jobbFajlokSzama.setText(fajlOsszesites(jobb,showHidden));
        } catch (NullPointerException ex) {
            jobb = new EFile(new File("/"));
            listDir(true,jobb,showHidden);
            jobbFajlokSzama.setText(fajlOsszesites(jobb,showHidden));
        }
        if (focus) {
            jobbLista.getSelectionModel().setSelectionInterval(id, id);
            balLista.clearSelection();
        } else {
            jobbLista.clearSelection();
            balLista.getSelectionModel().setSelectionInterval(id, id);
        }
    }
    /**
     * A könyvtárban lévő fájlok számát és méretét összesíti (az alkönyvtárakban lévőket nem)
     * @author Bíró Tímea
     * @param konyvtar      a könyvtár, amelyben elvégezzük az összesítést
     * @param rejtettekIs   a rejtett fájlokat is számolja-e
     * @return String       a szöveg, amit majd a fejlécben megjelenítünk
     */
    public String fajlOsszesites(EFile konyvtar, boolean rejtettekIs) {
        EntryAttributes[] ea = konyvtar.getContent(rejtettekIs);
        long osszmeret=0;
        int osszdarab=0;
        File nev=null;
        for (EntryAttributes e : ea) {
            nev = new File(konyvtar.getFile().getAbsolutePath()+File.separator+e.getName()+"."+e.getExt());
            if (nev.isFile()) {
                osszmeret+=nev.length();
                osszdarab++;
            }
        }
        return osszdarab+" fájl, összesen "+EntryAttributes.formatSize(osszmeret);
    }

    //ezek csak a MouseListener interface miatt kellenek
    public void mousePressed(MouseEvent e){}
    public void mouseReleased(MouseEvent e){}
    public void mouseEntered(MouseEvent e){}
    public void mouseExited(MouseEvent e){}

    /**
     * Belső class, hogy az enter actionMap-jába is mehessen a listazasAction fv.
     * @author Bán Dénes
     */
    class ListazasAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            listazasAction();
        }
    }

    /**
     * A fókuszváltást lekezelő belső class
     * @author Bán Dénes
     */
    class ChangeFocusAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            if (focus) {
                balLista.requestFocus();
                balLista.getSelectionModel().setSelectionInterval(holVoltBal, holVoltBal);
                holVoltJobb = jobbLista.getSelectedRow();
                jobbLista.clearSelection();
                focus = false;
            } else {
                jobbLista.requestFocus();
                jobbLista.getSelectionModel().setSelectionInterval(holVoltJobb, holVoltJobb);
                holVoltBal = balLista.getSelectedRow();
                balLista.clearSelection();
                focus = true;
            }
        }
    }


    public TGcommanderView(SingleFrameApplication app) {
        super(app);

        initComponents();

        //custom initialize
        listDir(false,new EFile(new File("/")),showHidden);
        listDir(true,new EFile(new File("/")),showHidden);
        balKonyvtar.setText("/");
        jobbKonyvtar.setText("/");
        balFajlokSzama.setText(fajlOsszesites(new EFile(new File("/")),showHidden));
        jobbFajlokSzama.setText(fajlOsszesites(new EFile(new File("/")),showHidden));
        balLista.addMouseListener(this);
        jobbLista.addMouseListener(this);

        //enter és tab működésének beállítása
        KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        KeyStroke tab = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
        balLista.getActionMap().put("listazas", new ListazasAction());
        balLista.getInputMap(JTable.WHEN_FOCUSED).put(enter, "listazas");
        jobbLista.getActionMap().put("listazas", new ListazasAction());
        jobbLista.getInputMap(JTable.WHEN_FOCUSED).put(enter, "listazas");
        balLista.getActionMap().put("changefocus", new ChangeFocusAction());
        balLista.getInputMap(JTable.WHEN_FOCUSED).put(tab, "changefocus");
        jobbLista.getActionMap().put("changefocus", new ChangeFocusAction());
        jobbLista.getInputMap(JTable.WHEN_FOCUSED).put(tab, "changefocus");

        balLista.requestFocus();

        // a táblázatoktól és a splitPane-től elvesszük az F6, F8 bilentyűkhöz tartozó actionöket
        panelek.getActionMap().getParent().remove("startResize");
        panelek.getActionMap().getParent().remove("toggleFocus");
        jobbLista.getActionMap().getParent().remove("focusHeader");
        balLista.getActionMap().getParent().remove("focusHeader");
    }

    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = TGcommanderApp.getApplication().getMainFrame();
            aboutBox = new TGcommanderAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        TGcommanderApp.getApplication().show(aboutBox);
    }


    /**
     * A program felülete a Netbeans formeditorjával készült.
     * @author Bíró Tímea, Módosította: Kádár István
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        eszkoztar = new javax.swing.JToolBar();
        atnevezesGomb = new javax.swing.JButton();
        masolasGomb = new javax.swing.JButton();
        athelyezesGomb = new javax.swing.JButton();
        ujKonyvtarGomb = new javax.swing.JButton();
        torlesGomb = new javax.swing.JButton();
        kilepesGomb = new javax.swing.JButton();
        panelek = new javax.swing.JSplitPane();
        balPanel = new javax.swing.JPanel();
        balScrollPane = new javax.swing.JScrollPane(balLista);
        balLista = new javax.swing.JTable();
        balFelsoPanel = new javax.swing.JPanel();
        balKonyvtar = new javax.swing.JLabel();
        balGyokerGomb = new javax.swing.JButton();
        balSzuloGomb = new javax.swing.JButton();
        balFajlokSzama = new javax.swing.JLabel();
        jobbPanel = new javax.swing.JPanel();
        jobbScrollPane = new javax.swing.JScrollPane(balLista);
        jobbLista = new javax.swing.JTable();
        jobbFelsoPanel = new javax.swing.JPanel();
        jobbKonyvtar = new javax.swing.JLabel();
        jobbGyokerGomb = new javax.swing.JButton();
        jobbSzuloGomb = new javax.swing.JButton();
        jobbFajlokSzama = new javax.swing.JLabel();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        atnevezesMenuItem = new javax.swing.JMenuItem();
        masolasMenuItem = new javax.swing.JMenuItem();
        athelyezesMenuItem = new javax.swing.JMenuItem();
        ujKonyvtarMenuItem = new javax.swing.JMenuItem();
        torlesMenuItem = new javax.swing.JMenuItem();
        elvalaszto1 = new javax.swing.JPopupMenu.Separator();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        nezetMenu = new javax.swing.JMenu();
        rejtettFajlMenupont = new javax.swing.JCheckBoxMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        statusMessageLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();

        mainPanel.setMaximumSize(new java.awt.Dimension(1280, 1280));
        mainPanel.setMinimumSize(new java.awt.Dimension(400, 400));
        mainPanel.setName("mainPanel"); // NOI18N
        mainPanel.setPreferredSize(new java.awt.Dimension(800, 600));

        eszkoztar.setRollover(true);
        eszkoztar.setMaximumSize(new java.awt.Dimension(1100, 25));
        eszkoztar.setMinimumSize(new java.awt.Dimension(300, 25));
        eszkoztar.setName("eszkoztar"); // NOI18N
        eszkoztar.setPreferredSize(new java.awt.Dimension(300, 25));

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(tgcommander.TGcommanderApp.class).getContext().getActionMap(TGcommanderView.class, this);
        atnevezesGomb.setAction(actionMap.get("atnevezes")); // NOI18N
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(tgcommander.TGcommanderApp.class).getContext().getResourceMap(TGcommanderView.class);
        atnevezesGomb.setText(resourceMap.getString("atnevezesGomb.text")); // NOI18N
        atnevezesGomb.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        atnevezesGomb.setFocusable(false);
        atnevezesGomb.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        atnevezesGomb.setMaximumSize(new java.awt.Dimension(90, 25));
        atnevezesGomb.setMinimumSize(new java.awt.Dimension(90, 25));
        atnevezesGomb.setName("atnevezesGomb"); // NOI18N
        atnevezesGomb.setPreferredSize(new java.awt.Dimension(90, 25));
        atnevezesGomb.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        eszkoztar.add(atnevezesGomb);

        masolasGomb.setAction(actionMap.get("masolas")); // NOI18N
        masolasGomb.setText(resourceMap.getString("masolasGomb.text")); // NOI18N
        masolasGomb.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        masolasGomb.setFocusable(false);
        masolasGomb.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        masolasGomb.setMaximumSize(new java.awt.Dimension(90, 25));
        masolasGomb.setMinimumSize(new java.awt.Dimension(90, 25));
        masolasGomb.setName("masolasGomb"); // NOI18N
        masolasGomb.setPreferredSize(new java.awt.Dimension(90, 25));
        eszkoztar.add(masolasGomb);

        athelyezesGomb.setAction(actionMap.get("athelyezes")); // NOI18N
        athelyezesGomb.setText(resourceMap.getString("athelyezesGomb.text")); // NOI18N
        athelyezesGomb.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        athelyezesGomb.setFocusable(false);
        athelyezesGomb.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        athelyezesGomb.setMaximumSize(new java.awt.Dimension(90, 25));
        athelyezesGomb.setMinimumSize(new java.awt.Dimension(90, 25));
        athelyezesGomb.setName("athelyezesGomb"); // NOI18N
        athelyezesGomb.setPreferredSize(new java.awt.Dimension(90, 25));
        eszkoztar.add(athelyezesGomb);

        ujKonyvtarGomb.setAction(actionMap.get("ujKonyvtar")); // NOI18N
        ujKonyvtarGomb.setText(resourceMap.getString("ujKonyvtarGomb.text")); // NOI18N
        ujKonyvtarGomb.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        ujKonyvtarGomb.setFocusable(false);
        ujKonyvtarGomb.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        ujKonyvtarGomb.setMaximumSize(new java.awt.Dimension(90, 25));
        ujKonyvtarGomb.setMinimumSize(new java.awt.Dimension(90, 25));
        ujKonyvtarGomb.setName("ujKonyvtarGomb"); // NOI18N
        ujKonyvtarGomb.setPreferredSize(new java.awt.Dimension(90, 25));
        eszkoztar.add(ujKonyvtarGomb);

        torlesGomb.setAction(actionMap.get("torles")); // NOI18N
        torlesGomb.setText(resourceMap.getString("torlesGomb.text")); // NOI18N
        torlesGomb.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        torlesGomb.setFocusable(false);
        torlesGomb.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        torlesGomb.setMaximumSize(new java.awt.Dimension(90, 25));
        torlesGomb.setMinimumSize(new java.awt.Dimension(90, 25));
        torlesGomb.setName("torlesGomb"); // NOI18N
        torlesGomb.setPreferredSize(new java.awt.Dimension(90, 25));
        torlesGomb.setMnemonic(KeyEvent.VK_F8);
        eszkoztar.add(torlesGomb);

        kilepesGomb.setAction(actionMap.get("quit")); // NOI18N
        kilepesGomb.setText(resourceMap.getString("kilepesGomb.text")); // NOI18N
        kilepesGomb.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        kilepesGomb.setFocusable(false);
        kilepesGomb.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        kilepesGomb.setMaximumSize(new java.awt.Dimension(90, 25));
        kilepesGomb.setMinimumSize(new java.awt.Dimension(90, 25));
        kilepesGomb.setName("kilepesGomb"); // NOI18N
        kilepesGomb.setPreferredSize(new java.awt.Dimension(90, 25));
        eszkoztar.add(kilepesGomb);

        panelek.setDividerLocation(400);
        panelek.setResizeWeight(0.5);
        panelek.setMinimumSize(new java.awt.Dimension(400, 200));
        panelek.setName("panelek"); // NOI18N
        panelek.setPreferredSize(new java.awt.Dimension(800, 500));

        balPanel.setName("balPanel"); // NOI18N

        balScrollPane.setAutoscrolls(true);
        balScrollPane.setName("balScrollPane"); // NOI18N
        balScrollPane.setPreferredSize(new java.awt.Dimension(400, 600));

        balLista.setBackground(resourceMap.getColor("balLista.background")); // NOI18N
        balLista.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Név", "Méret", "Utoljára módosítva", "Jogosultságok"
            }
        ));
        balLista.setFillsViewportHeight(true);
        balLista.setName("balLista"); // NOI18N
        balLista.setShowVerticalLines(false);
        balScrollPane.setViewportView(balLista);

        balFelsoPanel.setName("balFelsoPanel"); // NOI18N
        balFelsoPanel.setPreferredSize(new java.awt.Dimension(399, 60));

        balKonyvtar.setText(resourceMap.getString("balKonyvtar.text")); // NOI18N
        balKonyvtar.setName("balKonyvtar"); // NOI18N

        balGyokerGomb.setText(resourceMap.getString("balGyokerGomb.text")); // NOI18N
        balGyokerGomb.setMargin(new java.awt.Insets(2, 2, 2, 2));
        balGyokerGomb.setMaximumSize(new java.awt.Dimension(17, 23));
        balGyokerGomb.setMinimumSize(new java.awt.Dimension(17, 23));
        balGyokerGomb.setName("balGyokerGomb"); // NOI18N
        balGyokerGomb.setPreferredSize(new java.awt.Dimension(17, 23));
        balGyokerGomb.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                balGyokerGombMouseClicked(evt);
            }
        });

        balSzuloGomb.setText(resourceMap.getString("balSzuloGomb.text")); // NOI18N
        balSzuloGomb.setMargin(new java.awt.Insets(2, 2, 2, 2));
        balSzuloGomb.setName("balSzuloGomb"); // NOI18N
        balSzuloGomb.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                balSzuloGombMouseClicked(evt);
            }
        });

        balFajlokSzama.setText(resourceMap.getString("balFajlokSzama.text")); // NOI18N
        balFajlokSzama.setName("balFajlokSzama"); // NOI18N

        javax.swing.GroupLayout balFelsoPanelLayout = new javax.swing.GroupLayout(balFelsoPanel);
        balFelsoPanel.setLayout(balFelsoPanelLayout);
        balFelsoPanelLayout.setHorizontalGroup(
            balFelsoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(balFelsoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(balFelsoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, balFelsoPanelLayout.createSequentialGroup()
                        .addComponent(balKonyvtar, javax.swing.GroupLayout.DEFAULT_SIZE, 327, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(balGyokerGomb, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(4, 4, 4)
                        .addComponent(balSzuloGomb)
                        .addGap(12, 12, 12))
                    .addGroup(balFelsoPanelLayout.createSequentialGroup()
                        .addComponent(balFajlokSzama, javax.swing.GroupLayout.DEFAULT_SIZE, 320, Short.MAX_VALUE)
                        .addGap(69, 69, 69))))
        );
        balFelsoPanelLayout.setVerticalGroup(
            balFelsoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(balFelsoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(balFajlokSzama, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(balFelsoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(balKonyvtar, javax.swing.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
                    .addComponent(balSzuloGomb, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(balGyokerGomb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        javax.swing.GroupLayout balPanelLayout = new javax.swing.GroupLayout(balPanel);
        balPanel.setLayout(balPanelLayout);
        balPanelLayout.setHorizontalGroup(
            balPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(balFelsoPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(balScrollPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 399, Short.MAX_VALUE)
        );
        balPanelLayout.setVerticalGroup(
            balPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(balPanelLayout.createSequentialGroup()
                .addComponent(balFelsoPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(balScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE))
        );

        panelek.setLeftComponent(balPanel);

        jobbPanel.setName("jobbPanel"); // NOI18N

        jobbScrollPane.setAutoscrolls(true);
        jobbScrollPane.setName("jobbScrollPane"); // NOI18N
        jobbScrollPane.setPreferredSize(new java.awt.Dimension(400, 600));

        jobbLista.setBackground(resourceMap.getColor("jobbLista.background")); // NOI18N
        jobbLista.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Név", "Méret", "Utoljára módosítva", "Jogosultságok"
            }
        ));
        jobbLista.setFillsViewportHeight(true);
        jobbLista.setName("jobbLista"); // NOI18N
        jobbLista.setShowVerticalLines(false);
        jobbScrollPane.setViewportView(jobbLista);

        jobbFelsoPanel.setName("jobbFelsoPanel"); // NOI18N

        jobbKonyvtar.setText(resourceMap.getString("jobbKonyvtar.text")); // NOI18N
        jobbKonyvtar.setName("jobbKonyvtar"); // NOI18N

        jobbGyokerGomb.setAction(actionMap.get("gyokerkonyvtar")); // NOI18N
        jobbGyokerGomb.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jobbGyokerGomb.setMaximumSize(new java.awt.Dimension(17, 23));
        jobbGyokerGomb.setMinimumSize(new java.awt.Dimension(17, 23));
        jobbGyokerGomb.setName("jobbGyokerGomb"); // NOI18N
        jobbGyokerGomb.setPreferredSize(new java.awt.Dimension(17, 23));
        jobbGyokerGomb.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jobbGyokerGombMouseClicked(evt);
            }
        });

        jobbSzuloGomb.setAction(actionMap.get("szuloKonyvtar")); // NOI18N
        jobbSzuloGomb.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jobbSzuloGomb.setName("jobbSzuloGomb"); // NOI18N
        jobbSzuloGomb.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jobbSzuloGombMouseClicked(evt);
            }
        });

        jobbFajlokSzama.setText(resourceMap.getString("jobbFajlokSzama.text")); // NOI18N
        jobbFajlokSzama.setName("jobbFajlokSzama"); // NOI18N

        javax.swing.GroupLayout jobbFelsoPanelLayout = new javax.swing.GroupLayout(jobbFelsoPanel);
        jobbFelsoPanel.setLayout(jobbFelsoPanelLayout);
        jobbFelsoPanelLayout.setHorizontalGroup(
            jobbFelsoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jobbFelsoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jobbFelsoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jobbFelsoPanelLayout.createSequentialGroup()
                        .addComponent(jobbKonyvtar, javax.swing.GroupLayout.DEFAULT_SIZE, 231, Short.MAX_VALUE)
                        .addGap(42, 42, 42)
                        .addComponent(jobbGyokerGomb, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jobbSzuloGomb)
                        .addContainerGap())
                    .addGroup(jobbFelsoPanelLayout.createSequentialGroup()
                        .addComponent(jobbFajlokSzama, javax.swing.GroupLayout.DEFAULT_SIZE, 270, Short.MAX_VALUE)
                        .addGap(60, 60, 60))))
        );
        jobbFelsoPanelLayout.setVerticalGroup(
            jobbFelsoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jobbFelsoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jobbFajlokSzama, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jobbFelsoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jobbKonyvtar, javax.swing.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
                    .addComponent(jobbSzuloGomb, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jobbGyokerGomb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        javax.swing.GroupLayout jobbPanelLayout = new javax.swing.GroupLayout(jobbPanel);
        jobbPanel.setLayout(jobbPanelLayout);
        jobbPanelLayout.setHorizontalGroup(
            jobbPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jobbFelsoPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jobbScrollPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 340, Short.MAX_VALUE)
        );
        jobbPanelLayout.setVerticalGroup(
            jobbPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jobbPanelLayout.createSequentialGroup()
                .addComponent(jobbFelsoPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jobbScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE))
        );

        panelek.setRightComponent(jobbPanel);

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelek, javax.swing.GroupLayout.DEFAULT_SIZE, 746, Short.MAX_VALUE)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addComponent(eszkoztar, javax.swing.GroupLayout.DEFAULT_SIZE, 746, Short.MAX_VALUE)
                .addContainerGap())
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                .addComponent(panelek, javax.swing.GroupLayout.DEFAULT_SIZE, 424, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(eszkoztar, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        menuBar.setMaximumSize(new java.awt.Dimension(1280, 20));
        menuBar.setMinimumSize(new java.awt.Dimension(400, 20));
        menuBar.setName("menuBar"); // NOI18N
        menuBar.setPreferredSize(new java.awt.Dimension(600, 20));

        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        atnevezesMenuItem.setAction(actionMap.get("atnevezes")); // NOI18N
        atnevezesMenuItem.setText(resourceMap.getString("atnevezesMenuItem.text")); // NOI18N
        atnevezesMenuItem.setName("atnevezesMenuItem"); // NOI18N
        fileMenu.add(atnevezesMenuItem);

        masolasMenuItem.setAction(actionMap.get("masolas")); // NOI18N
        masolasMenuItem.setText(resourceMap.getString("masolasMenuItem.text")); // NOI18N
        masolasMenuItem.setName("masolasMenuItem"); // NOI18N
        fileMenu.add(masolasMenuItem);

        athelyezesMenuItem.setAction(actionMap.get("athelyezes")); // NOI18N
        athelyezesMenuItem.setText(resourceMap.getString("athelyezesMenuItem.text")); // NOI18N
        athelyezesMenuItem.setName("athelyezesMenuItem"); // NOI18N
        fileMenu.add(athelyezesMenuItem);

        ujKonyvtarMenuItem.setAction(actionMap.get("ujKonyvtar")); // NOI18N
        ujKonyvtarMenuItem.setText(resourceMap.getString("ujKonyvtarMenuItem.text")); // NOI18N
        ujKonyvtarMenuItem.setName("ujKonyvtarMenuItem"); // NOI18N
        fileMenu.add(ujKonyvtarMenuItem);

        torlesMenuItem.setAction(actionMap.get("torles")); // NOI18N
        torlesMenuItem.setText(resourceMap.getString("torlesMenuItem.text")); // NOI18N
        torlesMenuItem.setName("torlesMenuItem"); // NOI18N
        fileMenu.add(torlesMenuItem);

        elvalaszto1.setForeground(resourceMap.getColor("elvalaszto1.foreground")); // NOI18N
        elvalaszto1.setName("elvalaszto1"); // NOI18N
        elvalaszto1.setRequestFocusEnabled(false);
        elvalaszto1.setVerifyInputWhenFocusTarget(false);
        fileMenu.add(elvalaszto1);

        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setText(resourceMap.getString("exitMenuItem.text")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        nezetMenu.setAction(actionMap.get("rejtettFajlok")); // NOI18N
        nezetMenu.setText(resourceMap.getString("nezetMenu.text")); // NOI18N
        nezetMenu.setName("nezetMenu"); // NOI18N

        rejtettFajlMenupont.setAction(actionMap.get("rejtettFajlok")); // NOI18N
        rejtettFajlMenupont.setText(resourceMap.getString("rejtettFajlMenupont.text")); // NOI18N
        rejtettFajlMenupont.setName("rejtettFajlMenupont"); // NOI18N
        nezetMenu.add(rejtettFajlMenupont);

        menuBar.add(nezetMenu);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        statusPanel.setName("statusPanel"); // NOI18N
        statusPanel.setPreferredSize(new java.awt.Dimension(600, 25));

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        progressBar.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        progressBar.setName("progressBar"); // NOI18N

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 241, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 339, Short.MAX_VALUE)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(statusMessageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
        setToolBar(eszkoztar);
    }// </editor-fold>//GEN-END:initComponents
    /**
     * Bal gyökérkönyvtár gombra kattintás kezelése.
     * @author Bíró Tímea
     * @param evt
     */
    private void balGyokerGombMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_balGyokerGombMouseClicked
        gyokerkonyvtar(false);
    }//GEN-LAST:event_balGyokerGombMouseClicked
    /**
     * Bal szülőkönyvtár gombra kattintás kezelése.
     * @author Bíró Tímea
     * @param evt
    */
    private void balSzuloGombMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_balSzuloGombMouseClicked
        szuloKonyvtar(false);
    }//GEN-LAST:event_balSzuloGombMouseClicked
    /**
     * Jobb gyökérkönyvtár gombra kattintás kezelése.
     * @author Bíró Tímea
     * @param evt
     */
    private void jobbGyokerGombMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jobbGyokerGombMouseClicked
        gyokerkonyvtar(true);
    }//GEN-LAST:event_jobbGyokerGombMouseClicked
    /**
     * Jobb szülőkönyvtár gombra kattintás kezelése.
     * @author Bíró Tímea
     * @param evt
     */
    private void jobbSzuloGombMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jobbSzuloGombMouseClicked
        szuloKonyvtar(true);
    }//GEN-LAST:event_jobbSzuloGombMouseClicked

    /**
     * efile generálása
     * @author Bán Dénes
     * @param side - melyik oldalt nézze
     * @param index - hanyadik sorból szedje a file-t
     * @param source - ha true, akkor a source lesz belőle (az adott oldali path-szal)
     *                  ha false, akkor a dest lesz (a másik oldali path-szal)
     * @return efile
     * @throws Exception
     */
    public EFile genEFile(boolean side, int index, boolean source) throws Exception {
        JTable t = null;
        EFile parent = null;
        EFile masik = null;
        if (side) {
            t = jobbLista;
            parent = jobb;
            masik = bal;
        } else {
            t = balLista;
            parent = bal;
            masik = jobb;
        }
        String nev = parent.getFile().getAbsolutePath() + File.separator;
        if (!source) {
            nev = masik.getFile().getAbsolutePath() + File.separator;
        }
        nev += t.getValueAt(index, 0);
        if (t.getValueAt(index, 1) != "") {
            nev += "." + t.getValueAt(index, 1);
        }
        return new EFile(new File(nev));
    }

    /**
     * elmenti, hogy mik voltak kiválasztva az oldalakon
     * @author Bán Dénes
     */
    public void saveSelections() {
        holVoltBal = balLista.getSelectedRow();
        holVoltJobb = jobbLista.getSelectedRow();
    }

    /**
     * Bizonyos komponenseket letiltunk, hogy valmilyen művelet közben, ne lehessen
     * újat indítani, kilépni.
     * @author Kádár István
     */
    private void disableComponetes() {
        balLista.setEnabled(false);
        balLista.setBackground(new Color(230, 221, 213));
        jobbLista.setEnabled(false);
        jobbLista.setBackground(new Color(230, 221, 213));
        fileMenu.setEnabled(false);
        masolasGomb.setEnabled(false);
        athelyezesGomb.setEnabled(false);
        atnevezesGomb.setEnabled(false);
        torlesGomb.setEnabled(false);
        ujKonyvtarGomb.setEnabled(false);
        kilepesGomb.setEnabled(false);
        balGyokerGomb.setEnabled(false);
        balSzuloGomb.setEnabled(false);
        jobbGyokerGomb.setEnabled(false);
        jobbSzuloGomb.setEnabled(false);
        getFrame().setCursor(new Cursor(Cursor.WAIT_CURSOR));
        getFrame().setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    }

    /**
     * Komponensek tiltásának feloldása.
     * @author Kádár István
     */
    private void enableComponents() {
        balLista.setEnabled(true);
        balLista.setBackground(Color.white);
        jobbLista.setEnabled(true);
        jobbLista.setBackground(Color.white);
        fileMenu.setEnabled(true);
        masolasGomb.setEnabled(true);
        atnevezesGomb.setEnabled(true);
        athelyezesGomb.setEnabled(true);
        torlesGomb.setEnabled(true);
        ujKonyvtarGomb.setEnabled(true);
        kilepesGomb.setEnabled(true);
        balGyokerGomb.setEnabled(true);
        balSzuloGomb.setEnabled(true);
        jobbGyokerGomb.setEnabled(true);
        jobbSzuloGomb.setEnabled(true);
        getFrame().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        getFrame().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

   @Action
   /**
    * Fájlmásoló szál indítása.
    * @author Bíró Tímea
    */
    public void masolas() {
            Masolo masolo = new Masolo();        //másoló szál indítása
            masolo.execute();
    }
    /**
     * Másoló SwingWorker osztály.
     * @author Bíró Tímea
     * @author Módosította: Kádár István
     */
    class Masolo extends SwingWorker<Void, Void> {

        public Void doInBackground() {
        EFile source = null;
        File dest = null;
        JTable t = null;
        saveSelections();
        t = balLista;
        if (focus) {
            t = jobbLista;
        }
        if (t.getSelectedRowCount() == 0) {
            JOptionPane.showMessageDialog(mainPanel, "Nincs kijelölt file!");
        }
        int[] rows = t.getSelectedRows();


        // komponensek letiltása
        disableComponetes();

        /*
         * Összegezzuk, hogy hány bájtot kell másolni
         * addig knight rideres progressbar
         */
        statusMessageLabel.setText("Másolás...");           //állapotsor szövege
        // progressbar beállítása
        progressBar.setVisible(true);                       //progressbar megjelenítése
        progressBar.setIndeterminate(true);
        // kiszámoljuk hány bájtot kell másolni
        long bytesToCopy = 0;
        for (int i : rows) {
            source = null;
            try {
                source = genEFile(focus,i,true);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(mainPanel, "A forrás vagy a cél file nem létezik!");
                statusMessageLabel.setText("");                 //állapotsor törlése
                enableComponents();
            }
            bytesToCopy += source.getAttributes().getLength();
        }
        /*
         * 1024-gyel leosztunk, mert kifuthatunk az int értéktartományából.
         * Így egy egség a progressBar-on 1KB lesz kb, ez 2 TB másolandót kifut.
         */
        int progressBarMaxValue = (int)(bytesToCopy / 1024);
        progressBar.setIndeterminate(false);
        progressBar.setMaximum(progressBarMaxValue);

        /*
         * Másoljuk át a kijelölteket...
         */
        for (int i : rows) {
            source = null;
            dest = null;
            try {
                source = genEFile(focus,i,true);
                dest = genEFile(focus,i,false).getFile();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(mainPanel, "A forrás vagy a cél file nem létezik!");
                enableComponents();
            }

            progressBar.setStringPainted(true);
            masol(source, dest);
        }
        return null;
        }
    private void masol(EFile source, File dest) {
            try {
                source.copyEntry(dest, false, progressBar);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(mainPanel, e.getMessage());
            } catch (OverwritingException e) {
                int optionType = JOptionPane.YES_NO_CANCEL_OPTION;
                Object[] options = {"Felülírás","Átnevezés","Mégse"};
                int res = JOptionPane.showOptionDialog(mainPanel, "Már létezik: "+dest.getAbsolutePath()+"! Felülírod?",
                        "Létező file", optionType,JOptionPane.QUESTION_MESSAGE,null,options,options[2]);
                if (res == JOptionPane.YES_OPTION) {
                    try {
                        source.copyEntry(dest, true, progressBar);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(mainPanel, e.getMessage());
                    }
                } else if (res == JOptionPane.NO_OPTION) {
                    source = new EFile(atnevezes());
                    dest = new File(dest.getParent()+File.separator+source.getFile().getName());
                    if (source!=null) {masol(source,dest);};
                }
            }
            }
        @Override
        public void done() {                                //amikor kész van a másolással
            statusMessageLabel.setText("");                 //állapotsor törlése
            enableComponents();

            balLista.setBackground(java.awt.Color.white);
            jobbLista.setBackground(java.awt.Color.white);

            progressBar.setValue(progressBar.getMaximum()); // mennyen el a végéig biztos, ami biztos... :)
            progressBar.setValue(0);
            progressBar.setStringPainted(false);

            refresh();
            if (focus) {
                jobbLista.requestFocus();
            } else {
                balLista.requestFocus();
            }
        }
    }

    
    @Action
    /**
     * Áthelyező szál indítása.
     */
    public void athelyezes() {
        Athelyezo athelyezo = new Athelyezo();
        athelyezo.execute();
    }
    /**
     * Áthelyező SwingWorker osztály.
     * @author Bán Dénes
     * @author Módosította: Bíró Tímea, Kádár István
     */
    class Athelyezo extends SwingWorker<Void, Void> {

        public Void doInBackground() {
            JTable t = balLista;
            if (focus) {
                t = jobbLista;
            }
            EFile source = null;
            File dest = null;
            if (t.getSelectedRowCount() == 0) {
                JOptionPane.showMessageDialog(mainPanel, "Nincs kijelölt file!");
            }
            int[] rows = t.getSelectedRows();

            disableComponetes();
            statusMessageLabel.setText("Áthelyezés...");           //állapotsor szövege

            /*
             * A kijelöltek áthelyezése
             */
            for (int i : rows) {
                source = null;
                dest = null;
                try {
                    source = genEFile(focus, i, true);
                    dest = genEFile(focus, i, false).getFile();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(mainPanel, "A forrás vagy a cél file nem létezik!");
                    statusMessageLabel.setText("");                 //állapotsor törlése
                    enableComponents();
                }

                progressBar.setStringPainted(true);
                if (!source.getFile().renameTo(dest)) {             // ha átnvezéssel nem sikerül
                    // kiszámoljuk hány bájtot kell mozgatni
                    progressBar.setIndeterminate(true);
                    long bytesToMove = 0;
                    for (int j : rows) {
                        source = null;
                        try {
                            source = genEFile(focus, j, true);
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(mainPanel, "A forrás vagy a cél file nem létezik!");
                            statusMessageLabel.setText("");                 //állapotsor törlése
                            enableComponents();
                            progressBar.setIndeterminate(false);
                        }
                        bytesToMove += source.getAttributes().getLength();
                    }
                    // bytesToMove * 2 mert azt majd le is kel törölni
                    int progressBarMaxValue = (int) (bytesToMove*2 / 1024);
                    progressBar.setIndeterminate(false);
                    progressBar.setMaximum(progressBarMaxValue);

                    masolasTorles(source, dest);

                } else {
                    // feltöltjük a progressbart
                    progressBar.setValue(progressBar.getMaximum());
                }
            }
            return null;
        }

        private void masolasTorles(EFile source, File dest) {
            try {
                source.moveWithCopyAndDelete(dest, false, progressBar);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(mainPanel, e.getMessage());
            } catch (OverwritingException e) {
                int optionType = JOptionPane.YES_NO_CANCEL_OPTION;
                Object[] options = {"Felülírás","Átnevezés","Mégse"};
                int res = JOptionPane.showOptionDialog(mainPanel, "A file már létezik: "+dest.getAbsolutePath()+"! Felülírod?",
                        "Létező file", optionType,JOptionPane.QUESTION_MESSAGE,null,options,options[2]);
                if (res == JOptionPane.YES_OPTION) {
                    try {
                        source.moveWithCopyAndDelete(dest, true, progressBar);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(mainPanel, e.getMessage());
                    }
                } else if (res == JOptionPane.NO_OPTION) {
                    source = new EFile(atnevezes());
                    dest = new File(dest.getParent()+File.separator+source.getFile().getName());
                    if (source!=null) {
                        progressBar.setValue(0);
                        masolasTorles(source,dest);
                    }
                }
            }
        }

        @Override
        public void done() {                                
            statusMessageLabel.setText("");                 //állapotsor törlése
            enableComponents();

            progressBar.setValue(progressBar.getMaximum()); // mennyen el a végéig biztos, ami biztos...
            progressBar.setValue(0);
            progressBar.setStringPainted(false);

            refresh();
            if (focus) {
                jobbLista.requestFocus();
            } else {
                balLista.requestFocus();
            }
        }
    }

    @Action
    /**
     * Új könyvtár létrehozása.
     * @author Bán Dénes
     */
    public void ujKonyvtar() {
        EFile oldal = bal;
        if (focus) {
            oldal = jobb;
        }String ret = JOptionPane.showInputDialog(menuBar, "Új könyvtár itt: "+oldal.getFile().getAbsolutePath(), "");
        if (ret == null || ret.equals("")) {
            return;
        }
        try {
            new File(oldal.getFile().getAbsolutePath()+File.separator+ret).mkdir();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(mainPanel, "A könyvtár létrehozása nem sikerült!");
        }
        refresh();
    }

    @Action
    /**
     * Törlő szál indítása.
     * @author Bíró Tímea
     */
    public void torles() {
        int opt = JOptionPane.showConfirmDialog(mainPanel,
                        "Biztosan törlöd a kijelölt eleme(ke)t?", "Törlés",JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION ) {
            Torlo torlo = new Torlo();
            torlo.execute();
        }
    }
    /**
     * Törlő SwingWorker osztály.
     * @author Bíró Tímea
     * @author Módosította: Kádár István
     */
    class Torlo extends SwingWorker<Void,Void> {
        public Void doInBackground() {
            JTable t = balLista;
        if (focus) {
            t = jobbLista;
        }
        EFile source = null;
        if (t.getSelectedRowCount() == 0) {
            JOptionPane.showMessageDialog(mainPanel, "Nincs kijelölt file!");
            return null;
        }
        int[] rows = t.getSelectedRows();

        disableComponetes();

        /*
         * Összegezzuk, hogy hány bájtot kell másolni
         * addig knight rideres progressbar
         */
        statusMessageLabel.setText("Törlés...");           //állapotsor szövege
        // progressbar beállítása
        progressBar.setVisible(true);                       //progressbar megjelenítése
        progressBar.setIndeterminate(true);
        // kiszámoljuk hány bájtot kell másolni
        long bytesToDelete = 0;
        for (int i : rows) {
            source = null;
            try {
                source = genEFile(focus,i,true);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(mainPanel, "A forrás vagy a cél file nem létezik!");
                statusMessageLabel.setText("");                 //állapotsor törlése
                enableComponents();
                progressBar.setIndeterminate(false);
            }
            bytesToDelete += source.getAttributes().getLength();
        }
        /*
         * 1024-gyel leosztunk, mert kifuthatunk az int értéktartományából.
         * Így egy egség a progressBar-on 1KB lesz kb, ez 2 TB-ot kifut.
         */
        int progressBarMaxValue = (int)(bytesToDelete / 1024);
        progressBar.setIndeterminate(false);
        progressBar.setMaximum(progressBarMaxValue);
        progressBar.setStringPainted(true);

        for (int i : rows) {

            source = null;
            try {
                source = genEFile(focus, i, true);
                try {
                    source.deleteEntry(progressBar);
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(mainPanel, e.getMessage());
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(mainPanel, "A forrásfile nem létezik!");
                return null;
            }


        }
        refresh();
        return null;
        }
        @Override
         public void done() {                                //amikor kész van a másolással
            statusMessageLabel.setText("");                 //állapotsor törlése
            enableComponents();

            progressBar.setValue(progressBar.getMaximum()); // mennyen el a végéig biztos, ami biztos... :)
            progressBar.setValue(0);
            progressBar.setStringPainted(false);

            refresh();
            if (focus) {
                jobbLista.requestFocus();
            } else {
                balLista.requestFocus();
            }
        }
    }

    @Action
    /**
     * Gyökérkönyvtárba lépés a fejléc gyökérkönyvtár gombjával.
     * @author Bíró Tímea
     * @param   melyik oldalon nyomták meg a gyökérkönyvtár gombot
     */
    public void gyokerkonyvtar(boolean oldal) {
        listDir(oldal,new EFile(new File("/")),showHidden);                         //gyökérkönyvtár listázása
        if (oldal) {jobbKonyvtar.setText(jobb.getFile().getAbsolutePath());         //könyvtárjelző címke beállítása
        } else {balKonyvtar.setText(bal.getFile().getAbsolutePath());}
        if (oldal) jobbFajlokSzama.setText(fajlOsszesites(jobb,showHidden));        //fájlokat összegző címke beállítása
                else balFajlokSzama.setText(fajlOsszesites(bal,showHidden));
    }

    @Action
    /**
     * Szülőkönyvtárba lépés a fejléc szülőkönyvtár gombjával.
     * @author Bíró Tímea
     * @param   melyik oldalon nyomták meg a szülőkönyvtár gombot
     */
    public void szuloKonyvtar(boolean oldal) {
        try {
            listDir(oldal,new EFile((oldal?jobb:bal).getFile().getParentFile()),showHidden);    //szülőkönyvtár listázása
            if (oldal) {jobbKonyvtar.setText(jobb.getFile().getAbsolutePath());                 //könyvtárjelző címke beállítása
            } else {balKonyvtar.setText(bal.getFile().getAbsolutePath());}
            if (oldal) jobbFajlokSzama.setText(fajlOsszesites(jobb,showHidden));                //fájlokat összegző címke beállítása
                else balFajlokSzama.setText(fajlOsszesites(bal,showHidden));
        } catch (NullPointerException ex) {
            //már root voltunk
        }
    }

    @Action
    /**
     * Átnevezés.
     * @author Bán Dénes
     * @author Módosította: Bíró Tímea, Kádár István
     */
    public File atnevezes() {
        EFile oldal = bal;
        JTable t = balLista;
        EFile source = null;
        File dest = null;
        if (focus) {
            oldal = jobb;
            t = jobbLista;
        }
        if (t.getSelectedRowCount() == 0) {
            JOptionPane.showMessageDialog(t, "Nincs kijelölt file!");
        }
        int[] rows = t.getSelectedRows();
        for (int i : rows) {
            source = null;
            dest = null;
            try {
                source = genEFile(focus,i,true);
                String ujNev = (String)JOptionPane.showInputDialog(
                    mainPanel,
                    "Átnevezés: ",
                    "Átnevezés...",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    null,
                    source.getFile().getName()
                    );
                if (ujNev != null && ujNev.length()>0) {
                    dest = new File (oldal.getFile().getAbsolutePath()+File.separator+ujNev);
                } else
                    JOptionPane.showMessageDialog(mainPanel, "Sikertelen átnevezés!");
            // FIXME: Ezt nem itt kéne elkapni, mert igy a dest null is lehet...
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(t, "A fájl nem létezik!");
            }
            
            try {         
                source.renameEntry(dest, progressBar);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(t, e.getMessage());
            } catch (OverwritingException e) {
                int optionType = JOptionPane.YES_NO_CANCEL_OPTION;
                Object[] options = {"Felülírás","Átnevezés","Mégse"};
                int res = JOptionPane.showOptionDialog(null, "A file már létezik: "+dest.getAbsolutePath()+"! Felülírod?",
                    "Létező file", optionType,JOptionPane.QUESTION_MESSAGE,null,options,options[2]);
                if (res == JOptionPane.YES_OPTION) {
                    try {
                        source.copyEntry(dest, true, progressBar);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(t, e.getMessage());
                    }
                } else if (res == JOptionPane.NO_OPTION) {
                    atnevezes();
                }
            }
        }
        refresh();
        return dest;
    }

    @Action
    /**
     * Rejtett fájlok megjelenítésének be/kikapcsolása (a Nézet menü menüpontján keresztül)
     * @author Bíró Tímea
     */
    public void rejtettFajlok() {
        showHidden=rejtettFajlMenupont.isSelected();
        refresh();
    }

   

    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton athelyezesGomb;
    private javax.swing.JMenuItem athelyezesMenuItem;
    private javax.swing.JButton atnevezesGomb;
    private javax.swing.JMenuItem atnevezesMenuItem;
    private javax.swing.JLabel balFajlokSzama;
    private javax.swing.JPanel balFelsoPanel;
    private javax.swing.JButton balGyokerGomb;
    private javax.swing.JLabel balKonyvtar;
    private javax.swing.JTable balLista;
    private javax.swing.JPanel balPanel;
    private javax.swing.JScrollPane balScrollPane;
    private javax.swing.JButton balSzuloGomb;
    private javax.swing.JPopupMenu.Separator elvalaszto1;
    private javax.swing.JToolBar eszkoztar;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JLabel jobbFajlokSzama;
    private javax.swing.JPanel jobbFelsoPanel;
    private javax.swing.JButton jobbGyokerGomb;
    private javax.swing.JLabel jobbKonyvtar;
    private javax.swing.JTable jobbLista;
    private javax.swing.JPanel jobbPanel;
    private javax.swing.JScrollPane jobbScrollPane;
    private javax.swing.JButton jobbSzuloGomb;
    private javax.swing.JButton kilepesGomb;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JButton masolasGomb;
    private javax.swing.JMenuItem masolasMenuItem;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenu nezetMenu;
    private javax.swing.JSplitPane panelek;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JCheckBoxMenuItem rejtettFajlMenupont;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JButton torlesGomb;
    private javax.swing.JMenuItem torlesMenuItem;
    private javax.swing.JButton ujKonyvtarGomb;
    private javax.swing.JMenuItem ujKonyvtarMenuItem;
    // End of variables declaration//GEN-END:variables

    private JDialog aboutBox;
}
