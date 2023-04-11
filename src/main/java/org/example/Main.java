package org.example;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("------------------Welcome to NYT Articles Search------------------");
        NYTSearchApiResponse response;
        while (true){
            System.out.println("What would you want to search about: ");
            String search = sc.next();
            try {
                response = getArticlesBySearch(search);
                printArticles(response.response.docs);
                addArticlesToDB(response.response.docs);
            } catch (Exception e) {
                System.out.println(e);
            }
            System.out.println("Do you want to search for more articles (Y/N)?");
            String repeat = sc.next();
            if(!repeat.equalsIgnoreCase("y")){
                if(!repeat.equalsIgnoreCase("n")){
                    System.out.println("Invalid Input");
                }
                break;
            }
        }
    }
    static void addArticlesToDB(Article[] articles) throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        String url = "jdbc:sqlserver://localhost:1433;" + "databaseName=NYT;" + "encrypt=true;"
                + "trustServerCertificate=true";

        Driver driver = (Driver) Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver").newInstance();
        DriverManager.registerDriver(driver);
        Connection con = DriverManager.getConnection(url, "sa", "root");
        for (Article article:articles) {
            PreparedStatement st = con.prepareStatement("insert into articles\n" +
                    "values (?,?,?,?,?)");
            st.setString(1, article.headline.main);
            st.setString(2, article.byline.original);
            st.setString(3, article.pub_date);
            st.setString(4, article.section_name);
            st.setString(5, article.lead_paragraph);
            st.executeUpdate();
        }
        con.close();

    }
    static void printArticles(Article[] articles){
        for (Article article: articles){
            System.out.println("---------------------------------------");
            System.out.println(article.headline.main);
            System.out.println(article.byline.original);
            System.out.println(article.section_name);
            System.out.println(article.lead_paragraph);
            System.out.println(article.pub_date);
            System.out.println("---------------------------------------");
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