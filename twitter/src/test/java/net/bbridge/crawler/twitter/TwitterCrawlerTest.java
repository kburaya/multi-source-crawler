package net.bbridge.crawler.twitter;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import net.bbridge.crawler.utils.TestUtil;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by kseniya on 22/12/16.
 */
public class TwitterCrawlerTest {

    private TwitterCrawler twitterCrawler = new TwitterCrawler();

    @Test
    public void testURLResponseWithTrueUserName() throws Exception {
        String userName = "happyksuh";
        String urlResponse = twitterCrawler.requestTimelinePage(userName, null);
        String expected = TestUtil.readResource("twitter-good.json");
        JsonNode schemaNode = JsonLoader.fromString(expected);
        JsonNode response = JsonLoader.fromString(urlResponse);

        JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
        // load the schema and validate
        JsonSchema schema = factory.getJsonSchema(schemaNode);
        ProcessingReport result = schema.validate(response);
        assertTrue(result.isSuccess());
    }

    @Test
    public void testURLResponseWithWrongUserName() throws Exception {
        String userName = "23508ygdfjgbws";
        String urlResponse = twitterCrawler.requestTimelinePage(userName, null);
        String expected = TestUtil.readResource("twitter-empty.json");
        JsonNode schemaNode = JsonLoader.fromString(expected);
        JsonNode response = JsonLoader.fromString(urlResponse);

        JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
        // load the schema and validate
        JsonSchema schema = factory.getJsonSchema(schemaNode);
        ProcessingReport result = schema.validate(response);
        assertTrue(result.isSuccess());
    }
}
