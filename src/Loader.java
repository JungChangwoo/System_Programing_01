import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Loader {
	//private FileManager fileManager;
	private Memory memory;
	private CPU cpu;
	
//	private short currentAddress;
	private Process currentProcess;
	
	public Loader(Memory memory, CPU cpu) {
		this.memory = memory;
		this.cpu = cpu;
	}

	public void loadProcess(String fileName) {
		this.currentProcess = new Process(); 
		this.currentProcess.load(fileName);
	}
	
	class Process { //Process도 Header가 있다
		static final short sizeHeader = 2; 
//		static final short indexPC = 0;
//		static final short indexSP = 1;
		
		private short startAddress; //process 의 시작 주소 
		private short sizeData, sizeCode;
		//exe의 Header를 읽는다.
		private void loadHeader(Scanner scanner) {
			this.sizeCode =	Short.decode(scanner.nextLine());
			this.sizeData = Short.decode(scanner.nextLine());
			this.startAddress = memory.allocate(sizeHeader+this.sizeData+this.sizeCode); // 메모리에서 할당해주고 이 Process의 시작주소 반환
			cpu.setPC((short) (startAddress + sizeHeader));
			cpu.setSP((short) (startAddress + sizeHeader + this.sizeCode));
		}
		
		private void loadBody(Scanner scanner) {
			// data Segment, code segment를 메모리에 올려줌 
			short currentAddress = (short) (this.startAddress + sizeHeader);
			while(scanner.hasNext()) {
				memory.store(currentAddress, Short.decode(scanner.nextLine())); //주소랑 값
				currentAddress++;
			}
		}
		
		public void load(String fileName) {
			try {
				String path = "C:\\Users\\ikkwi\\Desktop\\JAVA\\workspace\\Computer_Project\\exe\\";
				Scanner scanner = new Scanner(new File(path+fileName));
				this.loadHeader(scanner);
				this.loadBody(scanner);
				scanner.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
}
