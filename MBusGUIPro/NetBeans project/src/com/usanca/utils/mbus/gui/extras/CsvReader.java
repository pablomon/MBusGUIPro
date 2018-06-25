/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.usanca.utils.mbus.gui.extras;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import com.usanca.utils.mbus.gui.types.MBusDevice;
import com.usanca.utils.mbus.gui.types.MBusStreamProperties;

/**
 *
 * @author Pablo Monteserin Garcia. pablomonteserin@gmail.com
 */
public class CsvReader {

    private BufferedReader reader = null;
    private String line = "";
    private String cvsSplitBy = ",";
    private File csvFile;
    ArrayList<MBusDevice> devList = new ArrayList<>();
    private int br, pa, sa, parity;
    private String model, userNotes;
    private boolean isListRead = false;
    private MBusStreamProperties readMBusStreamProps;

    public void CsvReader() {
    }

    private MBusStreamProperties line2MBusStreamProps(String[] line) {
        String COM, par;
        int baudRate, dataBits, stopBits, timeOut;

        COM = line[0];
        baudRate = Integer.parseInt(line[1]);
        dataBits = Integer.parseInt(line[2]);
        par = line[3];
        stopBits = Integer.parseInt(line[4]);
        timeOut = Integer.parseInt(line[5]);

        MBusStreamProperties sp = new MBusStreamProperties(baudRate, par, dataBits, stopBits, COM, timeOut);
        return sp;
    }

    private MBusDevice line2MBusDevice(String[] line) {
        pa = Integer.parseInt(line[1]);
        sa = Integer.parseInt(line[2]);

        userNotes = line[3];

        MBusDevice dev = new MBusDevice();
        dev.setPAddress(pa);
        dev.setSAddress(sa);
        dev.setUserReminder(userNotes);

        return dev;
    }

    public ArrayList<MBusDevice> getDevices() throws Exception {
        if (isListRead) {
            return devList;
        }
        throw new Exception(" no se ha leido correctamente el CSV ");
    }

    public MBusStreamProperties getStreamProps() throws Exception {
        if (isListRead) {
            return readMBusStreamProps;
        }
        throw new Exception(" no se ha leido correctamente el CSV ");
    }

    public void readCSV(File f) {
        System.out.println("Leyendo fichero CSV "+f);
        csvFile = f;

        try {
            String line;
            String[] splitedLine;
            reader = new BufferedReader(new FileReader(csvFile));
            // skiping first row: connection column description 
            if ((line = reader.readLine()) != null) {  // use comma as separator
                System.out.println("Leida la primera linea de descripcion de las columnas: "+line);
                // reading connection description and generating connection object
                line = reader.readLine();
                System.out.println("La linea de descripción de la conexión es: "+line);
                splitedLine = line.split(cvsSplitBy);
                readMBusStreamProps = line2MBusStreamProps(splitedLine);
                reader.readLine(); // skiping row: device column description
                while ((line = reader.readLine()) != null) {
                    String[] deviceLine = new String[4];
                    String[] readDevLine = line.split(cvsSplitBy);
                    System.out.print("Leida linea de contador: "+line+" de longitud "+readDevLine.length);
                    for (int posS=0;posS<4;posS++) {
                        if (posS<readDevLine.length) {
                            deviceLine[posS] = readDevLine[posS];
                        } else {
                            deviceLine[posS] = "";
                        }
                    }
                    System.out.println("");
                    MBusDevice d = line2MBusDevice(deviceLine);
                    devList.add(d);
                }
                System.out.println("Cargado fichero con " + devList.size() + " dispositivos.");

            }
            isListRead = true;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
