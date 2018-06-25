/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.usanca.utils.mbus.gui.mbus;

import com.usanca.utils.mbus.gui.extras.Printer;
import com.usanca.utils.mbus.gui.mbusguipro.TableModelDevices;
import java.io.IOException;
import java.util.ArrayList;
import org.openmuc.jmbus.MBusASAP;
import com.usanca.utils.mbus.gui.types.MBusDevice;
import com.usanca.utils.mbus.gui.types.MBusStreamProperties;
import java.util.List;
import javax.swing.SwingWorker;

/**
 *
 * @author Pablo Monteserin Garcia. pablomonteserin@gmail.com
 */
public class MBusFinder extends SwingWorker<Void, Integer> {

    private ArrayList<MBusDevice> devList;
    private TableModelDevices table;
    private boolean isPrimary = true;
    private MBusStreamProperties finderStreamProps;
    private int lastDigit2check = 0x10;

    public MBusFinder(TableModelDevices table) {
        this.devList = devList;
        this.table = table;
    }

    @Override
    protected Void doInBackground() {
        if (isPrimary) {
            findPrimary();
        } else {
            findSecondary();
        }
        Printer.out("-- Busqueda finalizada --");

        return null;
    }

    @Override
    protected void process(List<Integer> deviceNums) {
        while (deviceNums.size() > 0) {
            Integer deviceNum = deviceNums.remove(0);
            MBusDevice dev = new MBusDevice();
            if (isPrimary) {
                dev.setPAddress(deviceNum);
            } else {
                dev.setSAddress(deviceNum);
            }
            table.addDevice(dev);
        }
    }

    @Override
    protected void done() {
    }

    public void setPrimary() {
        isPrimary = true;
    }

    public void setSecondary() {
        isPrimary = false;
    }
    
    public void setIncludeHexadecimal(boolean include)
    {
        if (include == false) 
        {
            lastDigit2check = 0xA;
        }
        else {
            lastDigit2check = 0x10;
        }
    }

    public void setStreamProperties(MBusStreamProperties ss) {
        finderStreamProps = ss;
    }

    public void findPrimary() {
        setProgress(0);

        String d, cs;
        int timeOut;

        timeOut = finderStreamProps.getTimeOut();
        d = finderStreamProps.getCOM();
        cs = finderStreamProps.getConnString();

        Printer.out("Iniciando escaneo en " + d + " con los parámetros de conexion " + cs + " y un timeout de " + timeOut);
        Printer.out("Abriendo la conexion...en el dispositivo:" + d + " con los parámetros " + cs);


        //tries to open connection
        MBusASAP connection = null;
        int addr = 0;

        try {
            connection = new MBusASAP(d, cs, timeOut);
            try {
                for (addr = 1; addr < 253; addr++) {
                    if (this.isCancelled()) {
                        System.out.println("Cancelado por el usuario");
                        Printer.out("Cancelado por el usuario");
                        break;
                    }
                    // swingworker.cancel(true) is called from outside when pressed stop button
                    Printer.out("intentando el dispositivo: " + addr);

                    if (connection.checkBusActivity("p" + addr)) {
                        Printer.out("Encontrada coincidencia--->" + addr);
                        publish(new Integer(addr));
                    }
                    setProgress((int) (100 / 252f * addr));
                }
                setProgress(100);
            } catch (IOException ioe) {
                System.out.println(this + "ERROR De entrada salida comprobando la direccion primaria" + addr + ": " + ioe.getMessage() + "\n" + ioe.toString());
                Printer.out("ERROR De entrada salida comprobando la direccion primaria" + addr + ": " + ioe.getMessage());
            }
            
        } catch (IOException ioe) {
            //TODO: Hacer menos agresivo
            System.out.println("ERROR Abriendo la conexion: " + ioe.getMessage() + "\n" + ioe.toString());
            Printer.out("ERROR Abriendo la conexion: " + ioe.getMessage());

        } finally {
            if (connection != null) {
                connection.close();
                connection = null;
            }
        }


    }

    /*if (debug) {
     connection.setMsgDump(true);
     }*/
    /*    
     if (line.hasOption("delzeros")) {
     connection.setLeadingZeroRemoval(true);
     }*/
    
    public void findSecondary() {
        setProgress(0);

        String d, cs;
        int timeOut;

        timeOut = finderStreamProps.getTimeOut();
        d = finderStreamProps.getCOM();
        cs = finderStreamProps.getConnString();

        Printer.out("Iniciando escaneo en " + d + " con los parámetros de conexion " + cs + " y un timeout de " + timeOut);
        Printer.out("Abriendo la conexion...en el dispositivo:" + d + " con los parámetros " + cs);


        //tries to open connection
        MBusASAP connection = null;
        try {
            Double dval = 0d;
            connection = new MBusASAP(d, cs, timeOut);
            //connection.setMsgDump(true);
            int testNum = 0, addr2send = 0;
            try {
                for (int d1 = 0x0; d1 < lastDigit2check; d1++) {
                    if (this.isCancelled()) {
                        break;
                    }
                    // swingworker.cancel(true) is called from outside when pressed stop button
                    testNum = (d1 * 0x10000000);
                    addr2send = testNum + 0x0fffffff;
                    Printer.out(String.format("%08x", addr2send));
                    if (connection.checkBusActivity("s" + String.format("%08x", addr2send))) {
                        for (int d2 = 0x0; d2 < lastDigit2check; d2++) {
                            if (this.isCancelled()) {
                                break;
                            }
                            // swingworker.cancel(true) is called from outside when pressed stop button
                            testNum = (d1 * 0x10000000) + (d2 * 0x1000000);
                            addr2send = testNum + 0x0ffffff;
                            Printer.out(String.format("%08x", addr2send));
                            if (connection.checkBusActivity("s" + String.format("%08x", addr2send))) {
                                for (int d3 = 0x0; d3 < lastDigit2check; d3++) {
                                    if (this.isCancelled()) {
                                        break;
                                    }
                                    // swingworker.cancel(true) is called from outside when pressed stop button
                                    testNum = (d1 * 0x10000000) + (d2 * 0x01000000) + (d3 * 0x00100000);
                                    addr2send = testNum + 0x0fffff;
                                    Printer.out(String.format("%08x", addr2send));
                                    if (connection.checkBusActivity("s" + String.format("%08x", addr2send))) {
                                        for (int d4 = 0x0; d4 < lastDigit2check; d4++) {
                                            if (this.isCancelled()) {
                                                break;
                                            }
                                            // swingworker.cancel(true) is called from outside when pressed stop button
                                            testNum = (d1 * 0x10000000) + (d2 * 0x01000000) + (d3 * 0x00100000) + (d4 * 0x00010000);
                                            addr2send = testNum + 0x0ffff;
                                            Printer.out(String.format("%08x", addr2send));
                                            if (connection.checkBusActivity("s" + String.format("%08x", addr2send))) {
                                                for (int d5 = 0x0; d5 < lastDigit2check; d5++) {
                                                    if (this.isCancelled()) {
                                                        break;
                                                    }
                                                    // swingworker.cancel(true) is called from outside when pressed stop button
                                                    testNum = (d1 * 0x10000000) + (d2 * 0x01000000) + (d3 * 0x00100000) + (d4 * 0x00010000) + (d5 * 0x00001000);
                                                    addr2send = testNum + 0x0fff;
                                                    Printer.out(String.format("%08x", addr2send));
                                                    if (connection.checkBusActivity("s" + String.format("%08x", addr2send))) {
                                                        for (int d6 = 0x0; d6 < lastDigit2check; d6++) {
                                                            if (this.isCancelled()) {
                                                                break;
                                                            }
                                                            // swingworker.cancel(true) is called from outside when pressed stop button
                                                            testNum = (d1 * 0x10000000) + (d2 * 0x01000000) + (d3 * 0x00100000) + (d4 * 0x00010000) + (d5 * 0x00001000) + (d6 * 0x00000100);
                                                            addr2send = testNum + 0x0ff;
                                                            Printer.out(String.format("%08x", addr2send));
                                                            if (connection.checkBusActivity("s" + String.format("%08x", addr2send))) {
                                                                for (int d7 = 0x0; d7 < lastDigit2check; d7++) {
                                                                    if (this.isCancelled()) {
                                                                        break;
                                                                    }
                                                                    // swingworker.cancel(true) is called from outside when pressed stop button
                                                                    testNum = (d1 * 0x10000000) + (d2 * 0x01000000) + (d3 * 0x00100000) + (d4 * 0x00010000) + (d5 * 0x00001000) + (d6 * 0x00000100) + (d7 * 0x00000010);
                                                                    addr2send = testNum + 0xf;
                                                                    Printer.out(String.format("%08x", addr2send));
                                                                    if (connection.checkBusActivity("s" + String.format("%08x", addr2send))) {
                                                                        for (int d8 = 0x0; d8 < lastDigit2check; d8++) {
                                                                            if (this.isCancelled()) {
                                                                                break;
                                                                            }
                                                                            // swingworker.cancel(true) is called from outside when pressed stop button
                                                                            testNum = (d1 * 0x10000000) + (d2 * 0x01000000) + (d3 * 0x00100000) + (d4 * 0x00010000) + (d5 * 0x00001000) + (d6 * 0x00000100) + (d7 * 0x00000010) + d8;
                                                                            addr2send = testNum;
                                                                            Printer.out(String.format("%08x", addr2send));
                                                                            if (connection.checkBusActivity("s" + String.format("%08x", addr2send))) {

                                                                                Printer.out("Encontrada coincidencia--->" + String.format("%08x", addr2send));

                                                                                publish(new Integer(Integer.parseInt(String.format("%08x", addr2send))));

                                                                            } else {
                                                                                dval = dval + ((d8 + 1d) / Math.pow(16, 8)) * 100;
                                                                            }
                                                                            setProgress(dval.intValue());
                                                                        }
                                                                    } else {
                                                                        dval = dval + ((d7 + 1d) / Math.pow(16, 7)) * 100;
                                                                    }
                                                                    setProgress(dval.intValue());
                                                                }
                                                            } else {
                                                                dval = dval + ((d6 + 1d) / Math.pow(16, 6)) * 100;
                                                            }
                                                            setProgress(dval.intValue());
                                                        }
                                                    } else {
                                                        dval = dval + ((d5 + 1d) / Math.pow(16, 5)) * 100;
                                                    }
                                                    setProgress(dval.intValue());
                                                }
                                            } else {
                                                dval = dval + ((d4 + 1d) / Math.pow(16, 4)) * 100;
                                            }
                                            setProgress(dval.intValue());
                                        }
                                    } else {
                                        dval = dval + ((d3 + 1d) / Math.pow(16, 3)) * 100;
                                    }
                                    setProgress(dval.intValue());
                                }
                            } else {
                                dval = dval + ((d2 + 1d) / Math.pow(16, 2)) * 100;
                            }
                            setProgress(dval.intValue());
                        }
                    } else {
                        dval = ((d1 + 1d) / 16d) * 100;
                    }
                    setProgress(dval.intValue());
                }
            setProgress(100);

            } catch (IOException ioe) {
                System.out.println("ERROR De entrada salida buscando direcciones secundarias " + String.format("%08x", addr2send) + ": " + ioe.getMessage());
                Printer.out("ERROR De entrada salida buscando direcciones secundarias " + String.format("%08x", addr2send) + ": " + ioe.getMessage());
            }
        } catch (IOException ioe) {
            //TODO: Hacer menos agresivo
            System.out.println("ERROR Abriendo la conexion: " + ioe.getMessage());
            Printer.out("ERROR Abriendo la conexion: " + ioe.getMessage());

        } finally {
            if (connection != null) {
                connection.close();
                connection = null;
            }
        }

    }
}