
public class Memory {
	
	private short memory[];
	
	public Memory() {
		this.memory = new short[512];
	}
	
	public short load(short mar) {
		return this.memory[mar];
	}
	public void store(short mar, short mbr) {
		this.memory[mar] = mbr;
	}
	// 메모리 매니저가 원래 하는 역할인데, 지금은 메모리에 Program이 하나만 Load된다. 따라서 return 0
	public short allocate(int i) {
		return 0;
	}
	
}