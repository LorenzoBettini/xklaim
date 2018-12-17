package klava;

public interface TupleItem extends java.io.Serializable {

    /**
     * Whether this item is formal
     * 
     * @return
     */
    public boolean isFormal();

    /**
     * To set the value of this TupleItem
     * 
     * @param o
     */
    public void setValue(Object o);

    /**
     * To set the value of this TupleItem starting from a string specification.
     * 
     * @param o
     * @throws KlavaException
     *             when the item cannot be set from the passed string
     */
    public void setValue(String o) throws KlavaException;

    /**
     * Check whether this item is equal to the passed item. Of course, this
     * highly depends on the specific TupleItem, so check the documentation of
     * the implementation of a class that implements this interface.
     * 
     * @param o
     * @return
     */
    public boolean equals(Object o);

    /**
     * Duplicate an item.
     * 
     * @return
     */
    public Object duplicate();

}
