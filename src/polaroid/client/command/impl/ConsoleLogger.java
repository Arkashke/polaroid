package polaroid.client.command.impl;

import polaroid.client.command.Logger;

public class ConsoleLogger implements Logger {
    @Override
    public void log(String message) {
        System.out.println("message = " + message);
    }
}


