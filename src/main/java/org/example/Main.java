package org.example;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("------------------Welcome to NYT Articles Search------------------");
        Article[] articles = null;
        while (true) {
            System.out.println("Select an Action");
            System.out.println("1- Search articles from Api");
            System.out.println("2- Search articles from DB");
            System.out.println("3- Search Most Popular articles");
            System.out.println("4- Exit");
            String selection = sc.next();
            if (selection.equals("1")) {
                articles = articlesFromApiOption();
            } else if (selection.equals("2")) {
                articles = articlesFromDBOption();
            } else if (selection.equals("3")) {
                try {
                    printArticles(getMostPopArticles());
                } catch (IOException e) {
                    System.out.println(e);
                }
            } else if (selection.equals("4")) {
                break;
            } else {
                System.out.println("Invalid Input");
            }
        }

    }

    private static Article[] getMostPopArticles() throws IOException {
        String apiUrl = "https://api.nytimes.com/svc/search/v2/articlesearch.json?q=most%20popular&api-key=F5sDtSkCBG8qfXGtrJcwktG8YdrIL9pX";
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
        return apiResponses.response.docs;
    }

    private static Article[] articlesFromDBOption() {
        Scanner sc = new Scanner(System.in);
        System.out.print("From Which Category You want to get the articles from?: ");
        String category = sc.next();
        Article[] articles;
        try {
            articles = getArticlesByCategory(category);
            printArticles(articles);
            return articles;
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    static Article[] getArticlesByCategory(String category) throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        String url = "jdbc:sqlserver://localhost:1433;" + "databaseName=NYT;" + "encrypt=true;"
                + "trustServerCertificate=true";

        Driver driver = (Driver) Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver").newInstance();
        DriverManager.registerDriver(driver);
        Connection con = DriverManager.getConnection(url, "sa", "root");
        Statement st = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        ResultSet rs = st.executeQuery("SELECT * FROM articles WHERE LOWER(category) LIKE '%" + category + "%' order by publish_date");
        rs.last();
        Article[] articles = new Article[rs.getRow()];
        rs.beforeFirst();
        int i = 0;
        while (rs.next()) {
            Article article = new Article();
            article.headline.main = rs.getString(2);
            article.byline.original = rs.getString(3);
            article.pub_date = rs.getString(4);
            article.section_name = rs.getString(5);
            article.lead_paragraph = rs.getString(6);
            articles[i] = article;
            i++;
        }
        return articles;
    }

    private static Article[] articlesFromApiOption() {
        NYTSearchApiResponse response;
        Scanner sc = new Scanner(System.in);
        System.out.print("What would you want to search about: ");
        String search = sc.next();
        try {
            response = getArticlesBySearch(search);
            printArticles(response.response.docs);
            addArticlesToDB(response.response.docs);
            return response.response.docs;
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    static void addArticlesToDB(Article[] articles) throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        String url = "jdbc:sqlserver://localhost:1433;" + "databaseName=NYT;" + "encrypt=true;"
                + "trustServerCertificate=true";

        Driver driver = (Driver) Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver").newInstance();
        DriverManager.registerDriver(driver);
        Connection con = DriverManager.getConnection(url, "sa", "root");
        for (Article article : articles) {
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

    static void printArticles(Article[] articles) {
        for (Article article : articles) {
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
        String apiUrl = "https://api.nytimes.com/svc/search/v2/articlesearch.json?q=" + search + "&api-key=F5sDtSkCBG8qfXGtrJcwktG8YdrIL9pX";
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