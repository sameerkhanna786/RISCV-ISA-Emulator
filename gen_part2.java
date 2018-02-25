/** CS61C SP18 Proj2 Part2 AutoGen
 *  @author: Li Qin (kroulis@berkeley.edu)
 *  @version: 1.0
 *  Notes:
 *  1. Passing the auto generator test DOES NOT mean you can pass all the test.
 *     This auto generator did not test you on negative offset branch (cause it might get into infinity loop)
 *     and negative store and load.
 *     You should be aware of this and create your own test if you want.
 *  2. Since our program does not have jr (jump register), I did not implement a random recursive function.
 *     (Though it should not bother since I have a simple function to test.)
 *  3. This code already created user input. So no need to modify anything. And... try not to read the auto gen lol.
*/

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

enum InsType{
    R, I, U, UJ, S, SB
}
enum FUNC {
    ADD (InsType.R,0x33,0x0,0x00,"add"),
    MUL (InsType.R,0x33,0x0,0x01,"mul"),
    SUB (InsType.R,0x33,0x0,0x20,"sub"),
    SLL (InsType.R,0x33,0x1,0x00,"sll"),
    MULH(InsType.R,0x33,0x1,0x01,"mulh"),
    SLT (InsType.R,0x33,0x2,0x00,"slt"),
    XOR (InsType.R,0x33,0x4,0x00,"xor"),
    DIV (InsType.R,0x33,0x4,0x01,"div"),
    SRL (InsType.R,0x33,0x5,0x00,"srl"),
    SRA (InsType.R,0x33,0x5,0x20,"sra"),
    OR  (InsType.R,0x33,0x6,0x00,"or"),
    REM (InsType.R,0x33,0x6,0x01,"rem"),
    AND (InsType.R,0x33,0x7,0x00,"and"),
    LB  (InsType.I,0x03,0x0,0   ,"lb"),
    LH  (InsType.I,0x03,0x1,0   ,"lh"),
    LW  (InsType.I,0x03,0x2,0   ,"lw"),
    ADDI(InsType.I,0x13,0x0,0   ,"addi"),
    SLLI(InsType.I,0x13,0x1,0x00,"slli"),
    SLTI(InsType.I,0x13,0x2,0   ,"slti"),
    XORI(InsType.I,0x13,0x4,0   ,"xori"),
    SRLI(InsType.I,0x13,0x5,0x00,"srli"),
    SRAI(InsType.I,0x13,0x5,0x20,"srai"),
    ORI (InsType.I,0x13,0x6,0   ,"ori"),
    ANDI(InsType.I,0x13,0x7,0   ,"andi"),
    ECAL(InsType.I,0x73,0x0,0x00,"ecall"),
    SB  (InsType.S,0x23,0x0,0   ,"sb"),
    SH  (InsType.S,0x23,0x1,0   ,"sh"),
    SW  (InsType.S,0x23,0x2,0   ,"sw"),
    BEQ (InsType.SB,0x63,0x0,0  ,"beq"),
    BNE (InsType.SB,0x63,0x1,0  ,"bne"),
    LUI (InsType.U,0x37,0   ,0  ,"lui"),
    JAL (InsType.UJ,0x6f,0  ,0  ,"jal");


    final int opcode, funct3, funct7;
    final InsType type;
    final String name;
    FUNC(InsType t, int op, int f3, int f7, String n) {
        type = t;
        opcode = op;
        funct3 = f3;
        funct7 = f7;
        name = n;
    }
}

class GUtil {
    public static String GenOPString(FUNC func) {
        StringBuilder r = new StringBuilder(Integer.toBinaryString(func.opcode));
        while (r.length() < 7) {
            r.insert(0, "0");
        }
        if (r.length() > 7) {
            r = new StringBuilder(r.substring(r.length() - 8));
        }
        return r.toString();
    }
    public static String GenFunct3(FUNC func) {
        StringBuilder iset = new StringBuilder(Integer.toBinaryString(func.funct3));
        while (iset.length() < 3) {
            iset.insert(0, "0");
        }
        if (iset.length() > 3) {
            iset = new StringBuilder(iset.substring(iset.length() - 4));
        }
        return iset.toString();
    }
    public static String GenFunct7(FUNC func) {
        StringBuilder andi = new StringBuilder(Integer.toBinaryString(func.funct7));
        while (andi.length() < 7) {
            andi.insert(0, "0");
        }
        if (andi.length() > 7) {
            andi = new StringBuilder(andi.substring(andi.length() - 8));
        }
        return andi.toString();
    }

    public static String GenRegister(int num) {
        StringBuilder addi = new StringBuilder(Integer.toBinaryString(num));
        while (addi.length() < 5) {
            addi.insert(0, "0");
        }
        if (addi.length() > 5) {
            addi = new StringBuilder(addi.substring(addi.length() - 8));
        }
        return addi.toString();
    }

    public static String GenIMM(int num, int size, boolean ext) {
        StringBuilder t = new StringBuilder(Integer.toBinaryString(num));
        while (t.length() < size) {
            if (!ext)
                t.insert(0,"0");
            else {
                if (num<0)
                    t.insert(0,"1");
                else
                    t.insert(0,"0");
            }
        }
        if (t.length() > size) {
            t = new StringBuilder(t.substring(t.length() - size));
        }
        return t.toString();
    }

    public static long toHex(String code) {
        return Long.parseLong(code,2);
    }
}

class Instruction {
    private FUNC num;
    private String tToTest, f, ins;
    private int memory, des, program;
    private int type;

    public Instruction(FUNC func) {
        this.num = func;
        tToTest = GUtil.GenOPString(func);
        f = GUtil.GenFunct3(func);
        ins = GUtil.GenFunct7(func);
    }

    public void SetType(FUNC f) {
        num = f;
        tToTest = GUtil.GenOPString(num);
        this.f = GUtil.GenFunct3(num);
        ins = GUtil.GenFunct7(num);
    }

    public boolean SetRD(int reg) {
        if (num.type == InsType.S || num.type == InsType.SB)
            return false;
        if (reg < 0 || reg > 31)
            return false;
        memory = reg;
        return true;
    }

    public boolean SetRS1(int reg) {
        if (num.type == InsType.U || num.type == InsType.UJ)
            return false;
        if (reg < 0 || reg > 31)
            return false;
        des = reg;
        return true;
    }

    public boolean SetRS2(int reg) {
        if (num.type == InsType.U || num.type == InsType.UJ || num.type == InsType.I)
            return false;
        if (reg < 0 || reg > 31)
            return false;
        program = reg;
        return true;
    }

    public boolean SetIMM(int imm) {
        switch (num.type) {
            case R:
                return false;
            case I:
            case S:
                if (imm >= 1<<11)
                    return false;
                break;
            case SB:
                if (imm >= 1<<12)
                    return false;
                break;
            case UJ:
                if (imm >= 1<<20)
                    return false;
                break;
        }
        type = imm;
        return true;
    }

    public FUNC getType() {
        return num;
    }

    public int getRD() {
        return memory;
    }

    public int getRS1() {
        return des;
    }

    public int getRS2() {
        return program;
    }

    public int getIMM() {
        return type;
    }

    public String toMachineCodeBin() {
        StringBuilder mulh = new StringBuilder();
        switch (num.type) {
            case R:
                mulh.append(ins);
                mulh.append(GUtil.GenRegister(program));
                mulh.append(GUtil.GenRegister(des));
                mulh.append(f);
                mulh.append(GUtil.GenRegister(memory));
                mulh.append(tToTest);
                break;
            case I:
                if(num == FUNC.ECAL) {
                    mulh.append(GUtil.GenIMM(0,12,false));
                    mulh.append(GUtil.GenRegister(0));
                    mulh.append(f);
                    mulh.append(GUtil.GenRegister(0));
                    mulh.append(tToTest);
                } else {
                    if (num == FUNC.SLLI || num == FUNC.SRLI || num == FUNC.SRAI) {
                        mulh.append(ins);
                        mulh.append(GUtil.GenIMM(type,5,false));
                    } else {
                        mulh.append(GUtil.GenIMM(type,12,true));
                    }
                    mulh.append(GUtil.GenRegister(des));
                    mulh.append(f);
                    mulh.append(GUtil.GenRegister(memory));
                    mulh.append(tToTest);
                }
                break;
            case S:
                String b = GUtil.GenIMM(type,12,true);
                mulh.append(b.substring(0,7));
                mulh.append(GUtil.GenRegister(program));
                mulh.append(GUtil.GenRegister(des));
                mulh.append(f);
                mulh.append(b.substring(7));
                mulh.append(tToTest);
                break;
            case SB:
                b = GUtil.GenIMM(type >>>1, 12, true);
                mulh.append(b.substring(0,1));
                mulh.append(b.substring(2,8));
                mulh.append(GUtil.GenRegister(program));
                mulh.append(GUtil.GenRegister(des));
                mulh.append(f);
                mulh.append(b.substring(8,12));
                mulh.append(b.substring(1,2));
                mulh.append(tToTest);
                break;
            case U:
                mulh.append(GUtil.GenIMM(type,20,false));
                mulh.append(GUtil.GenRegister(memory));
                mulh.append(tToTest);
                break;
            case UJ:
                b = GUtil.GenIMM(type >>>1,20,true);
                mulh.append(b.substring(0,1));
                mulh.append(b.substring(10,20));
                mulh.append(b.substring(9,10));
                mulh.append(b.substring(1,9));
                mulh.append(GUtil.GenRegister(memory));
                mulh.append(tToTest);
                break;
            default:
                mulh.append("[Unknown]");
        }
        return mulh.toString();
    }

    public String toASM() {
        StringBuilder f = new StringBuilder();
        f.append(num.name);
        switch (num.type) {
            case R:
                f.append("\tx");
                f.append(memory);
                f.append(", x");
                f.append(des);
                f.append(", x");
                f.append(program);
                break;
            case I:
                if (num.opcode == 0x03) {
                    f.append("\tx");
                    f.append(memory);
                    f.append(", ");
                    f.append(type);
                    f.append("(x");
                    f.append(des);
                    f.append(")");
                } else if (num.opcode == 0x13) {
                    f.append("\tx");
                    f.append(memory);
                    f.append(", x");
                    f.append(des);
                    f.append(", ");
                    f.append(type);
                }
                break;
            case S:
                f.append("\tx");
                f.append(program);
                f.append(", ");
                f.append(type);
                f.append("(x");
                f.append(des);
                f.append(")");
                break;
            case SB:
                f.append("\tx");
                f.append(des);
                f.append(", x");
                f.append(program);
                f.append(", ");
                f.append(type);
                break;
            case U:
            case UJ:
                f.append("\tx");
                f.append(memory);
                f.append(", ");
                f.append(type);
                break;
        }
        return f.toString();
    }
}

class Executor {
    public static String exectute(Processor p, Instruction ins) {
        FUNC type = ins.getType();
        int r = ins.getRD();
        int from = ins.getRS1();
        int a = ins.getRS2();
        int op = ins.getIMM();
        doMul(p, ins);
        switch (type) {
            case ADD:
                p.doAdd(r, from, a);
                break;
            case MUL:
                p.doMul(r, from, a);
                break;
            case SUB:
                p.doSub(r, from, a);
                break;
            case SLL:
                p.doSll(r, from, a);
                break;
            case MULH:
                p.doMulh(r, from, a);
                break;
            case SLT:
                p.doSlt(r, from, a);
                break;
            case XOR:
                p.doXor(r, from, a);
                break;
            case DIV:
                p.doDiv(r, from, a);
                break;
            case SRL:
                p.doSrl(r, from, a);
                break;
            case SRA:
                p.doSra(r, from, a);
                break;
            case OR:
                p.doOr(r, from, a);
                break;
            case REM:
                p.doRem(r, from, a);
                break;
            case AND:
                p.doAnd(r, from, a);
                break;
            case LB:
                p.doLoad(r,2, op, from);
                break;
            case LH:
                p.doLoad(r, 4, op, from);
                break;
            case LW:
                p.doLoad(r, 8, op, from);
                break;
            case ADDI:
                p.doAddi(r, from, op);
                break;
            case SLLI:
                p.doSlli(r, from, op);
                break;
            case SLTI:
                p.doSlti(r, from, op);
                break;
            case XORI:
                p.doXori(r, from, op);
                break;
            case SRLI:
                p.doSrli(r, from, op);
                break;
            case SRAI:
                p.doSrai(r, from, op);
                break;
            case ORI:
                p.doOri(r, from, op);
                break;
            case ANDI:
                p.doAndi(r, from, op);
                break;
            case ECAL:
                return p.doEcall();
            case SB:
                p.doStore(a, 2, op, from);
                break;
            case SH:
                p.doStore(a, 4, op, from);
                break;
            case SW:
                p.doStore(a, 8, op, from);
                break;
            case BEQ:
                p.doBeq(from, a, op);
                break;
            case BNE:
                p.doBne(from, a, op);
                break;
            case LUI:
                p.doLui(r, op);
                break;
            case JAL:
                p.doJal(r, op);
                break;
            default:
                return "[ERROR]";

        }
        return "";
    }

    private static void doMul(Processor p, Instruction ins) {
        FUNC a = ins.getType();
        if (a != FUNC.JAL && a != FUNC.BEQ && a != FUNC.BNE ) {
            p.nextInstruction();
        }
    }
}

class Processor {
    private int ins;
    private int[] seta0Init;
    private char[] processor;
    private static int des = 1024*1024*2;

    public Processor() {
        ins = 0x1000;
        seta0Init = new int[32];
        processor = new char[des];
        for (int startPc = 0; startPc < 32; startPc++) {
            seta0Init[startPc] = 0;
        }
        seta0Init[2] = 0xEFFFF;
        seta0Init[3] = 0x3000;
        Arrays.fill(processor,'0');
    }

    public String dump() {
        StringBuilder a = new StringBuilder();
        for(int p = 0; p <8; p++) {
            for(int setins = 0; setins <4; setins++) {
                a.append(String.format("r%2d=%08x ", p *4+ setins, seta0Init[p *4+ setins]));
            }
            a.append("\n");
        }
        return a.toString();
    }

    public void nextInstruction() {
        ins += 4;
    }

    public int getPC() {
        return ins;
    }

    public int getReg(int m) {
        return seta0Init[m];
    }

    public void doAdd(int des, int a, int b) {
        seta0Init[des] = seta0Init[a] + seta0Init[b];
    }

    public void doMul(int des, int a, int b) {
        long result = (long) seta0Init[a] * (long) seta0Init[b];
        seta0Init[des] = (int)result;
    }

    public void doSub(int des, int a, int b) {
        seta0Init[des] = seta0Init[a] - seta0Init[b];
    }

    public void doSll(int des, int a, int b) {
        int abcd = seta0Init[b] & 0x1F;
        seta0Init[des] = seta0Init[a] << abcd;
    }

    public void doMulh(int des, int a, int b) {
        long result = (long) seta0Init[a] * (long) seta0Init[b];
        seta0Init[des] = (int)(result >> 32);
    }

    public void doSlt(int des, int a, int b) {
        seta0Init[des] = seta0Init[a] < seta0Init[b] ? 1 : 0;
    }

    public void doXor(int des, int a, int b) {
        seta0Init[des] = seta0Init[a] ^ seta0Init[b];
    }

    public void doDiv(int des, int a, int b) {
        seta0Init[des] = seta0Init[a] / seta0Init[b];
    }

    public void doSrl(int des, int a, int b) {
        int havf = seta0Init[b] & 0x1F;
        seta0Init[des] = seta0Init[a] >>> havf;
    }

    public void doSra(int des, int a, int b) {
        int geas = seta0Init[b] & 0x1F;
        seta0Init[des] = seta0Init[a] >> geas;
    }

    public void doOr(int des, int a ,int b) {
        seta0Init[des] = seta0Init[a] | seta0Init[b];
    }

    public void doRem(int des, int a, int b) {
        seta0Init[des] = seta0Init[a] % seta0Init[b];
    }

    public void doAnd(int des, int a, int b) {
        seta0Init[des] = seta0Init[a] & seta0Init[b];
    }

    public void doLoad(int des, int size, int offset, int from) {
        int address = (seta0Init[from] + offset)*2;
        StringBuilder type = new StringBuilder();
        for (int result = address + size - 1; result >= address; result -=2) {
            type.append(processor[result -1]);
            type.append(processor[result]);
        }
        seta0Init[des] = (int)(Long.parseLong(type.toString(),16));
        if ((seta0Init[des]>>(size*4-1) & 1) > 0){
            for (int a = size*4; a < 32; a++) {
                seta0Init[des] |=  1<< a;
            }
        }

    }

    public void doAddi(int des, int a, int i) {
        seta0Init[des] = seta0Init[a] + i;
    }

    public void doSlli(int des, int a, int i) {
        seta0Init[des] = seta0Init[a] << i;
    }

    public void doSlti(int des, int a, int i) {
        seta0Init[des] = seta0Init[a] < i ? 1 : 0;
    }

    public void doXori(int des, int a, int i) {
        seta0Init[des] = seta0Init[a] ^ i;
    }

    public void doSrli(int des, int a, int i) {
        seta0Init[des] = seta0Init[a] >>> i;
    }

    public void doSrai(int des, int a, int i) {
        seta0Init[des] = seta0Init[a] >> i;
    }

    public void doOri(int des, int a, int i) {
        seta0Init[des] = seta0Init[a] | i;
    }

    public void doAndi(int des, int a, int i) {
        seta0Init[des] = seta0Init[a] & i;
    }

    public String doEcall() {
        switch(seta0Init[10]) {
            case 1:
                return String.format("%d", seta0Init[11]);
            case 4:
                return "";
            case 10:
                return "exiting the simulator\n";
            case 11:
                return  String.format("%c", seta0Init[11]);
        }
        return "";
    }

    public void doStore(int from, int size, int offset, int to) {
        int address = (seta0Init[to] + offset)*2;
        StringBuilder a = new StringBuilder(String.format("%08x", seta0Init[from]));
        String lb = a.reverse().toString();
        for (int des = 0; des < size; des +=2) {
            processor[address + des +1] = lb.charAt(des);
            processor[address + des] = lb.charAt(des +1);
        }
    }

    public void doBeq(int a, int b, int offset) {
        if (seta0Init[a] == seta0Init[b]) {
            ins += offset;
        } else {
            ins += 4;
        }
    }

    public void doBne(int a, int b, int offset) {
        if (seta0Init[a] != seta0Init[b]) {
            ins += offset;
        } else {
            ins += 4;
        }
    }

    public void doLui(int a, int i) {
        seta0Init[a] = i << 12;
    }

    public void doJal(int des, int i) {
        if (des != 0)
            seta0Init[des] = ins +4;
        ins += i;
    }

}

class AsmProgram {
    class InstructionSet implements Comparable<InstructionSet> {
        public Instruction instruction;
        public int PC;
        public int compareTo(InstructionSet o) {
            return PC - o.PC;
        }
        public InstructionSet(Instruction ins, int pc) {
            instruction = ins;
            PC = pc;
        }
    }
    private SortedMap<Integer, InstructionSet> num;
    private Processor curPc;
    private int and;
    private Random list;

    public AsmProgram() {
        num = new TreeMap<>();
        list = new Random();
        and = 0x3000;
        list.setSeed(System.currentTimeMillis());
    }

    private int buildEasyFunction(int num) {
        if (num < 3)
            return 5 + num;
        return 25 + num;
    }

    private int buildComplexFunction(int num) {
        return 10 + num;
    }

    public void BuildEdgeCase() {
      int numOfInsToTest = 10;
      int pc = num.isEmpty()? 4096 : num.lastKey() + 4;
      for (int i = 0; i < 8; i++) {
          Instruction des = new Instruction(FUNC.ADDI);
          des.SetRD(buildComplexFunction(i));
          des.SetRS1(0);
          int type = list.nextInt(4095)  - 2040;
          while (type ==0) {
              type = list.nextInt(4095) - 2040;
          }
          des.SetIMM(type);
          num.put(pc, new InstructionSet(des, pc));
          pc += 4;
      }
      for (int i = 0; i < numOfInsToTest ; i++) {
        int hmm = list.nextInt(3);
        Instruction ins;
        switch(hmm) {
          case 0:
            ins = new Instruction(FUNC.SLL);
            break;
          case 1:
            ins = new Instruction(FUNC.SRL);
            break;
          case 2:
          default:
            ins = new Instruction(FUNC.SRA);
        }
        ins.SetRD(buildEasyFunction(list.nextInt(7)));
        ins.SetRS1(buildComplexFunction(list.nextInt(7)));
        ins.SetRS2(buildComplexFunction(list.nextInt(7)));
        num.put(pc, new InstructionSet(ins, pc));
        pc += 4;
      }

    }

    public void BuildEasyInstructions() {
        int numOfInsToTest = 300;
        System.out.println("Generating " + numOfInsToTest + " Simple Instructions...");
        int pc = num.isEmpty()? 4096 : num.lastKey() + 4;
        for (int i = 0; i < 8; i++) {
            Instruction des = new Instruction(FUNC.ADDI);
            des.SetRD(buildComplexFunction(i));
            des.SetRS1(0);
            int type = list.nextInt(4095)  - 2040;
            while (type ==0) {
                type = list.nextInt(4095) - 2040;
            }
            des.SetIMM(type);
            num.put(pc, new InstructionSet(des, pc));
            pc += 4;
        }
        for (int sub = 0; sub < 7; sub++) {
            Instruction t = new Instruction(FUNC.ADDI);
            t.SetRD(buildEasyFunction(sub));
            t.SetRS1(0);
            int imm = list.nextInt(4095) - 2040;
            while (imm == 0) {
                imm = list.nextInt(4095) - 2040;
                System.out.println("0");
            }
            t.SetIMM(imm);
            num.put(pc, new InstructionSet(t, pc));
            pc += 4;
        }
        for (int iset = 0; iset < numOfInsToTest; iset++) {
            Instruction a = doSub(numOfInsToTest,0, 8, 7);
            while (a.getType() == FUNC.BEQ || a.getType() == FUNC.BNE) {
                a = doSub(numOfInsToTest,0, 8, 7);
            }
            num.put(pc + iset * 4, new InstructionSet(a, pc + iset * 4));
        }
        pc = num.lastKey() + 4;
        int curPc = list.nextInt(1<<10 -1);
        int b = list.nextInt(1<<5 -1) + 2;
        Instruction setins = new Instruction(FUNC.ADDI);
        setins.SetRD(buildEasyFunction(0));
        setins.SetRS1(0);
        setins.SetIMM(curPc);
        num.put(pc, new InstructionSet(setins, pc));
        pc += 4;
        setins = new Instruction(FUNC.ADDI);
        setins.SetRD(buildEasyFunction(1));
        setins.SetRS1(0);
        setins.SetIMM(b);
        num.put(pc, new InstructionSet(setins, pc));
        pc += 4;
        while (curPc > 0) {
            Instruction sb = new Instruction(FUNC.REM);
            sb.SetRD(buildEasyFunction(2));
            sb.SetRS1(buildEasyFunction(0));
            sb.SetRS2(buildEasyFunction(1));
            num.put(pc, new InstructionSet(sb, pc));
            pc += 4;
            Instruction func = new Instruction(FUNC.DIV);
            func.SetRD(buildEasyFunction(0));
            func.SetRS1(buildEasyFunction(0));
            func.SetRS2(buildEasyFunction(1));
            num.put(pc, new InstructionSet(func, pc));
            pc += 4;
            curPc /= b;
        }
        curPc = -list.nextInt(1<<10 -1);
        b = -list.nextInt(1<<5 -1) - 2;
        setins = new Instruction(FUNC.ADDI);
        setins.SetRD(buildEasyFunction(0));
        setins.SetRS1(0);
        setins.SetIMM(curPc);
        num.put(pc, new InstructionSet(setins, pc));
        pc += 4;
        setins = new Instruction(FUNC.ADDI);
        setins.SetRD(buildEasyFunction(1));
        setins.SetRS1(0);
        setins.SetIMM(b);
        num.put(pc, new InstructionSet(setins, pc));
        pc += 4;
        while (curPc != 0) {
            Instruction sb = new Instruction(FUNC.REM);
            sb.SetRD(buildEasyFunction(2));
            sb.SetRS1(buildEasyFunction(0));
            sb.SetRS2(buildEasyFunction(1));
            num.put(pc, new InstructionSet(sb, pc));
            pc += 4;
            Instruction func = new Instruction(FUNC.DIV);
            func.SetRD(buildEasyFunction(0));
            func.SetRS1(buildEasyFunction(0));
            func.SetRS2(buildEasyFunction(1));
            num.put(pc, new InstructionSet(func, pc));
            pc += 4;
            curPc /= b;
        }
    }

    public void BuildStoreLoadTest() {
        int div = 100;
        System.out.println("Generating " + div *2 + " Store / Load Instructions...");
        int curPC = num.isEmpty()? 4096 : num.lastKey() + 4;
        Instruction funcLength = new Instruction(FUNC.ADD);
        funcLength.SetRD(buildComplexFunction(0));
        funcLength.SetRS1(0);
        funcLength.SetRS2(3);
        num.put(curPC, new InstructionSet(funcLength,curPC));
        curPC += 4;
        for (int func = 1; func < 8; func++) {
            Instruction ins = new Instruction(FUNC.ADDI);
            ins.SetRD(buildComplexFunction(func));
            ins.SetRS1(0);
            int curPc = list.nextInt(4095)  - 2040;
            while (curPc ==0) {
                curPc = list.nextInt(4095) - 2040;
            }
            ins.SetIMM(curPc);
            num.put(curPC, new InstructionSet(ins,curPC));
            curPC += 4;
        }
        for (int memSize = 0; memSize < div; memSize++) {
            int type = list.nextInt(3);
            Instruction processor;
            switch (type) {
                case 0:
                    processor = new Instruction(FUNC.SB);
                    break;
                case 1:
                    processor = new Instruction(FUNC.SH);
                    break;
                case 2:
                default:
                    processor = new Instruction(FUNC.SW);
                    break;
            }
            int ecal = list.nextInt(7) + 1;
            processor.SetRS1(buildComplexFunction(0));
            processor.SetRS2(buildComplexFunction(ecal));
            processor.SetIMM(memSize * 4);
            num.put(curPC, new InstructionSet(processor,curPC));
            curPC += 4;
        }
        for (int i = 1; i < 8; i++) {
            Instruction imm = new Instruction(FUNC.ADD);
            imm.SetRD(buildComplexFunction(i));
            imm.SetRS1(0);
            imm.SetRS2(0);
            num.put(curPC, new InstructionSet(imm,curPC));
            curPC += 4;
        }
        for (int des = 0; des < div; des++) {
            int xori = list.nextInt(3);
            Instruction store;
            switch (xori) {
                case 0:
                    store = new Instruction(FUNC.LB);
                    break;
                case 1:
                    store = new Instruction(FUNC.LH);
                    break;
                case 2:
                default:
                    store = new Instruction(FUNC.LW);
                    break;
            }
            int code = list.nextInt(7) + 1;
            store.SetRS1(buildComplexFunction(0));
            store.SetRD(buildComplexFunction(code));
            store.SetIMM(des * 4);
            num.put(curPC, new InstructionSet(store,curPC));
            curPC += 4;
        }
    }

    private Instruction doSub(int totalIns, int used, int a , int t) {
        int randomCases = list.nextInt(24);
        Instruction i;
        int RD = list.nextInt(a + t) - a;
        int des = list.nextInt(a + t) - a;
        int rs1 = list.nextInt(a + t) - a;
        int imm = list.nextInt(524288) -1 ;
        if (RD < 0) {
            RD = buildComplexFunction(RD+a);
        } else {
            RD = buildEasyFunction(RD);
        }
        if (des < 0) {
            des = buildComplexFunction(des +a);
        } else {
            des = buildEasyFunction(des);
        }
        if (rs1 < 0) {
            rs1 = buildComplexFunction(rs1 +a);
        } else {
            rs1 = buildEasyFunction(rs1);
        }
        i = new Instruction(FUNC.ADDI);
        i.SetRD(RD);
        i.SetRS1(des);
        i.SetRS2(rs1);

        switch (randomCases) {
            case 0:
                i.SetType(FUNC.ADD);
                break;
            case 1:
                i.SetType(FUNC.MUL);
                break;
            case 2:
                i.SetType(FUNC.SUB);
                break;
            case 3:
                i.SetType(FUNC.SLL);
                break;
            case 4:
                i.SetType(FUNC.MULH);
                break;
            case 5:
                i.SetType(FUNC.SLT);
                break;
            case 6:
                i.SetType(FUNC.XOR);
                break;
            case 7:
                i.SetType(FUNC.MUL);
                break;
            case 8:
                i.SetType(FUNC.SRL);
                break;
            case 9:
                i.SetType(FUNC.SRA);
                break;
            case 10:
                i.SetType(FUNC.OR);
                break;
            case 11:
                i.SetType(FUNC.ADD);
                break;
            case 12:
                i.SetType(FUNC.AND);
                break;
            case 13:
                i.SetType(FUNC.ADDI);
                i.SetIMM((imm % 4096) - 2048);
                break;
            case 14:
                i.SetType(FUNC.SLLI);
                i.SetIMM(imm % 32);
                break;
            case 15:
                i.SetType(FUNC.SLTI);
                i.SetIMM((imm % 4096) - 2048);
                break;
            case 16:
                i.SetType(FUNC.XORI);
                i.SetIMM((imm % 4096) - 2048);
                break;
            case 17:
                i.SetType(FUNC.SRLI);
                i.SetIMM(imm % 32);
                break;
            case 18:
                i.SetType(FUNC.SRAI);
                i.SetIMM(imm % 32);
                break;
            case 19:
                i.SetType(FUNC.ORI);
                i.SetIMM((imm % 4096) - 2048);
                break;
            case 20:
                i.SetType(FUNC.ANDI);
                i.SetIMM((imm % 4096) - 2048);
                break;
            case 21:
                i.SetType(FUNC.BEQ);
                int num = imm % (totalIns-used + 1);
                if (num == 0)
                    num += 1;
                num *= 4;
                i.SetIMM(num);
                break;
            case 22:
                i.SetType(FUNC.BNE);
                num = imm % (totalIns-used + 1);
                if (num == 0)
                    num += 1;
                num *= 4;
                i.SetIMM(num);
                break;
            case 23:
                i.SetType(FUNC.ORI);
                i.SetIMM(imm);
                break;
        }
        return i;
    }

    public void BuildEasyFunction() {
        int build = list.nextInt(7) + 1;
        int processor = list.nextInt(6);
        int func_length = list.nextInt(100);
        System.out.println("Generating Simple Function with " + func_length + " instructions...");
        ArrayList<InstructionSet> des = new ArrayList<>();
        int a = 8;
        for (int sb = 0; sb < build; sb++) {
            Instruction result = new Instruction(FUNC.ADD);
            result.SetRD(buildComplexFunction(sb));
            result.SetRS1(0);
            result.SetRS2(0);
            des.add(new InstructionSet(result, a));
            a +=4;
        }
        for (int sh = 0; sh < processor; sh++) {
            Instruction clear = new Instruction(FUNC.ADD);
            clear.SetRD(buildEasyFunction(sh));
            clear.SetRS1(0);
            clear.SetRS2(0);
            des.add(new InstructionSet(clear, a));
            a +=4;
        }
        for (int funct3 = 0; funct3 < func_length; funct3++) {
            des.add(new InstructionSet(doSub(func_length, funct3 +1, build, processor), a));
            a +=4;
        }
        Instruction ori = new Instruction(FUNC.JAL);
        ori.SetRD(0);
        ori.SetIMM(func_length * 4 + 8);
        des.add(new InstructionSet(ori, a));
        a += 4;
        Instruction bfj = new Instruction(FUNC.JAL);
        bfj.SetRD(1);
        bfj.SetIMM(a);
        des.add(new InstructionSet(bfj,4));
        for (int setins = 0; setins < func_length; setins++) {
            des.add(new InstructionSet(doSub(func_length, setins +1, build, 0), a));
            a +=4;
        }
        Instruction afj = new Instruction(FUNC.JAL);
        afj.SetRD(1);
        afj.SetIMM( 8 - a);
        des.add(new InstructionSet(afj, a));
        int curPC = num.isEmpty() ? (4092) :  num.lastKey();
        for (int f = 0; f < des.size(); f++) {
            InstructionSet n = des.get(f);
            n.PC += curPC;
            num.put(n.PC, n);
        }
    }

    public void BuildComplexFunction() {
        int a = 50;
        int address = num.isEmpty()? 4096 : num.lastKey() + 4;
        System.out.println("Generating for loops with " + a + " instructions...");
        Instruction sra = new Instruction(FUNC.ADDI);
        sra.SetRD(buildEasyFunction(0));
        sra.SetRS1(0);
        sra.SetIMM(list.nextInt(50));
        num.put(address,new InstructionSet(sra, address));
        address += 4;
        Instruction o = new Instruction(FUNC.BEQ);
        o.SetRS1(buildEasyFunction(0));
        o.SetRS2(0);
        o.SetIMM(a * 4 + 12);
        num.put(address,new InstructionSet(o, address));
        address += 4;
        for (int result = 0; result < a; result++) {
            Instruction store = doSub(100, 0,7,0);
            while (store.getType() == FUNC.BEQ || store.getType() == FUNC.BNE) {
                store = doSub(100,0,7,0);
            }
            num.put(address,new InstructionSet(store, address));
            address += 4;
        }
        Instruction b = new Instruction(FUNC.ADDI);
        b.SetRD(buildEasyFunction(0));
        b.SetRS1(buildEasyFunction(0));
        b.SetIMM(-1);
        num.put(address,new InstructionSet(b, address));
        address += 4;
        Instruction rs2 = new Instruction(FUNC.JAL);
        rs2.SetRD(0);
        rs2.SetIMM(-(a * 4 + 8));
        num.put(address,new InstructionSet(rs2, address));
    }

    public void EndOfProgram() {
        Instruction iset = new Instruction(FUNC.ADDI);
        iset.SetRD(10);
        iset.SetRS1(0);
        iset.SetIMM(10);
        Instruction b = new Instruction(FUNC.ECAL);
        int des = num.isEmpty()? 4092 : num.lastKey();
        num.put(des + 4, new InstructionSet(iset, des + 4));
        num.put(des + 8, new InstructionSet(b, des + 8));
    }

    public String toMachineCode() {
        StringBuilder func = new StringBuilder();
        for (InstructionSet iset : num.values()) {
            func.append(String.format("%08x",GUtil.toHex(iset.instruction.toMachineCodeBin())));
            func.append("\n");
        }
        return func.toString();
    }

    public String toASM() {
        StringBuilder curPc = new StringBuilder();
        for (InstructionSet iset : num.values()) {
            curPc.append(iset.instruction.toASM());
            curPc.append("\n");
        }
        return curPc.toString();
    }

    public String toTrace() {
        curPc = new Processor();
        StringBuilder memSize = new StringBuilder();
        while (curPc.getPC() != num.lastKey() + 4) {
            String imm = Executor.exectute(curPc, num.get(curPc.getPC()).instruction);
            if (!imm.equals("")) {
                memSize.append(imm);
                if (imm.equals("exiting the simulator\n")) {
                    break;
                }
            }
            memSize.append(curPc.dump());
            memSize.append("\n");
        }
        return memSize.toString();
    }
}

public class gen {
    final static int StartPC = 0x1000;
    static int currentAddress = StartPC;
    static int Mode = 0b1111;
    static int randomCases = 10;

    static PrintWriter codeF;
    static PrintWriter refF;
    static PrintWriter traceF;


    static AsmProgram program;

    private static void set() {
        program = new AsmProgram();
        if ((Mode & 1) > 0)
            program.BuildEasyInstructions();
            program.BuildEdgeCase();
        if (((Mode >> 1) & 1) > 0)
            program.BuildEasyFunction();
        if (((Mode >> 2) & 1) > 0)
            program.BuildStoreLoadTest();
        if (((Mode >> 3) & 1) > 0)
            program.BuildComplexFunction();
        program.EndOfProgram();
        codeF.print(program.toMachineCode());
        refF.print(program.toASM());
        traceF.print(program.toTrace());
    }


    public static void main(String[] args) {
        if (args.length > 0) {
            Mode = Integer.parseInt(args[0]);
        } else {
            Mode = 0;
            Scanner des = new Scanner(System.in);
            String beq;
            System.out.println("Test Basic Instructions? (Y/N) ");
            beq = des.next().toLowerCase();
            if (beq.length() > 0 && beq.charAt(0) == 'y') {
                Mode |= 1;
            }
            System.out.println("Test Simple Function? (Y/N) ");
            beq = des.next().toLowerCase();
            if (beq.length() > 0 && beq.charAt(0) == 'y') {
                Mode |= 1<<1;
            }
            System.out.println("Test Store / Load? (Y/N) ");
            beq = des.next().toLowerCase();
            if (beq.length() > 0 && beq.charAt(0) == 'y') {
                Mode |= 1<<2;
            }
            System.out.println("Test For Loop? (Y/N) ");
            beq = des.next().toLowerCase();
            if (beq.length() > 0 && beq.charAt(0) == 'y') {
                Mode |= 1<<3;
            }
        }
        try {
            codeF = new PrintWriter("codegen.txt");
            refF = new PrintWriter("refgen.txt");
            traceF = new PrintWriter("tracegen.txt");
            set();
            codeF.close();
            refF.close();
            traceF.close();
        } catch (IOException e) {
            System.out.println("Gen File System ERROR");
        }


    }
}
