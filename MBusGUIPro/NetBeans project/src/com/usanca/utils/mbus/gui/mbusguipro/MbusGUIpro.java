/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.usanca.utils.mbus.gui.mbusguipro;

import com.usanca.utils.mbus.gui.extras.AutoReader;
import com.usanca.utils.mbus.gui.extras.CommPortFinder;
import com.usanca.utils.mbus.gui.extras.CsvReader;
import com.usanca.utils.mbus.gui.extras.ExcelExporter;
import com.usanca.utils.mbus.gui.extras.Printer;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import com.usanca.utils.mbus.gui.mbus.MBusFinder;
import com.usanca.utils.mbus.gui.mbus.MBusReader;
import com.usanca.utils.mbus.gui.mbus.SwingWorkerTest;
import com.usanca.utils.mbus.gui.types.MBusDevice;
import com.usanca.utils.mbus.gui.types.MBusStreamProperties;
import java.awt.Component;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import jxl.write.WriteException;

/**
 *
 * @author Pablo Monteserin Garcia. pablomonteserin@gmail.com
 */
public class MbusGUIpro extends javax.swing.JFrame implements PropertyChangeListener, FocusListener, ListSelectionListener, TableModelListener, MouseListener {


    
    static public boolean proVersion = true;

    int inewdev = 0;
    public Printer log;
    static public String programTitle = "M-Bus Scanner";
    static public String acercadeText
            = "<html>"
            + "<center> "
            + "<p ALIGN='center'>Pablo Monteserín </p>"
            + "<p ALIGN='center'>Fernando Garcia </p>"
            + "</center>"
            + "</html>";
    /**
     * Creates new form MbusGUI
     */
    JFileChooser fc;
    String extension = "";
    static File devFile;

    SwingWorkerTest swt;
    MBusFinder mFinder;
    MBusReader mReader;
    MBusDevice devSelected;
    MBusStreamProperties streamProps = new MBusStreamProperties();
    CsvReader csvReader = new CsvReader();
    TableModelDevices tableModelDevices = new TableModelDevices();
    DefaultTableModel tableModelReadings = new DefaultTableModel();
    ArrayList<MBusDevice> devices = new ArrayList<>();
    TableRendererDevices devicesTableRenderer = new TableRendererDevices();
    TableRendererReadings readingsTableRenderer = new TableRendererReadings();
    boolean includeHexadecimal;

    AutoReader autoReader;
    int autoReadInterval;
    Date autoReadBaseTime;

    public MbusGUIpro() {
    java.net.URL url = ClassLoader.getSystemResource("com/xyz/resources/camera.png");
        
        initComponents();
        getIcon();
        acercadeLabel.setText(acercadeText);
        versionLabel.setText("Version de Java " + System.getProperty("java.version"));

        //setting area towards messages are fordwarded to   
        Printer.setArea(jTextAreaLog);

        setLocationRelativeTo(null); // centers the window

        // file chooser
        fc = new JFileChooser();
        FileNameExtensionFilter csvFilter = new FileNameExtensionFilter("Valores separados por coma", "csv", "CSV");
        fc.setFileFilter(csvFilter);

        // setting default for autoRead feature: disabled by default
        Date date = (Date) jSpinnerAutoReadBaseTime.getModel().getValue();
        System.out.println(date.toString());
        autoReader = new AutoReader();
        autoReadBaseTime = null;
        autoReadInterval = -1;

        // setting default options for connection properties
        jComboBoxBaudios.setSelectedItem(9600);
        jComboBoxParidad.getModel().setSelectedItem(jComboBoxParidad.getModel().getElementAt(0)); // at position 0 should be Even
        updateCOMPorts();
        jComboBoxPorts.setSelectedItem("COM1");
        jTextFieldDataBits.setText("8");
        jTextFieldStopBits.setText("1");
        jTextFieldTimeOut.setText("2000");
        updateStreamProps();

        // Devices table settings 
        jTableDevices.setDefaultRenderer(Object.class, devicesTableRenderer);
        jTableDevices.setName("tabla de dispositivos");
        jTableDevices.getColumnModel().getColumn(0).setMinWidth(0);         // hide index column
        jTableDevices.getColumnModel().getColumn(0).setMaxWidth(0);
        jTableDevices.getColumnModel().getColumn(0).setResizable(false);
        jTableDevices.getColumnModel().getColumn(1).setResizable(false);    // primaria
        jTableDevices.getColumnModel().getColumn(1).setMinWidth(50);
        jTableDevices.getColumnModel().getColumn(1).setMaxWidth(50);
        jTableDevices.getColumnModel().getColumn(2).setResizable(false);    // secundaria
        jTableDevices.getColumnModel().getColumn(2).setMinWidth(100);
        jTableDevices.getColumnModel().getColumn(2).setMaxWidth(100);
        jTableDevices.getColumnModel().getColumn(3).setMinWidth(300);       // notas
        jTableDevices.getColumnModel().getColumn(3).setResizable(true);

        TableRowSorter sorter = new TableRowSorter(tableModelDevices);
        sorter.setSortable(0, true);
        sorter.setSortable(1, true);
        sorter.setSortable(2, true);
        sorter.setSortable(3, true);
        jTableDevices.setRowSorter(sorter);

        // Readings table settings 
        jTableReadings.setDefaultRenderer(Object.class, readingsTableRenderer);

        // register this as listener
        jComboBoxBaudios.addFocusListener(this);
        jTextFieldDataBits.addFocusListener(this);
        jComboBoxParidad.addFocusListener(this);
        jComboBoxPorts.addFocusListener(this);
        jTextFieldStopBits.addFocusListener(this);
        jTextFieldTimeOut.addFocusListener(this);

        jTableDevices.getSelectionModel().addListSelectionListener(this);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                updateStreamProps();

                System.out.println("guardando los dispositivos en last.csv");
                devFile = new File("last");
                extension = ".csv";
                saveDevices2CSV();
            }
        }
        );

        // load last.csv
        devFile = new File("last.csv");
        try {
            loadDevicesFromCSV();
            jTextAreaLog.setText(""); //erases the log pane so it starts clean for the user            
            updateJPanelStream();
        } catch (Exception e) {
            System.out.println("problemas cargando last.csv");
        }

        Printer.out("Bienvenido a MBusGUI Pro");
        Printer.out("");
        //Printer.out("Utiliza el menu ayuda para aprender como funciona este programa");        
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        //if (proVersion) {
        if (tableModelReadings.getRowCount() > 0) {
            exportButton.setEnabled(true);
            jMenuExportar.setEnabled(true);
            /*
            } else {
                exportButton.setEnabled(false);
                jMenuExportar.setEnabled(false);
             */
        }
        //}
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("progress" == evt.getPropertyName()) {
            int progress = (Integer) evt.getNewValue();
            jProgressBar1.setValue(progress);
        }
        if ("state" == evt.getPropertyName()) {
            String aux = (String) evt.getNewValue().toString();
            if (aux.equalsIgnoreCase("done")) {
                stopButton.setEnabled(false);
                primarySearchButton.setEnabled(true);
                secondarySearchButton.setEnabled(true);
                addButton.setEnabled(true);
                removeButton.setEnabled(true);
                if (!jTableDevices.getSelectionModel().isSelectionEmpty()) {
                    readButton.setEnabled(true);
                }
                if ((mFinder != null) && (mFinder.isCancelled())) {
                    jProgressBar1.setString("Busqueda cancelada");
                } else {
                    jProgressBar1.setString("Finalizado");
                }

                if (mReader != null && mReader.isCancelled()) {
                    jProgressBar1.setString("Lectura cancelada");
                } else {
                    jProgressBar1.setString("Finalizado");
                }
            }

            if (aux.equalsIgnoreCase("started")) {
                jProgressBar1.setString(null);
                jProgressBar1.setValue(0);
            }
        }
    }

    @Override
    public void focusGained(FocusEvent fe) {

    }

    @Override
    public void focusLost(FocusEvent fe) {
        updateStreamProps();
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (!jTableDevices.getSelectionModel().isSelectionEmpty()) {
            readButton.setEnabled(true);
        } else {
            readButton.setEnabled(false);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    //@SuppressWarnings("unchecked")
    // Mouse listener events
    @Override
    public void mouseEntered(MouseEvent e) {
        // check ports and update the combobox.
        if (e.getComponent().getClass() == jComboBoxPorts.getClass()) {
            String selected = jComboBoxPorts.getSelectedItem().toString();
            String[] ports = CommPortFinder.find();
            jComboBoxPorts.setModel(new DefaultComboBoxModel<String>(ports));
            jComboBoxPorts.setSelectedItem(selected);   // maintain the selected element if possible

            updateStreamProps();
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        acercadePanel = new javax.swing.JPanel();
        versionLabel = new javax.swing.JLabel();
        programTitleLabel = new javax.swing.JLabel();
        acercadeLabel = new javax.swing.JLabel();
        timePickerPanel = new javax.swing.JPanel();
        toCSV1 = new javax.swing.JButton();
        jCheckBoxAutoReadEnabled = new javax.swing.JCheckBox();
        jPanelDate = new javax.swing.JPanel();
        jSpinnerReadBaseDay = new javax.swing.JSpinner();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        Date date = new Date();
        SpinnerDateModel sm =
        new SpinnerDateModel(date,null,null, Calendar.HOUR_OF_DAY);
        jSpinnerAutoReadBaseTime = new javax.swing.JSpinner(sm);
        jLabel3 = new javax.swing.JLabel();
        jSpinnerReadPeriod = new javax.swing.JSpinner();
        registerPanel = new javax.swing.JPanel();
        jPanelStream = new javax.swing.JPanel();
        jTextFieldDataBits = new javax.swing.JTextField();
        jTextFieldStopBits = new javax.swing.JTextField();
        jLabelStopBits = new javax.swing.JLabel();
        jLabelDataBits = new javax.swing.JLabel();
        jTextFieldTimeOut = new javax.swing.JTextField();
        jLabelTimeOut = new javax.swing.JLabel();
        jLabelParidad = new javax.swing.JLabel();
        jLabelCOM = new javax.swing.JLabel();
        jLabelBaudios = new javax.swing.JLabel();
        jComboBoxParidad = new javax.swing.JComboBox<>();
        jComboBoxBaudios = new javax.swing.JComboBox<>();
        jComboBoxPorts = new javax.swing.JComboBox<>();
        jProgressBar1 = new javax.swing.JProgressBar();
        jSplitPane2 = new javax.swing.JSplitPane();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextAreaLog = new javax.swing.JTextArea();
        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTableDevices = new javax.swing.JTable();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTableReadings = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        primarySearchButton = new javax.swing.JButton();
        secondarySearchButton = new javax.swing.JButton();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        readButton = new javax.swing.JButton();
        stopButton = new javax.swing.JButton();
        exportButton = new javax.swing.JButton();
        fromCSV = new javax.swing.JButton();
        toCSV = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenuArchivo = new javax.swing.JMenu();
        jMenuCargarSite = new javax.swing.JMenuItem();
        jMenuGuardarSite = new javax.swing.JMenuItem();
        jMenuContadores = new javax.swing.JMenu();
        jMenuPrimaria = new javax.swing.JMenuItem();
        jMenuSecundaria = new javax.swing.JMenuItem();
        jCheckBoxHexadecimal = new javax.swing.JCheckBoxMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        jMenuAñadir = new javax.swing.JMenuItem();
        jMenuEliminar = new javax.swing.JMenuItem();
        jMenuLecturas = new javax.swing.JMenu();
        jMenuLeerSeleccionados = new javax.swing.JMenuItem();
        jMenuLeerTodos = new javax.swing.JMenuItem();
        jMenuExportar = new javax.swing.JMenuItem();
        jMenuAyuda = new javax.swing.JMenu();
        jMenuAcercade = new javax.swing.JMenuItem();

        versionLabel.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        versionLabel.setForeground(new java.awt.Color(102, 102, 102));
        versionLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        versionLabel.setText("Java Version");

        programTitleLabel.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        programTitleLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        programTitleLabel.setText("Titulo del programa");

        acercadeLabel.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        acercadeLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        acercadeLabel.setText("AUTORES");

        javax.swing.GroupLayout acercadePanelLayout = new javax.swing.GroupLayout(acercadePanel);
        acercadePanel.setLayout(acercadePanelLayout);
        acercadePanelLayout.setHorizontalGroup(
            acercadePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(acercadePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(acercadePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(versionLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(programTitleLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 278, Short.MAX_VALUE)
                    .addComponent(acercadeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        acercadePanelLayout.setVerticalGroup(
            acercadePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, acercadePanelLayout.createSequentialGroup()
                .addContainerGap(23, Short.MAX_VALUE)
                .addComponent(programTitleLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(acercadeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(versionLabel)
                .addGap(33, 33, 33))
        );

        programTitleLabel.setText(programTitle);

        toCSV1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/usanca/utils/mbus/gui/Res/exportPro.png"))); // NOI18N
        toCSV1.setText("<html><center>Seleccionar<br><center>archivo</html>");
        toCSV1.setToolTipText("Guarda la lista actual de medidores a un fichero .CSV");
        toCSV1.setBorderPainted(false);
        toCSV1.setContentAreaFilled(false);
        toCSV1.setDebugGraphicsOptions(javax.swing.DebugGraphics.NONE_OPTION);
        toCSV1.setDoubleBuffered(true);
        toCSV1.setFocusPainted(false);
        toCSV1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        toCSV1.setIconTextGap(0);
        toCSV1.setMargin(new java.awt.Insets(0, 0, 0, 0));
        toCSV1.setMaximumSize(new java.awt.Dimension(100, 100));
        toCSV1.setMinimumSize(new java.awt.Dimension(43, 54));
        toCSV1.setPreferredSize(new java.awt.Dimension(57, 40));
        toCSV1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toCSV1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toCSV1ActionPerformed(evt);
            }
        });

        jCheckBoxAutoReadEnabled.setText("Activar lecturas automáticas");
        jCheckBoxAutoReadEnabled.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxAutoReadEnabledActionPerformed(evt);
            }
        });

        jSpinnerReadBaseDay.setModel(new javax.swing.SpinnerNumberModel(1, null, 31, 1));
        jSpinnerReadBaseDay.setEnabled(false);

        jLabel1.setText("Día");
        jLabel1.setToolTipText("");

        jLabel2.setText("Intervalo de repeticion ( horas )");
        jLabel2.setToolTipText("");

        JSpinner.DateEditor de = new JSpinner.DateEditor(jSpinnerAutoReadBaseTime, "HH:mm");
        jSpinnerAutoReadBaseTime.setEditor(de);
        jSpinnerAutoReadBaseTime.setToolTipText("Hora primera lectura");
        jSpinnerAutoReadBaseTime.setEnabled(false);
        jSpinnerAutoReadBaseTime.setName("Hora"); // NOI18N

        jLabel3.setText("Hora");
        jLabel3.setToolTipText("");

        jSpinnerReadPeriod.setModel(new javax.swing.SpinnerNumberModel(1, 1, 744, 1));
        jSpinnerReadPeriod.setEnabled(false);

        javax.swing.GroupLayout jPanelDateLayout = new javax.swing.GroupLayout(jPanelDate);
        jPanelDate.setLayout(jPanelDateLayout);
        jPanelDateLayout.setHorizontalGroup(
            jPanelDateLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelDateLayout.createSequentialGroup()
                .addGap(67, 67, 67)
                .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 292, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(jPanelDateLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSpinnerReadPeriod, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanelDateLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanelDateLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(jPanelDateLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanelDateLayout.createSequentialGroup()
                            .addGroup(jPanelDateLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jSpinnerReadBaseDay, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGap(18, 18, 18)
                            .addComponent(jSpinnerAutoReadBaseTime)))
                    .addContainerGap()))
        );
        jPanelDateLayout.setVerticalGroup(
            jPanelDateLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelDateLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 54, Short.MAX_VALUE)
                .addComponent(jSpinnerReadPeriod, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(51, 51, 51))
            .addGroup(jPanelDateLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanelDateLayout.createSequentialGroup()
                    .addGap(8, 8, 8)
                    .addComponent(jLabel1)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(jPanelDateLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jSpinnerAutoReadBaseTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jSpinnerReadBaseDay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(11, 11, 11)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(77, Short.MAX_VALUE)))
        );

        javax.swing.GroupLayout timePickerPanelLayout = new javax.swing.GroupLayout(timePickerPanel);
        timePickerPanel.setLayout(timePickerPanelLayout);
        timePickerPanelLayout.setHorizontalGroup(
            timePickerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(timePickerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(toCSV1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 155, Short.MAX_VALUE)
                .addComponent(jCheckBoxAutoReadEnabled)
                .addContainerGap())
            .addGroup(timePickerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(timePickerPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jPanelDate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        timePickerPanelLayout.setVerticalGroup(
            timePickerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, timePickerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(timePickerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(timePickerPanelLayout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(jCheckBoxAutoReadEnabled))
                    .addComponent(toCSV1, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(328, Short.MAX_VALUE))
            .addGroup(timePickerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, timePickerPanelLayout.createSequentialGroup()
                    .addContainerGap(247, Short.MAX_VALUE)
                    .addComponent(jPanelDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap()))
        );

        toCSV.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseEntered(MouseEvent event){
                JButton buton = (JButton)event.getSource();
                buton.setContentAreaFilled(true);  //when hoovered it will show borders and fill area.
            }

            @Override
            public void mouseExited(MouseEvent event){
                JButton buton = (JButton)event.getSource();
                buton.setContentAreaFilled(false); //when mouse is not on button then it will look the same.
            }
        });

        javax.swing.GroupLayout registerPanelLayout = new javax.swing.GroupLayout(registerPanel);
        registerPanel.setLayout(registerPanelLayout);
        registerPanelLayout.setHorizontalGroup(
            registerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        registerPanelLayout.setVerticalGroup(
            registerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Mbus scanner");
        setBackground(new java.awt.Color(0, 0, 0));
        setForeground(java.awt.Color.darkGray);
        setIconImage(getIconImage());
        setMinimumSize(new java.awt.Dimension(700, 500));
        setName("MBUSGUI"); // NOI18N

        jPanelStream.setBackground(new java.awt.Color(153, 153, 153));
        jPanelStream.setOpaque(false);

        jTextFieldDataBits.setText("vDataBits");
        jTextFieldDataBits.setToolTipText("Bits de datos");
        jTextFieldDataBits.setMinimumSize(new java.awt.Dimension(6, 16));
        jTextFieldDataBits.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldDataBitsActionPerformed(evt);
            }
        });

        jTextFieldStopBits.setText("vStopBits");
        jTextFieldStopBits.setToolTipText("Bits de parada");
        jTextFieldStopBits.setMinimumSize(new java.awt.Dimension(6, 16));
        jTextFieldStopBits.setPreferredSize(new java.awt.Dimension(75, 28));

        jLabelStopBits.setFont(new java.awt.Font("DejaVu Sans", 0, 13)); // NOI18N
        jLabelStopBits.setText("StopBits");

        jLabelDataBits.setFont(new java.awt.Font("DejaVu Sans", 0, 13)); // NOI18N
        jLabelDataBits.setText("DataBits");
        jLabelDataBits.setToolTipText("");

        jTextFieldTimeOut.setText("vTimeOut");
        jTextFieldTimeOut.setToolTipText("Timeout (ms)");
        jTextFieldTimeOut.setMinimumSize(new java.awt.Dimension(6, 16));
        jTextFieldTimeOut.setPreferredSize(new java.awt.Dimension(80, 28));
        jTextFieldTimeOut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldTimeOutActionPerformed(evt);
            }
        });

        jLabelTimeOut.setFont(new java.awt.Font("DejaVu Sans", 0, 13)); // NOI18N
        jLabelTimeOut.setText("TimeOut");

        jLabelParidad.setFont(new java.awt.Font("DejaVu Sans", 0, 13)); // NOI18N
        jLabelParidad.setText("Paridad");
        jLabelParidad.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        jLabelCOM.setFont(new java.awt.Font("DejaVu Sans", 0, 13)); // NOI18N
        jLabelCOM.setText("Puerto");
        jLabelCOM.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);

        jLabelBaudios.setFont(new java.awt.Font("DejaVu Sans", 0, 13)); // NOI18N
        jLabelBaudios.setText("Baudios");
        jLabelBaudios.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);

        jComboBoxParidad.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Par", "Impar", "Ninguna" }));
        jComboBoxParidad.setToolTipText("Bit de paridad: par (even), inpar (odd), sin paridad");
        jComboBoxParidad.setFocusable(false);
        jComboBoxParidad.setPreferredSize(new java.awt.Dimension(80, 28));
        jComboBoxParidad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxParidadActionPerformed(evt);
            }
        });

        jComboBoxBaudios.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "300", "600", "1200", "2400", "4800", "9600", "19200", "38400" }));
        jComboBoxBaudios.setToolTipText("Paridad del bit de paridad: par (even), inpar (odd), sin paridad");
        jComboBoxBaudios.setFocusable(false);
        jComboBoxBaudios.setPreferredSize(new java.awt.Dimension(80, 28));
        jComboBoxBaudios.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxBaudiosActionPerformed(evt);
            }
        });

        jComboBoxPorts.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "300", "600", "1200", "2400", "4800", "9600", "19200", "38400" }));
        jComboBoxPorts.setToolTipText("Paridad del bit de paridad: par (even), inpar (odd), sin paridad");
        jComboBoxPorts.setFocusable(false);
        jComboBoxPorts.setPreferredSize(new java.awt.Dimension(80, 28));
        jComboBoxPorts.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxPortsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelStreamLayout = new javax.swing.GroupLayout(jPanelStream);
        jPanelStream.setLayout(jPanelStreamLayout);
        jPanelStreamLayout.setHorizontalGroup(
            jPanelStreamLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelStreamLayout.createSequentialGroup()
                .addGroup(jPanelStreamLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabelCOM)
                    .addComponent(jLabelBaudios))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanelStreamLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jComboBoxBaudios, 0, 55, Short.MAX_VALUE)
                    .addComponent(jComboBoxPorts, 0, 55, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanelStreamLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabelParidad, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabelTimeOut, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanelStreamLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jTextFieldTimeOut, javax.swing.GroupLayout.DEFAULT_SIZE, 73, Short.MAX_VALUE)
                    .addComponent(jComboBoxParidad, 0, 1, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanelStreamLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabelDataBits)
                    .addComponent(jLabelStopBits))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanelStreamLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextFieldDataBits, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldStopBits, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
        jPanelStreamLayout.setVerticalGroup(
            jPanelStreamLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelStreamLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanelStreamLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanelStreamLayout.createSequentialGroup()
                        .addComponent(jTextFieldDataBits, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanelStreamLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabelStopBits)
                            .addComponent(jTextFieldStopBits, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanelStreamLayout.createSequentialGroup()
                        .addComponent(jLabelDataBits)
                        .addGap(24, 24, 24))
                    .addGroup(jPanelStreamLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabelTimeOut)
                        .addComponent(jTextFieldTimeOut, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jComboBoxPorts, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanelStreamLayout.createSequentialGroup()
                        .addGroup(jPanelStreamLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabelBaudios)
                            .addComponent(jLabelParidad)
                            .addComponent(jComboBoxParidad, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jComboBoxBaudios, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabelCOM)))
                .addGap(8, 8, 8))
        );

        jTextFieldTimeOut.getAccessibleContext().setAccessibleName("TimeOut");
        jTextFieldTimeOut.getAccessibleContext().setAccessibleDescription("TimeOut (ms)");
        jComboBoxPorts.addMouseListener(this);
        String[] ports = CommPortFinder.find();
        jComboBoxPorts.setModel(new DefaultComboBoxModel<String>(ports));

        jProgressBar1.setPreferredSize(new java.awt.Dimension(260, 20));
        jProgressBar1.setStringPainted(true);

        jSplitPane2.setBorder(null);
        jSplitPane2.setDividerLocation(400);
        jSplitPane2.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane2.setResizeWeight(1.0);

        jTextAreaLog.setEditable(false);
        jTextAreaLog.setColumns(20);
        jTextAreaLog.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        jTextAreaLog.setRows(5);
        jTextAreaLog.setToolTipText("LOG");
        jScrollPane3.setViewportView(jTextAreaLog);

        jSplitPane2.setBottomComponent(jScrollPane3);

        jSplitPane1.setBorder(null);
        jSplitPane1.setDividerLocation(190);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setResizeWeight(0.5);
        jSplitPane1.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        jTableDevices.setAutoCreateRowSorter(true);
        jTableDevices.setModel(tableModelDevices);
        jTableDevices.setToolTipText("Dispositivos MBUS");
        jTableDevices.setCellSelectionEnabled(true);
        jTableDevices.setDragEnabled(true);
        jTableDevices.setGridColor(new java.awt.Color(255, 255, 255));
        jTableDevices.setNextFocusableComponent(jTableReadings);
        jTableDevices.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        jTableDevices.setShowHorizontalLines(false);
        jTableDevices.setShowVerticalLines(false);
        jScrollPane1.setViewportView(jTableDevices);
        jTableDevices.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);

        jSplitPane1.setTopComponent(jScrollPane1);

        jTableReadings.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "TableID", "Fecha de lectura", "Direccion", "DIF", "VIF", "Medida", "Tipo", "Dato", "Unidad"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTableReadings.setToolTipText("Lecturas de los contadores");
        jTableReadings.setColumnSelectionAllowed(true);
        jTableReadings.setRowSelectionAllowed(false);
        jTableReadings.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        jTableReadings.setShowHorizontalLines(false);
        jTableReadings.setShowVerticalLines(false);
        jScrollPane4.setViewportView(jTableReadings);
        jTableReadings.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        if (jTableReadings.getColumnModel().getColumnCount() > 0) {
            jTableReadings.getColumnModel().getColumn(0).setMinWidth(0);
            jTableReadings.getColumnModel().getColumn(0).setPreferredWidth(0);
            jTableReadings.getColumnModel().getColumn(0).setMaxWidth(0);
            jTableReadings.getColumnModel().getColumn(1).setMinWidth(0);
            jTableReadings.getColumnModel().getColumn(1).setPreferredWidth(0);
            jTableReadings.getColumnModel().getColumn(1).setMaxWidth(0);
            jTableReadings.getColumnModel().getColumn(2).setMinWidth(5);
            jTableReadings.getColumnModel().getColumn(2).setMaxWidth(100);
            jTableReadings.getColumnModel().getColumn(3).setMinWidth(30);
            jTableReadings.getColumnModel().getColumn(3).setMaxWidth(30);
            jTableReadings.getColumnModel().getColumn(4).setMinWidth(30);
            jTableReadings.getColumnModel().getColumn(4).setMaxWidth(30);
            jTableReadings.getColumnModel().getColumn(6).setMinWidth(100);
            jTableReadings.getColumnModel().getColumn(6).setMaxWidth(140);
            jTableReadings.getColumnModel().getColumn(8).setMinWidth(50);
            jTableReadings.getColumnModel().getColumn(8).setMaxWidth(50);
        }

        jSplitPane1.setRightComponent(jScrollPane4);

        jSplitPane2.setTopComponent(jSplitPane1);

        jPanel3.setOpaque(false);

        primarySearchButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/usanca/utils/mbus/gui/Res/primariaPro.png"))); // NOI18N
        primarySearchButton.setText("<html><center>Buscar<br><center>primarias</html>");
        primarySearchButton.setToolTipText("Escanea el bus buscando por direcciones primarias");
        primarySearchButton.setBorderPainted(false);
        primarySearchButton.setContentAreaFilled(false);
        primarySearchButton.setDebugGraphicsOptions(javax.swing.DebugGraphics.NONE_OPTION);
        primarySearchButton.setDoubleBuffered(true);
        primarySearchButton.setFocusPainted(false);
        primarySearchButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        primarySearchButton.setIconTextGap(0);
        primarySearchButton.setMargin(new java.awt.Insets(2, 0, 2, 0));
        primarySearchButton.setMaximumSize(new java.awt.Dimension(100, 100));
        primarySearchButton.setMinimumSize(new java.awt.Dimension(43, 59));
        primarySearchButton.setName("PSearch"); // NOI18N
        primarySearchButton.setPreferredSize(new java.awt.Dimension(57, 40));
        primarySearchButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        primarySearchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                primarySearchButtonActionPerformed(evt);
            }
        });

        secondarySearchButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/usanca/utils/mbus/gui/Res/secundariaPro.png"))); // NOI18N
        secondarySearchButton.setText("<html><center>Buscar<br><center>secundarias</html>");
        secondarySearchButton.setToolTipText("Escanea el bus buscando por direcciones secundarias");
        secondarySearchButton.setBorderPainted(false);
        secondarySearchButton.setContentAreaFilled(false);
        secondarySearchButton.setDebugGraphicsOptions(javax.swing.DebugGraphics.NONE_OPTION);
        secondarySearchButton.setDoubleBuffered(true);
        secondarySearchButton.setFocusPainted(false);
        secondarySearchButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        secondarySearchButton.setIconTextGap(0);
        secondarySearchButton.setMargin(new java.awt.Insets(2, 0, 2, 0));
        secondarySearchButton.setMaximumSize(new java.awt.Dimension(100, 100));
        secondarySearchButton.setMinimumSize(new java.awt.Dimension(43, 59));
        secondarySearchButton.setName("SSearch"); // NOI18N
        secondarySearchButton.setPreferredSize(new java.awt.Dimension(57, 40));
        secondarySearchButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        secondarySearchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                secondarySearchButtonActionPerformed(evt);
            }
        });

        addButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/usanca/utils/mbus/gui/Res/addPro.png"))); // NOI18N
        addButton.setText("<html><center>Añadir<br><center>medidor</html>");
        addButton.setToolTipText("Añade una nueva fila a la lista de medidores");
        addButton.setBorderPainted(false);
        addButton.setContentAreaFilled(false);
        addButton.setDebugGraphicsOptions(javax.swing.DebugGraphics.NONE_OPTION);
        addButton.setDoubleBuffered(true);
        addButton.setFocusPainted(false);
        addButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        addButton.setIconTextGap(0);
        addButton.setMargin(new java.awt.Insets(2, 0, 2, 0));
        addButton.setMaximumSize(new java.awt.Dimension(100, 100));
        addButton.setMinimumSize(new java.awt.Dimension(43, 59));
        addButton.setPreferredSize(new java.awt.Dimension(57, 40));
        addButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        removeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/usanca/utils/mbus/gui/Res/delPro.png"))); // NOI18N
        removeButton.setText("<html><center>Eliminar<br><center>seleccion</html>");
        removeButton.setToolTipText("Elimina el(los) medidor(es) seleccionado(s)");
        removeButton.setBorderPainted(false);
        removeButton.setContentAreaFilled(false);
        removeButton.setDebugGraphicsOptions(javax.swing.DebugGraphics.NONE_OPTION);
        removeButton.setDoubleBuffered(true);
        removeButton.setFocusPainted(false);
        removeButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        removeButton.setIconTextGap(0);
        removeButton.setMargin(new java.awt.Insets(2, 0, 2, 0));
        removeButton.setMaximumSize(new java.awt.Dimension(100, 100));
        removeButton.setMinimumSize(new java.awt.Dimension(43, 59));
        removeButton.setPreferredSize(new java.awt.Dimension(57, 40));
        removeButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        readButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/usanca/utils/mbus/gui/Res/playPro.png"))); // NOI18N
        readButton.setText("<html><center>Leer<br><center>seleccion</html>");
        readButton.setToolTipText("Lee el(los) medidor(es) seleccionado(s)");
        readButton.setBorderPainted(false);
        readButton.setContentAreaFilled(false);
        readButton.setDebugGraphicsOptions(javax.swing.DebugGraphics.NONE_OPTION);
        readButton.setDoubleBuffered(true);
        readButton.setEnabled(false);
        readButton.setFocusPainted(false);
        readButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        readButton.setIconTextGap(0);
        readButton.setMargin(new java.awt.Insets(2, 0, 2, 0));
        readButton.setMaximumSize(new java.awt.Dimension(100, 100));
        readButton.setMinimumSize(new java.awt.Dimension(43, 59));
        readButton.setPreferredSize(new java.awt.Dimension(57, 40));
        readButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        readButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                readButtonActionPerformed(evt);
            }
        });

        stopButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/usanca/utils/mbus/gui/Res/stopPro.png"))); // NOI18N
        stopButton.setText("Detener");
        stopButton.setToolTipText("Detiene el escaneo de bus / lectura de medidores");
        stopButton.setActionCommand("<html><center>Detener<br></html>");
        stopButton.setBorderPainted(false);
        stopButton.setContentAreaFilled(false);
        stopButton.setDebugGraphicsOptions(javax.swing.DebugGraphics.NONE_OPTION);
        stopButton.setDoubleBuffered(true);
        stopButton.setEnabled(false);
        stopButton.setFocusPainted(false);
        stopButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        stopButton.setIconTextGap(7);
        stopButton.setMargin(new java.awt.Insets(2, 0, 10, 0));
        stopButton.setMaximumSize(new java.awt.Dimension(100, 100));
        stopButton.setMinimumSize(new java.awt.Dimension(43, 59));
        stopButton.setPreferredSize(new java.awt.Dimension(57, 40));
        stopButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        stopButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopButtonActionPerformed(evt);
            }
        });

        exportButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/usanca/utils/mbus/gui/Res/exportPro.png"))); // NOI18N
        exportButton.setText("<html><center>Exportar<br><center>a excel</html>");
        exportButton.setToolTipText("Exporta los resultados de las lecturas a un fichero .xls");
        exportButton.setBorderPainted(false);
        exportButton.setContentAreaFilled(false);
        exportButton.setDebugGraphicsOptions(javax.swing.DebugGraphics.NONE_OPTION);
        exportButton.setDoubleBuffered(true);
        exportButton.setEnabled(false);
        exportButton.setFocusPainted(false);
        exportButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        exportButton.setIconTextGap(0);
        exportButton.setMargin(new java.awt.Insets(8, 0, 2, 0));
        exportButton.setMaximumSize(new java.awt.Dimension(100, 100));
        exportButton.setMinimumSize(new java.awt.Dimension(43, 59));
        exportButton.setPreferredSize(new java.awt.Dimension(57, 40));
        exportButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        exportButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(primarySearchButton, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(secondarySearchButton, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(addButton, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(removeButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(readButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(stopButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(exportButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(primarySearchButton, javax.swing.GroupLayout.DEFAULT_SIZE, 65, Short.MAX_VALUE)
            .addComponent(secondarySearchButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(addButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(removeButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(readButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(stopButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(exportButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        primarySearchButton.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseEntered(MouseEvent event){
                JButton buton = (JButton)event.getSource();
                buton.setRolloverEnabled(false);
                buton.setContentAreaFilled(true);  //when hoovered it will show borders and fill area.
                buton.setRolloverEnabled(true);
            }

            @Override
            public void mouseExited(MouseEvent event){
                JButton buton = (JButton)event.getSource();
                buton.setContentAreaFilled(false); //when mouse is not on button then it will look the same.
            }
        });
        secondarySearchButton.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseEntered(MouseEvent event){
                JButton buton = (JButton)event.getSource();
                buton.setContentAreaFilled(true);  //when hoovered it will show borders and fill area.
            }

            @Override
            public void mouseExited(MouseEvent event){
                JButton buton = (JButton)event.getSource();
                buton.setContentAreaFilled(false); //when mouse is not on button then it will look the same.
            }
        });
        addButton.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseEntered(MouseEvent event){
                JButton buton = (JButton)event.getSource();
                buton.setContentAreaFilled(true);  //when hoovered it will show borders and fill area.
            }

            @Override
            public void mouseExited(MouseEvent event){
                JButton buton = (JButton)event.getSource();
                buton.setContentAreaFilled(false); //when mouse is not on button then it will look the same.
            }
        });
        removeButton.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseEntered(MouseEvent event){
                JButton buton = (JButton)event.getSource();
                buton.setContentAreaFilled(true);  //when hoovered it will show borders and fill area.
            }

            @Override
            public void mouseExited(MouseEvent event){
                JButton buton = (JButton)event.getSource();
                buton.setContentAreaFilled(false); //when mouse is not on button then it will look the same.
            }
        });
        readButton.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseEntered(MouseEvent event){
                JButton buton = (JButton)event.getSource();
                buton.setContentAreaFilled(true);  //when hoovered it will show borders and fill area.
            }

            @Override
            public void mouseExited(MouseEvent event){
                JButton buton = (JButton)event.getSource();
                buton.setContentAreaFilled(false); //when mouse is not on button then it will look the same.
            }
        });
        stopButton.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseEntered(MouseEvent event){
                JButton buton = (JButton)event.getSource();
                buton.setContentAreaFilled(true);  //when hoovered it will show borders and fill area.
            }

            @Override
            public void mouseExited(MouseEvent event){
                JButton buton = (JButton)event.getSource();
                buton.setContentAreaFilled(false); //when mouse is not on button then it will look the same.
            }
        });
        exportButton.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseEntered(MouseEvent event){
                JButton buton = (JButton)event.getSource();
                buton.setContentAreaFilled(true);  //when hoovered it will show borders and fill area.
            }

            @Override
            public void mouseExited(MouseEvent event){
                JButton buton = (JButton)event.getSource();
                buton.setContentAreaFilled(false); //when mouse is not on button then it will look the same.
            }
        });

        fromCSV.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/usanca/utils/mbus/gui/Res/loadPro.png"))); // NOI18N
        fromCSV.setText("<html><center>Cargar<br><center>lista</html>");
        fromCSV.setToolTipText("Carga una lista de medidores previamente guardada en un fichero CSV");
        fromCSV.setActionCommand("Cargar");
        fromCSV.setBorderPainted(false);
        fromCSV.setContentAreaFilled(false);
        fromCSV.setDebugGraphicsOptions(javax.swing.DebugGraphics.NONE_OPTION);
        fromCSV.setDoubleBuffered(true);
        fromCSV.setFocusPainted(false);
        fromCSV.setFocusable(false);
        fromCSV.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        fromCSV.setIconTextGap(0);
        fromCSV.setMargin(new java.awt.Insets(4, 2, 0, 2));
        fromCSV.setMaximumSize(new java.awt.Dimension(100, 100));
        fromCSV.setMinimumSize(new java.awt.Dimension(43, 55));
        fromCSV.setPreferredSize(new java.awt.Dimension(57, 40));
        fromCSV.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        fromCSV.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fromCSVActionPerformed(evt);
            }
        });

        toCSV.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/usanca/utils/mbus/gui/Res/savePro.png"))); // NOI18N
        toCSV.setText("<html><center>Guardar<br><center>lista</html>");
        toCSV.setToolTipText("Guarda la lista actual de medidores a un fichero .CSV");
        toCSV.setBorderPainted(false);
        toCSV.setContentAreaFilled(false);
        toCSV.setDebugGraphicsOptions(javax.swing.DebugGraphics.NONE_OPTION);
        toCSV.setDoubleBuffered(true);
        toCSV.setFocusPainted(false);
        toCSV.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        toCSV.setIconTextGap(0);
        toCSV.setMargin(new java.awt.Insets(4, 2, 0, 2));
        toCSV.setMaximumSize(new java.awt.Dimension(100, 100));
        toCSV.setMinimumSize(new java.awt.Dimension(43, 54));
        toCSV.setPreferredSize(new java.awt.Dimension(57, 40));
        toCSV.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toCSV.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toCSVActionPerformed(evt);
            }
        });

        jMenuBar1.setBackground(new java.awt.Color(204, 204, 204));
        jMenuBar1.setBorder(null);
        jMenuBar1.setBorderPainted(false);

        jMenuArchivo.setText("Archivo");

        jMenuCargarSite.setText("Cargar");
        jMenuCargarSite.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuCargarSiteActionPerformed(evt);
            }
        });
        jMenuArchivo.add(jMenuCargarSite);

        jMenuGuardarSite.setText("Guardar");
        jMenuGuardarSite.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuGuardarSiteActionPerformed(evt);
            }
        });
        jMenuArchivo.add(jMenuGuardarSite);

        jMenuBar1.add(jMenuArchivo);

        jMenuContadores.setText("Contadores");

        jMenuPrimaria.setText("Busqueda primaria");
        jMenuPrimaria.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuPrimariaActionPerformed(evt);
            }
        });
        jMenuContadores.add(jMenuPrimaria);

        jMenuSecundaria.setText("Busqueda secundaria");
        jMenuSecundaria.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuSecundariaActionPerformed(evt);
            }
        });
        jMenuContadores.add(jMenuSecundaria);

        jCheckBoxHexadecimal.setSelected(true);
        jCheckBoxHexadecimal.setText("Incluir parte hexadecimal");
        jCheckBoxHexadecimal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxHexadecimalActionPerformed(evt);
            }
        });
        jMenuContadores.add(jCheckBoxHexadecimal);
        jMenuContadores.add(jSeparator2);

        jMenuAñadir.setText("Añadir manualmente");
        jMenuAñadir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuAñadirActionPerformed(evt);
            }
        });
        jMenuContadores.add(jMenuAñadir);

        jMenuEliminar.setText("Eliminar seleccionado(s)");
        jMenuEliminar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuEliminarActionPerformed(evt);
            }
        });
        jMenuContadores.add(jMenuEliminar);

        jMenuBar1.add(jMenuContadores);

        jMenuLecturas.setText("Lecturas");

        jMenuLeerSeleccionados.setText("Leer seleccionados");
        jMenuLeerSeleccionados.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuLeerSeleccionadosActionPerformed(evt);
            }
        });
        jMenuLecturas.add(jMenuLeerSeleccionados);

        jMenuLeerTodos.setText("Leer todos");
        jMenuLeerTodos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuLeerTodosActionPerformed(evt);
            }
        });
        jMenuLecturas.add(jMenuLeerTodos);

        jMenuExportar.setText("Exportar a Excel");
        jMenuExportar.setEnabled(false);
        jMenuExportar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuExportarActionPerformed(evt);
            }
        });
        jMenuLecturas.add(jMenuExportar);

        jMenuBar1.add(jMenuLecturas);

        jMenuAyuda.setText("Ayuda");
        jMenuAyuda.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuAyudaActionPerformed(evt);
            }
        });

        jMenuAcercade.setText("Acerca de");
        jMenuAcercade.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuAcercadeActionPerformed(evt);
            }
        });
        jMenuAyuda.add(jMenuAcercade);

        jMenuBar1.add(jMenuAyuda);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jProgressBar1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jSplitPane2)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(jPanelStream, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 31, Short.MAX_VALUE)
                        .addComponent(fromCSV, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(toCSV, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(150, 150, 150)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanelStream, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(fromCSV, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(toCSV, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSplitPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 464, Short.MAX_VALUE)
                .addContainerGap())
        );

        fromCSV.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseEntered(MouseEvent event){
                JButton buton = (JButton)event.getSource();
                buton.setContentAreaFilled(true);  //when hoovered it will show borders and fill area.
            }

            @Override
            public void mouseExited(MouseEvent event){
                JButton buton = (JButton)event.getSource();
                buton.setContentAreaFilled(false); //when mouse is not on button then it will look the same.
            }
        });
        toCSV.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseEntered(MouseEvent event){
                JButton buton = (JButton)event.getSource();
                buton.setContentAreaFilled(true);  //when hoovered it will show borders and fill area.
            }

            @Override
            public void mouseExited(MouseEvent event){
                JButton buton = (JButton)event.getSource();
                buton.setContentAreaFilled(false); //when mouse is not on button then it will look the same.
            }
        });

        getAccessibleContext().setAccessibleName("MBUS GUI");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jTextFieldDataBitsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldDataBitsActionPerformed
        updateStreamProps();
    }//GEN-LAST:event_jTextFieldDataBitsActionPerformed

    private void primarySearchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_primarySearchButtonActionPerformed
        runPrimarySearch();
    }//GEN-LAST:event_primarySearchButtonActionPerformed

    private void toCSVActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toCSVActionPerformed
        saveDevices2CSVAction();
    }//GEN-LAST:event_toCSVActionPerformed

    private void fromCSVActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fromCSVActionPerformed
        loadFromCSVAction();
    }//GEN-LAST:event_fromCSVActionPerformed

    private void jTextFieldTimeOutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldTimeOutActionPerformed
        updateStreamProps();
    }//GEN-LAST:event_jTextFieldTimeOutActionPerformed

    private void readButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_readButtonActionPerformed
        readDevices(jTableDevices.getSelectedRows());
    }//GEN-LAST:event_readButtonActionPerformed

    private void secondarySearchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_secondarySearchButtonActionPerformed
        runSecondarySearch();
    }//GEN-LAST:event_secondarySearchButtonActionPerformed

    private void stopButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopButtonActionPerformed
        stopSearch();
        stopRead();
    }//GEN-LAST:event_stopButtonActionPerformed

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        addDeviceManually();
    }//GEN-LAST:event_addButtonActionPerformed

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        deleteSelectedDevices();
    }//GEN-LAST:event_removeButtonActionPerformed

    private void exportButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportButtonActionPerformed
        if (proVersion) {
            export();
        } else {
            pleaseRegister();
        }
    }//GEN-LAST:event_exportButtonActionPerformed

    private void jMenuCargarSiteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuCargarSiteActionPerformed
        loadFromCSVAction();
    }//GEN-LAST:event_jMenuCargarSiteActionPerformed

    private void jMenuPrimariaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuPrimariaActionPerformed
        runPrimarySearch();
    }//GEN-LAST:event_jMenuPrimariaActionPerformed

    private void jMenuGuardarSiteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuGuardarSiteActionPerformed
        saveDevices2CSVAction();
    }//GEN-LAST:event_jMenuGuardarSiteActionPerformed

    private void jMenuSecundariaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuSecundariaActionPerformed
        runSecondarySearch();
    }//GEN-LAST:event_jMenuSecundariaActionPerformed

    private void jMenuAñadirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuAñadirActionPerformed
        addDeviceManually();
    }//GEN-LAST:event_jMenuAñadirActionPerformed

    private void jMenuEliminarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuEliminarActionPerformed
        deleteSelectedDevices();
    }//GEN-LAST:event_jMenuEliminarActionPerformed

    private void jMenuLeerSeleccionadosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuLeerSeleccionadosActionPerformed
//        readSelected();
        readDevices(jTableDevices.getSelectedRows());
    }//GEN-LAST:event_jMenuLeerSeleccionadosActionPerformed

    private void jMenuAyudaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuAyudaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jMenuAyudaActionPerformed

    private void jMenuAcercadeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuAcercadeActionPerformed

        JOptionPane.showMessageDialog(this, acercadePanel, "Acerca de " + programTitle, JOptionPane.PLAIN_MESSAGE, null);


    }//GEN-LAST:event_jMenuAcercadeActionPerformed

    private void jMenuExportarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuExportarActionPerformed
        export();
    }//GEN-LAST:event_jMenuExportarActionPerformed

    private void jMenuLeerTodosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuLeerTodosActionPerformed

        int totalRows = jTableDevices.getRowCount();
        int[] allRows = new int[totalRows];
        for (int i = 0; i < allRows.length; i++) {
            allRows[i] = i;
        }

        readDevices(allRows);
    }//GEN-LAST:event_jMenuLeerTodosActionPerformed

    private void jCheckBoxHexadecimalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxHexadecimalActionPerformed
        includeHexadecimal = jCheckBoxHexadecimal.getState();

    }//GEN-LAST:event_jCheckBoxHexadecimalActionPerformed

    private void jComboBoxParidadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxParidadActionPerformed
        updateStreamProps();
    }//GEN-LAST:event_jComboBoxParidadActionPerformed

    private void jComboBoxBaudiosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxBaudiosActionPerformed
        updateStreamProps();
    }//GEN-LAST:event_jComboBoxBaudiosActionPerformed

    private void jComboBoxPortsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxPortsActionPerformed


    }//GEN-LAST:event_jComboBoxPortsActionPerformed

    private void toCSV1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toCSV1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_toCSV1ActionPerformed

    private void jCheckBoxAutoReadEnabledActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxAutoReadEnabledActionPerformed
        boolean enabled = false;
        if (proVersion) {
            enabled = jCheckBoxAutoReadEnabled.isSelected();
        } else {
            jCheckBoxAutoReadEnabled.setSelected(false);
            pleaseRegister();
        }

        jPanelDate.setEnabled(true);
        Component[] jPanelDateComps = jPanelDate.getComponents();
        for (Component jPanelDateComp : jPanelDateComps) {
            jPanelDateComp.setEnabled(enabled);
        }
    }//GEN-LAST:event_jCheckBoxAutoReadEnabledActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */

        try {

            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {

                    javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());

                    //javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    UIManager.getLookAndFeelDefaults().put("defaultFont", new Font("Tahoma", Font.PLAIN, 11));
                    break;
                }
            }

        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MbusGUIpro.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MbusGUIpro.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MbusGUIpro.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MbusGUIpro.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MbusGUIpro().setVisible(true);
            }
        });

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {

            }
        }));
    }

    private void updateTable(ArrayList<MBusDevice> list) {
        tableModelDevices.setDevicesList(list);
        devSelected = null;
    }

    private void updateStreamProps() {
        streamProps.setBaudRate(Integer.parseInt(jComboBoxBaudios.getModel().getSelectedItem().toString()));
        streamProps.setIntParity(jComboBoxParidad.getSelectedIndex());
        streamProps.setDataBits(Integer.parseInt(jTextFieldDataBits.getText()));
        streamProps.setStopBits(Integer.parseInt(jTextFieldStopBits.getText()));
        streamProps.setTimeOut(Integer.parseInt(jTextFieldTimeOut.getText()));
        streamProps.setCOM(jComboBoxPorts.getSelectedItem().toString());
    }

    private void updateJPanelStream() {
        String bypassCOM = streamProps.getCOM();
        int bypassBaudRate = streamProps.getBaudRate();
        int bypassDataBits = streamProps.getDataBits();
        int bypassStopBits = streamProps.getStopBits();
        int bypassTimeout = streamProps.getTimeOut();
        int bybassParity = streamProps.getIntParity();

        jComboBoxBaudios.getModel().setSelectedItem(bypassBaudRate);
        jTextFieldDataBits.setText(bypassDataBits + "");
        jTextFieldStopBits.setText(bypassStopBits + "");
        jTextFieldTimeOut.setText(bypassTimeout + "");
        jComboBoxPorts.setSelectedItem(bypassCOM);
        if (!bypassCOM.equals(jComboBoxPorts.getSelectedItem().toString())) {
            System.out.println("El puerto com que se ha intentado asignar al combobox no existe en el combobox");
            Printer.out("Advertencia: el puerto de comunicaciones " + bypassCOM + " no está disponible");
            bypassCOM = jComboBoxPorts.getSelectedItem().toString();
        }
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // ATENTION! HORRIBLE BUG
        // For whatever reason when jComboBoxParidad.setSelectedIndex(parity); is executed the streamProps.COM variable gets changed!!!
        // So in order to bypass it I replace the later COM with the former
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////                        
        jComboBoxParidad.setSelectedIndex(bybassParity);

        streamProps.setCOM(bypassCOM);
        streamProps.setBaudRate(bypassBaudRate);
        streamProps.setDataBits(bypassDataBits);
        streamProps.setStopBits(bypassStopBits);
        streamProps.setTimeOut(bypassTimeout);
        streamProps.setIntParity(bybassParity);
    }

    public void loadFromCSVAction() {
        tableModelDevices.clearDevices();
        updateCOMPorts();

        int returnVal = fc.showOpenDialog(MbusGUIpro.this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            devFile = fc.getSelectedFile();
            loadDevicesFromCSV();
        }
    }

    public void loadDevicesFromCSV() {
        csvReader.readCSV(devFile);
        try {
            devices = csvReader.getDevices();
            streamProps = csvReader.getStreamProps();
            Printer.out("Cargando lista del archivo '" + devFile.getName() + "'");

        } catch (Exception e) {
            System.out.println(this + "Error al recibir la lista de dispositivos que viene del CSV: " + e.getMessage() + "\n" + e.toString());
            Printer.out(this + "Error al recibir la lista de dispositivos que viene del CSV: " + e.getMessage());
        }

        updateJPanelStream();
        updateTable(devices);
    }

    public void saveDevices2CSVAction() {
        int returnVal = fc.showSaveDialog(MbusGUIpro.this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            devFile = fc.getSelectedFile();
            if (!devFile.getName().endsWith(".csv")) {
                extension = ".csv";
            } else {
                extension = "";
            }
            saveDevices2CSV();
        }
    }

    public void saveDevices2CSV() {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(devFile + extension));

            out.write("puerto,baudios,databits,paridad,stopbits,timeout\n");

            out.write(streamProps.getCOM() + "," + streamProps.getBaudRate() + "," + streamProps.getDataBits() + ","
                    + streamProps.getParity() + "," + streamProps.getStopBits() + "," + streamProps.getTimeOut() + "\n");

            out.write("nDispositvo,ID Primaria,ID Secundaria,Notas\n");
            devices = tableModelDevices.getDevicesList();

            int numDevs = devices.size();

            String index, idp, ids, model, userNotes;
            for (int n = 0; n < numDevs; n++) {
                MBusDevice d = devices.get(n);

                index = Integer.toString(n);
                idp = Integer.toString(d.getPAddress());
                ids = Integer.toString(d.getSAddress());
                userNotes = d.getUserReminder();
                if (n == 0) {
                    Printer.out(userNotes);
                }

                out.write(index + "," + idp + "," + ids + "," + userNotes);
                out.write("\n");
                Printer.out(index + "," + idp + "," + ids + "," + userNotes);
            }

            out.close();
        } catch (Exception e) {
            System.out.println("ERROR Salvando el archivo!!!");
        }
    }

    public void runPrimarySearch() {
        primarySearchButton.setEnabled(false);
        secondarySearchButton.setEnabled(false);
        stopButton.setEnabled(true);
        readButton.setEnabled(false);
        jProgressBar1.setString(null);
        tableModelDevices.clearDevices();
        mFinder = new MBusFinder(tableModelDevices);
        mFinder.setStreamProperties(streamProps);
        mFinder.setPrimary();
        mFinder.addPropertyChangeListener(this);
        mFinder.execute();
    }

    public void runSecondarySearch() {
        primarySearchButton.setEnabled(false);
        secondarySearchButton.setEnabled(false);
        stopButton.setEnabled(true);
        readButton.setEnabled(false);
        devices.clear();
        mFinder = new MBusFinder(tableModelDevices);
        mFinder.setStreamProperties(streamProps);
        mFinder.setSecondary();
        mFinder.addPropertyChangeListener(this);
        mFinder.setIncludeHexadecimal(includeHexadecimal);
        mFinder.execute();
    }

    public void addDeviceManually() {
        MBusDevice newDev = new MBusDevice(-1, -1);
        newDev.setUserReminder(inewdev + " Dispositivo añadido manualmente");
        inewdev++;
        tableModelDevices.addDevice(newDev);
    }

    public void deleteSelectedDevices() {
        int[] selectedRows = jTableDevices.getSelectedRows();
        try {
            int[] convertedRows = new int[selectedRows.length];
            for (int c = 0; c < convertedRows.length; c++) {
                int convertedRow = jTableDevices.convertRowIndexToModel(selectedRows[c]);
                convertedRows[c] = convertedRow;
            }
            if (convertedRows.length > 0) {
                tableModelDevices.removeDevices(convertedRows);
            }
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Error al pulsar el botón de eliminar elemento.\nNo hay mas elementos en la tabla. " + e.getMessage());
        }
    }

    public void readDevices(int[] selectedRows) {
        if (selectedRows.length > 0) {
            addButton.setEnabled(false);
            removeButton.setEnabled(false);
            stopButton.setEnabled(true);
            primarySearchButton.setEnabled(false);
            secondarySearchButton.setEnabled(false);

            mReader = new MBusReader();
            mReader.addPropertyChangeListener(this);
            tableModelReadings = (DefaultTableModel) jTableReadings.getModel();
            tableModelReadings.addTableModelListener(this);
            tableModelReadings.setRowCount(0);
            mReader.setTableModel(tableModelReadings);

            int row;
            List<MBusDevice> devList = new ArrayList<MBusDevice>();

            if (selectedRows.length > 1) {
                if (proVersion) {
                    Printer.out(" Leyendo dispositivos seleccionados :");
                    for (int devNum = 0; devNum < selectedRows.length; devNum++) {
                        row = selectedRows[devNum];
                        if (row != -1) {
                            int rowModel = jTableDevices.convertRowIndexToModel(row);

                            MBusDevice dev2read = tableModelDevices.getDevicesList().get(rowModel);
                            devList.add(dev2read);
                            Printer.out(" " + dev2read.getAddress2Read());

                        } else {
                            Printer.out("No hay un dispositivo seleccionado.");
                        }
                    }
                } else {
                    pleaseRegister();
                    Printer.out(" Version gratuita, Lectura limitada al primer dispositivo :");
                    int devNum = 0;
                    row = selectedRows[devNum];
                    if (row != -1) {
                        int rowModel = jTableDevices.convertRowIndexToModel(row);

                        MBusDevice dev2read = tableModelDevices.getDevicesList().get(rowModel);
                        devList.add(dev2read);
                        Printer.out(" " + dev2read.getAddress2Read());

                    } else {
                        Printer.out("No hay un dispositivo seleccionado.");
                    }
                }
            }

            if (selectedRows.length == 1) {
                int devNum = 0;
                row = selectedRows[devNum];
                if (row != -1) {
                    int rowModel = jTableDevices.convertRowIndexToModel(row);

                    MBusDevice dev2read = tableModelDevices.getDevicesList().get(rowModel);
                    devList.add(dev2read);
                    Printer.out(" " + dev2read.getAddress2Read());

                } else {
                    Printer.out("No hay un dispositivo seleccionado.");
                }
            }

            mReader.openConnection(streamProps);
            mReader.setDeviceList(devList);
            mReader.execute();

            tableModelReadings.fireTableDataChanged();

            for (int i = 0; i < tableModelReadings.getRowCount(); i++) {
                String aString = "";
                for (int q = 0; q < tableModelReadings.getColumnCount(); q++) {
                    Object o = tableModelReadings.getValueAt(i, q);
                    aString += o.toString();
                }
                Printer.out(aString);
            }
        }
    }

    public void stopSearch() {
        if (mFinder != null) {
            mFinder.cancel(true);
        }
        primarySearchButton.setEnabled(true);
        secondarySearchButton.setEnabled(true);
        stopButton.setEnabled(false);
    }

    public void stopRead() {
        if (mReader != null) {
            mReader.cancel(true);
            System.out.println("mReader.cancel(true) llamado");
        }

        primarySearchButton.setEnabled(true);
        secondarySearchButton.setEnabled(true);
        addButton.setEnabled(true);
        removeButton.setEnabled(true);
        stopButton.setEnabled(false);
    }

    public void export() {
        if (proVersion) {
            ExcelExporter exporter = new ExcelExporter();
            try {
                exporter.exportTable(tableModelReadings, this);
            } catch (WriteException ex) {
                Logger.getLogger(MbusGUIpro.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            pleaseRegister();
        }
    }

    public void updateCOMPorts() {
        // Updates ports available
        String selected = jComboBoxPorts.getSelectedItem().toString();
        String[] ports = CommPortFinder.find();
        jComboBoxPorts.setModel(new DefaultComboBoxModel<String>(ports));
        jComboBoxPorts.setSelectedItem(selected);   // maintain the selected element if possible        
    }

    public void pleaseRegister() {
        JOptionPane.showMessageDialog(this, registerPanel, "Register to full version", JOptionPane.PLAIN_MESSAGE, null);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel acercadeLabel;
    private javax.swing.JPanel acercadePanel;
    private javax.swing.JButton addButton;
    private javax.swing.JButton exportButton;
    private javax.swing.JButton fromCSV;
    private javax.swing.JCheckBox jCheckBoxAutoReadEnabled;
    private javax.swing.JCheckBoxMenuItem jCheckBoxHexadecimal;
    private javax.swing.JComboBox<String> jComboBoxBaudios;
    private javax.swing.JComboBox<String> jComboBoxParidad;
    private javax.swing.JComboBox<String> jComboBoxPorts;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabelBaudios;
    private javax.swing.JLabel jLabelCOM;
    private javax.swing.JLabel jLabelDataBits;
    private javax.swing.JLabel jLabelParidad;
    private javax.swing.JLabel jLabelStopBits;
    private javax.swing.JLabel jLabelTimeOut;
    private javax.swing.JMenuItem jMenuAcercade;
    private javax.swing.JMenu jMenuArchivo;
    private javax.swing.JMenu jMenuAyuda;
    private javax.swing.JMenuItem jMenuAñadir;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuCargarSite;
    private javax.swing.JMenu jMenuContadores;
    private javax.swing.JMenuItem jMenuEliminar;
    private javax.swing.JMenuItem jMenuExportar;
    private javax.swing.JMenuItem jMenuGuardarSite;
    private javax.swing.JMenu jMenuLecturas;
    private javax.swing.JMenuItem jMenuLeerSeleccionados;
    private javax.swing.JMenuItem jMenuLeerTodos;
    private javax.swing.JMenuItem jMenuPrimaria;
    private javax.swing.JMenuItem jMenuSecundaria;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanelDate;
    private javax.swing.JPanel jPanelStream;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JSpinner jSpinnerAutoReadBaseTime;
    private javax.swing.JSpinner jSpinnerReadBaseDay;
    private javax.swing.JSpinner jSpinnerReadPeriod;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JTable jTableDevices;
    private javax.swing.JTable jTableReadings;
    private javax.swing.JTextArea jTextAreaLog;
    private javax.swing.JTextField jTextFieldDataBits;
    private javax.swing.JTextField jTextFieldStopBits;
    private javax.swing.JTextField jTextFieldTimeOut;
    private javax.swing.JButton primarySearchButton;
    private javax.swing.JLabel programTitleLabel;
    private javax.swing.JButton readButton;
    private javax.swing.JPanel registerPanel;
    private javax.swing.JButton removeButton;
    private javax.swing.JButton secondarySearchButton;
    private javax.swing.JButton stopButton;
    private javax.swing.JPanel timePickerPanel;
    private javax.swing.JButton toCSV;
    private javax.swing.JButton toCSV1;
    private javax.swing.JLabel versionLabel;
    // End of variables declaration//GEN-END:variables

    private void getIcon() {
        java.net.URL url = ClassLoader.getSystemResource("com/usanca/utils/mbus/gui/Res/appIcon.png");
        setIconImage(Toolkit.getDefaultToolkit().getImage(url));
    }

}
