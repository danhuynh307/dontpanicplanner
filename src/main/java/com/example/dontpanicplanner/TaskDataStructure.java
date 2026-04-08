package com.example.dontpanicplanner;

public class TaskDataStructure<T>  {

    private static final int INITCAP = 5;
    private T[] tasks;
    private int size;

    // creates an empty array with default capacity
    @SuppressWarnings("unchecked")
    public TaskDataStructure(){
        tasks = (T[]) new Object[INITCAP];
        size = 0;
    }

    // creates an empty array with a custom starting capacity
    @SuppressWarnings("unchecked")
    public TaskDataStructure(int initCapacity){
        if(initCapacity < 1)
        {
            throw new IllegalArgumentException("Initial capacity must be >= 1");
        }
        tasks = (T[]) new Object[initCapacity];
        size = 0;
    }

    // returns how many elements are in the structure
    public int size() {
        return size;
    }

    // returns current array capacity
    public int capacity() {
        return tasks.length;
    }

    // replaces value at index and returns old value
    public T set(int index, T value){
        validIndex(index);
        T old = tasks[index];
        tasks[index] = value;
        return old;
    }

    // gets the value at a specific index
    public T get(int index){
        validIndex(index);
        return tasks[index];
    }

    // adds value to the end of the array
    @SuppressWarnings("unchecked")
    public boolean add(T value){
        expandArray(size + 1);
        tasks[size++] = value;
        return true;
    }

    // inserts value at index and shifts elements right
    @SuppressWarnings("unchecked")
    public void add(int index, T value){
        if(index < 0 || index > size)
        {
            throw new IndexOutOfBoundsException();
        }
        expandArray(size + 1);
        for(int i = size; i > index; i--)
        {
            tasks[i] = tasks[i - 1];
        }
        tasks[index] = value;
        size++;
    }

    // removes element at index and shifts elements left
    @SuppressWarnings("unchecked")
    public T remove(int index){
        // remove and return element at position index
        // shift elements to remove any gap in the list
        // throw IndexOutOfBoundsException for invalid index

        // halve capacity if the number of elements falls below 1/3 of the capacity
        // capacity should NOT go below INITCAP

        // O(N) where N is the number of elements in the list
        validIndex(index);
        T removed = tasks[index];
        for (int i = index; i < size - 1; i++)
        {
            tasks[i] = tasks[i + 1];
        }
        tasks[size - 1] = null;
        size--;
        shrinkArray();
        return removed;
    }

    // removes the last element in the array
    public T removeEnd() {
        if (size == 0) {
            throw new IndexOutOfBoundsException("Array is empty");
        }

        T removed = tasks[size - 1];
        tasks[size - 1] = null;
        size--;

        shrinkArray();
        return removed;
    }

    // checks if the structure is empty
    public boolean isEmpty() {
        return size == 0;
    }

    //Private helpers
    @SuppressWarnings("unchecked")
    private void expandArray(int minCapacity)
    {
        if(minCapacity > tasks.length)
        {
            int newCap = tasks.length * 2;
            T[] newStorage = (T[]) new Object[newCap];
            for(int i = 0; i < size; i++)
            {
                newStorage[i] = tasks[i];
            }
            tasks = newStorage;
        }
    }

    // shrinks array if too much unused space
    @SuppressWarnings("unchecked")
    private void shrinkArray()
    {
        int cap = tasks.length;
        if(size < (cap / 3.0) && cap / 2 >= INITCAP)
        {
            int newCap = cap / 2;
            //@SuppressWarnings("unchecked")
            T[] newStorage = (T[]) new Object[newCap];
            for(int i = 0; i < size; i++)
            {
                newStorage[i] = tasks[i];
            }
            tasks = newStorage;
        }
    }

    // checks if index is valid
    private void validIndex(int index)
    {
        if(index < 0 || index >= size)
        {
            throw new IndexOutOfBoundsException();
        }
    }


}
