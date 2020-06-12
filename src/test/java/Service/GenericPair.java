package Service;

/**
 * @param <E>
 * @author wy_okmeiyu from CSDN
 * @QQ You can't see
 * @Version 1.0
 * @TODO: 创造一个类似C++中的Pair类
 * @UpdateDate： 2015-11-19
 */
public class GenericPair<E, F> {
    private E first;
    private F second;

    public GenericPair() {

    }

    public GenericPair(E first, F second) {
        this.first = first;
        this.second = second;
    }

    public E getFirst() {
        return first;
    }

    public void setFirst(E first) {
        this.first = first;
    }

    public F getSecond() {
        return second;
    }

    public void setSecond(F second) {
        this.second = second;
    }


}
