package com.example.csanchez.ift2905_wordonthestreet;

public class Source {
    String id;
    String name;
    String description;
    String url;
    String category;
    String language;
    String country;

    public Source(String _id, String _name, String _description, String _url, String _categorie, String _language, String _country){
        this.id          = _id;
        this.name        = _name;
        this.description = _description;
        this.url         = _url;
        this.category    = _categorie;
        this.language    = _language;
        this.country     = _country;
    }
}
