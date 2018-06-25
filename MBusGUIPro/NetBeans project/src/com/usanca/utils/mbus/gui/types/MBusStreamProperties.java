/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.usanca.utils.mbus.gui.types;

import com.usanca.utils.mbus.gui.extras.Printer;

/**
 *
 * @author Pablo Monteserin Garcia. pablomonteserin@gmail.com Defines the data stream properties, conexión en modbus. por
 * defecto: baudRate, parity, dataBits, stopBits, serial port, timeOut en ms.
 *
 */
public class MBusStreamProperties {

    private int baudRate = 9600,
            dataBits = 8,
            stopBits = 1,
            timeOut = 10;
    private String parity = ""; // N->None, E->Even, O->Odd, M->Mark
    public int intParity = 0; // 0->Even, 1->Odd, 2->None, 3->Mark

    private String COM,
            connString,
            sTimeOut;

    public MBusStreamProperties(int baudRate, String parity, int dataBits, int stopBits, String serial, int timeOut) {
        setBaudRate(baudRate);
        setStopBits(stopBits);
        setDataBits(dataBits);
        setParity(parity);
        setCOM(serial);
        setTimeOut(timeOut);

        updateStrings();

    }

    private void updateStrings() {

        // si ejecuto esta linea se va todo al carajo
        connString = baudRate + "-" + dataBits + parity + stopBits;
        sTimeOut = Integer.toString(timeOut);

    }

    public MBusStreamProperties() {
    }

    public void Print() {
        Printer.out(" baudRate=" + baudRate + ", parity=" + parity + ", DataBits=" + dataBits
                + ", StopBits=" + stopBits + ", serial=" + COM + ", TimeOut=" + timeOut);
    }

    public String getCOM() {
        return COM;
    }

    public String getConnString() {
        updateStrings();
        return connString;
    }

    public int getTimeOut() {
        updateStrings();
        return timeOut;
    }

    public String getSTimeOut() {
        updateStrings();
        return sTimeOut;

    }

    /**
     * @return the baudRate
     */
    public int getBaudRate() {
        return baudRate;
    }

    /**
     * @param baudRate the baudRate to set
     */
    public void setBaudRate(int baudRate) {
        this.baudRate = baudRate;
    }

    /**
     * @return the dataBits
     */
    public int getDataBits() {
        return dataBits;
    }

    /**
     * @param dataBits the dataBits to set
     */
    public void setDataBits(int dataBits) {
        this.dataBits = dataBits;
    }

    /**
     * @return the stopBits
     */
    public int getStopBits() {
        return stopBits;
    }

    /**
     * @param stopBits the stopBits to set
     */
    public void setStopBits(int stopBits) {
        this.stopBits = stopBits;
    }

    /**
     * @return the parity
     */
    public String getParity() {
        return parity;
    }

    public int getIntParity() {
        int iPar;
        switch (parity) {
            case "E":
                iPar = 0;
                break;
            case "O":
                iPar = 1;
                break;
            case "N":
                iPar = 2;
                break;
            case "M":
                iPar = 3;
                break;
            default:
                System.out.println("error interpretando la paridad, devolviendo 0 por defecto");
                Printer.out("Error interpretando la paridad, devolviendo 0 por defecto");
                iPar = 0;
        }
        return iPar;
    }

    /**
     * @param parity the parity to set
     */
    public String setParity(String parity) {
        if ((!parity.contentEquals("N")) && (!parity.contentEquals("E")) && (!parity.contentEquals("O")) && (!parity.equals("M"))) {
            Printer.out("paridad inválida, usa uno de los siguientes valores: \nE = par,O = impar ,N = sin paridad ,M = marca.");
        } else {
            this.parity = parity;
        }

        return this.parity;
    }

    public void setIntParity(int iparity) {
        intParity = iparity;
        switch (iparity) {
            case 0:
                parity = "E";
                break;
            case 1:
                parity = "O";
                break;
            case 2:
                parity = "N";
                break;
            case 3:
                parity = "M";
                break;
            default:
                System.out.println("error asignando paridad, asignando even por defecto");
                Printer.out("Error al interpretar el desplegable de paridad, usando par como paridad por defecto");
                parity = "E";
        }
    }

    public void setCOM(String c) {
        this.COM = c;
    }

    public void setTimeOut(int t) {
        this.timeOut = t;
    }
}
