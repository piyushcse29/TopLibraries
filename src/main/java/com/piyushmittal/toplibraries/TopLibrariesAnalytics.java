package com.piyushmittal.toplibraries;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class TopLibrariesAnalytics {

    ConcurrentHashMap<String, Integer> hm = null;
    ThreadPoolExecutor executor = null;

    TopLibrariesAnalytics() {
        hm = new ConcurrentHashMap<String, Integer>();
        executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(100);
    }

    /*
     This method first call the google api to get all of the links associated with search text.
     Thereafter for each link it get the html code and search for all the js libraries. The library count is tracked through map.
     Concurrent threads are used to achieve the task.
     */
    public void webCrawler(String searchString, String google_key, String google_cx) throws IOException, ExecutionException, InterruptedException {

        RestTemplate restTemplate = new RestTemplate();
        String fooResourceUrl
                = "https://www.googleapis.com/customsearch/v1?q=" + searchString + "&key=" + google_key + "&cx=" + google_cx + "";
        ResponseEntity<String> response
                = restTemplate.getForEntity(fooResourceUrl, String.class);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonObject = mapper.readTree(restTemplate.getForEntity(fooResourceUrl, String.class).getBody());
        int size = jsonObject.size();
        Future future = null;

        jsonObject.get("items").forEach((x) -> {
            final String urlToCall = x.get("link").asText();
            executor.submit(() -> {
                Matcher m = callService(urlToCall);
                while (null != m && m.find()) {
                    String library = m.group(2);
                    if (hm.containsKey(library)) {
                        hm.put(library, hm.get(library) + 1);
                    } else {
                        hm.put(library, 1);
                    }
                }
                return;
            });
        });

        if (future.isDone()) {
            System.out.println("Updating analytics data");
            Map<String, Integer> sortedMapDesc = sortByValue(hm, false);
            printMap(sortedMapDesc);
        }
    }

    /*
       This method calls the link that we got from google search. The html code is searched for js libraries.
     */
    Matcher callService(String url) {
        Matcher m = null;
        try {
            RestTemplate restTemplate = new RestTemplate();
            String fooResourceUrl = url;
            System.out.println(url);
            ResponseEntity<String> response = restTemplate.getForEntity(fooResourceUrl, String.class);
            Pattern p = Pattern.compile("(/)(\\w*-*\\w*\\d*-*[.]*\\w*-*\\w*\\d*-*)(.js[^on])");
            m = p.matcher(response.toString());

        } catch (Exception e) {
            System.out.println("Exception occurred while invoking the URL" + e);
        }
        return m;
    }

    /*
    Method to sort the hash map by values in descending order.
     */
    private static Map<String, Integer> sortByValue(Map<String, Integer> unsortMap, final boolean order) {
        List<Map.Entry<String, Integer>> list = new LinkedList<>(unsortMap.entrySet());

        // Sorting the list based on values
        list.sort((o1, o2) -> order ? o1.getValue().compareTo(o2.getValue()) == 0
                ? o1.getKey().compareTo(o2.getKey())
                : o1.getValue().compareTo(o2.getValue()) : o2.getValue().compareTo(o1.getValue()) == 0
                ? o2.getKey().compareTo(o1.getKey())
                : o2.getValue().compareTo(o1.getValue()));
        return list.stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b, LinkedHashMap::new));
    }

    /*
    Method to print top 5 results.
     */
    private static void printMap(Map<String, Integer> map) {
        System.out.println("Top 5 Libraries");
        Iterator it = map.entrySet().iterator();
        int i = 0;
        while (it.hasNext() && i++ < 5) {
            Map.Entry pair = (Map.Entry) it.next();
            System.out.println(pair.getKey() + " = " + pair.getValue());
        }
    }
}
