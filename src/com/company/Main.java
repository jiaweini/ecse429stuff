package com.company;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.io.FileWriter;
import java.lang.StringBuilder;

public class Main {
    static int plus;
    static int minus;
    static int times;
    static int divide;
    static ArrayList<String> output=new ArrayList<String>();

    public static void main(String[] args) throws IOException {

        //read code to be mutated
        String fileName = "Helloworld.java";
        Path path = Paths.get(fileName);
        byte[] bytes = Files.readAllBytes(path);
        ArrayList<String> allLines = new ArrayList<>(Files.readAllLines(path, StandardCharsets.UTF_8));

        //Generate a copy of original code
        FileWriter writer = new FileWriter("original.java");
        for (String str : allLines) {
            writer.write(str + System.lineSeparator());
        }
        writer.close();

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

        output.add("'+' mutant: "+plus+"\n'-' mutant: "+minus+"\n'*' mutant: "+times+"\n'/' mutant: "+divide);
        String outputFileName="mutantLibrary.txt";
        FileWriter libWriter = new FileWriter(outputFileName);
        for (String str : output) {
            libWriter.write(str + System.lineSeparator());
        }
        libWriter.close();

    }

    public static int mutate(int mutantIndex,ArrayList<String> allLines,int lineIndex,int charIndex, int operator) throws IOException {

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
            String outputFileName="mutant"+mutantIndex+".txt";
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
}
/*

*/