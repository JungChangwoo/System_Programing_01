
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
	// �޸� �Ŵ����� ���� �ϴ� �����ε�, ������ �޸𸮿� Program�� �ϳ��� Load�ȴ�. ���� return 0
	public short allocate(int i) {
		return 0;
	}
	
}