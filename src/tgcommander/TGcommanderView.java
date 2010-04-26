/*
 * TGcommanderView.java
 */

package tgcommander;

import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFrame;

/**
 * The application's main frame.
 */
public class TGcommanderView extends FrameView {

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
        aktualisKonyvtar = new javax.swing.JSplitPane();
        konyvtarJobb = new javax.swing.JLabel();
        konyvtarBal = new javax.swing.JLabel();
        panelek = new javax.swing.JSplitPane();
        panelBal = new javax.swing.JScrollPane();
        listaBal = new javax.swing.JTable();
        panelJobb = new javax.swing.JScrollPane();
        listaJobb = new javax.swing.JTable();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();

        mainPanel.setName("mainPanel"); // NOI18N
        mainPanel.setPreferredSize(new java.awt.Dimension(800, 600));

        eszkoztar.setRollover(true);
        eszkoztar.setName("eszkoztar"); // NOI18N
        eszkoztar.setPreferredSize(new java.awt.Dimension(600, 25));

        aktualisKonyvtar.setDividerLocation(300);
        aktualisKonyvtar.setDividerSize(0);
        aktualisKonyvtar.setResizeWeight(0.5);
        aktualisKonyvtar.setMinimumSize(new java.awt.Dimension(100, 25));
        aktualisKonyvtar.setName("aktualisKonyvtar"); // NOI18N
        aktualisKonyvtar.setPreferredSize(new java.awt.Dimension(100, 25));

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(tgcommander.TGcommanderApp.class).getContext().getResourceMap(TGcommanderView.class);
        konyvtarJobb.setText(resourceMap.getString("konyvtarJobb.text")); // NOI18N
        konyvtarJobb.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        konyvtarJobb.setName("konyvtarJobb"); // NOI18N
        konyvtarJobb.setNextFocusableComponent(listaJobb);
        aktualisKonyvtar.setRightComponent(konyvtarJobb);

        konyvtarBal.setText(resourceMap.getString("konyvtarBal.text")); // NOI18N
        konyvtarBal.setName("konyvtarBal"); // NOI18N
        aktualisKonyvtar.setLeftComponent(konyvtarBal);

        panelek.setDividerLocation(300);
        panelek.setResizeWeight(0.5);
        panelek.setName("panelek"); // NOI18N
        panelek.setPreferredSize(new java.awt.Dimension(800, 400));
        panelek.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                panelekPropertyChange(evt);
            }
        });

        panelBal.setName("panelBal"); // NOI18N

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
        ) {
            boolean[] canEdit = new boolean [] {
                true, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        listaBal.setName("listaBal"); // NOI18N
        listaBal.setPreferredSize(new java.awt.Dimension(300, 400));
        listaBal.setShowVerticalLines(false);
        listaBal.getTableHeader().setReorderingAllowed(false);
        listaBal.setVerifyInputWhenFocusTarget(false);
        panelBal.setViewportView(listaBal);
        listaBal.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("listaBal.columnModel.title0")); // NOI18N
        listaBal.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("listaBal.columnModel.title1")); // NOI18N
        listaBal.getColumnModel().getColumn(2).setHeaderValue(resourceMap.getString("listaBal.columnModel.title2")); // NOI18N
        listaBal.getColumnModel().getColumn(3).setHeaderValue(resourceMap.getString("listaBal.columnModel.title3")); // NOI18N

        panelek.setRightComponent(panelBal);

        panelJobb.setName("panelJobb"); // NOI18N

        listaJobb.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"", null, null, null},
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
        listaJobb.setName("listaJobb"); // NOI18N
        listaJobb.setPreferredSize(new java.awt.Dimension(300, 400));
        listaJobb.setShowVerticalLines(false);
        listaJobb.setVerifyInputWhenFocusTarget(false);
        panelJobb.setViewportView(listaJobb);
        listaJobb.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("listaJobb.columnModel.title0")); // NOI18N
        listaJobb.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("listaJobb.columnModel.title1")); // NOI18N
        listaJobb.getColumnModel().getColumn(2).setHeaderValue(resourceMap.getString("listaJobb.columnModel.title2")); // NOI18N
        listaJobb.getColumnModel().getColumn(3).setHeaderValue(resourceMap.getString("listaJobb.columnModel.title3")); // NOI18N

        panelek.setLeftComponent(panelJobb);

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(eszkoztar, javax.swing.GroupLayout.DEFAULT_SIZE, 705, Short.MAX_VALUE)
            .addComponent(aktualisKonyvtar, javax.swing.GroupLayout.DEFAULT_SIZE, 705, Short.MAX_VALUE)
            .addComponent(panelek, javax.swing.GroupLayout.DEFAULT_SIZE, 705, Short.MAX_VALUE)
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addComponent(eszkoztar, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(aktualisKonyvtar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelek, javax.swing.GroupLayout.DEFAULT_SIZE, 333, Short.MAX_VALUE))
        );

        menuBar.setName("menuBar"); // NOI18N
        menuBar.setPreferredSize(new java.awt.Dimension(600, 20));

        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(tgcommander.TGcommanderApp.class).getContext().getActionMap(TGcommanderView.class, this);
        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

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
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 705, Short.MAX_VALUE)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 535, Short.MAX_VALUE)
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
    }// </editor-fold>//GEN-END:initComponents

    private void panelekPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_panelekPropertyChange
        aktualisKonyvtar.setDividerLocation(panelek.getDividerLocation());
    }//GEN-LAST:event_panelekPropertyChange

   private void konyvtarListazas(){

   }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSplitPane aktualisKonyvtar;
    private javax.swing.JToolBar eszkoztar;
    private javax.swing.JLabel konyvtarBal;
    private javax.swing.JLabel konyvtarJobb;
    private javax.swing.JTable listaBal;
    private javax.swing.JTable listaJobb;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JScrollPane panelBal;
    private javax.swing.JScrollPane panelJobb;
    private javax.swing.JSplitPane panelek;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    // End of variables declaration//GEN-END:variables

    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;

    private JDialog aboutBox;
}