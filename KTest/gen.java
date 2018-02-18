/* Auto Generator for CS61C SP18 Proj2 P1.
Author : Li Qin
Version: V1.0
*/
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

enum InsType{
    R, I, U, UJ, S, SB
}
enum FUNC {
    //---------------R TYPE ------------------------
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
    //--------------I TYPE (LOAD)-------------------
    LB  (InsType.I,0x03,0x0,0   ,"lb"),
    LH  (InsType.I,0x03,0x1,0   ,"lh"),
    LW  (InsType.I,0x03,0x2,0   ,"lw"),
    //--------------I TYPE -------------------------
    ADDI(InsType.I,0x13,0x0,0   ,"addi"),
    SLLI(InsType.I,0x13,0x1,0x00,"slli"),
    SLTI(InsType.I,0x13,0x2,0   ,"slti"),
    XORI(InsType.I,0x13,0x4,0   ,"xori"),
    SRLI(InsType.I,0x13,0x5,0x00,"srli"),
    SRAI(InsType.I,0x13,0x5,0x20,"srai"),
    ORI (InsType.I,0x13,0x6,0   ,"ori"),
    ADNI(InsType.I,0x13,0x7,0   ,"andi"),
    ECAL(InsType.I,0x73,0x0,0x00,"ecall"),
    //-------------S TYPE --------------------------
    SB  (InsType.S,0x23,0x0,0   ,"sb"),
    SH  (InsType.S,0x23,0x1,0   ,"sh"),
    SW  (InsType.S,0x23,0x2,0   ,"sw"),
    //-------------SB TYPE -------------------------
    BEQ (InsType.SB,0x63,0x0,0  ,"beq"),
    BNE (InsType.SB,0x63,0x1,0  ,"bne"),
    //-------------U TYPE --------------------------
    LUI (InsType.U,0x37,0   ,0  ,"lui"),
    //-------------UJ TYPE -------------------------
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
        StringBuilder op = new StringBuilder(Integer.toBinaryString(func.opcode));
        while (op.length() < 7) {
            op.insert(0, "0");
        }
        if (op.length() > 7) {
            op = new StringBuilder(op.substring(op.length() - 8));
        }
        return op.toString();
    }
    public static String GenFunct3(FUNC func) {
        StringBuilder f3 = new StringBuilder(Integer.toBinaryString(func.funct3));
        while (f3.length() < 3) {
            f3.insert(0, "0");
        }
        if (f3.length() > 3) {
            f3 = new StringBuilder(f3.substring(f3.length() - 4));
        }
        return f3.toString();
    }
    public static String GenFunct7(FUNC func) {
        StringBuilder f7 = new StringBuilder(Integer.toBinaryString(func.funct7));
        while (f7.length() < 7) {
            f7.insert(0, "0");
        }
        if (f7.length() > 7) {
            f7 = new StringBuilder(f7.substring(f7.length() - 8));
        }
        return f7.toString();
    }

    public static String GenRegister(int num) {
        StringBuilder reg = new StringBuilder(Integer.toBinaryString(num));
        while (reg.length() < 5) {
            reg.insert(0, "0");
        }
        if (reg.length() > 5) {
            reg = new StringBuilder(reg.substring(reg.length() - 8));
        }
        return reg.toString();
    }

    public static String GenIMM(int num, int size, boolean ext) {
        StringBuilder imm = new StringBuilder(Integer.toBinaryString(num));
        while (imm.length() < size) {
            if (!ext)
                imm.insert(0,"0");
            else {
                if (num<0)
                    imm.insert(0,"1");
                else
                    imm.insert(0,"0");
            }
        }
        if (imm.length() > size) {
            imm = new StringBuilder(imm.substring(imm.length() - size));
        }
        //System.out.println(num);
        //System.out.println(imm.toString());
        return imm.toString();
    }

    public static long toHex(String code) {
        return Long.parseLong(code,2);
    }
}

class Instruction {
    private FUNC func;
    private String OP, F3, F7;
    private int RD, RS1, RS2;
    private int IMM;

    public Instruction(FUNC func) {
        this.func = func;
        OP = GUtil.GenOPString(func);
        F3 = GUtil.GenFunct3(func);
        F7 = GUtil.GenFunct7(func);
    }

    public boolean SetRD(int reg) {
        if (func.type == InsType.S || func.type == InsType.SB)
            return false;
        if (reg < 0 || reg > 31)
            return false;
        RD = reg;
        return true;
    }

    public boolean SetRS1(int reg) {
        if (func.type == InsType.U || func.type == InsType.UJ)
            return false;
        if (reg < 0 || reg > 31)
            return false;
        RS1 = reg;
        return true;
    }

    public boolean SetRS2(int reg) {
        if (func.type == InsType.U || func.type == InsType.UJ || func.type == InsType.I)
            return false;
        if (reg < 0 || reg > 31)
            return false;
        RS2 = reg;
        return true;
    }

    public boolean SetIMM(int imm) {
        switch (func.type) {
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
        IMM = imm;
        return true;
    }

    public String toMachineCodeBin() {
        StringBuilder result = new StringBuilder();
        switch (func.type) {
            case R:
                result.append(F7);
                result.append(GUtil.GenRegister(RS2));
                result.append(GUtil.GenRegister(RS1));
                result.append(F3);
                result.append(GUtil.GenRegister(RD));
                result.append(OP);
                break;
            case I:
                if(func == FUNC.ECAL) {
                    result.append(GUtil.GenIMM(0,12,false));
                    result.append(GUtil.GenRegister(0));
                    result.append(F3);
                    result.append(GUtil.GenRegister(0));
                    result.append(OP);
                } else {
                    if (func == FUNC.SLLI || func == FUNC.SRLI || func == FUNC.SRAI) {
                        result.append(F7);
                        result.append(GUtil.GenIMM(IMM,5,false));
                    } else {
                        result.append(GUtil.GenIMM(IMM,12,true));
                    }
                    result.append(GUtil.GenRegister(RS1));
                    result.append(F3);
                    result.append(GUtil.GenRegister(RD));
                    result.append(OP);
                }
                break;
            case S:
                String imm = GUtil.GenIMM(IMM,12,true);
                result.append(imm.substring(0,7));
                result.append(GUtil.GenRegister(RS2));
                result.append(GUtil.GenRegister(RS1));
                result.append(F3);
                result.append(imm.substring(7));
                result.append(OP);
                break;
            case SB:
                imm = GUtil.GenIMM(IMM>>>1, 12, true);
                result.append(imm.substring(0,1));
                result.append(imm.substring(2,8));
                result.append(GUtil.GenRegister(RS2));
                result.append(GUtil.GenRegister(RS1));
                result.append(F3);
                result.append(imm.substring(8,12));
                result.append(imm.substring(1,2));
                result.append(OP);
                break;
            case U:
                result.append(GUtil.GenIMM(IMM,20,false));
                result.append(GUtil.GenRegister(RD));
                result.append(OP);
                break;
            case UJ:
                imm = GUtil.GenIMM(IMM>>>1,20,true);
                result.append(imm.substring(0,1));
                result.append(imm.substring(10,20));
                result.append(imm.substring(9,10));
                result.append(imm.substring(1,9));
                result.append(GUtil.GenRegister(RD));
                result.append(OP);
                break;
            default:
                result.append("[Unknown]");
        }
        return result.toString();
    }

    public String toASM() {
        StringBuilder result = new StringBuilder();
        result.append(func.name);
        switch (func.type) {
            case R:
                result.append("\tx");
                result.append(RD);
                result.append(", x");
                result.append(RS1);
                result.append(", x");
                result.append(RS2);
                break;
            case I:
                if (func.opcode == 0x03) {
                    result.append("\tx");
                    result.append(RD);
                    result.append(", ");
                    result.append(IMM);
                    result.append("(x");
                    result.append(RS1);
                    result.append(")");
                } else if (func.opcode == 0x13) {
                    result.append("\tx");
                    result.append(RD);
                    result.append(", x");
                    result.append(RS1);
                    result.append(", ");
                    result.append(IMM);
                }
                break;
            case S:
                result.append("\tx");
                result.append(RS2);
                result.append(", ");
                result.append(IMM);
                result.append("(x");
                result.append(RS1);
                result.append(")");
                break;
            case SB:
                result.append("\tx");
                result.append(RS1);
                result.append(", x");
                result.append(RS2);
                result.append(", ");
                result.append(IMM);
                break;
            case U:
            case UJ:
                result.append("\tx");
                result.append(RD);
                result.append(", ");
                result.append(IMM);
                break;
        }
        return result.toString();
    }
}


public class gen {
    final static int StartPC = 0x1000;
    static int currentAddress = StartPC;
    /* Do Not Change Anything Above this line.*/
    /* Change Here for different Default values. */
    static int Mode = 0b111111;
    static int FullGen = 0b111111;
    static int randomCases = 10;
    /* Do Not Change Anything after this line.*/
    static PrintWriter codeF;
    static PrintWriter refF;
    static Random rand;

    private static void doRTestF() {
            boolean fully = false;
            for (FUNC f: FUNC.values()) {
                if (f.type == InsType.R) {
                    for (int rd = 0; rd < (fully? 10: 32); rd++) {
                        for (int rs1 = 0; rs1 < (fully? 10 : 32); rs1++) {
                            for (int rs2 = 0; rs2 < (fully? 10 : 32); rs2++) {
                                Instruction ins = new Instruction(f);
                                ins.SetRD(rd);
                                ins.SetRS1(rs1);
                                ins.SetRS2(rs2);
                                refF.printf("%08x: %s\n",currentAddress, ins.toASM());
                                codeF.printf("%08x\n",GUtil.toHex(ins.toMachineCodeBin()));
                                currentAddress += 4;
                            }
                        }
                    }
                    fully = true;
                }
            }
    }

    private static void doRTestR() {
        for (FUNC f: FUNC.values()) {
            if (f.type == InsType.R) {
                for (int i = 0; i < randomCases; i++) {
                    int rd = rand.nextInt(32);
                    int rs1 = rand.nextInt(32);
                    int rs2 = rand.nextInt(32);
                    Instruction ins = new Instruction(f);
                    ins.SetRD(rd);
                    ins.SetRS1(rs1);
                    ins.SetRS2(rs2);
                    refF.printf("%08x: %s\n",currentAddress, ins.toASM());
                    codeF.printf("%08x\n",GUtil.toHex(ins.toMachineCodeBin()));
                    currentAddress += 4;
                }
            }
        }
    }


    private static void doITestF() {
        boolean fullys = false;
        boolean fullyg = false;
        boolean fullyi = false;
        for (FUNC f: FUNC.values()) {
            if (f.type == InsType.I) {
                if (f == FUNC.ECAL) {
                    Instruction ins = new Instruction(f);
                    refF.printf("%08x: %s\n",currentAddress, ins.toASM());
                    codeF.printf("%08x\n",GUtil.toHex(ins.toMachineCodeBin()));
                    currentAddress += 4;
                } else if (f == FUNC.SLLI || f == FUNC.SRLI || f == FUNC.SRAI) {
                    for (int rd = 0; rd < (fullys? 10: 32); rd++) {
                        for (int rs1 = 0; rs1 <(fullys? 10 : 32); rs1++) {
                            for (int imm = 0; imm < 32; imm++) {
                                Instruction ins = new Instruction(f);
                                ins.SetRD(rd);
                                ins.SetRS1(rs1);
                                ins.SetIMM(imm);
                                refF.printf("%08x: %s\n",currentAddress, ins.toASM());
                                codeF.printf("%08x\n",GUtil.toHex(ins.toMachineCodeBin()));
                                currentAddress += 4;
                            }
                        }
                    }
                    fullys = true;
                } else {
                    // TEST REG
                    for (int rd = 0; rd < (fullyg? 10 : 32); rd++) {
                        for (int rs1 = 0; rs1 < (fullyg? 10 :32); rs1++) {
                            for (int imm = -20; imm < 20; imm++) {
                                Instruction ins = new Instruction(f);
                                ins.SetRD(rd);
                                ins.SetRS1(rs1);
                                ins.SetIMM(imm);
                                refF.printf("%08x: %s\n",currentAddress, ins.toASM());
                                codeF.printf("%08x\n",GUtil.toHex(ins.toMachineCodeBin()));
                                currentAddress += 4;
                            }
                        }
                    }
                    fullyg = true;
                    //TEST imm
                    for (int imm = (fullyi? -100 : -1024); imm <= (fullyi? 100: 1024); imm++) {
                        Instruction ins = new Instruction(f);
                        ins.SetRD(5);
                        ins.SetRS1(3);
                        ins.SetIMM(imm);
                        refF.printf("%08x: %s\n",currentAddress, ins.toASM());
                        codeF.printf("%08x\n",GUtil.toHex(ins.toMachineCodeBin()));
                        currentAddress += 4;
                    }
                    fullyi = true;
                }
            }
        }
    }

    private static void doITestR() {
        for (FUNC f: FUNC.values()) {
            if (f.type == InsType.I) {
                if (f == FUNC.ECAL) {
                    Instruction ins = new Instruction(f);
                    refF.printf("%08x: %s\n",currentAddress, ins.toASM());
                    codeF.printf("%08x\n",GUtil.toHex(ins.toMachineCodeBin()));
                    currentAddress += 4;
                } else if (f == FUNC.SLLI || f == FUNC.SRLI || f == FUNC.SRAI) {
                    for (int i = 0; i < randomCases; i++) {
                        int rd = rand.nextInt(32);
                        int rs1 = rand.nextInt(32);
                        int imm = rand.nextInt(32);
                        Instruction ins = new Instruction(f);
                        ins.SetRD(rd);
                        ins.SetRS1(rs1);
                        ins.SetIMM(imm);
                        refF.printf("%08x: %s\n",currentAddress, ins.toASM());
                        codeF.printf("%08x\n",GUtil.toHex(ins.toMachineCodeBin()));
                        currentAddress += 4;
                    }
                } else {
                    for (int i = 0; i < randomCases; i++) {
                        int rd = rand.nextInt(32);
                        int rs1 = rand.nextInt(32);
                        int imm = rand.nextInt(2049);
                        imm -= 1024;
                        Instruction ins = new Instruction(f);
                        ins.SetRD(rd);
                        ins.SetRS1(rs1);
                        ins.SetIMM(imm);
                        refF.printf("%08x: %s\n",currentAddress, ins.toASM());
                        codeF.printf("%08x\n",GUtil.toHex(ins.toMachineCodeBin()));
                        currentAddress += 4;
                    }
                }

            }
        }
    }

    private static void doUTestF() {
        // test reg
        for (int rd = 0; rd < 32; rd++) {
            for (int imm = 0; imm < 200; imm++){
                Instruction ins = new Instruction(FUNC.LUI);
                ins.SetRD(rd);
                ins.SetIMM(imm);
                refF.printf("%08x: %s\n",currentAddress, ins.toASM());
                codeF.printf("%08x\n",GUtil.toHex(ins.toMachineCodeBin()));
                currentAddress += 4;
            }
        }
        // test num
        int rd = 3;
        for (int i = 19; i > 10; i--) {
            for (int imm = 1<<i; imm > (1<<i) - 1024; imm--) {
                Instruction ins = new Instruction(FUNC.LUI);
                ins.SetRD(rd);
                ins.SetIMM(imm);
                refF.printf("%08x: %s\n",currentAddress, ins.toASM());
                codeF.printf("%08x\n",GUtil.toHex(ins.toMachineCodeBin()));
                currentAddress += 4;
            }
        }

    }

    private static void doUTestR() {
        for (int i = 0; i < randomCases; i++) {
            int rd = rand.nextInt(32);
            int imm = rand.nextInt(1<<19);
            Instruction ins = new Instruction(FUNC.LUI);
            ins.SetRD(rd);
            ins.SetIMM(imm);
            refF.printf("%08x: %s\n",currentAddress, ins.toASM());
            codeF.printf("%08x\n",GUtil.toHex(ins.toMachineCodeBin()));
            currentAddress += 4;
        }
    }

    private static void doUJTestF() {
        // test reg
        for (int rd = 0; rd < 32; rd++) {
            for (int imm = -50; imm < 50; imm+=2){
                Instruction ins = new Instruction(FUNC.JAL);
                ins.SetRD(rd);
                ins.SetIMM(imm);
                refF.printf("%08x: %s\n",currentAddress, ins.toASM());
                codeF.printf("%08x\n",GUtil.toHex(ins.toMachineCodeBin()));
                currentAddress += 4;
            }
        }
        // test num
        int rd = 3;
        for (int i = 19; i > 11; i--) {
            for (int imm = 1<<i; imm > (1<<i) - 512; imm-=2) {
                Instruction ins = new Instruction(FUNC.JAL);
                ins.SetRD(rd);
                ins.SetIMM(imm);
                refF.printf("%08x: %s\n",currentAddress, ins.toASM());
                codeF.printf("%08x\n",GUtil.toHex(ins.toMachineCodeBin()));
                currentAddress += 4;
            }
            for (int imm = -1<<i; imm < -(1<<i) + 256; imm+=2) {
                Instruction ins = new Instruction(FUNC.JAL);
                ins.SetRD(rd);
                ins.SetIMM(imm);
                refF.printf("%08x: %s\n",currentAddress, ins.toASM());
                codeF.printf("%08x\n",GUtil.toHex(ins.toMachineCodeBin()));
                currentAddress += 4;
            }
        }
    }

    private static void doUJTestR() {
        for (int i = 0; i < randomCases; i++) {
            int rd = rand.nextInt(32);
            int imm = rand.nextInt(1<<19);
            if (imm % 2 == 1)
                imm--;
            Instruction ins = new Instruction(FUNC.JAL);
            ins.SetRD(rd);
            ins.SetIMM(imm);
            refF.printf("%08x: %s\n",currentAddress, ins.toASM());
            codeF.printf("%08x\n",GUtil.toHex(ins.toMachineCodeBin()));
            currentAddress += 4;
        }
    }

    private static void doSTestF() {
        for (FUNC f: FUNC.values()) {
            if (f.type == InsType.S) {
                // TEST REG
                for (int rs1 = 0; rs1 < 32; rs1++) {
                    for (int rs2 = 0; rs2 < 32; rs2++) {
                        for (int imm = -20; imm < 20; imm++) {
                            Instruction ins = new Instruction(f);
                            ins.SetRS1(rs1);
                            ins.SetRS2(rs2);
                            ins.SetIMM(imm);
                            refF.printf("%08x: %s\n",currentAddress, ins.toASM());
                            codeF.printf("%08x\n",GUtil.toHex(ins.toMachineCodeBin()));
                            currentAddress += 4;
                        }
                    }
                }
                //TEST imm
                for (int imm = -1024; imm <= 1024; imm++) {
                    Instruction ins = new Instruction(f);
                    ins.SetRS1(5);
                    ins.SetRS2(3);
                    ins.SetIMM(imm);
                    refF.printf("%08x: %s\n",currentAddress, ins.toASM());
                    codeF.printf("%08x\n",GUtil.toHex(ins.toMachineCodeBin()));
                    currentAddress += 4;
                }
            }
        }
    }

    private static void doSTestR() {
        for (FUNC f: FUNC.values()) {
            if (f.type == InsType.S) {
                for (int i = 0; i < randomCases; i++) {
                    int rs1 = rand.nextInt(32);
                    int rs2 = rand.nextInt(32);
                    int imm = rand.nextInt(2049);
                    imm -= 1024;
                    Instruction ins = new Instruction(f);
                    ins.SetRS1(rs1);
                    ins.SetRS2(rs2);
                    ins.SetIMM(imm);
                    refF.printf("%08x: %s\n", currentAddress, ins.toASM());
                    codeF.printf("%08x\n", GUtil.toHex(ins.toMachineCodeBin()));
                    currentAddress += 4;
                }

            }
        }
    }

    private static void doSBTestF() {
        boolean fully = false;
        for (FUNC f: FUNC.values()) {
            if (f.type == InsType.SB) {
                // TEST REG
                for (int rs1 = 0; rs1 < (fully? 10 : 32); rs1++) {
                    for (int rs2 = 0; rs2 < (fully? 10 : 32); rs2++) {
                        for (int imm = -20; imm < 20; imm+=2) {
                            Instruction ins = new Instruction(f);
                            ins.SetRS1(rs1);
                            ins.SetRS2(rs2);
                            ins.SetIMM(imm);
                            refF.printf("%08x: %s\n",currentAddress, ins.toASM());
                            codeF.printf("%08x\n",GUtil.toHex(ins.toMachineCodeBin()));
                            currentAddress += 4;
                        }
                    }
                }
                //TEST imm
                for (int imm = -4096; imm <= 4094; imm+=2) {
                    Instruction ins = new Instruction(f);
                    ins.SetRS1(5);
                    ins.SetRS2(3);
                    ins.SetIMM(imm);
                    refF.printf("%08x: %s\n",currentAddress, ins.toASM());
                    codeF.printf("%08x\n",GUtil.toHex(ins.toMachineCodeBin()));
                    currentAddress += 4;
                }
            }
            fully = true;
        }
    }

    private static void doSBTestR() {
        for (FUNC f: FUNC.values()) {
            if (f.type == InsType.SB) {
                for (int i = 0; i < randomCases; i++) {
                    int rs1 = rand.nextInt(32);
                    int rs2 = rand.nextInt(32);
                    int imm = rand.nextInt(8190);
                    if (imm %2 ==1)
                        imm--;
                    imm -= 4096;
                    Instruction ins = new Instruction(f);
                    ins.SetRS1(rs1);
                    ins.SetRS2(rs2);
                    ins.SetIMM(imm);
                    refF.printf("%08x: %s\n", currentAddress, ins.toASM());
                    codeF.printf("%08x\n", GUtil.toHex(ins.toMachineCodeBin()));
                    currentAddress += 4;
                }

            }
        }
    }

    private static void doGen() {
        //R
        if ((Mode>>5 & 1) > 0) {
            System.err.println("Start Generate R Tests...");
            if ((FullGen >> 5 & 1) > 0) {
                doRTestF();
            } else {
                doRTestR();
            }
            System.err.println("Done.");
        }
        //I
        if ((Mode>>4 & 1) > 0) {
            System.err.println("Start Generate I Tests...");
            if ((FullGen >> 4 & 1) > 0) {
                doITestF();
            } else {
                doITestR();
            }
            System.err.println("Done.");
        }
        //U
        if ((Mode>>3 & 1) > 0) {
            System.err.println("Start Generate U Tests...");
            if ((FullGen >> 3 & 1) > 0) {
                doUTestF();
            } else {
                doUTestR();
            }
            System.err.println("Done.");
        }
        //UJ
        if ((Mode>>2 & 1) > 0) {
            System.err.println("Start Generate UJ Tests...");
            if ((FullGen >> 2 & 1) > 0) {
                doUJTestF();
            } else {
                doUJTestR();
            }
            System.err.println("Done.");
        }
        //S
        if ((Mode>>1 & 1) > 0) {
            System.err.println("Start Generate S Tests...");
            if ((FullGen >> 1 & 1) > 0) {
                doSTestF();
            } else {
                doSTestR();
            }
            System.err.println("Done.");
        }
        //SB
        if ((Mode & 1) > 0) {
            System.err.println("Start Generate UB Tests...");
            if ((FullGen & 1) > 0) {
                doSBTestF();
            } else {
                doSBTestR();
            }
            System.err.println("Done.");
        }
    }


    public static void main(String[] args) {
        if (args.length > 0) {
            Mode = Integer.parseInt(args[0]);
        }
        if (args.length > 1) {
            FullGen = Integer.parseInt(args[1]);
        }
        rand = new Random();
        try {
            codeF = new PrintWriter("codegen.txt");
            refF = new PrintWriter("refgen.txt");
            doGen();
            codeF.close();
            refF.close();
        } catch (IOException e) {
            System.out.println("Gen File System ERROR");
        }


    }
}
