package com.example.boot;

interface BootService {
    void boot() throws RuntimeException;

    void shutdown() throws RuntimeException;
}
