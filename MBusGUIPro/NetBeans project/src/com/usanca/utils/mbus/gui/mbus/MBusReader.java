/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.usanca.utils.mbus.gui.mbus;

import com.usanca.utils.mbus.gui.extras.Printer;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.openmuc.jmbus.common.DLMSUnit;
import org.openmuc.jmbus.MBusASAP;
import org.openmuc.jmbus.common.Util;
import org.openmuc.jmbus.application.AbstractHeader;
import org.openmuc.jmbus.application.AbstractPayload;
import org.openmuc.jmbus.application.ApplicationMessage;
import org.openmuc.jmbus.application.MBUSLongHeader;
import org.openmuc.jmbus.application.VariableDataBlock;
import org.openmuc.jmbus.application.VariableDataBlockPayload;
import com.usanca.utils.mbus.gui.types.MBusDevice;
import com.usanca.utils.mbus.gui.types.MBusStreamProperties;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Pablo Monteserin Garcia. pablomonteserin@gmail.com
 */
public class MBusReader extends SwingWorker<Void, Result2Table> {

    //Connection related
    MBusASAP connection = null;
    private String serialDevice, commStr;
    private int timeOut;
    private DefaultTableModel tableModel;
    List<MBusDevice> devList;

    public void setDeviceList(List<MBusDevice> list) {
        devList = list;
    }

    @Override
    protected Void doInBackground() throws Exception {
        readMultiple();
        Printer.out("-- Lectura finalizada --");

        return null;
    }

    @Override
    protected void process(List<Result2Table> partialResult) {
        for (Result2Table result2table : partialResult) {
            for (List<String> fila : result2table.result) {
                tableModel.addRow(new Object[]{result2table.id, result2table.readingDate, result2table.adress, fila.get(0), fila.get(1), fila.get(2), fila.get(3), fila.get(4), fila.get(5)});
                tableModel.fireTableDataChanged();
            }
        }
    }

    @Override
    protected void done() {

    }

    public void readMultiple() {
        if (connection != null) {
            setProgress(0);
            Result2Table devResult;
            int devCount = 0;

            for (MBusDevice dev : devList) {
                devResult = readDevice(dev);
                devResult.id = devCount;

                DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                Date date = new Date();
                devResult.readingDate = dateFormat.format(date);

                publish(devResult);

                devCount++;
                float total = devList.size();
                float fdevCount = devCount;
                setProgress((int) (100f * fdevCount / total));

                if (isCancelled()) {
                    System.out.println("Cancelado por el usuario");
                    Printer.out("Cancelado por el usuario");
                    setProgress(100);
                    break;
                }

                try {
                    Thread.sleep(1000L);
                } catch (Exception e) {
                }
            }
        }

        if (connection != null) {
            connection.close();
            connection = null;
        } else {
            Printer.out("No hay conexión abierta");
            System.out.println("No hay conexión abierta");
        }
    }

    Result2Table readDevice(MBusDevice device) {
        List<List<String>> result;
        result = new ArrayList<List<String>>();
        Result2Table result2table = new Result2Table();

        if (connection != null) {

            int deviceAddress = device.getPAddress();
            String address;
            String genericAddress;
            if (deviceAddress == -1) {
                deviceAddress = device.getSAddress();
                Printer.out("Leyendo dirección secundaria = " + deviceAddress);
                genericAddress = device.getSAddress()+"";
                address = "s" + device.getSAddress();
            } else {
                Printer.out("Leyendo dirección primaria = " + deviceAddress);
                genericAddress = device.getSAddress()+"";
                address = "p" + device.getPAddress();
            }
            
            try {
                ApplicationMessage apdu = connection.getUserData(address);
                Printer.out("Lectura completada");

                AbstractPayload payload = apdu.getBody();
                AbstractHeader header = apdu.getHeader();

                if (header instanceof MBUSLongHeader) {
                    Short manufacurerID = ((MBUSLongHeader) header).getLongAddress().getManufacturerID();
                    Printer.out("Vendor ID: " + Util.vendorID(manufacurerID) + "\n");
                }

                if (payload instanceof VariableDataBlockPayload) {

                    List<VariableDataBlock> blocks = ((VariableDataBlockPayload) payload).getVariableDataBlocks();

                    for (VariableDataBlock dataBlock : blocks) {
                        List<String> fila = new ArrayList<String>();
                        result.add(fila);
                        fila.add(Util.composeHexStringFromByteArray(dataBlock.getDIB()));
                        fila.add(Util.composeHexStringFromByteArray(dataBlock.getVIB()));
                        try {
                            dataBlock.parse();
                            if (dataBlock.getDescription() != null) {
                                fila.add(dataBlock.getFullDescriptionStr());
                                fila.add(dataBlock.getFunctionField().toString());
                                String svalue = dataBlock.getData().toString();
                                boolean isInt = false;
                                int ivalue = 0;
                                try {
                                    ivalue = Integer.parseInt(svalue);
                                    isInt = true;
                                } catch (Exception e) {
                                    //System.out.println(this+" Fallo al parsear el dato en svalue "+svalue);
                                }

                                Integer scale = (int) dataBlock.getScaler();
                                if ((isInt) && (scale != 0)) {
                                    svalue = "" + ivalue * Math.pow(10, scale);
                                }
                                fila.add(svalue);
                                fila.add(DLMSUnit.getString(dataBlock.getUnit()));
                            }
                        } catch (ParseException e) {
                            System.out.println("Fallo en el analisis sintactico ( parsing ): " + e.getMessage());
                            Printer.out("Fallo en el analisis sintactico ( parsing ): " + e.getMessage());
                        }
                        while (fila.size() < 6) {
                            fila.add("");
                        }
                    }
                }

                result2table.result = result;                

            } catch (IOException ioEx) {
                System.out.println("\tERROR: Excepcion de E/S leyendo el contador...");
                Printer.out("\tERROR: Excepcion de E/S leyendo el contador...");
            } catch (TimeoutException toEx) {
                System.out.println("\tERROR: Timeout esperando la respuesta del contador");
                Printer.out("\tERROR: Timeout esperando la respuesta del contador");
            } finally {
                if (result.isEmpty())
                {   
                    List<String> warningFila = new ArrayList<String>();
                    for (int c = 0; c<6; c++)
                    {
                        warningFila.add("fallo");
                    }
                    result.add(warningFila);
                    result2table.result = result;
                }
                
                result2table.adress = genericAddress;
            }
        } else {
            Printer.out("No hay conexión abierta");
        }
        return result2table;

    }

    public void openConnection(MBusStreamProperties s) {
        this.serialDevice = s.getCOM();
        this.timeOut = s.getTimeOut();
        this.commStr = s.getConnString();

        connection = null; //deberíamos hacer un conection.close() si es que no es null?
        try {
            Printer.out("Abriendo la conexion : " + serialDevice + " " + commStr + " " + timeOut);
            connection = new MBusASAP(serialDevice, commStr, timeOut);
        } catch (IOException e) {
            Printer.out("ERROR: Estableciendo la conexion MBUS....");
            System.out.println("ERROR: Estableciendo la conexion MBUS....");
        } catch (UnsatisfiedLinkError e) {
            Printer.out("ERROR: No puede cargarse la librería, existe el directorio /lib ?");
            System.out.println("ERROR: No puede cargarse la librería, existe el directorio /lib ?");
        }

    }

    /**
     * @param tableModel the tableModel to set
     */
    public void setTableModel(DefaultTableModel tableModel) {
        this.tableModel = tableModel;
    }
}

class Result2Table {

    public int id;
    public String readingDate;
    public String adress;
    public List<List<String>> result;
}
