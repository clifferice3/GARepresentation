
/**
 * ****************************************************************************
 * A Teaching GA	Developed by Hal Stringer & Annie Wu, UCF Version 2, January
 * 18, 2004
******************************************************************************
 */

import java.io.*;
import java.util.*;
import java.text.*;

public class Search {

    /**
     * *****************************************************************************
     * INSTANCE VARIABLES *
******************************************************************************
     */
    /**
     * *****************************************************************************
     * STATIC VARIABLES *
******************************************************************************
     */
    public static FitnessFunction problem;

    public static Chromo[] member;
    public static Chromo[] child;

    public static Chromo bestOfGenChromo;
    public static int bestOfGenR;
    public static int bestOfGenG;
    public static Chromo bestOfRunChromo;
    public static int bestOfRunR;
    public static int bestOfRunG;
    public static Chromo bestOverAllChromo;
    public static int bestOverAllR;
    public static int bestOverAllG;

    public static double sumRawFitness;
    public static double sumRawFitness2;	// sum of squares of fitness
    public static double sumBestFitness;
    public static double sumBestFitness2; // sum of squares of best fitness
    public static double sumSclFitness;
    public static double sumProFitness;
    public static double defaultBest;
    public static double defaultWorst;
    public static int optimalGenerations;
    public static int optimalGenerationsTotal;
    public static int optimalGenerationsTotal2;

    public static double averageRawFitness;
    public static double stdevRawFitness;
    public static double stdevBestFitness;
    public static double averageConfidenceInterval;
    public static double bestConfidenceInterval;

    public static int G;
    public static int R;
    public static Random r = new Random();
    private static double randnum;

    private static int memberIndex[];
    private static double memberFitness[];
    private static int TmemberIndex;
    private static double TmemberFitness;

    private static double fitnessStats[][];  // 0=Avg, 1=Best

    /**
     * *****************************************************************************
     * CONSTRUCTORS *
******************************************************************************
     */
    /**
     * *****************************************************************************
     * MEMBER METHODS *
******************************************************************************
     */
    /**
     * *****************************************************************************
     * STATIC METHODS *
******************************************************************************
     */
    public static void main(String[] args) throws java.io.IOException {

        Calendar dateAndTime = Calendar.getInstance();
        Date startTime = dateAndTime.getTime();

	//  Read Parameter File
        System.out.println("\nParameter File Name is: " + args[0] + "\n");
        Parameters parmValues = new Parameters(args[0]);

        //  Write Parameters To Summary Output File
        String summaryFileName = Parameters.expID + "_summary.txt";
        FileWriter summaryOutput = new FileWriter(summaryFileName);
        parmValues.outputParameters(summaryOutput);

        //	Set up Fitness Statistics matrix
        fitnessStats = new double[7][Parameters.generations];
        for (int i = 0; i < Parameters.generations; i++) {
            fitnessStats[0][i] = 0;
            fitnessStats[1][i] = 0;
            fitnessStats[2][i] = 0;
            fitnessStats[3][i] = 0;
            fitnessStats[4][i] = 0;
            fitnessStats[5][i] = 0;
            fitnessStats[6][i] = 0;
        }

	//	Problem Specific Setup - For new new fitness function problems, create
        //	the appropriate class file (extending FitnessFunction.java) and add
        //	an else_if block below to instantiate the problem.
        if (Parameters.problemType.equals("NM")) {
            problem = new NumberMatch();
        } else if (Parameters.problemType.equals("OM")) {
            problem = new OneMax();
        } else if (Parameters.problemType.equals("RK")) {

            problem = new LabSchedulingFunction();
        } else if (Parameters.problemType.equals("INT")) {

            problem = new LabSchedulingFunction();
        } else if (Parameters.problemType.equals("LSi")) {
            /*Chathika: if problem parameter is introns set evaluation function accordingly*/
            problem = new LabSchedulingFunction();
        } else if (Parameters.problemType.equals("CL")) {

            problem = new LabSchedulingFunction();
        }else {
            System.out.println("Invalid Problem Type");
        } 

        System.out.println(problem.name);

        //	Initialize RNG, array sizes and other objects
        r.setSeed(Parameters.seed);
        memberIndex = new int[Parameters.popSize];
        memberFitness = new double[Parameters.popSize];
        member = new Chromo[Parameters.popSize];
        child = new Chromo[Parameters.popSize];
        bestOfGenChromo = new Chromo();
        bestOfRunChromo = new Chromo();
        bestOverAllChromo = new Chromo();
        optimalGenerationsTotal = 0;
        optimalGenerationsTotal2 = 0;

        if (Parameters.minORmax.equals("max")) {
            defaultBest = -999999.0;
            defaultWorst = 999999.0;
        } else {
            defaultBest = 999999.0;
            defaultWorst = -999999.0;
        }

        bestOverAllChromo.rawFitness = defaultBest;

        //  Start program for multiple runs
        for (R = 1; R <= Parameters.numRuns; R++) {

            bestOfRunChromo.rawFitness = defaultBest;
            optimalGenerations = 0;

            double optimalGenerationsLocal = 0;

            System.out.println();

            //	Initialize First Generation
            for (int i = 0; i < Parameters.popSize; i++) {
                member[i] = new Chromo();
                child[i] = new Chromo();
            }

            //	Begin Each Run
            for (G = 0; G < Parameters.generations; G++) {

                sumProFitness = 0;
                sumSclFitness = 0;
                sumRawFitness = 0;
                sumRawFitness2 = 0;
                sumBestFitness = 0;
                sumBestFitness2 = 0;

                bestOfGenChromo.rawFitness = defaultBest;

                //	Test Fitness of Each Member
                for (int i = 0; i < Parameters.popSize; i++) {

                    member[i].rawFitness = 0;
                    member[i].sclFitness = 0;
                    member[i].proFitness = 0;

                    problem.doRawFitness(member[i]);

                    sumRawFitness = sumRawFitness + member[i].rawFitness;
                    sumRawFitness2 = sumRawFitness2
                            + member[i].rawFitness * member[i].rawFitness;

                    if (Parameters.minORmax.equals("max")) {
                        if (member[i].rawFitness > bestOfGenChromo.rawFitness) {
                            Chromo.copyB2A(bestOfGenChromo, member[i]);
                            bestOfGenR = R;
                            bestOfGenG = G;
                        }
                        if (member[i].rawFitness > bestOfRunChromo.rawFitness) {
                            Chromo.copyB2A(bestOfRunChromo, member[i]);
                            bestOfRunR = R;
                            bestOfRunG = G;
                        }
                        if (member[i].rawFitness >= bestOverAllChromo.rawFitness) {
                            Chromo.copyB2A(bestOverAllChromo, member[i]);
                            bestOverAllR = R;
                            bestOverAllG = G;
                        }
                    } else {
                        if (member[i].rawFitness < bestOfGenChromo.rawFitness) {
                            Chromo.copyB2A(bestOfGenChromo, member[i]);
                            bestOfGenR = R;
                            bestOfGenG = G;
                        }
                        if (member[i].rawFitness < bestOfRunChromo.rawFitness) {
                            Chromo.copyB2A(bestOfRunChromo, member[i]);
                            bestOfRunR = R;
                            bestOfRunG = G;
                        }
                        if (member[i].rawFitness < bestOverAllChromo.rawFitness) {
                            Chromo.copyB2A(bestOverAllChromo, member[i]);
                            bestOverAllR = R;
                            bestOverAllG = G;
                        }
                    }
                }

				// Accumulate fitness statistics
                sumBestFitness = sumBestFitness + bestOfGenChromo.rawFitness;
                sumBestFitness2 = sumBestFitness + bestOfGenChromo.rawFitness * bestOfGenChromo.rawFitness;
                fitnessStats[0][G] += sumRawFitness / Parameters.popSize;
                fitnessStats[1][G] += bestOfGenChromo.rawFitness;

                averageRawFitness = sumRawFitness / Parameters.popSize;
                stdevRawFitness = Math.sqrt(
                        Math.abs(sumRawFitness2
                                - sumRawFitness * sumRawFitness / Parameters.popSize)
                        / (Parameters.popSize - 1)
                );

                fitnessStats[2][G] += stdevRawFitness;

                stdevBestFitness = Math.sqrt(
                        Math.abs(sumBestFitness2
                                - sumBestFitness * sumBestFitness / Parameters.popSize)
                        / (Parameters.popSize - 1)
                );
                fitnessStats[3][G] += stdevBestFitness;

                averageConfidenceInterval = 1.96 * (stdevRawFitness / Math.sqrt(Parameters.popSize));
                bestConfidenceInterval = 1.96 * (stdevBestFitness / Math.sqrt(Parameters.popSize));

                fitnessStats[4][G] += averageConfidenceInterval;
                fitnessStats[5][G] += bestConfidenceInterval;

                if (bestOfGenChromo.rawFitness == Parameters.geneSize) {
                    optimalGenerations++;
                    optimalGenerationsTotal += G;
                    optimalGenerationsLocal += G;
                    optimalGenerationsTotal2++;

                }

                // Output generation statistics to screen
                System.out.println(R + "\t" + G + "\t" + " BestFit: " + (int) bestOfGenChromo.rawFitness + "\t" + averageRawFitness + "\t" + stdevRawFitness + "\t" + stdevBestFitness + "\t" + averageConfidenceInterval + "\t" + bestConfidenceInterval);

                // Output generation statistics to summary file
                summaryOutput.write(" R ");
                Hwrite.right(R, 3, summaryOutput);
                summaryOutput.write(" G ");
                Hwrite.right(G, 3, summaryOutput);
                Hwrite.right((int) bestOfGenChromo.rawFitness, 7, summaryOutput);
                Hwrite.right(averageRawFitness, 11, 3, summaryOutput);
                Hwrite.right(stdevRawFitness, 11, 3, summaryOutput);
                Hwrite.right(stdevBestFitness, 11, 3, summaryOutput);
                Hwrite.right(averageConfidenceInterval, 11, 3, summaryOutput);
                Hwrite.right(bestConfidenceInterval, 11, 3, summaryOutput);
                summaryOutput.write("\n");

                if (optimalGenerations > 0) {
                    fitnessStats[6][G] = optimalGenerationsLocal / optimalGenerations;
                }

		// *********************************************************************
                // **************** SCALE FITNESS OF EACH MEMBER AND SUM ***************
                // *********************************************************************
                switch (Parameters.scaleType) {

                    case 0:     // No change to raw fitness
                        for (int i = 0; i < Parameters.popSize; i++) {
                            member[i].sclFitness = member[i].rawFitness + .000001;
                            sumSclFitness += member[i].sclFitness;
                        }
                        break;

                    case 1:     // Fitness not scaled.  Only inverted.
                        for (int i = 0; i < Parameters.popSize; i++) {
                            member[i].sclFitness = 1 / (member[i].rawFitness + .000001);
                            sumSclFitness += member[i].sclFitness;
                        }
                        break;

                    case 2:     // Fitness scaled by Rank (Maximizing fitness)

                        //  Copy genetic data to temp array
                        for (int i = 0; i < Parameters.popSize; i++) {
                            memberIndex[i] = i;
                            memberFitness[i] = member[i].rawFitness;
                        }
                        //  Bubble Sort the array by floating point number
                        for (int i = Parameters.popSize - 1; i > 0; i--) {
                            for (int j = 0; j < i; j++) {
                                if (memberFitness[j] > memberFitness[j + 1]) {
                                    TmemberIndex = memberIndex[j];
                                    TmemberFitness = memberFitness[j];
                                    memberIndex[j] = memberIndex[j + 1];
                                    memberFitness[j] = memberFitness[j + 1];
                                    memberIndex[j + 1] = TmemberIndex;
                                    memberFitness[j + 1] = TmemberFitness;
                                }
                            }
                        }
                        //  Copy ordered array to scale fitness fields
                        for (int i = 0; i < Parameters.popSize; i++) {
                            member[memberIndex[i]].sclFitness = i;
                            sumSclFitness += member[memberIndex[i]].sclFitness;
                        }

                        break;

                    case 3:     // Fitness scaled by Rank (minimizing fitness)

                        //  Copy genetic data to temp array
                        for (int i = 0; i < Parameters.popSize; i++) {
                            memberIndex[i] = i;
                            memberFitness[i] = member[i].rawFitness;
                        }
                        //  Bubble Sort the array by floating point number
                        for (int i = 1; i < Parameters.popSize; i++) {
                            for (int j = (Parameters.popSize - 1); j >= i; j--) {
                                if (memberFitness[j - i] < memberFitness[j]) {
                                    TmemberIndex = memberIndex[j - 1];
                                    TmemberFitness = memberFitness[j - 1];
                                    memberIndex[j - 1] = memberIndex[j];
                                    memberFitness[j - 1] = memberFitness[j];
                                    memberIndex[j] = TmemberIndex;
                                    memberFitness[j] = TmemberFitness;
                                }
                            }
                        }
                        //  Copy array order to scale fitness fields
                        for (int i = 0; i < Parameters.popSize; i++) {
                            member[memberIndex[i]].sclFitness = i;
                            sumSclFitness += member[memberIndex[i]].sclFitness;
                        }

                        break;

                    default:
                        System.out.println("ERROR - No scaling method selected");
                }

		// *********************************************************************
                // ****** PROPORTIONALIZE SCALED FITNESS FOR EACH MEMBER AND SUM *******
                // *********************************************************************
                for (int i = 0; i < Parameters.popSize; i++) {
                    member[i].proFitness = member[i].sclFitness / sumSclFitness;
                    sumProFitness = sumProFitness + member[i].proFitness;
                }

		// *********************************************************************
                // ************ CROSSOVER AND CREATE NEXT GENERATION *******************
                // *********************************************************************
                int parent1 = -1;
                int parent2 = -1;

               
                //  Assumes always two offspring per mating
                for (int i = 0; i < Parameters.popSize; i = i + 2) {

                    //	Select Two Parents
                    parent1 = Chromo.selectParent();
                    parent2 = parent1;
                    while (parent2 == parent1) {
                        parent2 = Chromo.selectParent();
                    }

                    //	Crossover Two Parents to Create Two Children
                    randnum = r.nextDouble();

                    if (Parameters.problemType.equalsIgnoreCase("RK")) {
                        if (randnum < Parameters.xoverRate) {
                            Chromo.mateParents(parent1, parent2, member[parent1], member[parent2], child[i], child[i + 1], member[parent1].randomizeChromo(), member[parent2].randomizeChromo());
                            //Chromo.mateParents(parent1, parent2, member[parent1], member[parent2], child[i], child[i + 1], member[parent1].randomArray, member[parent2].randomArray);
                        } else {
                            Chromo.mateParents(parent1, member[parent1], child[i]);
                            Chromo.mateParents(parent2, member[parent2], child[i + 1]);
                        }
                    } else {
                        
                        if (randnum < Parameters.xoverRate) {
                            Chromo.mateParents(parent1, parent2, member[parent1], member[parent2], child[i], child[i + 1]);
                        } else {
                            Chromo.mateParents(parent1, member[parent1], child[i]);
                            Chromo.mateParents(parent2, member[parent2], child[i + 1]);
                        }
                    }
                } // End Crossover

                //	Mutate Children
                for (int i = 0; i < Parameters.popSize; i++) {
                    child[i].doMutation();
                }
                
                //	Swap Children with Last Generation
                for (int i = 0; i < Parameters.popSize; i++) {
                    /*Chathika: Elitism of top 10%*/
                    if(member[i].sclFitness<=Parameters.popSize-(Parameters.popSize/10))
                        Chromo.copyB2A(member[i], child[i]);
                }

            } //  Repeat the above loop for each generation

            Hwrite.left(bestOfRunR, 4, summaryOutput);
            Hwrite.right(bestOfRunG, 4, summaryOutput);

            problem.doPrintGenes(bestOfRunChromo, summaryOutput);

            System.out.println(R + "\t" + "B" + "\t" + (int) bestOfRunChromo.rawFitness);

            System.out.println("Best Schedule of Run: " + bestOfRunChromo.chromo);

        } //End of a Run

        Hwrite.left("B", 8, summaryOutput);

        problem.doPrintGenes(bestOverAllChromo, summaryOutput);

        LabSchedulingFunction.validate(bestOverAllChromo);

        System.out.println("Best Schedule Overall: " + bestOverAllChromo.chromo);
        System.out.println("Best Fitness Overall: " + bestOverAllChromo.rawFitness);
        //	Output Fitness Statistics matrix
        summaryOutput.write("Gen                 AvgFit              BestFit            AvgStdDev    BestStdDev     AvgAvgCI        AvgBestCI        BestGeneration\n");
        for (int i = 0; i < Parameters.generations; i++) {
            Hwrite.left(i, 15, summaryOutput);
            Hwrite.left(fitnessStats[0][i] / Parameters.numRuns, 20, 2, summaryOutput);
            Hwrite.left(fitnessStats[1][i] / Parameters.numRuns, 20, 2, summaryOutput);
            Hwrite.left(fitnessStats[2][i] / Parameters.numRuns, 20, 2, summaryOutput);
            Hwrite.left(fitnessStats[3][i] / Parameters.numRuns, 20, 2, summaryOutput);
            Hwrite.left(fitnessStats[4][i] / Parameters.numRuns, 20, 2, summaryOutput);
            Hwrite.left(fitnessStats[5][i] / Parameters.numRuns, 20, 2, summaryOutput);
            Hwrite.left(fitnessStats[6][i], 20, 2, summaryOutput);

            summaryOutput.write("\n");
        }

        summaryOutput.write("\n");
        summaryOutput.close();

        System.out.println();
        System.out.println("Start:  " + startTime);
        dateAndTime = Calendar.getInstance();
        Date endTime = dateAndTime.getTime();
        System.out.println("End  :  " + endTime);

    } // End of Main Class

}   // End of Search.Java ******************************************************

