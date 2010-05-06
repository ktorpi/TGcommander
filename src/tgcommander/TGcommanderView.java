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

    private EFile bal;
    private EFile jobb;
    private boolean focus = false;
    private boolean balHidden = false;
    private boolean jobbHidden = false;

    public void mouseClicked(MouseEvent e){

        //melyik oldalra kattintottak
        JTable target = (JTable)e.getSource();
        //melyik sorra
        int id = target.getSelectedRow();

        //melyik oldalon van a fókusz
        EFile oldal;
        boolean hidden;
        if (target == listaBal) {
            focus = false;
            oldal = bal;
            hidden = balHidden;
        } else {
            oldal = jobb;
            focus = true;
            hidden = jobbHidden;
        }

        String uj = oldal.getFile().getAbsolutePath();
        if (id == 0) {
            uj = oldal.getFile().getParent();
        } else if (id > 0) {
            uj = oldal.getFile().getAbsolutePath() +
                    File.separator + target.getValueAt(id, 0);
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
            jobb = mit;
        } else {
            target = listaBal;
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
    }

    public void refresh() {
        JOptionPane.showMessageDialog(menuBar, "REFRESSS");
        bal = new EFile(bal.getFile());
        listDir(false,bal,balHidden);
        jobb = new EFile(jobb.getFile());
        listDir(true,jobb,jobbHidden);
    }

    public void mousePressed(MouseEvent e){}
    public void mouseReleased(MouseEvent e){}
    public void mouseEntered(MouseEvent e){}
    public void mouseExited(MouseEvent e){}

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
        listDir(false,new EFile(new File("/bin")),true);
        listDir(true,new EFile(new File("/bin")),true);
        listaBal.addMouseListener(this);
        listaJobb.addMouseListener(this);
        
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
        masolasGomb = new javax.swing.JButton();
        athelyezesGomb = new javax.swing.JButton();
        ujKonyvtarGomb = new javax.swing.JButton();
        torlesGomb = new javax.swing.JButton();
        panelek = new javax.swing.JSplitPane();
        panelBal = new javax.swing.JScrollPane(listaBal);
        listaBal = new javax.swing.JTable();
        panelJobb = new javax.swing.JScrollPane();
        listaJobb = new javax.swing.JTable();
        szabadHelyPane = new javax.swing.JSplitPane();
        szabadHelyBal = new javax.swing.JLabel();
        szabadHelyJobb = new javax.swing.JLabel();
        aktualisKonyvtarPane = new javax.swing.JSplitPane();
        konyvtarBal = new javax.swing.JSplitPane();
        gyorsGombokBal = new javax.swing.JPanel();
        gyokerkonyvtarGombBal = new javax.swing.JButton();
        szulokonyvtarGombBal = new javax.swing.JButton();
        aktualisKonyvtarBal = new javax.swing.JLabel();
        konyvtarJobb = new javax.swing.JSplitPane();
        gyorsGombokJobb = new javax.swing.JPanel();
        gyokerkonyvtarGombJobb = new javax.swing.JButton();
        szulokonyvtarGombJobb = new javax.swing.JButton();
        aktualisKonyvtarJobb = new javax.swing.JLabel();
        jToolBar1 = new javax.swing.JToolBar();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        masolasMenuItem = new javax.swing.JMenuItem();
        athelyezesMenuItem = new javax.swing.JMenuItem();
        ujKonyvtarMenuItem = new javax.swing.JMenuItem();
        torlesMenuItem = new javax.swing.JMenuItem();
        elvalaszto1 = new javax.swing.JPopupMenu.Separator();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
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
        masolasGomb.setAction(actionMap.get("masolas")); // NOI18N
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(tgcommander.TGcommanderApp.class).getContext().getResourceMap(TGcommanderView.class);
        masolasGomb.setText(resourceMap.getString("masolasGomb.text")); // NOI18N
        masolasGomb.setFocusable(false);
        masolasGomb.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        masolasGomb.setMargin(new java.awt.Insets(2, 2, 2, 2));
        masolasGomb.setMaximumSize(new java.awt.Dimension(47, 25));
        masolasGomb.setMinimumSize(new java.awt.Dimension(47, 25));
        masolasGomb.setName("masolasGomb"); // NOI18N
        masolasGomb.setPreferredSize(new java.awt.Dimension(47, 25));
        masolasGomb.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        eszkoztar.add(masolasGomb);

        athelyezesGomb.setAction(actionMap.get("athelyezes")); // NOI18N
        athelyezesGomb.setText(resourceMap.getString("athelyezesGomb.text")); // NOI18N
        athelyezesGomb.setFocusable(false);
        athelyezesGomb.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        athelyezesGomb.setName("athelyezesGomb"); // NOI18N
        athelyezesGomb.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        eszkoztar.add(athelyezesGomb);

        ujKonyvtarGomb.setAction(actionMap.get("ujKonyvtar")); // NOI18N
        ujKonyvtarGomb.setText(resourceMap.getString("ujKonyvtarGomb.text")); // NOI18N
        ujKonyvtarGomb.setFocusable(false);
        ujKonyvtarGomb.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        ujKonyvtarGomb.setName("ujKonyvtarGomb"); // NOI18N
        ujKonyvtarGomb.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        eszkoztar.add(ujKonyvtarGomb);

        torlesGomb.setAction(actionMap.get("torles")); // NOI18N
        torlesGomb.setText(resourceMap.getString("torlesGomb.text")); // NOI18N
        torlesGomb.setFocusable(false);
        torlesGomb.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        torlesGomb.setName("torlesGomb"); // NOI18N
        torlesGomb.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        eszkoztar.add(torlesGomb);

        panelek.setDividerLocation(300);
        panelek.setResizeWeight(0.5);
        panelek.setMinimumSize(new java.awt.Dimension(400, 200));
        panelek.setName("panelek"); // NOI18N
        panelek.setPreferredSize(new java.awt.Dimension(800, 500));
        panelek.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                panelekPropertyChange(evt);
            }
        });

        panelBal.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        panelBal.setAutoscrolls(true);
        panelBal.setName("panelBal"); // NOI18N
        panelBal.setPreferredSize(new java.awt.Dimension(400, 500));

        listaBal.setModel(new javax.swing.table.DefaultTableModel(
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
        listaBal.setName("listaBal"); // NOI18N
        panelBal.setViewportView(listaBal);

        panelek.setLeftComponent(panelBal);

        panelJobb.setName("panelJobb"); // NOI18N
        panelJobb.setPreferredSize(new java.awt.Dimension(400, 500));

        listaJobb.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Név", "Méret", "Utoljára módosítva", "Jogosultságok"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                true, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        listaJobb.setMaximumSize(new java.awt.Dimension(2147483647, 1200));
        listaJobb.setName("listaJobb"); // NOI18N
        listaJobb.setPreferredSize(new java.awt.Dimension(390, 500));
        listaJobb.setShowVerticalLines(false);
        listaJobb.getTableHeader().setReorderingAllowed(false);
        listaJobb.setVerifyInputWhenFocusTarget(false);
        panelJobb.setViewportView(listaJobb);
        listaJobb.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("listaJobb.columnModel.title0")); // NOI18N
        listaJobb.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("listaJobb.columnModel.title1")); // NOI18N
        listaJobb.getColumnModel().getColumn(2).setHeaderValue(resourceMap.getString("listaJobb.columnModel.title2")); // NOI18N
        listaJobb.getColumnModel().getColumn(3).setHeaderValue(resourceMap.getString("listaJobb.columnModel.title3")); // NOI18N

        panelek.setRightComponent(panelJobb);

        szabadHelyPane.setDividerLocation(350);
        szabadHelyPane.setDividerSize(0);
        szabadHelyPane.setName("szabadHelyPane"); // NOI18N

        szabadHelyBal.setText(resourceMap.getString("szabadHelyBal.text")); // NOI18N
        szabadHelyBal.setMaximumSize(new java.awt.Dimension(640, 15));
        szabadHelyBal.setMinimumSize(new java.awt.Dimension(200, 15));
        szabadHelyBal.setName("szabadHelyBal"); // NOI18N
        szabadHelyBal.setPreferredSize(new java.awt.Dimension(400, 15));
        szabadHelyPane.setLeftComponent(szabadHelyBal);

        szabadHelyJobb.setText(resourceMap.getString("szabadHelyJobb.text")); // NOI18N
        szabadHelyJobb.setDoubleBuffered(true);
        szabadHelyJobb.setMaximumSize(new java.awt.Dimension(640, 15));
        szabadHelyJobb.setMinimumSize(new java.awt.Dimension(200, 15));
        szabadHelyJobb.setName("szabadHelyJobb"); // NOI18N
        szabadHelyJobb.setPreferredSize(new java.awt.Dimension(400, 15));
        szabadHelyPane.setRightComponent(szabadHelyJobb);

        aktualisKonyvtarPane.setDividerLocation(400);
        aktualisKonyvtarPane.setMaximumSize(new java.awt.Dimension(1280, 25));
        aktualisKonyvtarPane.setMinimumSize(new java.awt.Dimension(400, 25));
        aktualisKonyvtarPane.setName("aktualisKonyvtarPane"); // NOI18N
        aktualisKonyvtarPane.setPreferredSize(new java.awt.Dimension(800, 25));

        konyvtarBal.setDividerLocation(350);
        konyvtarBal.setDividerSize(0);
        konyvtarBal.setResizeWeight(1.0);
        konyvtarBal.setLastDividerLocation(350);
        konyvtarBal.setMaximumSize(new java.awt.Dimension(640, 25));
        konyvtarBal.setMinimumSize(new java.awt.Dimension(200, 25));
        konyvtarBal.setName("konyvtarBal"); // NOI18N
        konyvtarBal.setPreferredSize(new java.awt.Dimension(400, 25));

        gyorsGombokBal.setAlignmentX(1.0F);
        gyorsGombokBal.setMaximumSize(new java.awt.Dimension(50, 25));
        gyorsGombokBal.setMinimumSize(new java.awt.Dimension(50, 25));
        gyorsGombokBal.setName("gyorsGombokBal"); // NOI18N
        gyorsGombokBal.setPreferredSize(new java.awt.Dimension(50, 25));

        gyokerkonyvtarGombBal.setAction(actionMap.get("gyokerkonyvtar")); // NOI18N
        gyokerkonyvtarGombBal.setIconTextGap(0);
        gyokerkonyvtarGombBal.setMargin(new java.awt.Insets(2, 2, 2, 2));
        gyokerkonyvtarGombBal.setMaximumSize(new java.awt.Dimension(20, 20));
        gyokerkonyvtarGombBal.setMinimumSize(new java.awt.Dimension(20, 20));
        gyokerkonyvtarGombBal.setName("gyokerkonyvtarGombBal"); // NOI18N
        gyokerkonyvtarGombBal.setPreferredSize(new java.awt.Dimension(20, 20));

        szulokonyvtarGombBal.setAction(actionMap.get("szuloKonyvtar")); // NOI18N
        szulokonyvtarGombBal.setMargin(new java.awt.Insets(2, 2, 2, 2));
        szulokonyvtarGombBal.setName("szulokonyvtarGombBal"); // NOI18N

        javax.swing.GroupLayout gyorsGombokBalLayout = new javax.swing.GroupLayout(gyorsGombokBal);
        gyorsGombokBal.setLayout(gyorsGombokBalLayout);
        gyorsGombokBalLayout.setHorizontalGroup(
            gyorsGombokBalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, gyorsGombokBalLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(gyokerkonyvtarGombBal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(szulokonyvtarGombBal, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        gyorsGombokBalLayout.setVerticalGroup(
            gyorsGombokBalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(gyorsGombokBalLayout.createSequentialGroup()
                .addGroup(gyorsGombokBalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(szulokonyvtarGombBal, javax.swing.GroupLayout.PREFERRED_SIZE, 21, Short.MAX_VALUE)
                    .addComponent(gyokerkonyvtarGombBal, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        konyvtarBal.setRightComponent(gyorsGombokBal);

        aktualisKonyvtarBal.setText(resourceMap.getString("aktualisKonyvtarBal.text")); // NOI18N
        aktualisKonyvtarBal.setMaximumSize(new java.awt.Dimension(590, 25));
        aktualisKonyvtarBal.setMinimumSize(new java.awt.Dimension(150, 25));
        aktualisKonyvtarBal.setName("aktualisKonyvtarBal"); // NOI18N
        aktualisKonyvtarBal.setPreferredSize(new java.awt.Dimension(350, 25));
        konyvtarBal.setLeftComponent(aktualisKonyvtarBal);

        aktualisKonyvtarPane.setLeftComponent(konyvtarBal);

        konyvtarJobb.setDividerLocation(350);
        konyvtarJobb.setDividerSize(0);
        konyvtarJobb.setLastDividerLocation(350);
        konyvtarJobb.setMaximumSize(new java.awt.Dimension(640, 25));
        konyvtarJobb.setMinimumSize(new java.awt.Dimension(400, 25));
        konyvtarJobb.setName("konyvtarJobb"); // NOI18N
        konyvtarJobb.setPreferredSize(new java.awt.Dimension(400, 25));

        gyorsGombokJobb.setAlignmentX(0.0F);
        gyorsGombokJobb.setMaximumSize(new java.awt.Dimension(50, 25));
        gyorsGombokJobb.setMinimumSize(new java.awt.Dimension(50, 25));
        gyorsGombokJobb.setName("gyorsGombokJobb"); // NOI18N
        gyorsGombokJobb.setPreferredSize(new java.awt.Dimension(50, 25));

        gyokerkonyvtarGombJobb.setAction(actionMap.get("gyokerkonyvtar")); // NOI18N
        gyokerkonyvtarGombJobb.setMargin(new java.awt.Insets(2, 2, 2, 2));
        gyokerkonyvtarGombJobb.setMaximumSize(new java.awt.Dimension(20, 20));
        gyokerkonyvtarGombJobb.setMinimumSize(new java.awt.Dimension(20, 20));
        gyokerkonyvtarGombJobb.setName("gyokerkonyvtarGombJobb"); // NOI18N
        gyokerkonyvtarGombJobb.setPreferredSize(new java.awt.Dimension(20, 20));

        szulokonyvtarGombJobb.setAction(actionMap.get("szuloKonyvtar")); // NOI18N
        szulokonyvtarGombJobb.setMargin(new java.awt.Insets(2, 2, 2, 2));
        szulokonyvtarGombJobb.setMaximumSize(new java.awt.Dimension(20, 20));
        szulokonyvtarGombJobb.setMinimumSize(new java.awt.Dimension(20, 20));
        szulokonyvtarGombJobb.setName("szulokonyvtarGombJobb"); // NOI18N
        szulokonyvtarGombJobb.setPreferredSize(new java.awt.Dimension(20, 20));

        javax.swing.GroupLayout gyorsGombokJobbLayout = new javax.swing.GroupLayout(gyorsGombokJobb);
        gyorsGombokJobb.setLayout(gyorsGombokJobbLayout);
        gyorsGombokJobbLayout.setHorizontalGroup(
            gyorsGombokJobbLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(gyorsGombokJobbLayout.createSequentialGroup()
                .addComponent(gyokerkonyvtarGombJobb, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(szulokonyvtarGombJobb, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        gyorsGombokJobbLayout.setVerticalGroup(
            gyorsGombokJobbLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(gyorsGombokJobbLayout.createSequentialGroup()
                .addGroup(gyorsGombokJobbLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(gyokerkonyvtarGombJobb, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(szulokonyvtarGombJobb, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        konyvtarJobb.setRightComponent(gyorsGombokJobb);

        aktualisKonyvtarJobb.setText(resourceMap.getString("aktualisKonyvtarJobb.text")); // NOI18N
        aktualisKonyvtarJobb.setMaximumSize(new java.awt.Dimension(590, 25));
        aktualisKonyvtarJobb.setMinimumSize(new java.awt.Dimension(150, 25));
        aktualisKonyvtarJobb.setName("aktualisKonyvtarJobb"); // NOI18N
        aktualisKonyvtarJobb.setPreferredSize(new java.awt.Dimension(350, 25));
        konyvtarJobb.setLeftComponent(aktualisKonyvtarJobb);

        aktualisKonyvtarPane.setRightComponent(konyvtarJobb);

        jToolBar1.setRollover(true);
        jToolBar1.setMaximumSize(new java.awt.Dimension(180, 25));
        jToolBar1.setMinimumSize(new java.awt.Dimension(50, 25));
        jToolBar1.setName("jToolBar1"); // NOI18N

        jButton1.setAction(actionMap.get("sugo")); // NOI18N
        jButton1.setText(resourceMap.getString("jButton1.text")); // NOI18N
        jButton1.setFocusable(false);
        jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton1.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButton1.setMaximumSize(new java.awt.Dimension(31, 25));
        jButton1.setMinimumSize(new java.awt.Dimension(31, 25));
        jButton1.setName("jButton1"); // NOI18N
        jButton1.setPreferredSize(new java.awt.Dimension(31, 25));
        jButton1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jButton1);

        jButton2.setAction(actionMap.get("showAboutBox")); // NOI18N
        jButton2.setText(resourceMap.getString("jButton2.text")); // NOI18N
        jButton2.setFocusable(false);
        jButton2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton2.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButton2.setMaximumSize(new java.awt.Dimension(47, 25));
        jButton2.setMinimumSize(new java.awt.Dimension(47, 25));
        jButton2.setName("jButton2"); // NOI18N
        jButton2.setPreferredSize(new java.awt.Dimension(47, 25));
        jButton2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jButton2);

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                .addComponent(eszkoztar, javax.swing.GroupLayout.PREFERRED_SIZE, 302, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 408, Short.MAX_VALUE)
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addComponent(aktualisKonyvtarPane, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(szabadHelyPane, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE))
            .addComponent(panelek, javax.swing.GroupLayout.DEFAULT_SIZE, 806, Short.MAX_VALUE)
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(eszkoztar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(szabadHelyPane, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(aktualisKonyvtarPane, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(30, 30, 30)
                .addComponent(panelek, javax.swing.GroupLayout.DEFAULT_SIZE, 363, Short.MAX_VALUE))
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

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        jMenuItem1.setAction(actionMap.get("sugo")); // NOI18N
        jMenuItem1.setName("jMenuItem1"); // NOI18N
        helpMenu.add(jMenuItem1);

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
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 732, Short.MAX_VALUE)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 550, Short.MAX_VALUE)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusAnimationLabel)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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

    private void panelekPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_panelekPropertyChange
        aktualisKonyvtarPane.setDividerLocation(panelek.getDividerLocation());
        szabadHelyPane.setDividerLocation(panelek.getDividerLocation());
    }//GEN-LAST:event_panelekPropertyChange

    @Action
    public void masolas() {
        EFile oldal = bal;
        EFile masik = jobb;
        JTable t = listaBal;
        if (focus) {
            oldal = jobb;
            masik = bal;
            t = listaJobb;
        }
        if (t.getSelectedRowCount() == 0) {
            JOptionPane.showMessageDialog(t, "Nincs kijelölt file!");
        }
        int[] rows = t.getSelectedRows();
        for (int i : rows) {

            String filenev = File.separator + t.getValueAt(i, 0);
            if (t.getValueAt(i,1) != "") {
                filenev += "."+t.getValueAt(i, 1);
            }

            EFile ef = new EFile(new File(oldal.getFile().getAbsolutePath() + filenev));
            File dest = new File(masik.getFile().getAbsolutePath() + filenev);
            try {
                ef.copyEntry(dest, false);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(t, e.getMessage());
            } catch (OverwritingException e) {
                int optionType = JOptionPane.YES_NO_CANCEL_OPTION;
                int res = JOptionPane.showConfirmDialog(null, "A file már létezik! Felülírod?",
                        "Létező file", optionType);
                if (res == JOptionPane.YES_OPTION) {
                    try {
                        ef.copyEntry(dest, true);
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
    }

    @Action
    public void ujKonyvtar() {
    }

    @Action
    public void torles() {
    }

    @Action
    public void gyokerkonyvtar() {
    }

    @Action
    public void szuloKonyvtar() {
    }

    @Action
    public void sugo() {
        if (sugoAblak == null) {
            JFrame mainFrame = TGcommanderApp.getApplication().getMainFrame();
            sugoAblak = new TGcommanderSugo();
            sugoAblak.setLocationRelativeTo(mainFrame);
        }
        TGcommanderApp.getApplication().show(sugoAblak);
    }



    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel aktualisKonyvtarBal;
    private javax.swing.JLabel aktualisKonyvtarJobb;
    private javax.swing.JSplitPane aktualisKonyvtarPane;
    private javax.swing.JButton athelyezesGomb;
    private javax.swing.JMenuItem athelyezesMenuItem;
    private javax.swing.JPopupMenu.Separator elvalaszto1;
    private javax.swing.JToolBar eszkoztar;
    private javax.swing.JButton gyokerkonyvtarGombBal;
    private javax.swing.JButton gyokerkonyvtarGombJobb;
    private javax.swing.JPanel gyorsGombokBal;
    private javax.swing.JPanel gyorsGombokJobb;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JSplitPane konyvtarBal;
    private javax.swing.JSplitPane konyvtarJobb;
    private javax.swing.JTable listaBal;
    private javax.swing.JTable listaJobb;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JButton masolasGomb;
    private javax.swing.JMenuItem masolasMenuItem;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JScrollPane panelBal;
    private javax.swing.JScrollPane panelJobb;
    private javax.swing.JSplitPane panelek;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JLabel szabadHelyBal;
    private javax.swing.JLabel szabadHelyJobb;
    private javax.swing.JSplitPane szabadHelyPane;
    private javax.swing.JButton szulokonyvtarGombBal;
    private javax.swing.JButton szulokonyvtarGombJobb;
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
    private JFrame sugoAblak;
}
