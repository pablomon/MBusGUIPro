/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.usanca.utils.mbus.gui.extras;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author Pablo Monteserin Garcia. pablomonteserin@gmail.com
 */
public class AutoReader {

    int period;
    Date firstTime;
    Timer timer;

    public AutoReader() {
        this.period = period;
        this.firstTime = firstTime;

    }

    public void start() {
        if (timer!=null) 
        {
            timer.cancel();
            Printer.out("Se ha descartado la programación previa");
        }
        
        if ((period > 0) && (firstTime != null)) {
            timer = new Timer();
            
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, firstTime.getHours());
            cal.set(Calendar.MINUTE, firstTime.getMinutes());
            cal.set(Calendar.SECOND, firstTime.getSeconds());
            
            timer.schedule(new Task(), cal.getTime(), period);
            Printer.out(" La lectura programada empezará el "+cal.getTime()+" y se repetirá cada "+period/3600000+"h");
        }
    }

    public void updateValues(Date startTime, int interval) {
        this.firstTime = startTime;
        this.period = interval;
        start();
    }
}

class Task extends TimerTask {

    @Override
    public void run() {        
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        System.out.println("Ejecutando run desde task a las "+sdf.format(cal.getTime()));        
    }
}
