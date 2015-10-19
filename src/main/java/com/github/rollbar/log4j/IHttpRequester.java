package com.github.rollbar.log4j;

import java.io.IOException;

public interface IHttpRequester {
    
    int send(HttpRequest request) throws IOException;

}
