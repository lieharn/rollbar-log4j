package com.github.rollbar.log4j;

import java.io.IOException;

public interface IHttpRequester {
    
    public int send(HttpRequest request) throws IOException;

}
