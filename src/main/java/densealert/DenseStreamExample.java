/* =================================================================================
 *
 * DenseAlert: Incremental Dense-Block Detection in Tensor Streams
 * Authors: Kijung Shin, Bryan Hooi, Jisu Kim, and Christos Faloutsos
 *
 * Version: 1.0
 * Date: Oct 24, 2016
 * Main Contact: Kijung Shin (kijungs@cs.cmu.edu)
 *
 * This software is free of charge under research purposes.
 * For commercial purposes, please contact the author.
 *
 * =================================================================================
 */

package densealert;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.io.*; 
import java.util.List;

import java.io.FileWriter;

/**
 * Example Using DenseStream
 *
 * @author kijungs
 */
public class DenseStreamExample {

    /**
     * Example Code for Dense Stream
     * @throws IOException
     */
    public static void main(String[] ar) throws IOException {

        /**
         * Initialize DenseStream Object
         */
        //final String dataPath = "../coupled/tensors/denseAlert_example.txt";
        final String dataPath = "example_data.txt";
        final String output_file = "denseBlocks_output.txt";
        final int order = 3;
        final String delim = ",";
        final DenseStream denseStream = new DenseStream(order);        
        String density_key = "'density'";
        /**
         * insert entries one by one
         */

        System.out.println("Start Insertion: ");
        BufferedReader br = new BufferedReader(new FileReader(dataPath));

        int count = 0;
        while(true) {

            final String line = br.readLine();
            if(line == null) {
                break;
            }

            denseStream.insert(parseEntry(line, delim, order));

            if((++count) % 100000 == 0) {
                
                System.out.println("Processed Entry Num: " + count + ", Current Density: " + denseStream.getDensity());
                Map<Integer, int[]> modeToIndices= denseStream.getBlockIndices();
                // will hold the current dense block values for indexes & modes
                Map<String, String> block = new HashMap<String, String>();

                /**
                 * Print the intermediate maintained Block
                 */
                System.out.println("Indices forming a dense block");
                for(int mode = 0; mode < order; mode++) {
                    String ids = "[";
                    System.out.println("Mode " + mode +": ");
                    int[] indices = modeToIndices.get(mode);

                    for(int i=0; i<indices.length; i++) {
                        System.out.print(indices[i]+" ");
                        ids += String.valueOf(indices[i])+",";
                    }
                    ids += "]";
                    block.put('"'+String.valueOf(mode)+'"', ids);
                }
                block.put(density_key.toString(), String.valueOf(denseStream.getDensity()));
                // write updated block
                BufferedWriter writer = new BufferedWriter( new FileWriter(output_file, true)); 
                writer.write(block.toString().replace("=", ":").replace(",]", "]")+"\n");
                writer.close();
            }
        }

        br.close();

        /**
         * Print the maintained Block
         */
        Map<Integer, int[]> modeToIndices= denseStream.getBlockIndices();
        System.out.println("Density of the maintained block: " + denseStream.getDensity());
        System.out.println("Indices forming a dense block");
        for(int mode = 0; mode < order; mode++) {
            System.out.println("Mode " + mode +": ");
            int[] indices = modeToIndices.get(mode);
            for(int i=0; i<indices.length; i++) {
                System.out.print(indices[i]+" ");
            }
            System.out.println();
        }

        /**
         * delete entries one by one
         */
        System.out.println("Start Deletion: ");
        br = new BufferedReader(new FileReader(dataPath));

        count = 0;
        while(true) {

            final String line = br.readLine();
            if(line == null) {
                break;
            }

            denseStream.delete(parseEntry(line, delim, order));

            if((++count) % 100000 == 0) {
                System.out.println("Processed Entry Num: " + count + ", Current Density: " + denseStream.getDensity());
            }
        }

        return;
    }

    private static int[] parseEntry(String line, String delim, int order) {

        String[] tokens = line.split(delim);

        int[] entry = new int[order+1];

        for(int mode = 0; mode < order; mode++) {
            entry[mode] = Integer.valueOf(tokens[mode]);
        }
        entry[order] = Integer.valueOf(tokens[order]);

        return entry;
    }

}
