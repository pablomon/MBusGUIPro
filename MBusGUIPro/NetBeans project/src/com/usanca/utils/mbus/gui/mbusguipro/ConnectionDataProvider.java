/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.usanca.utils.mbus.gui.mbusguipro;

import com.usanca.utils.mbus.gui.types.MBusStreamProperties;

/**
 *
 * @author Pablo Monteserin Garcia. pablomonteserin@gmail.com
 */
interface ConnectionDataProvider {
    public MBusStreamProperties getConnectionFields ();
}
