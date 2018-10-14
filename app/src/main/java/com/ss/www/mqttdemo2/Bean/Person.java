package com.ss.www.mqttdemo2.Bean;

import java.io.Serializable;

/**
 * Created by 小松松 on 2018/10/11.
 */

public class Person implements Serializable {
    private String name;
    private String password;
    private String number;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
