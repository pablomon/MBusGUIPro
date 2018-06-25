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
public class TableRendererDevices extends DefaultTableCellRenderer {   
    public CellLook normalLook = new CellLook(new Color(230,243,255), Color.black);
    public CellLook altLook = new CellLook(new Color(202,230,254), Color.black);
    public CellLook selectedLook = new CellLook(new Color(51,153,255), Color.white);
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
    {                
        final Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        c.setBackground(row % 2 == 0 ? normalLook.backroundColor : altLook.backroundColor);
        c.setForeground(row % 2 == 0 ? normalLook.textColor : altLook.textColor);
        
        boolean anySelected = false;
        for (int col = 0; col < table.getModel().getColumnCount(); col++)
        {
            if (table.isCellSelected(row,col))
            {
                anySelected = true;
                break;
            }
        }
        
        if (anySelected) 
        {
        c.setBackground(selectedLook.backroundColor);
        c.setForeground(selectedLook.textColor);
        }
        
        return c;
    }
    
    
    
    public void setNormalLook(CellLook look)
    {
        normalLook = look;
    }
    
    public void setAltLook(CellLook look)
    {
        altLook = look;
    }
}