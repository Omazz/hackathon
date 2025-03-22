package com.example.AlgosWeb.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "boxes")
public class Box {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "height")
    private String height;

    @Column(name = "width")
    private String width;

    @Column(name = "length")
    private String length;

    @Column(name = "is_fragile")
    private Boolean isFragile;

    public Box() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public Boolean getFragile() {
        return isFragile;
    }

    public void setFragile(Boolean fragile) {
        isFragile = fragile;
    }
}
