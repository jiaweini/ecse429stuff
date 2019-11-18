package com.company;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.io.FileWriter;
import java.lang.StringBuilder;

public class Main {
    static int plus;
    static int minus;
    static int times;
    static int divide;
    static ArrayList<String> output=new ArrayList<String>();
    final int threadCount=3;

    public static void main(String[] args) throws IOException, InterruptedException {

        //read code to be mutated
        String fileName = "Helloworld.py";
        Path path = Paths.get(fileName);
        ArrayList<String> allLines = new ArrayList<>(Files.readAllLines(path, StandardCharsets.UTF_8));

        //Generate a copy of original code
        FileWriter writer = new FileWriter("original.py");
        for (String str : allLines) {
            writer.write(str + System.lineSeparator());
        }
        writer.close();

        //Mutate the code
        int mutantIndex=0;
        for(int i =0;i<allLines.size();i++){
            String thisLine=allLines.get(i);
            for(int c=0;c<thisLine.length();c++){
                switch(thisLine.charAt(c)){
                    case '+': //operator 1
                        minus++;times++;divide++;
                        mutantIndex = mutate(mutantIndex,allLines,i,c,1);
                        break;
                    case '-': //operator 2
                        plus++;times++;divide++;
                        mutantIndex = mutate(mutantIndex,allLines,i,c,2);
                        break;
                    case '*': //operator 3
                        plus++;minus++;divide++;
                        mutantIndex = mutate(mutantIndex,allLines,i,c,3);
                        break;
                    case '/': //operator 4
                        if(c!=thisLine.length()-1&&thisLine.charAt(c+1)=='/'){
                            c=thisLine.length();
                            continue;
                        }else{
                            plus++;minus++;times++;
                            mutantIndex = mutate(mutantIndex,allLines,i,c,4);
                            break;
                        }
                }
            }
        }

        //print mutation statistics
        output.add("'+' mutant: "+plus+"\n'-' mutant: "+minus+"\n'*' mutant: "+times+"\n'/' mutant: "+divide);
        String outputFileName="mutantLibrary.txt";
        FileWriter libWriter = new FileWriter(outputFileName);
        for (String str : output) {
            libWriter.write(str + System.lineSeparator());
        }
        libWriter.close();

        //read simulation file, save parameters as array
        String simFileName = "simulation.txt";
        Path simPath = Paths.get(simFileName);
        ArrayList<String> simLines = new ArrayList<>(Files.readAllLines(simPath, StandardCharsets.UTF_8));
        ArrayList<Integer> parameters = new ArrayList<>();
        for(String simLine:simLines){
            parameters.add(Integer.parseInt(simLine));
        }

        //run params on original program to get correct results
        for(int p:parameters) {
            Process process;
            String command = "cmd.exe /c python " + fileName + " " + p + " >> correctResults.txt";
            process = Runtime.getRuntime().exec(command);
            process.waitFor();
        }

        //simulate(run params on mutants)
        Thread thread1 = new Thread(new SimulationThread(0,mutantIndex, parameters));
        Thread thread2 = new Thread(new SimulationThread(1,mutantIndex, parameters));
        Thread thread3 = new Thread(new SimulationThread(2,mutantIndex, parameters));
        thread1.start();
        thread2.start();
        thread3.start();
        thread1.join();
        thread2.join();
        thread3.join();
    }

     static int mutate(int mutantIndex,ArrayList<String> allLines,int lineIndex,int charIndex, int operator) throws IOException {

        String mutantOperators="";
        switch (operator){
            case 1:
                mutantOperators="-*/";
                break;
            case 2:
                mutantOperators="+*/";
                break;
            case 3:
                mutantOperators="+-/";
                break;
            case 4:
                mutantOperators="+-*";
                break;
        }

        ArrayList<String> mutantAllLines= (ArrayList<String>) allLines.clone();

        for(int i =0;i<mutantOperators.length();i++) {
            //mutate program
            StringBuilder sb = new StringBuilder(mutantAllLines.get(lineIndex));
            sb.setCharAt(charIndex,mutantOperators.charAt(i));
            mutantAllLines.set(lineIndex,sb.toString());
            //output mutated program
            String outputFileName="mutant"+mutantIndex+".py";
            FileWriter writer = new FileWriter(outputFileName);
            for (String str : mutantAllLines) {
                writer.write(str + System.lineSeparator());
            }
            writer.close();
            //create library
            output.add("MUTANT No. "+mutantIndex);
            output.add("line "+(lineIndex+1)+":"+allLines.get(lineIndex));
            output.add("changed to -> "+mutantAllLines.get(lineIndex));
            output.add("mutant type: '"+mutantOperators.charAt(i)+"'");
            output.add(" ");


            mutantIndex++;

        }
        return mutantIndex;
    }
/*
    static int simulate(int mutantIndex, ArrayList<Integer> parameters) throws IOException, InterruptedException {
        String mutantFileName = "mutant"+mutantIndex+".py";
        String mutantOutputFile ="mutant"+mutantIndex+".txt";

        for(int p:parameters) {
            Process process;
            String command = "cmd.exe /c python " + mutantFileName + " " + p + " >> " + mutantOutputFile;
            process = Runtime.getRuntime().exec(command);
            process.waitFor();
        }

        Path path1 = Paths.get(mutantOutputFile);
        ArrayList<String> mutantResults = new ArrayList<>(Files.readAllLines(path1, StandardCharsets.UTF_8));
        Path path2 = Paths.get("correctResults.txt");
        ArrayList<String> correctResults = new ArrayList<>(Files.readAllLines(path2, StandardCharsets.UTF_8));
        Boolean kill = false;
        String killCode=" killed by param: ";
        for(int i=0;i<parameters.size();i++){
            if(mutantResults.get(i)!=correctResults.get(i)){
                kill=true;
                killCode+=parameters.get(i)+", ";
            }
        }
        output.set(5*mutantIndex,output.get(5*mutantIndex)+killCode);
        return(0);
    }
*/

}




class SimulationThread implements Runnable
{
    int threadId;
    int totalMutants;
    ArrayList<Integer> parameters;
    public void run()
    {
        for(int i =threadId*totalMutants/3; i<totalMutants; i++){
            try {
                simulate(i,parameters);
            } catch (IOException e) { e.printStackTrace();
            } catch (InterruptedException e) { e.printStackTrace();
            }
        }
        return;
    }

    public SimulationThread(int threadId,int totalMutants, ArrayList<Integer> parameters){
        this.threadId=threadId;
        this.totalMutants=totalMutants;
        this.parameters=parameters;
    }

    int simulate(int mutantIndex, ArrayList<Integer> parameters) throws IOException, InterruptedException {
        String mutantFileName = "mutant"+mutantIndex+".py";
        String mutantOutputFile ="mutant"+mutantIndex+".txt";

        for(int p:parameters) {
            Process process;
            String command = "cmd.exe /c python " + mutantFileName + " " + p + " >> " + mutantOutputFile;
            process = Runtime.getRuntime().exec(command);
            process.waitFor();
        }

        Path path1 = Paths.get("correctResults.txt");
        ArrayList<String> correctResults = new ArrayList<>(Files.readAllLines(path1, StandardCharsets.UTF_8));
        Path path2 = Paths.get(mutantOutputFile);
        ArrayList<String> mutantResults = new ArrayList<>(Files.readAllLines(path2, StandardCharsets.UTF_8));

        Boolean kill = false;
        String killMsg=" killed by param: ";
        for(int p=0;p<parameters.size();p++){
            if(!mutantResults.get(p).equals(correctResults.get(p))){
                kill=true;
                killMsg+=parameters.get(p)+", ";
            }
        }
        print(mutantIndex,killMsg);
        return 0;

    }

    //print kill results to mutantLibrary
    synchronized void print(int mutantIndex,String killMsg) throws IOException {
        //read mutantLibrary
        String mutantLibrary="mutantLibrary.txt";
        Path path = Paths.get(mutantLibrary);
        ArrayList<String> output = new ArrayList<>(Files.readAllLines(path, StandardCharsets.UTF_8));
        //add kill results
        output.set(5*mutantIndex,output.get(5*mutantIndex)+killMsg);
        //write to mutantLibrary
        FileWriter libWriter = new FileWriter(mutantLibrary);
        for (String str : output) {
            libWriter.write(str + System.lineSeparator());
        }
        libWriter.close();

        return;
    }
}
