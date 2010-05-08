/*
 * TGcommanderView.java
 */

/*
 * TODO:    *áthelyezésnél és törlésnél nem működik a gyorsgomb
 *          *több fájlra nem működik a másolás/áthelyezés átnevezéssel
 *          *átnevezésnél mégse gombnál is hibaüzenet van (mert nem lehet a fájlnév 0 karakteres)
 *          *gyökér- és szülőkönyvtár gomb a saját oldali táblázatot állítsa be (ne azt, amelyiken a fókusz van)
 *          *a progressbar mutassa, hogy hol tart a másolás
 *          *esetleg space-re könyvtár méretének kijelzése
 *          *táblázat oszlopfejléceire kattintva rendezés
 *          *kommentek, javadoc
 */

package tgcommander;

import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
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

    public void mouseClicked(MouseEvent e){

        JTable target = (JTable)e.getSource();
        int selection = target.getSelectedRow();
        if ((target == balLista && focus) || (target == jobbLista && !focus)) {
            new ChangeFocusAction().actionPerformed(null);
            target.getSelectionModel().setSelectionInterval(selection, selection);
        }

        if (e.getClickCount() == 2) {
            listazasAction();
        }
    }

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
                if (focus) jobbKonyvtar.setText(uj);
                else balKonyvtar.setText(uj);
                if (focus) jobbFajlokSzama.setText(fajlOsszesites(new EFile(temp),showHidden));
                else balFajlokSzama.setText(fajlOsszesites(new EFile(temp),showHidden));
            } else {
                JOptionPane.showMessageDialog(mainPanel, "Nincs jogosultságod megnyitni!");
            }
        }
    }

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
        Object[][] tomb = new Object[ea.length+1][5];
        tomb[0][0] = "..";
        for (int j=1; j<5; j++) {tomb[0][j] = ""; }
        int i = 1;
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

    public void __dump(String m) {
        String msg = m + "\n\njobb: "+ jobb.getFile().getAbsolutePath()
                + "\nbal: "+ bal.getFile().getAbsolutePath()
                + "\nfocus: "+focus;
        JOptionPane.showMessageDialog(menuBar, msg);
    }

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

    public void mousePressed(MouseEvent e){}
    public void mouseReleased(MouseEvent e){}
    public void mouseEntered(MouseEvent e){}
    public void mouseExited(MouseEvent e){}


    class ListazasAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            listazasAction();
        }
    }

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

        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(false);

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String)(evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer)(evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });

        //custom initialize
        listDir(false,new EFile(new File("/")),showHidden);
        listDir(true,new EFile(new File("/")),showHidden);
        balKonyvtar.setText("/");
        jobbKonyvtar.setText("/");
        balFajlokSzama.setText(fajlOsszesites(new EFile(new File("/")),showHidden));
        jobbFajlokSzama.setText(fajlOsszesites(new EFile(new File("/")),showHidden));
        balLista.addMouseListener(this);
        jobbLista.addMouseListener(this);


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


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
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
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
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
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
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

        balGyokerGomb.setAction(actionMap.get("gyokerkonyvtar")); // NOI18N
        balGyokerGomb.setText(resourceMap.getString("balGyokerGomb.text")); // NOI18N
        balGyokerGomb.setMargin(new java.awt.Insets(2, 2, 2, 2));
        balGyokerGomb.setMaximumSize(new java.awt.Dimension(17, 23));
        balGyokerGomb.setMinimumSize(new java.awt.Dimension(17, 23));
        balGyokerGomb.setName("balGyokerGomb"); // NOI18N
        balGyokerGomb.setPreferredSize(new java.awt.Dimension(17, 23));

        balSzuloGomb.setAction(actionMap.get("szuloKonyvtar")); // NOI18N
        balSzuloGomb.setText(resourceMap.getString("balSzuloGomb.text")); // NOI18N
        balSzuloGomb.setMargin(new java.awt.Insets(2, 2, 2, 2));
        balSzuloGomb.setName("balSzuloGomb"); // NOI18N

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
                        .addComponent(balKonyvtar, javax.swing.GroupLayout.DEFAULT_SIZE, 323, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(balGyokerGomb, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(4, 4, 4)
                        .addComponent(balSzuloGomb, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(11, 11, 11))
                    .addGroup(balFelsoPanelLayout.createSequentialGroup()
                        .addComponent(balFajlokSzama, javax.swing.GroupLayout.DEFAULT_SIZE, 323, Short.MAX_VALUE)
                        .addGap(66, 66, 66))))
        );
        balFelsoPanelLayout.setVerticalGroup(
            balFelsoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(balFelsoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(balFajlokSzama, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(balFelsoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(balKonyvtar, javax.swing.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
                    .addComponent(balSzuloGomb)
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
                .addComponent(balScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 354, Short.MAX_VALUE))
        );

        panelek.setLeftComponent(balPanel);

        jobbPanel.setName("jobbPanel"); // NOI18N

        jobbScrollPane.setAutoscrolls(true);
        jobbScrollPane.setName("jobbScrollPane"); // NOI18N
        jobbScrollPane.setPreferredSize(new java.awt.Dimension(400, 600));

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

        jobbSzuloGomb.setAction(actionMap.get("szuloKonyvtar")); // NOI18N
        jobbSzuloGomb.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jobbSzuloGomb.setName("jobbSzuloGomb"); // NOI18N

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
                        .addComponent(jobbKonyvtar, javax.swing.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
                        .addGap(8, 8, 8)
                        .addComponent(jobbGyokerGomb, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jobbSzuloGomb, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                    .addComponent(jobbSzuloGomb)
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
                .addComponent(jobbScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 354, Short.MAX_VALUE))
        );

        panelek.setRightComponent(jobbPanel);

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addComponent(eszkoztar, javax.swing.GroupLayout.DEFAULT_SIZE, 736, Short.MAX_VALUE)
                .addContainerGap())
            .addComponent(panelek, javax.swing.GroupLayout.DEFAULT_SIZE, 746, Short.MAX_VALUE)
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                .addComponent(panelek, javax.swing.GroupLayout.DEFAULT_SIZE, 418, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(eszkoztar, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
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

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        progressBar.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        progressBar.setName("progressBar"); // NOI18N

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 241, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 335, Short.MAX_VALUE)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusAnimationLabel)
                .addContainerGap())
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 746, Short.MAX_VALUE)
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, statusPanelLayout.createSequentialGroup()
                .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 2, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(statusMessageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(statusAnimationLabel)
                    .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3))
        );

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
        setToolBar(eszkoztar);
    }// </editor-fold>//GEN-END:initComponents

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
        JOptionPane.showMessageDialog(t, nev);
        return new EFile(new File(nev));
    }

    public void saveSelections() {
        holVoltBal = balLista.getSelectedRow();
        holVoltJobb = jobbLista.getSelectedRow();
    }



    @Action
    public void masolas() {
        saveSelections();
        EFile oldal = bal;
        EFile masik = jobb;
        JTable t = balLista;
        if (focus) {
            oldal = jobb;
            masik = bal;
            t = jobbLista;
        }
        if (t.getSelectedRowCount() == 0) {
            JOptionPane.showMessageDialog(mainPanel, "Nincs kijelölt file!");

        }
        int[] rows = t.getSelectedRows();
        for (int i : rows) {

            EFile source = null;
            File dest = null;
            try {
                source = genEFile(focus,i,true);
                dest = genEFile(focus,i,false).getFile();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(mainPanel, "A forrás vagy a cél file nem létezik!");

            }
        statusMessageLabel.setText("Másolás...");           //állapotsor szövege
        balLista.setEnabled(false);                         //táblázatok "letiltása"
        jobbLista.setEnabled(false);
        balLista.setBackground(java.awt.Color.LIGHT_GRAY);
        jobbLista.setBackground(java.awt.Color.LIGHT_GRAY);
        Masolo masolo = new Masolo(source, dest, t);        //másoló szál indítása
        masolo.execute();
        progressBar.setVisible(true);                       //progressbar megjelenítése
        progressBar.setIndeterminate(true);                 //meghatározatlan idejű legyen
        
        
    }
    }
    class Masolo extends SwingWorker<Void, Void> {

        EFile source = null;
        File dest = null;
        JTable t = null;
        Masolo(EFile src, File dst, JTable table) {
            source = src;
            dest = dst;
            t = table;
        }

        public Void doInBackground() {
            try {
                source.copyEntry(dest, false);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(mainPanel, e.getMessage());
            } catch (OverwritingException e) {
                int optionType = JOptionPane.YES_NO_CANCEL_OPTION;
                Object[] options = {"Felülírás","Átnevezés","Mégse"};
                int res = JOptionPane.showOptionDialog(mainPanel, "A file már létezik: "+dest.getAbsolutePath()+"! Felülírod?",
                        "Létező file", optionType,JOptionPane.QUESTION_MESSAGE,null,options,options[2]);
                if (res == JOptionPane.YES_OPTION) {
                    try {
                        source.copyEntry(dest, true);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(mainPanel, e.getMessage());
                    }
                } else if (res == JOptionPane.NO_OPTION) {
                    atnevezes();
                    doInBackground();
                }
            }
        return null;
        }

        @Override
        public void done() {                                //amikor kész van a másolással
            statusMessageLabel.setText("");                 //állapotsor törlése
            balLista.setEnabled(true);                      //táblázatok engedélyezése
            jobbLista.setEnabled(true);
            balLista.setBackground(java.awt.Color.white);
            jobbLista.setBackground(java.awt.Color.white);
            progressBar.setIndeterminate(false);            //progressbar eltüntetése
            progressBar.setVisible(false);

            refresh();
            if (focus) {
                jobbLista.requestFocus();
            } else {
                balLista.requestFocus();
            }
        }
    }



    @Action
    public void athelyezes() {
        EFile oldal = bal;
        EFile masik = jobb;
        JTable t = balLista;
        if (focus) {
            oldal = jobb;
            masik = bal;
            t = jobbLista;
        }
        if (t.getSelectedRowCount() == 0) {
            JOptionPane.showMessageDialog(mainPanel, "Nincs kijelölt file!");

        }
        int[] rows = t.getSelectedRows();
        for (int i : rows) {

            EFile source = null;
            File dest = null;
            try {
                source = genEFile(focus,i,true);
                dest = new File (masik.getFile().getAbsolutePath()+File.separator+source.getFile().getName());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(mainPanel, "A forrásfile nem létezik!");

            }
        try {
                source.renameEntry(dest);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(t, e.getMessage());
            } catch (OverwritingException e) {
                int optionType = JOptionPane.YES_NO_CANCEL_OPTION;
                Object[] options = {"Felülírás","Átnevezés","Mégse"};
                int res = JOptionPane.showOptionDialog(mainPanel, "A file már létezik: "+dest.getAbsolutePath()+"! Felülírod?",
                        "Létező file", optionType,JOptionPane.QUESTION_MESSAGE,null,options,options[2]);
                if (res == JOptionPane.YES_OPTION) {
                    try {
                        source.copyEntry(dest, true);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(mainPanel, e.getMessage());
                    }
                } else if (res == JOptionPane.NO_OPTION) {
                    atnevezes();
                    athelyezes();
                }
            }
            }
        refresh();
    }

    @Action
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
    public void torles() {
        JTable t = balLista;
        if (focus) {
            t = jobbLista;
        }
        if (t.getSelectedRowCount() == 0) {
            JOptionPane.showMessageDialog(mainPanel, "Nincs kijelölt file!");
            return;
        }
        int[] rows = t.getSelectedRows();
        for (int i : rows) {

            EFile source = null;
            try {
                source = genEFile(focus,i,true);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(mainPanel, "A forrásfile nem létezik!");
                return;
            }

            try {
                source.deleteEntry();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(mainPanel, e.getMessage());
            }
        }
        refresh();
    }

    @Action
    public void gyokerkonyvtar() {                                          //egyelőre ott lép vissza a gyökérbe, ahol a fókusz van
        listDir(focus,new EFile(new File("/")),showHidden);                //gyökérkönyvtár listázása
        if (focus) {jobbKonyvtar.setText(jobb.getFile().getAbsolutePath()); //könyvtárjelző címke beállítása
        } else {balKonyvtar.setText(bal.getFile().getAbsolutePath());}
        if (focus) jobbFajlokSzama.setText(fajlOsszesites(jobb,showHidden));
                else balFajlokSzama.setText(fajlOsszesites(bal,showHidden));
    }

    @Action
    public void szuloKonyvtar() {                                           //egyelőre ott lép vissza a szülőkönyvtárba, ahol a fókusz van
        try {
            listDir(focus,new EFile((focus?jobb:bal).getFile().getParentFile()),showHidden);    //szülőkönyvtár listázása
            if (focus) {jobbKonyvtar.setText(jobb.getFile().getAbsolutePath());                 //könyvtárjelző címke beállítása
            } else {balKonyvtar.setText(bal.getFile().getAbsolutePath());}
            if (focus) jobbFajlokSzama.setText(fajlOsszesites(jobb,showHidden));
                else balFajlokSzama.setText(fajlOsszesites(bal,showHidden));
        } catch (NullPointerException ex) {
            //már root voltunk
        }
    }

    @Action
    public void atnevezes() {
        EFile oldal = bal;
        EFile masik = jobb;
        JTable t = balLista;
        if (focus) {
            oldal = jobb;
            masik = bal;
            t = jobbLista;
        }
        if (t.getSelectedRowCount() == 0) {
            JOptionPane.showMessageDialog(t, "Nincs kijelölt file!");
        }
        int[] rows = t.getSelectedRows();
        for (int i : rows) {
            EFile source = null;
            File dest = null;
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
                } else JOptionPane.showMessageDialog(mainPanel, "A file neve nem lehet 0 karakter hosszú!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(t, "A forrásfile nem létezik!");
            }
            
            try {
                source.renameEntry(dest);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(t, e.getMessage());
            } catch (OverwritingException e) {
                int optionType = JOptionPane.YES_NO_CANCEL_OPTION;
                Object[] options = {"Felülírás","Átnevezés","Mégse"};
                int res = JOptionPane.showOptionDialog(null, "A file már létezik: "+dest.getAbsolutePath()+"! Felülírod?",
                    "Létező file", optionType,JOptionPane.QUESTION_MESSAGE,null,options,options[2]);
                if (res == JOptionPane.YES_OPTION) {
                    try {
                        source.copyEntry(dest, true);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(t, e.getMessage());
                    }
                } else if (res == JOptionPane.NO_OPTION) {
                    atnevezes();
                }
            }
        }
        refresh();
    }

    @Action
    public void rejtettFajlok() {
        showHidden=rejtettFajlMenupont.isSelected();
        refresh();
        jobbFajlokSzama.setText(fajlOsszesites(jobb,showHidden));
        balFajlokSzama.setText(fajlOsszesites(bal,showHidden));
    }

   



    private boolean ActionEvent = false;
    public boolean isActionEvent() {
        return ActionEvent;
    }

    public void setActionEvent(boolean b) {
        boolean old = isActionEvent();
        this.ActionEvent = b;
        firePropertyChange("ActionEvent", old, isActionEvent());
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
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JButton torlesGomb;
    private javax.swing.JMenuItem torlesMenuItem;
    private javax.swing.JButton ujKonyvtarGomb;
    private javax.swing.JMenuItem ujKonyvtarMenuItem;
    // End of variables declaration//GEN-END:variables

    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;

    private JDialog aboutBox;
}
