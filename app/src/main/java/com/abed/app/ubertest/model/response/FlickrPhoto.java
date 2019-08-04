package com.abed.app.ubertest.model.response;

public class FlickrPhoto implements ApiResponse {

    private String id;

    private String secret;

    private String server;

    private int farm;

    private String title;

    public FlickrPhoto(String id, String secret, String server, int farm, String title) {
        this.id = id;
        this.secret = secret;
        this.server = server;
        this.farm = farm;
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public String getImageURL() {
        // URL Format-> http://farm{farm}.static.flickr.com/{server}/{id}_{secret}.jpg
        String imageUrl = "https://farm{farm}.static.flickr.com/{server}/{id}_{secret}.jpg";
        imageUrl = imageUrl.replace("{farm}", String.valueOf(farm));
        imageUrl = imageUrl.replace("{server}", server);
        imageUrl = imageUrl.replace("{id}", id);
        imageUrl = imageUrl.replace("{secret}", secret);
        return imageUrl;
    }


}