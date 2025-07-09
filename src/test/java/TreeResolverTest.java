import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.TreeResolver;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TreeResolverTest {

    private final ObjectMapper mapper = new ObjectMapper();

    // ---------- Atom Tests ----------
    @Test
    public void testAtomNode() throws Exception {
        String json = "[\"Hello\", \"World\"]";

        TreeResolver resolver = new TreeResolver();
        JsonNode root = mapper.readTree(json);
        JsonNode result = resolver.resolve(root);

        assertEquals("Hello", result.get(0).asText());
        assertEquals("World", result.get(1).asText());
    }

    // ---------- Sequence Tests ----------
    @Test
    public void testSequenceExpansion() throws Exception {
        String json = "[{ \"seq\": { \"start\": 1, \"end\": 3 } }]";
        TreeResolver resolver = new TreeResolver();
        JsonNode root = mapper.readTree(json);
        JsonNode result = resolver.resolve(root);

        assertTrue(result.isArray());
        assertEquals("'1'", result.get(0).asText());
        assertEquals("'2'", result.get(1).asText());
        assertEquals("'3'", result.get(2).asText());
    }



    // ---------- Reference Path Tests ----------
    @Test
    public void testShortPathSuccess() throws Exception {
        String json = "[ { \"A\": [\"X\"] } ]";
        TreeResolver resolver = new TreeResolver();
        JsonNode root = mapper.readTree(json);
        resolver.resolve(root);

        JsonNode result = resolver.searchByShortPath("A/0");
        assertEquals("X", result.asText());
    }

    @Test
    public void testInvalidShortPath() throws Exception {
        String json = "[ { \"A\": [\"X\"] } ]";

        TreeResolver resolver = new TreeResolver();
        JsonNode root = mapper.readTree(json);
        resolver.resolve(root);

        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            resolver.searchByShortPath("B");
        });

        assertTrue(e.getMessage().contains("Invalid Reference"));
    }

    @Test
    public void testShortPathAmbiguous() throws Exception {
        String json = "[ { \"A\": [\"X\"] }, { \"A\": [\"Y\"] } ]";
        TreeResolver resolver = new TreeResolver();
        JsonNode root = mapper.readTree(json);
        resolver.resolve(root);

        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            resolver.searchByShortPath("A/0");
        });

        assertTrue(e.getMessage().contains("Ambiguous"));
    }

    @Test
    public void testAncestorReference() throws Exception {
        String json = "[ { \"A\": [ { \"ref\": \"/0\" } ] } ]";

        TreeResolver resolver = new TreeResolver();
        JsonNode root = mapper.readTree(json);

        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            resolver.resolve(root);
        });

        assertTrue(e.getMessage().contains("Ancestor Path"));
    }

    @Test
    public void testForwardReference() throws Exception {
        String json = "[ { \"ref\": \"/1\" }, \"A\" ]";

        TreeResolver resolver = new TreeResolver();
        JsonNode root = mapper.readTree(json);

        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            resolver.resolve(root);
        });

        assertTrue(e.getMessage().contains("Invalid Reference"));
    }

    // ---------- Hierarchy Object Tests ----------
    @Test
    public void testHierarchyObject() throws Exception {
        String json = "[ { \"A\": [\"B\"] } ]";

        TreeResolver resolver = new TreeResolver();
        JsonNode root = mapper.readTree(json);
        JsonNode result = resolver.resolve(root);

        // result[0] = { "A": ["B"] }
        JsonNode obj = result.get(0);
        assertTrue(obj.has("A"));
        assertEquals("B", obj.get("A").get(0).asText());
    }

    // ---------- Array Tests ----------
    @Test
    public void testNestedArray() throws Exception {
        String json = "[ \"A\", [\"B\", \"C\"], \"D\" ]";

        TreeResolver resolver = new TreeResolver();
        JsonNode root = mapper.readTree(json);
        JsonNode result = resolver.resolve(root);

        assertEquals("A", result.get(0).asText());
        assertEquals("B", result.get(1).get(0).asText());
        assertEquals("C", result.get(1).get(1).asText());
        assertEquals("D", result.get(2).asText());
    }


}