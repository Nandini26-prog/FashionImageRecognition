package org.example;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;

public class FashionImageSearch {

        public static void main(String[] args) throws Exception {
            // Load the input image
            StringimageData = "iVBORw0KGg..."; // base64 encoded image data
            BufferedImage inputImage = ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(imageData)));

            // Extract features from the input image using OpenCV
            Mat inputMat = new Mat(inputImage.getHeight(), inputImage.getWidth(), CvType.CV_8UC3);
            Imgcodecs.imencode(".jpg", inputImage, inputMat);
            MatOfKeyPoint keypoints = new MatOfKeyPoint();
            FeatureDetector detector = FeatureDetector.create(FeatureDetector.SIFT);
            detector.detect(inputMat, keypoints);
            DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.SIFT);
            Mat descriptors = new Mat();
            extractor.compute(inputMat, keypoints, descriptors);

            // Index the features in Elasticsearch
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.createObjectNode();
            jsonNode.put("image_id", "input_image");
            jsonNode.put("features", descriptors.dump());
            jsonNode.put("metadata", mapper.createObjectNode().put("category", "t-shirts").put("color", "blue").put("style", "casual"));
            String json = jsonNode.toString();
            // Index the JSON object in Elasticsearch

            // Search for similar images in Elasticsearch
            String query = "{\"query\":{\"match\":{\"features\":{\"query\":\"" + descriptors.dump() + "\",\"operator\":\"AND\"}}}}";
            // Execute the search query in Elasticsearch
            JsonNode searchResult = // execute search query and get the result

                    // Display the matches found
                    List<String> matches = new ArrayList<>();
            for (JsonNode hit : searchResult.get("hits").get("hits")) {
                String imageId = hit.get("_source").get("image_id").asText();
                String category = hit.get("_source").get("metadata").get("category").asText();
                String color = hit.get("_source").get("metadata").get("color").asText();
                String style = hit.get("_source").get("metadata").get("style").asText();
                matches.add("Image ID: " + imageId + ", Category: " + category + ", Color: " + color + ", Style: " + style);
            }
            System.out.println("Matches found:");
            for (String match : matches) {
                System.out.println(match);
            }
        }
    }
}
