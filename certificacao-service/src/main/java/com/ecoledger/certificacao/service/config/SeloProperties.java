package com.ecoledger.certificacao.service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "certificacao.selo")
public class SeloProperties {

    private int expirationDays = 180;
    private int bronzeThreshold = 70;
    private int prataThreshold = 80;
    private int ouroThreshold = 90;

    public int getExpirationDays() {
        return expirationDays;
    }

    public void setExpirationDays(int expirationDays) {
        this.expirationDays = expirationDays;
    }

    public int getBronzeThreshold() {
        return bronzeThreshold;
    }

    public void setBronzeThreshold(int bronzeThreshold) {
        this.bronzeThreshold = bronzeThreshold;
    }

    public int getPrataThreshold() {
        return prataThreshold;
    }

    public void setPrataThreshold(int prataThreshold) {
        this.prataThreshold = prataThreshold;
    }

    public int getOuroThreshold() {
        return ouroThreshold;
    }

    public void setOuroThreshold(int ouroThreshold) {
        this.ouroThreshold = ouroThreshold;
    }
}
