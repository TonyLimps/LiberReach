package com.tonylimps.filerelay.windows;

import java.util.Scanner;

public class ExceptionManager extends com.tonylimps.filerelay.core.ExceptionManager {

    private boolean DEBUG;
    private int exceptions;

    public ExceptionManager(boolean DEBUG) {
        this.DEBUG = DEBUG;
    }

    public void throwException(Exception e) {
        if(DEBUG){
            e.printStackTrace();
            new Scanner(System.in).nextLine();
            System.exit(1);
        }
        try{
            new ExceptionDialog(e,exceptions).show();
            exceptions++;
        } catch(NullPointerException npe){
            e.printStackTrace();
            new Scanner(System.in).nextLine();
            System.exit(1);
        }
    }
}
