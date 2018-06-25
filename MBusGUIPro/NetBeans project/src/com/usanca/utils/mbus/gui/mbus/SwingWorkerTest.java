/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.usanca.utils.mbus.gui.mbus;

import javax.swing.SwingWorker;

/**
 *
 * @author Pablo Monteserin Garcia. pablomonteserin@gmail.com
 */
public class SwingWorkerTest extends SwingWorker<Void, Integer>{

    @Override
    protected Void doInBackground() throws Exception {
        
        int c = 0;
        while (!isCancelled())
        {
            System.out.println(c);
            c++;
        }
        
        System.out.println("cancelled");
        
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
