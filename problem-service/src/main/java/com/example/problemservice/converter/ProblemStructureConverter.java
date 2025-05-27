package com.example.problemservice.converter;


import com.example.problemservice.core.DataField;
import com.example.problemservice.core.ProblemStructure;

import java.util.ArrayList;
import java.util.List;

public class ProblemStructureConverter {
    public static String convertObjectToString(ProblemStructure object) {
        StringBuilder sb = new StringBuilder();

        sb.append("Problem Name: ");
        sb.append(object.getProblemName());
        sb.append("\n");

        sb.append("Function Name: ");
        sb.append(object.getFunctionName());
        sb.append("\n");

        sb.append("Input Structure: ");
        sb.append("\n");

        for (int i = 0; i < object.getInputStructure().size(); i++) {
            sb.append("Input Field: ");
            sb.append(object.getInputStructure().get(i).getType());
            sb.append(" ");
            sb.append(object.getInputStructure().get(i).getName());
            sb.append("\n");
        }

        sb.append("Output Structure: ");
        sb.append("\n");

        for (int i = 0; i < object.getOutputStructure().size(); i++) {
            sb.append("Output Field: ");
            sb.append(object.getOutputStructure().get(i).getType());
            sb.append(" ");
            sb.append(object.getOutputStructure().get(i).getName());
            sb.append("\n");
        }

        return sb.toString();
    }

    public static ProblemStructure convertStringToObject(String input) {
        ProblemStructure.ProblemStructureBuilder builder = ProblemStructure.builder();
        List<DataField> inputFields = new ArrayList<>();
        List<DataField> outputFields = new ArrayList<>();

        String[] lines = input.split("\\n");
        int i = 0;

        if (lines[i].startsWith("Problem Name: ")) {
            builder.problemName(lines[i++].substring("Problem Name: ".length()));
        }

        if (i < lines.length && lines[i].startsWith("Function Name: ")) {
            builder.functionName(lines[i++].substring("Function Name: ".length()));
        }

        if (i < lines.length && lines[i].startsWith("Input Structure:")) {
            i++; // skip this line
        }

        while (i < lines.length && lines[i].startsWith("Input Field: ")) {
            String[] parts = lines[i++].substring("Input Field: ".length()).split(" ");
            if (parts.length >= 2) {
                inputFields.add(new DataField(parts[0], parts[1]));
            }
        }

        if (i < lines.length && lines[i].startsWith("Output Structure:")) {
            i++; // skip this line
        }

        while (i < lines.length && lines[i].startsWith("Output Field: ")) {
            String[] parts = lines[i++].substring("Output Field: ".length()).split(" ");
            if (parts.length >= 2) {
                outputFields.add(new DataField(parts[0], parts[1]));
            }
        }

        builder.inputStructure(inputFields);
        builder.outputStructure(outputFields);
        return builder.build();
    }
}
