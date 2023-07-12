package com;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        printHelp();
        System.out.println("Enter a command");

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("$ ");
        String line = br.readLine();
        boolean done = false;
        while (line != null && !done) {
            String[] tokens = line.split(" ");
            String command = tokens[0];
            String[] commandArgs = new String[tokens.length - 1];
            for (int i=1; i<tokens.length; i++) {
                commandArgs[i-1] = tokens[i];
            }
            switch (command) {
                case "IndexOfLastNewSubword":
                    com.commands.IndexOfLastNewSubword.main(commandArgs);
                    break;
                case "EarliestSubwordMatch":
                    com.commands.EarliestSubwordMatch.main(commandArgs);
                    break;
                case "PrintSymbolSequence":
                    com.commands.PrintSymbolSequence.main(commandArgs);
                case "DrawTrapezoids":
                    com.commands.DrawTrapezoids.main(commandArgs);
                    break;
                case "CountCollinearTrapezoids":
                    com.commands.CountCollinearTrapezoids.main(commandArgs);
                    break;
                case "AssertBoundedDistanceRatio":
                    com.commands.AssertBoundedDistanceRatio.main(commandArgs);
                    break;
                case "AssertBoundedMaxDistance":
                    com.commands.AssertBoundedMaxDistance.main(commandArgs);
                    break;
                case "AssertBoundedMinDistance":
                    com.commands.AssertBoundedMinDistance.main(commandArgs);
                    break;
                case "DistinctSubwordIntervals":
                    com.commands.DistinctSubwordIntervals.main(commandArgs);
                    break;
                case "help":
                    printHelp();
                    break;
                case "exit", "quit", "stop":
                    done = true;
                    break;
                default:
                    System.out.println("Unrecognised command.");
                    printHelp();
                    break;
            }
            if (!done) {
                System.out.print("$ ");
                line = br.readLine();
            }
        }
    }

    public static void printHelp() {
        System.out.println("Available commands:");
        System.out.println(
                """
                        \tAssertBoundedDistanceRatio, AssertBoundedMaxDistance,\s
                        \tAssertBoundedMinDistance, CountCollinearTrapezoids, DrawTrapezoids,\s
                        \tEarliestSubwordMatch, IndexOfLastNewSubword, PrintSymbolSequence,\s
                        \tDistinctSubwordIntervals
                        """
        );
    }

}
