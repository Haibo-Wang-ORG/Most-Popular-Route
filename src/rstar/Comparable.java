
package rstar;
public interface Comparable
{
    /**
     * Returns:
     *   =0 means equal
     *   <0 means strictly less than
     *   >0 means strictly greater than
     */
    public abstract int compare(Object other);
}