package com.example.csanchez.ift2905_wordonthestreet;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class News {
    String title;
    String description;
    String source;
    String author;
    String image;
    String url;
    Date date;
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);

    public News(String _title, String _description, String _source, String _author, String _image, String _url, String _date) throws ParseException {
        this.title       = _title;
        this.description = _description;
        this.source      = _source;
        this.author      = _author;
        this.image       = _image;
        this.url         = _url;
        if(_date != null) {
            try {
                this.date = df.parse(_date);
            } catch (ParseException e) {
                //Got null date, putting actual time and date as placeholder
                this.date = new Date();
            }
        }else{
            this.date = new Date();
        }

    }

}
