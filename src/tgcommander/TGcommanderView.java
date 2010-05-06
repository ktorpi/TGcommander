/*
 * TGcommanderView.java
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
    private boolean balHidden = false;
    private boolean jobbHidden = false;

    //hol volt
    private int holVoltBal;
    private int holVoltJobb;

    public void mouseClicked(MouseEvent e){

        JTable target = (JTable)e.getSource();
        int selection = target.getSelectedRow();
        if ((target == listaBal && focus) || (target == listaJobb && !focus)) {
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
        boolean hidden;
        if (focus) {
            oldal = jobb;
            t = jobbLista;
            hidden = jobbHidden;
        } else {
            oldal = bal;
            t = balLista;
            hidden = balHidden;
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
                listDir(focus,new EFile(temp),hidden);
                if (focus) {jobbKonyvtar.setText(uj);}
                else {balKonyvtar.setText(uj);}
            } else {
                JOptionPane.showMessageDialog(mainPanel, "Nincs jogosultságod megnyitni!");
            }
        }
    }

    public void listDir(boolean hova, EFile mit, boolean hidden) {
        EntryAttributes[] ea = mit.getContent(hidden);
        JTable target;

        if (hova) {
            target = listaJobb;
            holVoltJobb = 0;
            jobb = mit;
        } else {
            target = listaBal;
            holVoltBal = 0;
>>>>>>> zealot_temp
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
        
    }
    public void refresh() {
        JOptionPane.showMessageDialog(menuBar, "REFRESSS");
        int id = listaBal.getSelectedRow();
        bal = new EFile(bal.getFile());
        listDir(false,bal,balHidden);
        listaBal.getSelectionModel().setSelectionInterval(id, id);
        id = listaJobb.getSelectedRow();
        jobb = new EFile(jobb.getFile());
        listDir(true,jobb,jobbHidden);
        listaJobb.getSelectionModel().setSelectionInterval(id, id);
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
                listaBal.requestFocus();
                listaBal.getSelectionModel().setSelectionInterval(holVoltBal, holVoltBal);
                holVoltJobb = listaJobb.getSelectedRow();
                listaJobb.clearSelection();
                focus = false;
            } else {
                listaJobb.requestFocus();
                listaJobb.getSelectionModel().setSelectionInterval(holVoltJobb, holVoltJobb);
                holVoltBal = listaBal.getSelectedRow();
                listaBal.clearSelection();
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
        listDir(false,new EFile(new File("D:/")),true);
        listDir(true,new EFile(new File("D:/")),true);
        balKonyvtar.setText("D:/");
        jobbKonyvtar.setText("D:/");
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
        torlesGomb1 = new javax.swing.JButton();
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
        jobbPanel = new javax.swing.JPanel();
        jobbScrollPane = new javax.swing.JScrollPane(balLista);
        jobbLista = new javax.swing.JTable();
        jobbFelsoPanel = new javax.swing.JPanel();
        jobbKonyvtar = new javax.swing.JLabel();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
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
        torlesGomb1.setAction(actionMap.get("masolas")); // NOI18N
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(tgcommander.TGcommanderApp.class).getContext().getResourceMap(TGcommanderView.class);
        torlesGomb1.setText(resourceMap.getString("torlesGomb1.text")); // NOI18N
        torlesGomb1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        torlesGomb1.setFocusable(false);
        torlesGomb1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        torlesGomb1.setMaximumSize(new java.awt.Dimension(90, 25));
        torlesGomb1.setMinimumSize(new java.awt.Dimension(90, 25));
        torlesGomb1.setName("torlesGomb1"); // NOI18N
        torlesGomb1.setPreferredSize(new java.awt.Dimension(90, 25));
        eszkoztar.add(torlesGomb1);

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

        panelek.setDividerLocation(300);
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

        balKonyvtar.setText(resourceMap.getString("balKonyvtar.text")); // NOI18N
        balKonyvtar.setName("balKonyvtar"); // NOI18N

        javax.swing.GroupLayout balFelsoPanelLayout = new javax.swing.GroupLayout(balFelsoPanel);
        balFelsoPanel.setLayout(balFelsoPanelLayout);
        balFelsoPanelLayout.setHorizontalGroup(
            balFelsoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(balFelsoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(balKonyvtar, javax.swing.GroupLayout.DEFAULT_SIZE, 422, Short.MAX_VALUE)
                .addContainerGap())
        );
        balFelsoPanelLayout.setVerticalGroup(
            balFelsoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(balKonyvtar, javax.swing.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout balPanelLayout = new javax.swing.GroupLayout(balPanel);
        balPanel.setLayout(balPanelLayout);
        balPanelLayout.setHorizontalGroup(
            balPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(balFelsoPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(balPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(balScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 442, Short.MAX_VALUE))
        );
        balPanelLayout.setVerticalGroup(
            balPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(balPanelLayout.createSequentialGroup()
                .addComponent(balFelsoPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(391, Short.MAX_VALUE))
            .addGroup(balPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(balPanelLayout.createSequentialGroup()
                    .addGap(29, 29, 29)
                    .addComponent(balScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 387, Short.MAX_VALUE)
                    .addGap(0, 0, 0)))
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

        javax.swing.GroupLayout jobbFelsoPanelLayout = new javax.swing.GroupLayout(jobbFelsoPanel);
        jobbFelsoPanel.setLayout(jobbFelsoPanelLayout);
        jobbFelsoPanelLayout.setHorizontalGroup(
            jobbFelsoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jobbFelsoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jobbKonyvtar, javax.swing.GroupLayout.DEFAULT_SIZE, 261, Short.MAX_VALUE)
                .addContainerGap())
        );
        jobbFelsoPanelLayout.setVerticalGroup(
            jobbFelsoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jobbKonyvtar, javax.swing.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jobbPanelLayout = new javax.swing.GroupLayout(jobbPanel);
        jobbPanel.setLayout(jobbPanelLayout);
        jobbPanelLayout.setHorizontalGroup(
            jobbPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jobbFelsoPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jobbPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jobbScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 281, Short.MAX_VALUE))
        );
        jobbPanelLayout.setVerticalGroup(
            jobbPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jobbPanelLayout.createSequentialGroup()
                .addComponent(jobbFelsoPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(391, Short.MAX_VALUE))
            .addGroup(jobbPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jobbPanelLayout.createSequentialGroup()
                    .addGap(29, 29, 29)
                    .addComponent(jobbScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 387, Short.MAX_VALUE)
                    .addGap(0, 0, 0)))
        );

        panelek.setRightComponent(jobbPanel);

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addComponent(eszkoztar, javax.swing.GroupLayout.DEFAULT_SIZE, 720, Short.MAX_VALUE)
                .addContainerGap())
            .addComponent(panelek, javax.swing.GroupLayout.DEFAULT_SIZE, 730, Short.MAX_VALUE)
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

        nezetMenu.setText(resourceMap.getString("nezetMenu.text")); // NOI18N
        nezetMenu.setName("nezetMenu"); // NOI18N

        rejtettFajlMenupont.setSelected(true);
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

        progressBar.setName("progressBar"); // NOI18N

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 560, Short.MAX_VALUE)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusAnimationLabel)
                .addContainerGap())
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 730, Short.MAX_VALUE)
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, statusPanelLayout.createSequentialGroup()
                .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 2, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(statusMessageLabel)
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

    @Action
    public void masolas() {
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
            return;
        }
        int[] rows = t.getSelectedRows();
        for (int i : rows) {
           
            EFile source = null;
            File dest = null;
            try {
                source = genEFile(focus,i,true);
                dest = genEFile(focus,i,false).getFile();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(t, "A forrás vagy a cél file nem létezik!");
                return;
            }

            try {
                source.copyEntry(dest, false);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(t, e.getMessage());
            } catch (OverwritingException e) {
                int optionType = JOptionPane.YES_NO_CANCEL_OPTION;
                int res = JOptionPane.showConfirmDialog(null, "A file már létezik! Felülírod?",
                        "Létező file", optionType);
                if (res == JOptionPane.YES_OPTION) {
                    try {
                        source.copyEntry(dest, true);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(t, e.getMessage());
                    }
                }
            }
        }
        refresh();
    }

    @Action
    public void athelyezes() {
        masolas();
        torles();
    }

    @Action
    public void ujKonyvtar() {
        EFile oldal = bal;
        if (focus) {
            oldal = jobb;
        }
        String ret = JOptionPane.showInputDialog(menuBar, "Új könyvtár itt: "+oldal.getFile().getAbsolutePath(), "");
        try {
            new File(oldal.getFile().getAbsolutePath()+File.separator+ret).mkdir();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(menuBar, "A könyvtár létrehozása nem sikerült!");
        }
        refresh();
    }

    @Action
    public void torles() {
        JTable t = listaBal;
        if (focus) {
            t = listaJobb;
        }
        if (t.getSelectedRowCount() == 0) {
            JOptionPane.showMessageDialog(t, "Nincs kijelölt file!");
            return;
        }
        int[] rows = t.getSelectedRows();
        for (int i : rows) {

            EFile source = null;
            try {
                source = genEFile(focus,i,true);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(t, "A forrásfile nem létezik!");
                return;
            }

            try {
                source.deleteEntry();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(t, e.getMessage());
            }
        }
        refresh();
    }

    @Action
    public void gyokerkonyvtar() {
    }

    @Action
    public void szuloKonyvtar() {
    }

   



    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton athelyezesGomb;
    private javax.swing.JMenuItem athelyezesMenuItem;
    private javax.swing.JPanel balFelsoPanel;
    private javax.swing.JLabel balKonyvtar;
    private javax.swing.JTable balLista;
    private javax.swing.JPanel balPanel;
    private javax.swing.JScrollPane balScrollPane;
    private javax.swing.JPopupMenu.Separator elvalaszto1;
    private javax.swing.JToolBar eszkoztar;
    private javax.swing.JPanel jobbFelsoPanel;
    private javax.swing.JLabel jobbKonyvtar;
    private javax.swing.JTable jobbLista;
    private javax.swing.JPanel jobbPanel;
    private javax.swing.JScrollPane jobbScrollPane;
    private javax.swing.JButton kilepesGomb;
    private javax.swing.JPanel mainPanel;
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
    private javax.swing.JButton torlesGomb1;
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
