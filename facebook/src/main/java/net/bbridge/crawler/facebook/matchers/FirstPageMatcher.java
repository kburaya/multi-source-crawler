package net.bbridge.crawler.facebook.matchers;

import net.bbridge.crawler.exception.DataRetrievalException;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class FirstPageMatcher extends FacebookPostsFetcher {

    private static final String CONTAINER_ID_REGEX = "content:\\{pagelet_timeline_recent_segment_0_0_story:\\{container_id:\"(\\w*)\"}},";

    private ContainerMatcher containerMatcher = new ContainerMatcher(CONTAINER_ID_REGEX);

    @Override
    public String getUserData(Document pageDocument) throws DataRetrievalException, IOException {
        return containerMatcher.getContainerData(pageDocument);
    }
}
