package com.fem.fea;

import com.fem.elem.*;
import com.fem.model.*;
import com.fem.solver.*;
import com.fem.util.*;
import java.io.*;

// Main class of the finite element processor
public class Jfem {

    private static FeScanner readData;
    private static PrintWriter printWriter;
    public static String fileOut;

    public static void main(String[] args) {

        if (args.length == 0) {
            System.out.println(
                   "Usage: java fea.JFEM FileIn [FileOut]\n");
            return;
        }

        readData = new FeScanner(args[0]);

        fileOut = (args.length==1) ? args[0]+".lst" : args[1];
        printWriter = new FePrintWriter().getPrinter(fileOut);

        printWriter.println("fea.JFEM: FE code. Data file: " + args[0]);
        System.out.println("fea.JFEM: FE code. Data file: "
                + args[0]);

        new Jfem();
        printWriter.close();
    }

    public Jfem () {

        UTIL.printDate(printWriter);

        FeModel fem = new FeModel(readData, printWriter);
        Element.fem = fem;

        fem.readData();

        printWriter.printf("\nNumber of elements    nEl = %d\n"+
                  "Number of nodes      nNod = %d\n"+
                  "Number of dimensions nDim = %d\n",
                  fem.nEl, fem.nNod, fem.nDim);

        long t0 = System.currentTimeMillis();

        Solver solver = Solver.newSolver(fem);
        solver.assembleGSM();

        printWriter.printf("Memory for global matrix: %7.2f MB\n",
                Solver.lengthOfGSM*8.0e-6);

        FeLoad load = new FeLoad(fem);
        Element.load = load;

        FeStress stress = new FeStress(fem);

        // Load step loop
        while (load.readData( )) {
            load.assembleRHS();
            int iter = 0;
            // Equilibrium iterations
            do {
                iter++;
                int its = solver.solve(FeLoad.RHS);
                if (its > 0) printWriter.printf(
                    "Solver: %d iterations\n", its);
                stress.computeIncrement();
            } while (!stress.equilibrium(iter));

            stress.accumulate();
            stress.writeResults();
            printWriter.printf("Loadstep %s", FeLoad.loadStepName);
            if (iter>1) printWriter.printf(" %5d iterations, " +
                "Relative residual norm = %10.5f",
                iter, FeStress.relResidNorm);
            printWriter.printf("\n");
        }

        printWriter.printf("\nSolution time = %10.2f s\n",
                (System.currentTimeMillis()-t0)*0.001);
    }

}