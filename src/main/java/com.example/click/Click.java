package com.example.click;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "Click")
public class Click {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String ip;

    private long count;

    public long getId() {
        return id;
    }

    public Click setId( int id ) {
        this.id = id;

        return this;
    }

    public String getIp() {
        return ip;
    }

    public Click setIp( String ip ) {
        this.ip = ip;

        return this;
    }

    public long getCount() {
        return count;
    }

    public Click setCount( long clicks ) {

        this.count = clicks;

        return this;
    }
}