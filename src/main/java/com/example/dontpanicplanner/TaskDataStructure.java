package com.example.dontpanicplanner;

public class TaskDataStructure<T>  {

    private static final int INITCAP = 5;
    private T[] tasks;
    private int size;

    /**
     * Constructs an empty DynamicArray with initial capacity.
     */
    @SuppressWarnings("unchecked")
    public TaskDataStructure(){
        tasks = (T[]) new Object[INITCAP];
        size = 0;
    }

    /**
     * Constructs an empty DynamicArray with a given initial capacity.
     * @param initCapacity the initial capacity
     * @throws IllegalArgumentException if initCapacity < 1
     */
    @SuppressWarnings("unchecked")
    public TaskDataStructure(int initCapacity){
        if(initCapacity < 1)
        {
            throw new IllegalArgumentException("Initial capacity must be >= 1");
        }
        tasks = (T[]) new Object[initCapacity];
        size = 0;
    }

    /**
     * Returns the number of elements stored.
     * @return current number of elements
     */
    public int size() {
        return size;
    }

    /**
     * Returns the current capacity.
     * @return capacity of storage
     */
    public int capacity() {
        return tasks.length;
    }

    /**
     * Replaces the element at index with a value.
     * @param index index of element to replace
     * @param value new value to set
     * @return the old value at index
     * @throws IndexOutOfBoundsException if index is invalid
     */
    public T set(int index, T value){
        validIndex(index);
        T old = tasks[index];
        tasks[index] = value;
        return old;
    }

    /**
     * Get the element at index.
     * @param index index to get
     * @return the element at index
     * @throws IndexOutOfBoundsException if index is invalid
     */
    public T get(int index){
        validIndex(index);
        return tasks[index];
    }

    /**
     * Add value to the end of the array.
     * @param value element to add
     * @return always true
     */
    @SuppressWarnings("unchecked")
    public boolean add(T value){
        expandArray(size + 1);
        tasks[size++] = value;
        return true;
    }

    /**
     * Add value at index, shifting later elements right.
     * @param index index to insert at
     * @param value element to insert
     * @throws IndexOutOfBoundsException if index is invalid
     */
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

    /**
     * Removes and returns the element at index, shifting later elements left.
     * @param index index of element to remove
     * @return removed element
     * @throws IndexOutOfBoundsException if index is invalid
     */
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

    /**
     * Removes and returns the last element.
     * @return removed element
     * @throws IndexOutOfBoundsException if array is empty
     */
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

    public boolean isEmpty() {
        return size == 0;
    }

    //Private helpers
    /**
     * Doubles the array capacity if required for minCapacity}.
     * @param minCapacity required capacity
     */
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

    /**
     * Halves the array capacity if size < capacity/3.
     * Capacity will not drop below INITCAP}.
     */
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

    /**
     * Validates an index is within bound.
     * @param index index to check
     * @throws IndexOutOfBoundsException if invalid
     */
    private void validIndex(int index)
    {
        if(index < 0 || index >= size)
        {
            throw new IndexOutOfBoundsException();
        }
    }


}
