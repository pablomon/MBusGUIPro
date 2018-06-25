/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.usanca.utils.mbus.gui.types;

import com.usanca.utils.mbus.gui.extras.Printer;

/**
 *
 * @author Pablo Monteserin Garcia. pablomonteserin@gmail.com
 * Holds primary or secondary address of an existing mbus slave.
 * There is also a String meant for the user to write notes down. ( reminders ) 
 */

public class MBusDevice {
    private boolean marked4removal = false;
    private int pAddress=-1 , sAddress=-1;
    private String vendorID="", userReminder="", deviceModel="";
    
    public MBusDevice(){
    }
    public MBusDevice(int pa, int sa) {
        pAddress = pa;
        sAddress = sa;
    }

    public String getAddress2Read()
    {
        if (pAddress != -1) return "p"+pAddress;
        else return "s"+sAddress;
    }
    /**
     * @return the pAddress
     */
    public int getPAddress() {
        return pAddress;
    }

    /**
     * @param pAddress the pAddress to set
     */
    public void setPAddress(int pAddress) {
        this.pAddress = pAddress;
    }
    public void setPAddress(String pAddress) {
        this.pAddress = Integer.parseInt(pAddress);
    }

    /**
     * @return the sAddress
     */
    public int getSAddress() {
        return sAddress;
    }

    /**
     * @param sAddress the sAddress to set
     */
    public void setSAddress(int sAddress) {
        this.sAddress = sAddress;
    }
    public void setSAddress(String sAddress){
        this.sAddress = Integer.parseInt(sAddress);
    }


    /**
     * @return the vendorID
     */
    public String getVendorID() {
        return vendorID;
    }

    /**
     * @param vendorID the vendorID to set
     */
    public void setVendorID(String vendorID) {
        this.vendorID = vendorID;
    }

    /**
     * @return the userReminder
     */
    public String getUserReminder() {
        return userReminder;
    }

    /**
     * @param userReminder the userReminder to set
     */
    public void setUserReminder(String UserString1) {
        this.userReminder = UserString1;
    }

    /**
     * @return the deviceModel
     */
    public String getDeviceModel() {
        return deviceModel;
    }

    /**
     * @param deviceModel the deviceModel to set
     */
    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    /**
     * @return the marked4removal
     */
    public boolean isMarked4removal() {
        return marked4removal;
    }

    /**
     * @param marked4removal the marked4removal to set
     */
    public void setMarked4removal(boolean marked4removal) {
        this.marked4removal = marked4removal;
    }

    
}
