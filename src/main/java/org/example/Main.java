package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.File;
import java.io.IOException;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        // Read input from JSON file
        JsonNode input = mapper.readTree(new File("input.json"));

        // Resolving Processing
        TreeResolver resolver = new TreeResolver();
        JsonNode output = resolver.resolve(input);

        // Print the output
        System.out.println(
                mapper.writerWithDefaultPrettyPrinter().writeValueAsString(output)
        );
    }
}