package com.webapp.web;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ratings")
public class Rating {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private int stars;
    private String description;
    private String username;

    public Rating(int stars, String description, String username) {
        this.stars = stars;
        this.description = description;
        this.username = username;
    }

    public Rating (int stars) {
        this.stars = stars;
    }

    public Rating() {
    }

    public int getId() {
        return id;
    }

    public int getStars() {
        return stars;
    }

    public String getDescription() {
        return description;
    }

    public String getUsername() {
        return username;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setStars(int stars) {
        this.stars = stars;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
