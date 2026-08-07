package com.bkelly.demo.config;

interface SecretLookup {

  String resolve(String reference);
}
