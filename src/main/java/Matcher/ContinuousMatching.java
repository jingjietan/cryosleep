package Matcher;

import java.util.function.Consumer;

public class ContinuousMatching {
    public Consumer<Integer> registerCallback;
    public ContinuousMatching(Consumer<Integer> registerCallback) {
        this.registerCallback = registerCallback;
    }


}
