package rstar;

public class SLink
{
    public SLink() {}
    public SLink(Object anObject) { this.d = anObject; }
    public Object d    = null;
    public SLink  next = null;
    public SLink  prev = null;
}