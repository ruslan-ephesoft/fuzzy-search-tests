package ephesoft.fuzzytest.impl.symspell;

public class ChunkArray {

    private final int chunkSize = 4096; //this must be a power of 2, otherwise can't optimize Row and Col functions
    private final int divShift = 12; // number of bits to shift right to do division by ChunkSize (the bit position of ChunkSize)
    public Node[][] values;
	public int count;
    
	public ChunkArray(int initialCapacity)
    {
        int chunks = (int)((initialCapacity + chunkSize - 1) / chunkSize);
        values = new Node[chunks][chunkSize];
    }
    public int add(Node value)
    {
        if (count == getCapacity())
        {
            Node[][] newValues = new Node[values.length + 1][];
            System.arraycopy(values, 0, newValues, 0, values.length);
            newValues[values.length] = new Node[chunkSize];
            values = newValues;
        }
        values[row(count)][col(count)] = value;
        count++;
        return count - 1;
    }
    public void clear()
    {
        count = 0;
    }
    
    public Node getNode(int index) {
    	return values[row(index)][col(index)];
    }
    
    public void setNode(Node value, int index) {
    	values[row(index)][col(index)] = value;
    }

    private int row(int index) { 
    	return index >> divShift; 
    }
    
    private int col(int index) { 
    	return index & (chunkSize - 1); 
    }
    
    private int getCapacity() { 
    	return values.length * chunkSize; 
    }

    public Node[][] getValues() {
		return values;
	}
	public int getCount() {
		return count;
	}
}
