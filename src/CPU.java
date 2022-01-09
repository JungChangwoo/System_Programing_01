import java.io.IOException;

public class CPU {
	// declaration
	private enum ERegisters {
		ePC, eSP, eAC, eIR, eSR, eMAR, eMBR
	}

	private enum EOpCode {
		eHalt, eLDC, eLDA, eSTA, eADDA, eADDC, eSUBA, eSUBC, eMULA, eMULC, eDIVA, eDIVC, eANDA, eNOTA, eJMPZ, eJMPBZ,eJMP
	}

	private class Register {
		protected short value;
		public short getValue() {return this.value;}
		public void setValue(short value) {this.value = value;}
	}

	private class IR extends Register {
		public short getOpCode() {return (short) (this.value >> 8);}
		public short getOperand() {return (short) (this.value & 0x00FF);}
	}
	
	private class SR extends Register {
		public void setFlag(short result) {
			if(result == 0) {
				this.value = Short.decode("0x0800");
			} else if(result < 0) {
				this.value = Short.decode("0x0400");
			} else {
				this.value = Short.decode("0x0000");
			}
		}
	}
	
	private class CU {
		public boolean isZero(Register sr) {
			if((short) (sr.getValue() & 0x0800) == 0) {
				return false;
			}
			return true;
		}
		public boolean isBZ(Register sr) {
			if((short) (sr.getValue() & 0x0400) == 0) {
				return false;
			} else {
				return true;
			}
		}
	}

	private class ALU {
		protected short value;
		public void store(short value) {
			this.value = value;
		}
		public void add(short value) { //���� ���� �ٽ� AC�� �־���
			registers[ERegisters.eAC.ordinal()].setValue((short) (this.value+value));
		}
		public void sub(short value) {
			short result = (short) (this.value - value);
			registers[ERegisters.eAC.ordinal()].setValue(result);
			// SR��  �ݿ� 
			((CPU.SR) registers[ERegisters.eSR.ordinal()]).setFlag(result);
		}
		public void div(short value) {
			registers[ERegisters.eAC.ordinal()].setValue((short) (this.value / value));
		}
		public void mul(short value2) {
			registers[ERegisters.eAC.ordinal()].setValue((short) (this.value*value));
		}
	}

	// components
	private Register registers[];
	private ALU alu;
	private CU cu;

	// association
	private Memory memory;

	// instruction
	private void Halt() {}

	private void LDC() { // ��� : memory�� ��ġ���ʰ� �ٷ� AC�� ������
		// IR.operand -> MBR
		this.registers[ERegisters.eMBR.ordinal()]
				.setValue(((CPU.IR) this.registers[ERegisters.eIR.ordinal()]).getOperand());
		// MBR -> AC
		this.registers[ERegisters.eAC.ordinal()].setValue(this.registers[ERegisters.eMBR.ordinal()].getValue());
	}

	private void LDA() { // �ּ�
		// IR.operand -> MAR
		this.registers[ERegisters.eMAR.ordinal()]
				.setValue((short)(((CPU.IR) this.registers[ERegisters.eIR.ordinal()]).getOperand() + this.registers[ERegisters.eSP.ordinal()].getValue()));
		// Memory.load
		this.registers[ERegisters.eMBR.ordinal()]
				.setValue(this.memory.load(this.registers[ERegisters.eMAR.ordinal()].getValue()));
		// MBR -> AC
		this.registers[ERegisters.eAC.ordinal()].setValue(this.registers[ERegisters.eMBR.ordinal()].getValue());
	}

	private void STA() {
		// IR.operand -> MAR // SP�� ���ؼ� 
		this.registers[ERegisters.eMAR.ordinal()]
				.setValue((short) (((CPU.IR) this.registers[ERegisters.eIR.ordinal()]).getOperand() + this.registers[ERegisters.eSP.ordinal()].getValue()));
		// AC -> MBR
		this.registers[ERegisters.eMBR.ordinal()].setValue(this.registers[ERegisters.eAC.ordinal()].getValue());
		// memory.store(MAR, MBR)
		this.memory.store(this.registers[ERegisters.eMAR.ordinal()].getValue(),
				this.registers[ERegisters.eMBR.ordinal()].getValue());
		// PC ++
		this.registers[ERegisters.ePC.ordinal()].setValue((short) (this.registers[ERegisters.ePC.ordinal()].getValue()+1));
	};

	private void ADDA() { // �ּҰ� �ִ� ���� ������ (example : x, y)
		// AC -> ALU
		this.alu.store(this.registers[ERegisters.eAC.ordinal()].getValue());
		// IR.operand -> AC
		this.LDA();
		// ALU �� add ����
		this.alu.add(this.registers[ERegisters.eAC.ordinal()].getValue());
		// PC ++
		this.registers[ERegisters.ePC.ordinal()].setValue((short) (this.registers[ERegisters.ePC.ordinal()].getValue()+1));
	}

	private void ADDC() {
		// AC -> ALU
		this.alu.store(this.registers[ERegisters.eAC.ordinal()].getValue());
		// IR.operand -> MAR
		this.LDC();
		// alu���� ���ؼ� AC�� �������� 
		this.alu.add(this.registers[ERegisters.eAC.ordinal()].getValue());
		// PC ++
		this.registers[ERegisters.ePC.ordinal()].setValue((short) (this.registers[ERegisters.ePC.ordinal()].getValue()+1));
	}

	private void SUBA() {
		// AC -> ALU
		this.alu.store(this.registers[ERegisters.eAC.ordinal()].getValue());
		// IR.operand -> MAR
		this.LDA();
		// ALU���� ���� AC�� ��������
		this.alu.sub(this.registers[ERegisters.eAC.ordinal()].getValue());
		// PC ++
		this.registers[ERegisters.ePC.ordinal()].setValue((short) (this.registers[ERegisters.ePC.ordinal()].getValue()+1));
	}
	
	private void SUBC() {
		// AC -> ALU
		this.alu.store(this.registers[ERegisters.eAC.ordinal()].getValue());
		// IR.operand -> MAR
		this.LDC();
		// ALU���� ���� AC�� ��������
		this.alu.sub(this.registers[ERegisters.eAC.ordinal()].getValue());
		// PC ++
		this.registers[ERegisters.ePC.ordinal()].setValue((short) (this.registers[ERegisters.ePC.ordinal()].getValue()+1));
	}
	
	private void MULA() {
		// AC -> ALU
		this.alu.store(this.registers[ERegisters.eAC.ordinal()].getValue());
		// IR.operand -> MAR
		this.LDA();
		// ALU���� ���� AC�� ��������
		this.alu.mul(this.registers[ERegisters.eAC.ordinal()].getValue());
		// PC ++
		this.registers[ERegisters.ePC.ordinal()].setValue((short) (this.registers[ERegisters.ePC.ordinal()].getValue()+1));

	}
	
	private void MULC() {
		// AC -> ALU
		this.alu.store(this.registers[ERegisters.eAC.ordinal()].getValue());
		// IR.operand -> MAR
		this.LDC();
		// ALU���� ���� AC�� ��������
		this.alu.mul(this.registers[ERegisters.eAC.ordinal()].getValue());
		// PC ++
		this.registers[ERegisters.ePC.ordinal()].setValue((short) (this.registers[ERegisters.ePC.ordinal()].getValue()+1));

	}

	private void DIVA() {
		// AC -> ALU
		this.alu.store(this.registers[ERegisters.eAC.ordinal()].getValue());
		// IR.operand -> MAR
		this.LDA();
		// ALU���� ���� AC�� ��������
		this.alu.div(this.registers[ERegisters.eAC.ordinal()].getValue());
		// PC ++
		this.registers[ERegisters.ePC.ordinal()].setValue((short) (this.registers[ERegisters.ePC.ordinal()].getValue()+1));
	}
	
	private void DIVC() {
		// AC -> ALU
		this.alu.store(this.registers[ERegisters.eAC.ordinal()].getValue());
		// IR.operand -> MAR
		this.LDC();
		// ALU���� ���� AC�� ��������
		this.alu.div(this.registers[ERegisters.eAC.ordinal()].getValue());
		// PC ++
		this.registers[ERegisters.ePC.ordinal()].setValue((short) (this.registers[ERegisters.ePC.ordinal()].getValue()+1));
	}

//	private void ANDA() {
//	}
//
//	private void NOTA() {
//	}

	private void JMPZ() {
		if(this.cu.isZero(this.registers[ERegisters.eSR.ordinal()])) {
			//ir.operand > PC
			this.registers[ERegisters.ePC.ordinal()].setValue(
					(short) (((CPU.IR) this.registers[ERegisters.eIR.ordinal()]).getOperand() + 2));
		} else {
			// PC ++
			this.registers[ERegisters.ePC.ordinal()].setValue((short) (this.registers[ERegisters.ePC.ordinal()].getValue()+1));
		}
	}

	private void JMPBZ() {
		if(this.cu.isBZ(this.registers[ERegisters.eSR.ordinal()])) {
			//ir.operand > PC
			this.registers[ERegisters.ePC.ordinal()].setValue(
					(short) (((CPU.IR) this.registers[ERegisters.eIR.ordinal()]).getOperand() + 2));
		} else {
			// PC ++
			this.registers[ERegisters.ePC.ordinal()].setValue((short) (this.registers[ERegisters.ePC.ordinal()].getValue()+1));
		}
	}
	
	private void JMP() {
		//ir.operand > PC
		this.registers[ERegisters.ePC.ordinal()].setValue(
				(short) (((CPU.IR) this.registers[ERegisters.eIR.ordinal()]).getOperand() + 2));
	}

	public CPU() {
		this.cu = new CU();
		this.alu = new ALU();
		this.registers = new Register[ERegisters.values().length];
		for (ERegisters eRegister : ERegisters.values()) {
			this.registers[eRegister.ordinal()] = new Register();
		}
		this.registers[ERegisters.eIR.ordinal()] = new IR();
		this.registers[ERegisters.eSR.ordinal()] = new SR();
	}

	public void association(Memory memory) {
		this.memory = memory;
	}

	private void fetch() {
		// PC -> MAR
		this.registers[ERegisters.eMAR.ordinal()].setValue(this.registers[ERegisters.ePC.ordinal()].getValue());
		// Memory.load
		this.registers[ERegisters.eMBR.ordinal()]
				.setValue(this.memory.load(this.registers[ERegisters.eMAR.ordinal()].getValue()));
		// MBR -> IR
		this.registers[ERegisters.eIR.ordinal()].setValue(this.registers[ERegisters.eMBR.ordinal()].getValue());
	}

	// Execute���� ����Ǵ� �ڵ�
	private void decode() {}

	private void execute() {
		switch(EOpCode.values()[((IR)this.registers[ERegisters.eIR.ordinal()]).getOpCode()]) {
		case eHalt : 
			shutDown();
			break;
		case eLDC :
			LDC();
			// PC ++
			this.registers[ERegisters.ePC.ordinal()].setValue((short) (this.registers[ERegisters.ePC.ordinal()].getValue()+1));
			break;
		case eLDA : 
			LDA();
			// PC ++
			this.registers[ERegisters.ePC.ordinal()].setValue((short) (this.registers[ERegisters.ePC.ordinal()].getValue()+1));
			break;
		case eSTA : 
			STA();
			break;
		case eADDA : 
			ADDA();
			break;
		case eADDC : 
			ADDC();
			break;
		case eSUBA : 
			SUBA();
			break;
		case eSUBC : 
			SUBC();
			break;
		case eMULA : 
			MULA();
			break;
		case eMULC : 
			MULC();
			break;
		case eDIVA : 
			DIVA();
			break;
		case eDIVC : 
			DIVC();
			break;
		case eANDA : 
			break;
		case eNOTA : 
			break;
		case eJMPZ :
			JMPZ();
			break;
		case eJMPBZ :
			JMPBZ();
			break;
		case eJMP : 
			JMP();
			break;
		}
	}

	// status
	private boolean bPowerOn;
	private boolean isPowerOn() {return this.bPowerOn;}
	public void setPowerOn() {
		this.bPowerOn = true;
		this.run();
	}
	public void shutDown() {this.bPowerOn = false;}
	private void checkInterrupt() {}

	// ���Ⱑ ���� ������ ���� ����
	public void run() {
		while (this.isPowerOn()) {
			this.fetch();
			this.execute();
			checkInterrupt();
		}
	}

	public static void main(String args[]) throws IOException {
		CPU cpu = new CPU();
		Memory memory = new Memory();
		cpu.association(memory);
		Loader loader = new Loader(memory, cpu);
		loader.loadProcess("exe1");
		cpu.setPowerOn();
	}

	public void setPC(short pc) {
		this.registers[ERegisters.ePC.ordinal()].setValue(pc);
	}

	public void setSP(short sp) {
		this.registers[ERegisters.eSP.ordinal()].setValue(sp);
		
	}
}
