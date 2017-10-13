package net.bbridge.crawler.instagram;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import net.bbridge.crawler.utils.TestUtil;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by kseniya on 22/12/16.
 */
public class MediaExtractorTest {

    private MediaExtractor mediaExtractor = new MediaExtractor();

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void getJSONFromPageCorrectURL() throws Exception {
        String imageURL = "https://www.instagram.com/p/BOOTNXcBvdb";
        String imageInfoString = mediaExtractor.getJSONFromImagePage(imageURL);

        String expected = TestUtil.readResource("media-extractor.json");
        JsonNode schemaNode = JsonLoader.fromString(expected);
        JsonNode response = JsonLoader.fromString(imageInfoString);

        JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
        // load the schema and validate
        JsonSchema schema = factory.getJsonSchema(schemaNode);
        ProcessingReport result = schema.validate(response);
        assertTrue(result.isSuccess());
    }

    @Test
    public void getJSONFromPageSourceWrongURL() {
        String imageURL = "https://www.instagram.com/BOOTNXcBvdb348tyeg";
        String imageInfoString = mediaExtractor.getJSONFromImagePage(imageURL);
        assertNull(imageInfoString);
    }
}
