package com.piyushmittal.toplibraries;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = ToplibrariesApplication.class)
public class ToplibrariesApplicationTests {

    String google_key = "";
    String google_cx = "";

    @Autowired
    TopLibrariesAnalytics analytics;

    @Test
    public void callServiceTest() {
        Matcher m = analytics.callService("https://www.linkedin.com");
        assert(m.find());
        m = analytics.callService("https://www.google.com");
        assert(m.find());
    }

    @Test
    public void webCrawlerTest() throws InterruptedException, ExecutionException, IOException {
       analytics.webCrawler("facebook",google_key,google_cx);
       assert(analytics.hm.size()>5);
    }

}
