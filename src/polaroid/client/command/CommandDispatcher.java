package polaroid.client.command;

import polaroid.client.command.impl.DispatchResult;

public interface CommandDispatcher {
    DispatchResult dispatch(String command);
}


