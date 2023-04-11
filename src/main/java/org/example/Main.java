package org.example;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("------------------Welcome to NYT Articles Search------------------");
        NYTSearchApiResponse articles;
        while (true){
            System.out.println("What would you want to search about: ");
            String search = sc.next();
            try {
                articles = getArticlesBySearch(search);
                for (Article article: articles.response.docs){
                    System.out.println();
                    System.out.println(article.headline.main);
                    System.out.println(article.byline.original);
                    System.out.println(article.section_name);
                    System.out.println(article.lead_paragraph);
                    System.out.println(article.pub_date);
                    System.out.println("---------------------------------------");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.out.println("Do you want to search for more articles (Y/N)?");
            String repeat = sc.next();
            if(!repeat.equalsIgnoreCase("y")){
                if(!repeat.equalsIgnoreCase("n")){
                    System.out.println("Invalid Input");
                    break;
                }
            }
        }
    }

    private static NYTSearchApiResponse getArticlesBySearch(String search) throws IOException {
        String apiUrl = "https://api.nytimes.com/svc/search/v2/articlesearch.json?q="+search+"&api-key=F5sDtSkCBG8qfXGtrJcwktG8YdrIL9pX";
        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("HTTP error code : " + conn.getResponseCode());
        }

        BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
        String output;
        StringBuilder json = new StringBuilder();

        while ((output = br.readLine()) != null) {
            json.append(output);
        }

        conn.disconnect();

        Gson gson = new Gson();
        NYTSearchApiResponse apiResponses = gson.fromJson(json.toString(), NYTSearchApiResponse.class);
        return apiResponses;
    }


}