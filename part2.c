#include <stdio.h> // for stderr
#include <stdlib.h> // for exit()
#include "types.h"
#include "utils.h"
#include "riscv.h"

void execute_rtype(Instruction, Processor *);
void execute_itype_except_load(Instruction, Processor *);
void execute_branch(Instruction, Processor *);
void execute_jal(Instruction, Processor *);
void execute_load(Instruction, Processor *, Byte *);
void execute_store(Instruction, Processor *, Byte *);
void execute_ecall(Processor *, Byte *);
void execute_lui(Instruction, Processor *);

/** SO post 5253194 */
int logical_right_shift(int x, int n)
{
    if (n == 0)
	return x;
    int size = sizeof(int);
    return (x >> n) & ~(((x >> (size << 3) - 1) << (size << 3) -1)) >> (n-1);
}

void execute_instruction(uint32_t instruction_bits, Processor *processor,Byte *memory) {
    Instruction instruction = parse_instruction(instruction_bits);
    switch(instruction.opcode) {
        case 0x33:
            execute_rtype(instruction, processor);
            break;
        case 0x13:
            execute_itype_except_load(instruction, processor);
            break;
        case 0x73:
            execute_ecall(processor, memory);
            processor->PC = processor->PC + 4;
            break;
        case 0x63:
            execute_branch(instruction, processor);
            break;
        case 0x6F:
            execute_jal(instruction, processor);
            break;
        case 0x23:
            execute_store(instruction, processor, memory);
            break;
        case 0x03:
            execute_load(instruction, processor, memory);
            break;
        case 0x37:
            execute_lui(instruction, processor);
            break;
        default: // undefined opcode
            handle_invalid_instruction(instruction);
            exit(-1);
            break;
    }
}

void execute_rtype(Instruction instruction, Processor *processor) {
    Double tmp;
    switch (instruction.rtype.funct3){
        case 0x0:
            switch (instruction.rtype.funct7) {
                case 0x0:
                    // Add
		    processor->R[instruction.rtype.rd] = processor->R[instruction.rtype.rs1] + processor->R[instruction.rtype.rs2];
		    processor->PC += 4;
                    break;
                case 0x1:
		    //Mul
		    tmp = (((sDouble)((sWord) processor->R[instruction.rtype.rs1])) * ((sDouble)((sWord) processor->R[instruction.rtype.rs2])));
                    processor->R[instruction.rtype.rd] = ((tmp<<32)>>32);
		    processor->PC += 4; 
                    break;
                case 0x20:
		    //Sub
		    processor->R[instruction.rtype.rd] = processor->R[instruction.rtype.rs1] - processor->R[instruction.rtype.rs2];
		    processor->PC += 4;
                    break;
                default:
                    handle_invalid_instruction(instruction);
                    exit(-1);
                    break;
            }
            break;
        case 0x1:
            switch (instruction.rtype.funct7) {
                case 0x0:
		    //sll
		    processor->R[instruction.rtype.rd] = processor->R[instruction.rtype.rs1] << processor->R[instruction.rtype.rs2];
		    processor->PC += 4;
                    break;
                case 0x1:
		    //mulh
		    tmp = (((sDouble)((sWord) processor->R[instruction.rtype.rs1])) * ((sDouble)((sWord) processor->R[instruction.rtype.rs2])));
                    processor->R[instruction.rtype.rd] = (tmp>>32);
		    processor->PC += 4;
                    break;
            }
            break;
        case 0x2:
            // SLT
	    if ((int)processor->R[instruction.rtype.rs1] < (int)processor->R[instruction.rtype.rs2]) {
		processor->R[instruction.rtype.rd] = 1;
	    } else {
	        processor->R[instruction.rtype.rd] = 0;
	    }
	    processor->PC += 4;            
 	    break;
        case 0x4:
            switch (instruction.rtype.funct7) {
                case 0x0:
                    // XOR
		    processor->R[instruction.rtype.rd] = (processor->R[instruction.rtype.rs1] ^ processor->R[instruction.rtype.rs2]);
                    processor->PC += 4;
		    break;
                case 0x1:
                    // DIV
		    processor->R[instruction.rtype.rd] = (((sDouble)((sWord) processor->R[instruction.rtype.rs1])) / ((sDouble)((sWord) processor->R[instruction.rtype.rs2])));
                    processor->PC += 4;
		    break;
                default:
                    handle_invalid_instruction(instruction);
                    exit(-1);
                    break;
            }
            break;
        case 0x5:
            switch (instruction.rtype.funct7) {
                case 0x0:
                    // SRL 
		    processor->R[instruction.rtype.rd] = (sign_extend_number(processor->R[instruction.rtype.rs1], 5) >> sign_extend_number(processor->R[instruction.rtype.rs2], 5));
                    processor->PC += 4;
		    break;     
                case 0x20:
                    // SRA
		    processor->R[instruction.rtype.rd] = sign_extend_number((sign_extend_number(processor->R[instruction.rtype.rs1], 5) >> sign_extend_number(processor->R[instruction.rtype.rs2], 5)) , 32 - sign_extend_number(processor->R[instruction.rtype.rs1], 5));
                    processor->PC += 4;
		    break;
                default:
                    handle_invalid_instruction(instruction);
                    exit(-1);
                break;
            }
            break;
        case 0x6:
            switch (instruction.rtype.funct7) {
                case 0x0:
                    // OR
		    processor->R[instruction.rtype.rd] = (processor->R[instruction.rtype.rs1] | processor->R[instruction.rtype.rs2]);
 		    processor->PC += 4;
                    break;
                case 0x1:
                    // REM
		    processor->R[instruction.rtype.rd] = (((sDouble)((sWord) processor->R[instruction.rtype.rs1])) % ((sDouble)((sWord) processor->R[instruction.rtype.rs2])));
                    processor->PC += 4;
                    break;
                default:
                    handle_invalid_instruction(instruction);
                    exit(-1);
                    break;
            }
            break;
        case 0x7:
            // AND
	    processor->R[instruction.rtype.rd] = processor->R[instruction.rtype.rs1] & processor->R[instruction.rtype.rs2];
	    processor->PC += 4;            
	    break;
        default:
            handle_invalid_instruction(instruction);
            exit(-1);
            break;
    }
}

void execute_itype_except_load(Instruction instruction, Processor *processor) {
    int shiftOp;
    switch (instruction.itype.funct3) {
        case 0x0:
            // ADDI
	    processor->R[instruction.itype.rd] = processor->R[instruction.itype.rs1] + sign_extend_number(instruction.itype.imm, 12);
	    processor->PC += 4;
            break;
        case 0x1:
            // SLLI
	    processor->R[instruction.itype.rd] = (processor->R[instruction.itype.rs1] << sign_extend_number(instruction.itype.imm, 12));
	    processor->PC += 4;            
	    break;
        case 0x2:
	    processor->R[instruction.itype.rd] = (sign_extend_number(processor->R[instruction.itype.rs1], 5) < sign_extend_number(instruction.itype.imm, 12)) ? 1 : 0;
	    processor->PC += 4;            
	    break;
        case 0x4:
            // XORI
	    processor->R[instruction.itype.rd] = (processor->R[instruction.itype.rs1] ^ sign_extend_number(instruction.itype.imm, 12));
	    processor->PC += 4;            
	    break;
        case 0x5:
	    shiftOp = instruction.itype.imm >> 10;
	    int imm = instruction.itype.imm & 0x1F;
	    switch (shiftOp) {
		case 0x0:
		    //srli
	    	    processor->R[instruction.itype.rd] = logical_right_shift(processor->R[instruction.itype.rs1], imm);
	    	    processor->PC += 4; 
		    break;
		case 0x1:
	    	    processor->R[instruction.itype.rd] = sign_extend_number(processor->R[instruction.itype.rs1] >> sign_extend_number(imm, 12), 32-sign_extend_number(imm, 12));
	    	    processor->PC += 4; 
		    //srai
		    break;
		default:
		    printf("ERROR!");
            }
	    break;
            // Shift Right (You must handle both logical and arithmetic)
        case 0x6:
            // ORI
	    processor->R[instruction.itype.rd] = (processor->R[instruction.itype.rs1] | sign_extend_number(instruction.itype.imm, 12));
	    processor->PC += 4; 
            break;
        case 0x7:
            // ANDI
	    processor->R[instruction.itype.rd] = (processor->R[instruction.itype.rs1] & sign_extend_number(instruction.itype.imm, 12));
	    processor->PC += 4; 
            break;
        default:
            handle_invalid_instruction(instruction);
            break;
    }
}

void execute_ecall(Processor *p, Byte *memory) {
    Register i;
    // syscall number is given by a0 (x10)
    // argument is given by a1
    switch(p->R[10]) {
        case 1: // print an integer
            printf("%d",p->R[11]);
            break;
        case 4: // print a string
            for(i=p->R[11];i<MEMORY_SPACE && load(memory,i,LENGTH_BYTE);i++) {
                printf("%c",load(memory,i,LENGTH_BYTE));
            }
            break;
        case 10: // exit
            printf("exiting the simulator\n");
            exit(0);
            break;
        case 11: // print a character
            printf("%c",p->R[11]);
            break;
        default: // undefined ecall
            printf("Illegal ecall number %d\n", p->R[10]);
            exit(-1);
            break;
    }
}

void execute_branch(Instruction instruction, Processor *processor) {
    int rs1 = instruction.sbtype.rs1;
    int rs2 = instruction.sbtype.rs2;
    switch (instruction.sbtype.funct3) {
        case 0x0:
            // BEQ
	    if (processor->R[rs1] == processor->R[rs2]) {
		processor->PC = processor->PC + get_branch_offset(instruction);
		break;
            }
	    processor->PC = processor->PC + 4;
            break;
        case 0x1:
            // BNE
	    if (processor->R[rs1] != processor->R[rs2])
		processor->PC = processor->PC + get_branch_offset(instruction);
	    else
		processor->PC = processor->PC + 4;
            break;
        default:
            handle_invalid_instruction(instruction);
            exit(-1);
            break;
    }
}

void execute_load(Instruction instruction, Processor *processor, Byte *memory) {
    switch (instruction.itype.funct3) {
        case 0x0:
            // LB
	    processor->R[instruction.itype.rd] = sign_extend_number(load(memory, processor->R[instruction.itype.rs1] + sign_extend_number(instruction.itype.imm, 12), LENGTH_BYTE), 8);
	    processor->PC += 4;
            break;
        case 0x1:
            // LH
	    processor->R[instruction.itype.rd] = sign_extend_number(load(memory, processor->R[instruction.itype.rs1] + sign_extend_number(instruction.itype.imm, 12), LENGTH_HALF_WORD), 16);
            processor->PC += 4;
	    break;
        case 0x2:
            // LW
	    processor->R[instruction.itype.rd] = sign_extend_number(load(memory, processor->R[instruction.itype.rs1] + sign_extend_number(instruction.itype.imm, 12), LENGTH_WORD), 32);
            processor->PC += 4;
	    break;
        default:
            handle_invalid_instruction(instruction);
            break;
    }
}

void execute_store(Instruction instruction, Processor *processor, Byte *memory) {
    switch (instruction.stype.funct3) {
        case 0x0:
            // SB
	    store(memory, processor->R[instruction.stype.rs1] + get_store_offset(instruction), LENGTH_BYTE, processor->R[instruction.stype.rs2]);	
	    processor->PC += 4;
            break;
        case 0x1:
            // SH
	    store(memory, processor->R[instruction.stype.rs1] + get_store_offset(instruction), LENGTH_HALF_WORD, processor->R[instruction.stype.rs2]);	
	    processor->PC += 4;
            break;
        case 0x2:
            // SW
	    store(memory, processor->R[instruction.stype.rs1] + get_store_offset(instruction), LENGTH_WORD, processor->R[instruction.stype.rs2]);	
	    processor->PC += 4;
            break;
        default:
            handle_invalid_instruction(instruction);
            exit(-1);
            break;
    }
}

void execute_jal(Instruction instruction, Processor *processor) {
    int imm = get_jump_offset(instruction);
    processor->R[instruction.ujtype.rd] = processor->PC + 4;
    processor->PC = processor->PC + imm;
}

void execute_lui(Instruction instruction, Processor *processor) {
    unsigned int a = 0;
    processor->R[instruction.utype.rd] = (a+instruction.utype.imm) << 12;	
    processor->PC += 4;
}

void store(Byte *memory, Address address, Alignment alignment, Word value) {
    if (alignment == LENGTH_WORD) 
		*(uint32_t*)(memory + address) = (uint32_t)value;
    else if (alignment == LENGTH_HALF_WORD)
		*(uint16_t*)(memory + address) = (uint16_t)value;
    else if (alignment == LENGTH_BYTE)
		*(uint8_t*)(memory + address) = (uint8_t)value;           
}

Word load(Byte *memory, Address address, Alignment alignment) {
    if (alignment == LENGTH_WORD)
		return *(uint32_t*)(memory + address);
    else if (alignment == LENGTH_HALF_WORD)
		return  *(uint16_t*)(memory + address);
    else
		return  *(uint8_t*)(memory + address);
}
