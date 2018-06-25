/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.usanca.utils.mbus.gui.mbusguipro;

import com.usanca.utils.mbus.gui.extras.Printer;
import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;
import com.usanca.utils.mbus.gui.types.MBusDevice;
import java.util.Iterator;

/**
 *
 * @author Pablo Monteserin Garcia. pablomonteserin@gmail.com
 */
public class TableModelReadings extends AbstractTableModel {
    
    private String[] columnNames = {"  ", "Primaria", "Secundaria", "Nota de usuario"};
    private ArrayList<MBusDevice> devices = new ArrayList();   
    
    public TableModelReadings(ArrayList<MBusDevice> dl) {        
        this.devices = dl;
        fireTableDataChanged();
    }
    
    TableModelReadings() {        
        
    }
    
    public void setDevicesList(ArrayList<MBusDevice> dl) {
        if (dl != null) {
            devices = dl;
            Printer.out("lista asignada");
            fireTableDataChanged();
        } else {
            Printer.out("!! la lista devuelve null");
        }
    }
    
    public ArrayList<MBusDevice> getDevicesList() {
        return devices;
    }
    
    public void addDevice(MBusDevice d) {
        devices.add(d);
        fireTableDataChanged();
    }
    
    private void removeMarked() {
        Iterator<MBusDevice> it = devices.iterator();
        MBusDevice d;
        while (it.hasNext())
        {
            d = it.next();
            if (d.isMarked4removal()) it.remove();
        }

        fireTableDataChanged();
    }
    
    public void clearDevices() {
        devices.clear();
        fireTableDataChanged();
    }
    
    public void removeDevices(int[] rows) 
    {        
        for (int i=0; i<rows.length; i++)
        {            
            devices.get(rows[i]).setMarked4removal(true);                                            
        }        
        
        removeMarked();               
    }

    // Overrides for the abstract table model
    // --------------------------------------------------------------------------------------------------
    @Override
    public String getColumnName(int columnIndex) {
        return columnNames[columnIndex];
    }

    @Override
    public int getRowCount() {
        return devices.size();
    }

    @Override
    public int getColumnCount() {
        return 4;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0:
                return rowIndex;
            case 1:
                return Integer.toString (devices.get(rowIndex).getPAddress());
            case 2:
                return Integer.toString (devices.get(rowIndex).getSAddress());
            case 3:
                return devices.get(rowIndex).getUserReminder();
            default:
                return null;
        }
    }

    @Override
    public Class getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex > 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void setValueAt(Object oValue, int rowIndex, int columnIndex) {
        String s = (String) oValue;

        MBusDevice d = devices.get(rowIndex);

        switch (columnIndex) {
            case 1:
                try {
                    int i = Integer.parseInt(s);
                    d.setPAddress(s);
                } catch (NumberFormatException e) {
                    Printer.out(s + ": no puede convertirse en un entero, introduce un número decimal");
                    System.out.println("la cadena introducida no puede convertirse en un entero" + e.getMessage());
                }
                break;
            case 2:
                try {
                    int i = Integer.parseInt(s);
                    d.setSAddress(s);
                } catch (Exception e) {
                    Printer.out(s + ": no puede convertirse en un entero, introduce un número decimal");
                    System.out.println("la cadena introducida no puede convertirse en un entero" + e.getMessage());
                }
                break;
            case 3:
                d.setUserReminder(s);
                break;
        }
        fireTableCellUpdated(rowIndex, columnIndex);
    }


}
