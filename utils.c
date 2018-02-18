#include "utils.h"
#include <stdio.h>
#include <stdlib.h>
#include <inttypes.h>
#include <math.h>

/* Sign extends the given field to a 32-bit integer where field is
 * interpreted an n-bit integer. */ 
int sign_extend_number( unsigned int field, unsigned int n) {
    int temp = field/pow(2, n-1);
    if (temp == 0) {
       return field;
    } else {
    int value = 0;
    for (int i = 0; i <= 32-n; i++) {
        value = value*2 + 1;
    }
    return field + (value << n);
    }
}

/* From stack overflow post number 10090326 */
int extract(int value, int begin, int end)
{
    int mask = (1 << (end - begin)) - 1;
    return (value >> begin) & mask;
}

/* Unpacks the 32-bit machine code instruction given into the correct
 * type within the instruction struct */ 
Instruction parse_instruction(uint32_t instruction_bits) {
    /* YOUR CODE HERE */
    Instruction instruction;

    /* Opcode is always first 7 bits. */
    instruction.opcode = extract(instruction_bits, 0, 7);

    /* Make rest moniker to have way to obtain rest of bits. */
    instruction.rest = extract(instruction_bits, 7, 32);

    /* Do splitting assuming it is r type. */
    instruction.rtype.rd = extract(instruction_bits, 7, 12);
    instruction.rtype.funct3 = extract(instruction_bits, 12, 15);
    instruction.rtype.rs1 = extract(instruction_bits, 15, 20);
    instruction.rtype.rs2 = extract(instruction_bits, 20, 25);
    instruction.rtype.funct7 = extract(instruction_bits, 25, 32);

    
    /* Do splitting assuming it is i type. */
    instruction.itype.rd = extract(instruction_bits, 7, 12);
    instruction.itype.funct3 = extract(instruction_bits, 12, 15);
    instruction.itype.rs1 = extract(instruction_bits, 15, 20);
    instruction.itype.imm = extract(instruction_bits, 20, 32);


    /* Do splitting assuming it is u type. */
    instruction.utype.rd = extract(instruction_bits, 7, 12);
    instruction.utype.imm = extract(instruction_bits, 12, 32);

    /* Do splitting assuming it is uj type. */
    instruction.ujtype.rd = extract(instruction_bits, 7, 12);
    instruction.ujtype.imm = extract(instruction_bits, 12, 32);

    /* Do splitting assuming it is s type. */
    instruction.stype.imm5 = extract(instruction_bits, 7, 12);
    instruction.stype.funct3 = extract(instruction_bits, 12, 15);
    instruction.stype.rs1 = extract(instruction_bits, 15, 20);
    instruction.stype.rs2 = extract(instruction_bits, 20, 25);
    instruction.stype.imm7 = extract(instruction_bits, 25, 32);

    /* Do splitting assuming it is sb type. */
    instruction.sbtype.imm5 = extract(instruction_bits, 7, 12);
    instruction.sbtype.funct3 = extract(instruction_bits, 12, 15);
    instruction.sbtype.rs1 = extract(instruction_bits, 15, 20);
    instruction.sbtype.rs2 = extract(instruction_bits, 20, 25);
    instruction.sbtype.imm7 = extract(instruction_bits, 25, 32);

    return instruction;
}

/* Return the number of bytes (from the current PC) to the branch label using the given
 * branch instruction */
int get_branch_offset(Instruction instruction) {
    int imm1 = extract(instruction.sbtype.imm5, 0, 0);
    int imm2 = extract(instruction.sbtype.imm5, 1, 5);
    int imm3 = extract(instruction.sbtype.imm7, 0, 6);
    int imm4 = extract(instruction.sbtype.imm7, 6, 7);
    int imm = (imm2 + pow(2, 4)*imm3 + pow(2, 9)*imm1 + pow(2, 10)*imm4)*2;
    return sign_extend_number(imm, 12); 
}

/* Returns the number of bytes (from the current PC) to the jump label using the given
 * jump instruction */
int get_jump_offset(Instruction instruction) {
    int imm1 = extract(instruction.ujtype.imm, 0, 8);
    int imm2 = extract(instruction.ujtype.imm, 8, 9);
    int imm3 = extract(instruction.ujtype.imm, 9, 19);
    int imm4 = extract(instruction.ujtype.imm, 19, 20);
    int imm = (imm3 + pow(2, 10)*imm2 + pow(2, 11)*imm1 + pow(2, 19)*imm4);
    return sign_extend_number(imm, 20)*2;
}

int get_store_offset(Instruction instruction) {
    int imm = instruction.stype.imm5 + 32*instruction.stype.imm7;
    return sign_extend_number(imm, 12);
}

void handle_invalid_instruction(Instruction instruction) {
    printf("Invalid Instruction: 0x%08x\n", instruction.bits); 
}

void handle_invalid_read(Address address) {
    printf("Bad Read. Address: 0x%08x\n", address);
    exit(-1);
}

void handle_invalid_write(Address address) {
    printf("Bad Write. Address: 0x%08x\n", address);
    exit(-1);
}

