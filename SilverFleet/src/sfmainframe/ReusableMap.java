package sfmainframe;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

public final class ReusableMap<T extends Enum<T>> {

    private final Map<T, ReusableElement> container;


    public ReusableMap(Class<T> clazz, Collection<T> keys) {
        container = new EnumMap<T, ReusableElement>(clazz);

        for (T t : keys)
            container.put(t, new ReusableElement());
    }


    public void use(T t, int number) {
        assert (number > 0);
        assert (container.get(t).getReady() >= number);
        container.get(t).use(number);
    }


    public void refresh() {
        for (T t : container.keySet())
            container.get(t).refresh();
    }


    public ReusableElement getElement(T t) {
        return container.get(t);
    }


    public void clear() {

        for (T t : container.keySet())
            container.get(t).clear();
    }
}
