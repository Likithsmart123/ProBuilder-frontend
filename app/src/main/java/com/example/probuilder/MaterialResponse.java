package com.example.probuilder;

import java.io.Serializable;

public class MaterialResponse implements Serializable {

    public String material;
    public double current_price;
    public double predicted_price;
    public String trend;
    public double confidence;
}
