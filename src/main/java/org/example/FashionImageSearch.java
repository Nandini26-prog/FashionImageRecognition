package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.HttpHost;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.SIFT;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class FashionImageSearch {

    private static RestHighLevelClient client; // Initialize your Elasticsearch client here

    private static Mat bufferedImageToMat(BufferedImage bufferedImage) {
        // Convert BufferedImage to Mat
        int type = bufferedImage.getType() == 0 ? BufferedImage.TYPE_3BYTE_BGR : bufferedImage.getType();
        BufferedImage convertedImg = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), type);
        convertedImg.getGraphics().drawImage(bufferedImage, 0, 0, null);
        byte[] data = ((DataBufferByte) convertedImg.getRaster().getDataBuffer()).getData();
        Mat mat = new Mat(bufferedImage.getHeight(), bufferedImage.getWidth(), CvType.CV_8UC3);
        mat.put(0, 0, data);
        return mat;
    }

    private static void indexToElasticsearch(Mat descriptors) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode jsonNode = mapper.createObjectNode();
        jsonNode.put("image_id", "input_image");
        ObjectNode metadataNode = mapper.createObjectNode();
        metadataNode.put("category", "t-shirts");
        metadataNode.put("color", "blue");
        metadataNode.put("style", "casual");
        jsonNode.set("metadata", metadataNode);
        jsonNode.put("features", descriptors.dump());
        String json = mapper.writeValueAsString(jsonNode);


        IndexRequest indexRequest = new IndexRequest("fashion_image_index").id("input_image").source(json, XContentType.JSON);
        client.index(indexRequest, RequestOptions.DEFAULT);
    }

    private static List<String> searchInElasticsearch(Mat descriptors) throws Exception {
        List<String> matches = new ArrayList<>();

        SearchRequest searchRequest = new SearchRequest("fashion_image_index");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.matchQuery("features", descriptors.dump()));
        searchRequest.source(sourceBuilder);

        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
        for (SearchHit hit : response.getHits().getHits()) {
            String imageId = hit.getSourceAsMap().get("image_id").toString();
            String category = hit.getSourceAsMap().get("metadata.category").toString();
            String color = hit.getSourceAsMap().get("metadata.color").toString();
            String style = hit.getSourceAsMap().get("metadata.style").toString();
            matches.add("Image ID: " + imageId + ", Category: " + category + ", Color: " + color + ", Style: " + style);
        }

        return matches;
    }

    public static void main(String[] args) throws Exception {

        RestClientBuilder builder = RestClient.builder(
                new HttpHost("localhost", 9200, "https")
        );
        RestClient restClient = builder.build();

        // Load the input image (base64 encoded image data)
        String imageData =
         "zL3BsYXRmb3JtL3JkZjpzdFJlZElEPSJhZG9iZTpjbGFzc2VzOmRzIj4gPC9zdFJlZD48L3R5cGU+IDxzdFJlZD48YmFzZT5pbWFnZTwvYmFzZT48L3N0UmVkPjwvcmRmOlJERj48L3g6eG1wbWV0YT4gPD94cGFja2V0IGJlZ2luPSLvu78iIGlkPSJXNU0wTXBDZWhpSHpyZVN6TlRjemtjOWQiPz4gPHRzcGFuSW5jcnlwdGlvbiA6cGFnZW9wPSIiPjwvTHRzcGFuSW5jcnlwdGlvbj4gPC9yZGY6UkRGIHhtbG5zOnJkZj0iaHR0cDovL3B1cmwub3JnL2RzL3BsYXRmb3JtL3JkZjpzdFJlZElEPSJhZG9iZTpjbGFzc2VzOmRzIj4gPC9zdFJlZD48L3R5cGU+IDxzdFJlZD48YmFzZT5pbWFnZTwvYmFzZT48L3N0UmVkPjwvcmRmOlJERj48L3g6eG1wbWV0YT4gPD94cGFja2V0IGJlZ2luPSLvu78iIGlkPSJXNU0wTXBDZWhpSHpyZVN6TlRjemtjOWQiPz4gPHRzcGFuSW5jcnlwdGlvbiA6cGFnZW9wPSIiPjwvTHRzcGFuSW5jcnl";
        BufferedImage inputImage = ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(imageData)));

        // Extract features from the input image using OpenCV
        Mat inputMat = bufferedImageToMat(inputImage);
        MatOfKeyPoint keypoints = new MatOfKeyPoint();
        SIFT sift = SIFT.create();
        sift.detect(inputMat, keypoints);
        Mat descriptors = new Mat();
        sift.compute(inputMat, keypoints, descriptors);

        // Index the features in Elasticsearch
        indexToElasticsearch(descriptors);

        // Search for similar images in Elasticsearch
        List<String> matches = searchInElasticsearch(descriptors);

        // Display the matches found
        System.out.println("Matches found:");
        for (String match : matches) {
            System.out.println(match);
        }
    }
}
