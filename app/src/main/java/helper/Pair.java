package helper;

import androidx.annotation.NonNull;

/**
 * Created by antoine on 8/20/15.
 */
public class Pair implements Comparable<Pair> {

    public final String first;
    public final String second;
//    public String third;
//    public String fourth;
//
//    public Pair(String first, String second, String third, String fourth) {
//        this.first = first;
//        this.second = second;
//        this.third = third;
//        this.fourth = fourth;
//    }

    public Pair(String first, String second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public int compareTo(Pair o) {
        int cmp = compare(first, o.first);
        return cmp == 0 ? compare(second, o.second) : cmp;
    }

    // todo move this to a helper class.
    private static int compare(Object o1, Object o2) {
        return o1 == null ? o2 == null ? 0 : -1 : o2 == null ? +1
                : ((Comparable) o1).compareTo(o2);
    }

    @Override
    public int hashCode() {
//        if (third != null && fourth != null) {
//            return 31 * hashcode(first) + hashcode(second) + hashcode(third) + hashcode(fourth);
//        }else{
            return 31 * hashcode(first) + hashcode(second);
//        }
    }

    // todo move this to a helper class.
    private static int hashcode(Object o) {
        return o == null ? 0 : o.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Pair))
            return false;
        if (this == obj)
            return true;

//        if (third != null && fourth != null) {
//            return equal(first, ((Pair) obj).first)
//                    && equal(second, ((Pair) obj).second)
//                    && equal(third, ((Pair) obj).third)
//                    && equal(fourth, ((Pair) obj).fourth);
//        }else{
            return equal(first, ((Pair) obj).first)
                    && equal(second, ((Pair) obj).second);
//        }
    }

    // todo move this to a helper class.
    private boolean equal(Object o1, Object o2) {
        return o1 == null ? o2 == null : (o1 == o2 || o1.equals(o2));
    }

    @NonNull
    @Override
    public String toString() {
//        if (third != null && fourth != null) {
//            return "(" + first + ", " + second +", "+ third + ", " + fourth + ')';
//        }else{
            return "(" + first + ", " + second + ')';
//        }
    }
}