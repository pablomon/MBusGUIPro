/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.usanca.utils.mbus.gui.mbusguipro;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author Pablo Monteserin Garcia. pablomonteserin@gmail.com
 */
public class TableRendererReadings extends DefaultTableCellRenderer {   
    public CellLook normalLook1 = new CellLook(new Color(230,243,255), Color.black);
    public CellLook altLook1 = new CellLook(new Color(202,230,254), Color.black);
    public CellLook normalLook2 = new CellLook(new Color(254,245,230), Color.black);
    public CellLook altLook2 = new CellLook(new Color(254,234,202), Color.black);
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
    {
        final Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        int iDev = (Integer) table.getValueAt(row, 0);       
        if (iDev % 2 == 0) 
        {
           c.setBackground(row % 2 == 0 ? normalLook1.backroundColor : altLook1.backroundColor);           
        }
        else
        {                       
           c.setBackground(row % 2 == 0 ? normalLook2.backroundColor : altLook2.backroundColor);
        } 

        c.setForeground(Color.BLACK);
        
        return c;
    }
    
    public void setNormalLook1(CellLook look)
    {
        normalLook1 = look;
    }    
    public void setAltLook1(CellLook look)
    {
        altLook1 = look;
    }
     public void setNormalLook2(CellLook look)
    {
        normalLook1 = look;
    }    
    public void setAltLook2(CellLook look)
    {
        altLook1 = look;
    }   
    
    
}
