package com.cgi.reservation.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

public class TablePositionUpdateDTO {

    @DecimalMin(value = "0", message = "posX peab olema vähemalt 0")
    @DecimalMax(value = "1040", message = "posX ei tohi ületada 1040")
    private double posX;

    @DecimalMin(value = "0", message = "posY peab olema vähemalt 0")
    @DecimalMax(value = "770", message = "posY ei tohi ületada 770")
    private double posY;

    public TablePositionUpdateDTO() {}

    public double getPosX() { return posX; }
    public void setPosX(double posX) { this.posX = posX; }

    public double getPosY() { return posY; }
    public void setPosY(double posY) { this.posY = posY; }
}
