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

    public static void main(String[] args) throws IOException {

        String fileName = "Helloworld.java";
        Path path = Paths.get(fileName);
        byte[] bytes = Files.readAllBytes(path);
        ArrayList<String> allLines = new ArrayList<>(Files.readAllLines(path, StandardCharsets.UTF_8));

        int mutantIndex=0;

        for(int i =0;i<allLines.size();i++){
            String thisLine=allLines.get(i);
            for(int c=0;c<thisLine.length();c++){
                switch(thisLine.charAt(c)){
                    case '+': //operator 1
                        mutantIndex = mutate(mutantIndex,allLines,i,c,1);
                        break;
                    case '-': //operator 2
                        mutantIndex = mutate(mutantIndex,allLines,i,c,2);
                        break;
                    case '*': //operator 3
                        mutantIndex = mutate(mutantIndex,allLines,i,c,3);
                        break;
                    case '/': //operator 4
                        if(c!=thisLine.length()-1&&thisLine.charAt(c+1)=='/'){
                            continue;
                        }else{
                            mutantIndex = mutate(mutantIndex,allLines,i,c,4);
                            break;
                        }
                }
            }
        }

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

            StringBuilder sb = new StringBuilder(mutantAllLines.get(lineIndex));
            sb.setCharAt(charIndex,mutantOperators.charAt(i));
            mutantAllLines.set(lineIndex,sb.toString());
            mutantAllLines.add("line "+(lineIndex+1)+":"+allLines.get(lineIndex));



            mutantIndex++;
            String outputFileName="mutant"+mutantIndex+".java";
            FileWriter writer = new FileWriter(outputFileName);
            for (String str : mutantAllLines) {
                writer.write(str + System.lineSeparator());
            }
            writer.close();
        }
        return mutantIndex;
    }
}
/*

*/