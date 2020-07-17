package com.fem.util;

import java.io.*;

//  Finite element printer to file
public class FePrintWriter {
    PrintWriter PR;
    public PrintWriter getPrinter(String fileOut) {
        try {
            PR = new PrintWriter(
                    new BufferedWriter(
                            new FileWriter(fileOut)));
        } catch (Exception e) {
            com.fem.util.UTIL.errorMsg("Cannot open output file: " + fileOut);
        }
        return PR;
   }

}
