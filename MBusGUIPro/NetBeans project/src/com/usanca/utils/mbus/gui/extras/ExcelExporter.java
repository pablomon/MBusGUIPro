/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.usanca.utils.mbus.gui.extras;

import com.usanca.utils.mbus.gui.mbusguipro.MbusGUIpro;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.table.TableModel;
import jxl.*;
import jxl.write.*;

/**
 *
 * @author Pablo Monteserin Garcia. pablomonteserin@gmail.com
 */
public class ExcelExporter {

    JFileChooser fc;
    int startColumn = 1; //zerobased
    int firstDataExcelRow = 1; // first excel row where actual readings will be written starting from
    // asign variable colors to api palette colors
    jxl.format.Colour normalLook1 = jxl.format.Colour.LIGHT_TURQUOISE;
    jxl.format.Colour altLook1 = jxl.format.Colour.LIGHT_TURQUOISE2;
    jxl.format.Colour normalLook2 = jxl.format.Colour.YELLOW;
    jxl.format.Colour altLook2 = jxl.format.Colour.YELLOW2;
    jxl.format.Colour normalColor, alternateColor, color2use;
    int dateColumnLength = 100;

    public void exportTable(TableModel table2export, Component component) throws WriteException {
        if (!MbusGUIpro.proVersion) return; //makes sure it doesnt run in demo version
        
        Printer.out("Exportando a excel");
        
        fc = new JFileChooser();
        int returnVal = fc.showSaveDialog(component);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            String extension = "";
            try {
                if (!file.getName().endsWith(".xls")) {
                    extension = ".xls";
                }
                String filePath = file.getAbsolutePath();
                file = new File(filePath + extension);

                WritableWorkbook workbook = Workbook.createWorkbook(file);
                WritableSheet sheet = workbook.createSheet(MbusGUIpro.programTitle, 0);

                //Prepare colors
                //Since there is no way to create a new custom I need to override the palette colors with my desired rgb values
                workbook.setColourRGB(jxl.format.Colour.LIGHT_TURQUOISE, 230, 243, 255);
                workbook.setColourRGB(jxl.format.Colour.LIGHT_TURQUOISE2, 202, 230, 254);
                workbook.setColourRGB(jxl.format.Colour.YELLOW, 254, 245, 230);
                workbook.setColourRGB(jxl.format.Colour.YELLOW2, 254, 234, 202);

                //write headers
                for (int c = startColumn; c < table2export.getColumnCount(); c++) {
                    //CellView cellView = sheet.getColumnView(c);
                    //cellView.setSize(20);
                    sheet.setColumnView(c - startColumn, 20);
                    
                    Label label = new Label(c - startColumn, 0, table2export.getColumnName(c));
                    sheet.addCell(label);
                }

                for (int r = 0; r < table2export.getRowCount(); r++) {
                    // select colors for odd/even devices
                    int iDev = (Integer) table2export.getValueAt(r, 0);
                    if (iDev % 2 == 0) {
                        normalColor = normalLook1;
                        alternateColor = altLook1;
                    } else {
                        normalColor = normalLook2;
                        alternateColor = altLook2;
                    }

                    // select row color for odd/even row
                    if (r % 2 > 0) {
                        color2use = normalColor;
                    } else {
                        color2use = alternateColor;
                    }

                    //
                    int excelCol = 0;
                    for (int c = startColumn; c < table2export.getColumnCount(); c++) {
                        Object o = table2export.getValueAt(r, c);
                        String s = o.toString();

                        WritableCellFormat format = new WritableCellFormat();
                        format.setBackground(color2use);

                        Label label = new Label(excelCol, r + firstDataExcelRow, s);
                        label.setCellFormat(format);
                        sheet.addCell(label);
                        excelCol++;
                    }
                }

                workbook.write();
                workbook.close();

            } catch (IOException ex) {
                Printer.out("Error al exportar a excel");
                Logger.getLogger(ExcelExporter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
