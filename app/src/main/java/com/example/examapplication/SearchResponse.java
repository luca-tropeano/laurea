package com.example.examapplication;

import java.util.List;

public class SearchResponse {

    private List<Item> items;

    public List<Item> getItems() {
        return items;
    }

    public static class Item {
        private String title;
        private String link;

        public String getTitle() {
            return title;
        }

        public String getLink() {
            return link;
        }
    }
}
