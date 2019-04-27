package com.example.boot;

interface NamedObject<T> {
    String getName();

    T getNamedObject();
}