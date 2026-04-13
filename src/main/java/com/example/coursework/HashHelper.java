package com.example.coursework;

import org.mindrot.jbcrypt.BCrypt;

public class HashHelper {
    public static void main(String[] args) {
        String hash = BCrypt.hashpw("deriver1", BCrypt.gensalt());
        System.out.println(hash);
    }
    }