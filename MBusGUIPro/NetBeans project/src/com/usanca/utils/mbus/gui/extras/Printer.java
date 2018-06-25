/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.usanca.utils.mbus.gui.extras;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextArea;

/**
 *
 * @author Pablo Monteserin Garcia. pablomonteserin@gmail.com
 *
 * Singleton class, provides global access so we can print in the JtextArea.
 */
public abstract class Printer {

    static Printer instance;
    static JTextArea area;
    static int BufferLines = 200;

    public static void setArea(JTextArea a) {
        Printer.area = a;
    }

    public static void out(String s) {
        try {
            if (area.getLineCount() > BufferLines) {
                try {
                    area.replaceRange(null, area.getLineStartOffset(0), area.getLineEndOffset(0));
                } catch (Exception e) {
                    System.out.println(Printer.class + "WARNING: Failed removing buffer's first line" + e.toString());
                }
            }
            area.append(s + "\n");

            final int length = Printer.area.getText().length();
            Printer.area.setCaretPosition(length);
            
        } catch (NullPointerException e) {
            System.out.println(Printer.class + "\nJtextArea must be set first, use setArea method\n" + e.toString());
        }
    }
}
