package polaroid.client.command.impl;

import polaroid.client.command.Parameters;
import polaroid.client.command.ParametersFactory;

public class ParametersFactoryImpl implements ParametersFactory {

    @Override
    public Parameters createParameters(String message, String delimiter) {
        return new ParametersImpl(message.split(delimiter));
    }
}


