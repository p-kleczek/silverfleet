package sfmainframe;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

public final class Reusable<T extends Enum<T>> {

    private final Map<T, Integer> usedNumber;
    private final Map<T, Integer> readyNumber;


    public Reusable(Class<T> clazz, Collection<T> keys) {
        usedNumber = new EnumMap<T, Integer>(clazz);
        readyNumber = new EnumMap<T, Integer>(clazz);

        for (T t : keys)
            clear(t);
    }


    public void use(T t, int number) {
        assert (number > 0);
        assert (readyNumber.get(t) >= number);
        readyNumber.put(t, readyNumber.get(t) - number);
    }


    public void refresh() {
        for (T t : readyNumber.keySet())
            readyNumber.put(t, readyNumber.get(t) + usedNumber.get(t));
    }


    public int getReady(T t) {
        return readyNumber.get(t);
    }


    public int getUsed(T t) {
        return usedNumber.get(t);
    }


    public void setReady(T t, int number) {
        assert (number > 0);
        readyNumber.put(t, number);
    }


    public void setUsed(T t, int number) {
        assert (number > 0);
        usedNumber.put(t, number);
    }


    public void modifyReady(T t, int number) {
        assert (getReady(t) + number >= 0);
        readyNumber.put(t, getReady(t) + number);
    }


    public void modifyUsed(T t, int number) {
        assert (getUsed(t) + number >= 0);
        usedNumber.put(t, getUsed(t) + number);
    }


    public int getTotal(T t) {
        return readyNumber.get(t) + usedNumber.get(t);
    }


    public void clear(T t) {
        usedNumber.put(t, 0);
        readyNumber.put(t, 0);
    }
}
