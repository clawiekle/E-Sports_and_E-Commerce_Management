package dsa;

class Link
{
    public Object dData;
    public Link next;

    // -------------------------------------------------------------
    public Link(Object d)  // constructor
    { dData = d; }

    // -------------------------------------------------------------
    public void displayLink()  // display this link
    { System.out.print(dData + " "); }

}
public class SinglyLinkedList
{
    private Link first;
    private Link last;
    private int size;

    // -------------------------------------------------------------
    public SinglyLinkedList()
    {
        first = null;
        last = null;
        size = 0;
    }
    // -------------------------------------------------------------
    public boolean isEmpty()
    { return size == 0; }

    // -------------------------------------------------------------
    public int size()
    { return size; }
    // -------------------------------------------------------------
    public void add(Object item)
    {
        Link newLink = new Link(item);
        if( isEmpty() )
            first = newLink;
        else
            last.next = newLink;
        last = newLink;
        size++;
    }

    // -------------------------------------------------------------
    public Object get(int index)
    {
        if(index < 0 || index >= size)
        {
            System.out.println("Index: " + index + ", Size: " + size);
            return -1;
        }
        Link current = first;
        for(int i = 0; i < index; i++)
            current = current.next;
        return current.dData;
    }
    // -------------------------------------------------------------
    public void clear()
    {
        first = null;
        last = null;
        size = 0;
    }
    // -------------------------------------------------------------
    public void displayList()
    {
        System.out.print("List (first-->last): ");
        Link current = first;
        while(current != null)
        {
            current.displayLink();
            current = current.next;
        }
        System.out.println("");
    }

}

class SinglyLinkedApp
{
    public static void main(String[] args)
    {
        SinglyLinkedList theList = new SinglyLinkedList();
        theList.add(22);
        theList.add(44);
        theList.add(66);
        theList.displayList();
        System.out.println("Item at index 1: " + theList.get(1));
        System.out.println("Size: " + theList.size());
        theList.clear();
        System.out.println("Empty? " + theList.isEmpty());
    }
}