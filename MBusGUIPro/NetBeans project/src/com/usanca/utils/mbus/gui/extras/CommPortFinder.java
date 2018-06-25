/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.usanca.utils.mbus.gui.extras;
import jssc.SerialPortList;

/**
 *
 * @author Pablo Monteserin Garcia. pablomonteserin@gmail.com
 */
public class CommPortFinder {
    public static String[] find()
    {
        String[] ports = SerialPortList.getPortNames();
        return ports;        
    }
}
