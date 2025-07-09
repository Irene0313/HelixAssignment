package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;
import java.util.*;

public class TreeResolver {

    private final ObjectMapper mapper = new ObjectMapper();

    // Used for future 'ref' checking
    private final Map<String, JsonNode> pathMap = new LinkedHashMap<>();

    // Constructor
    public TreeResolver() {
    }

    public JsonNode resolve(JsonNode root) {
        return this.expand(root, new ArrayList<>());
    }

    private JsonNode expand(JsonNode node, List<String> path) {

        // ---------- Sequence----------
        if (node.isObject() && node.has("seq")) {
            JsonNode seq = node.get("seq");
            int start = seq.get("start").asInt();
            int end   = seq.get("end").asInt();

            ArrayNode arr = mapper.createArrayNode();
            for (int i = start; i <= end; i++) {
                arr.add(TextNode.valueOf("'" + i + "'"));
            }

            // Record the canonical path to this node for future reference
            this.recordPath(path, arr);

            return arr;
        }

        // ---------- Reference  ----------
        if (node.isObject() && node.has("ref")) {
            String refPath = node.get("ref").asText();

            JsonNode target;

            // Canonical Path
            if (refPath.startsWith("/")){
                // Check whether the given reference is an ancestor reference
                String currentPath = "/" + String.join("/", path);

                if (currentPath.startsWith(refPath) && !currentPath.equals(refPath)) {
                    throw new IllegalArgumentException("Invalid Ancestor Path Reference Detected: " + refPath);
                }

                target = pathMap.get(refPath);
                if (target == null) {
                    throw new IllegalArgumentException("Invalid Reference Path Detected: " + refPath);
                }
            }
            else{
                // Short Path
                target = searchByShortPath(refPath);
            }

            return target.deepCopy(); // Handle condition when two ref have same value -> aim for separation
        }

        // ---------- Array  ----------
        if (node.isArray()) {
            ArrayNode output = mapper.createArrayNode();
            int index = 0;

            for (JsonNode item : node) {
                boolean isSeq = item.isObject() && item.has("seq");

                if (isSeq) {
                    // Expand Sequence node
                    ArrayNode seqArr = (ArrayNode) expand(item, path);

                    // insert each sequence node into expand tree
                    for (JsonNode child : seqArr) {
                        List<String> childPath = new ArrayList<>(path);
                        childPath.add(String.valueOf(index));
                        output.add(child);
                        recordPath(childPath, child);
                        index++;
                    }
                }
                else {
                    // Node type expect for sequence i.e., atom, reference, hierarchy object and array
                    List<String> childPath = new ArrayList<>(path);
                    childPath.add(String.valueOf(index));
                    JsonNode expanded = expand(item, childPath);
                    output.add(expanded);
                    index++;
                }
            }


            recordPath(path, output);
            return output;
        }

        // ---------- Hierarchy Object ----------
        // Key is an Atom, Value is a node [Sub-Tree]
        if (node.isObject() && node.size() == 1 && !node.has("ref")) {
            String key = node.fieldNames().next();
            List<String> subPath = new ArrayList<>(path);
            subPath.add(key);
            ObjectNode output = mapper.createObjectNode();

            // Recursion Method
            output.set(key, expand(node.get(key), subPath));

            this.recordPath(path, output);
            return output;
        }

        // // ---------- Atom  ----------
        this.recordPath(path, node);
        return node;

    }

    /**
     * Records the canonical path of the current node into the path map.
     * @param path path to current node
     * @param node current node
     */
    private void recordPath(List<String> path, JsonNode node) {
        String canonicalPath = "/" + String.join("/", path);
        pathMap.put(canonicalPath, node);
    }


    /**
     * Search a node using short path in the pathMap
     * Short Path Example: 'A/0'
     *
     * The path is unique and unambiguous.
     * @param shortPath
     * @return a JsonNode object which is stored in the destination path
     */
    public JsonNode searchByShortPath(String shortPath) {

        // Check the format of short path
        if (!shortPath.matches("[A-Za-z][A-Za-z0-9']*(?:/[A-Za-z0-9']+)*")) {
            throw new IllegalArgumentException("Invalid Format of Short Path Detected: " + shortPath);
        }


        String[] parts = shortPath.split("/");

        //List<JsonNode> matches = new ArrayList<>();
        //List<String> anchor = List.of(parts[0]);
        String anchor = parts[0];

        JsonNode match = null;
        int hits = 0; // record the number of match

        // Loop through all the record paths
        for (Map.Entry<String, JsonNode> entry : pathMap.entrySet()) {
            //List<String> candidate = Arrays.asList(entry.getKey().substring(1).split("/")); // remove the first '/'

            String[] nodes = entry.getKey().substring(1).split("/"); // 去掉前导 '/'

            for (int position = 0; position < nodes.length; position++) {
                if (!nodes[position].equals(anchor)) continue;

                // Find the position where the anchor exist
                boolean isMatch = true;
                for (int i = 1; i < parts.length; i++) {
                    int j = position + i;
                    if (j >= nodes.length || !nodes[j].equals(parts[i])) {
                        isMatch = false;
                        break;
                    }
                }
                if (!isMatch) continue;

                hits++;

                if (hits > 1)
                    throw new IllegalArgumentException("Ambiguous Reference Short Path Detected: " + shortPath);

                match = entry.getValue();
                break;
            }


        }

        if (match == null) {
            throw new IllegalArgumentException("Invalid Reference Short Path Detected: " + shortPath);
        }

        return match;
    }

}
