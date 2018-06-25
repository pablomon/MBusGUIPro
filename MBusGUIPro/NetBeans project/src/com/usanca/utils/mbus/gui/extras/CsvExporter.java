/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.usanca.utils.mbus.gui.extras;

import java.awt.Component;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableModel;

/**
 *
 * @author Pablo Monteserin Garcia. pablomonteserin@gmail.com
 */
public class CsvExporter {

    JFileChooser fc;
    String dateHeader;
    int startColumn = 1; //zerobased

    public void exportTable(TableModel table2export, Component component) {
        
        fc = new JFileChooser();
        FileNameExtensionFilter csvFilter = new FileNameExtensionFilter("Valores separados por coma", "csv", "CSV");
        fc.setFileFilter(csvFilter);

        int returnVal = fc.showSaveDialog(component);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            String extension = "";
            try {
                if (!file.getName().endsWith(".csv")) {
                    extension = ".csv";
                }

                BufferedWriter out = new BufferedWriter(new FileWriter(file + extension));

                for (int c = startColumn; c < table2export.getColumnCount(); c++) {
                    out.write(table2export.getColumnName(c));
                    if (c != table2export.getColumnCount() - 1) {
                        out.write(",");
                    } else {
                        out.write("\n");
                    }
                }

                for (int r = 0; r < table2export.getRowCount(); r++) {
                    for (int c = startColumn; c < table2export.getColumnCount(); c++) {
                        Object o = table2export.getValueAt(r, c);
                        String s = o.toString();                       
                        
                        out.write(s);
                        if (c != table2export.getColumnCount() - 1) {
                            out.write(",");
                        } else {
                            out.write("\n");
                        }
                    }
                }

                out.close();

            } catch (Exception e) {
                System.out.println("Fallo al exportar el archivo");
            }
            //Saving File

        }
    }
}
