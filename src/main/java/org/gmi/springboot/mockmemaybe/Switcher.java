package org.gmi.springboot.mockmemaybe;

import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.Nullable;

@Getter
@Setter
public final class Switcher<T> {
    @Nullable
    private T mock;
    private boolean usingMock;
    private boolean inPostConstruct;
}
