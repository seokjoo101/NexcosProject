package com.example.seokjoo.contactex;

/**
 * Created by Seokjoo on 2016-08-08.
 */
public class DbInfo {
    public int _id;
    public String name;
    public String phone;



    public DbInfo(){}

    public DbInfo(int _id, String name, String phone ){
        this._id = _id;
        this.name = name;
        this.phone = phone;
    }
}
