package com.example.problemservice.converter;


import com.example.problemservice.core.ProblemStructure;

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
}
