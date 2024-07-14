package org.example;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.imageio.ImageIO;

import org.opencv.core.*;
//import org.opencv.features2d.DescriptorExtractor;
//import org.opencv.features2d.FeatureDetector;

import org.opencv.features2d.ORB;  // Replace DescriptorExtractor and FeatureDetector with ORB or any other Feature2D subclass
import org.opencv.features2d.SIFT;
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

        public static void main(String[] args) throws Exception {
            // Load the input image
            String imageData = "iVBORw0KGgoAAAANSUhEUgAAAJYAAACWCAYAAADYiZgNAAAAOXRFWHRTb2Z0d2FyZQBNYXRwbG90bGliIHZlcnNpb24zLjQuMywgaHR0cHM6Ly9tYXRwbG90bGliLm9yZy/4";
            // base64 encoded image data
            BufferedImage inputImageputImage = ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(imageData)));

            // Extract features from the input image using OpenCV
            Mat inputImage = bufferedImageToMat(inputImageputImage);
            Mat inputMat = new Mat(inputImage.height(), inputImage.width(), CvType.CV_8UC3);
            Imgcodecs.imencode(".jpg", inputImage, (MatOfByte) inputMat);
            MatOfKeyPoint keypoints = new MatOfKeyPoint();
            SIFT sift = SIFT.create();

            sift.detect(inputMat, keypoints);
          //  DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.SIFT);
            Mat descriptors = new Mat();
            sift.compute(inputMat, keypoints, descriptors);


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

