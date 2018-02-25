#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdarg.h>
#include <time.h>

#include "riscv.h"

struct test_case {
	unsigned int instruction;
	unsigned int rd_mem_value;
	unsigned int address;
	unsigned int mem_mask;
	unsigned int rs1_initial;
	unsigned int rs2_initial;
	unsigned int PC_offset;
};
int build_test_suite(struct test_case* buffer);
int build_error_test(struct test_case* buffer);

struct test_case* cases;
struct test_case* error_cases;
unsigned int cases_counter = 0;
int verbose = 0;

void print_test_case(int error_case) {
	const char* format;
	struct test_case* current_case;
	if(error_case) {
		format = "Test case %2d (erroneous) (where rs1 = 0x%.8x, rs2 = 0x%.8x):\t\t";
		current_case = &error_cases[error_case - 1];
	} else {
		format = "Test case %2d (where rs1 = 0x%.8x, rs2 = 0x%.8x):\t\t";
		current_case = &cases[cases_counter];
	}
	printf(format, cases_counter, current_case->rs1_initial, current_case->rs2_initial);
	decode_instruction(current_case->instruction);
}

int assert_equal(int error_case, int is_memory, unsigned int actual, unsigned int expect, const char* error_format, ...) {
	if (actual == expect) {
		return 1;
	}
	if(!verbose && !error_case)
		print_test_case(error_case);
	va_list args;
	va_start(args, error_format);
	vprintf(error_format, args);
	va_end(args);
	if (is_memory)
		printf("Expect: 0x%.2x; Actual: 0x%.2x\n", expect, actual);
	else
		printf("Expect: 0x%.8x; Actual: 0x%.8x\n", expect, actual);
	// print instruction
	printf("\n");
	return 0;
}

unsigned int PC;
Processor processor;
Byte memory[MEMORY_SPACE];
void execute_test_case(int error_case) {
	if(verbose && !error_case)
		print_test_case(error_case);
	struct test_case* current_case;
	if (error_case)
		current_case = &error_cases[error_case - 1];
	else
		current_case = &cases[cases_counter];

	int assertion_result = 1;
	processor.PC = (rand() >> 22) << 2;
	processor.R[0] = 0;
	Instruction i;
	Processor control_group_processor;
	Byte control_group_memory[MEMORY_SPACE];
	i.bits = current_case->instruction;
	// Get the instruction type so that it can initialize the registers (and memory)
	switch (i.opcode)
	{
	case 0x33: // R
	case 0x23: // S
	case 0x63: // SB
		processor.R[i.rtype.rs2] = current_case->rs2_initial;
	case 0x13: // I
		processor.R[i.rtype.rs1] = current_case->rs1_initial;
	case 0x37: // U
	case 0x6F: // UJ
	case 0x73: // (ecall)
		break;
	case 0x03: // (L)
		processor.R[i.rtype.rs1] = current_case->rs1_initial;
		// If it is out of bound, ignore it
		if(current_case->address > MEMORY_SPACE)
			break;
		unsigned int* memory_location = (unsigned int*) &memory[current_case->address];
		*memory_location &= ~current_case->mem_mask;
		*memory_location |= current_case->rd_mem_value & current_case->mem_mask;
		break;
	default:
		break;
	}
	// Prepare for the control group
	memcpy(&control_group_processor, &processor, sizeof(processor));
	memcpy(&control_group_memory, &memory, sizeof(memory));
	switch (i.opcode) {
	case 0x33:
	case 0x13:
	case 0x3:
	case 0x37:
		control_group_processor.R[i.rtype.rd] = current_case->rd_mem_value;
	case 0x73:
	case 0x63:
		break;
	case 0x6F: // jal
		control_group_processor.R[i.rtype.rd] = control_group_processor.PC + 4;
		break;
	case 0x23:
		// Store
		// If it is out of bounds, ignore it
		if(current_case->address > MEMORY_SPACE)
			break;
		unsigned int* memory_location = (unsigned int*) &control_group_memory[current_case->address];
		*memory_location &= ~current_case->mem_mask;
		*memory_location |= current_case->rd_mem_value & current_case->mem_mask;
		break;
	}

	unsigned int PC_before = 0;
	unsigned int memory_value = 0;
	// If this is jal, report the current PC
	if (i.opcode == 0x6F)
		PC_before = processor.PC;
	// If this is a load instruction, report the current memory content
	if (i.opcode == 0x03 && !error_case)
		memory_value = *((unsigned int*) &memory[current_case->address]);
	// Execute!
	execute_instruction(i.bits, &processor, memory);

	// Clear x0
	processor.R[0] = 0;
	// Compare PC
	if(!error_case)
		assertion_result &= assert_equal(error_case, 0, processor.PC - control_group_processor.PC, current_case->PC_offset, "PC offset assertion failed:\n");
	// Compare R
	int count;
	if(!error_case)
		for (count = 0; count < 32; count++) {
			assertion_result &= assert_equal(error_case, 0, processor.R[count], control_group_processor.R[count], "Register x%d assertion failed:\n", count);
		}
	// If this is a data transfer instruction, check the memory
	if ((i.opcode == 0x03 || i.opcode == 0x23) && !error_case) {
		int address;
		for (address = 0; address < MEMORY_SPACE; address++) {
			assertion_result &= assert_equal(error_case, 1, memory[address], control_group_memory[address], "Memory assertion failed at address 0x%.8x:\n", address);
		}
	}
	if (assertion_result == 0 && !error_case) {
		if(i.opcode == 0x6F)
			printf("PC before execution: 0x%.8x\n", PC_before);
		else if(i.opcode == 0x03)
			printf("Memory content at 0x%.8x before execution: 0x%.8x\n", current_case->address, memory_value);
	}

	if (verbose && assertion_result == 1 && !error_case)
		printf("Test Passed\n\n");
	cases_counter++;
}

int main(int arc, char **argv) {
	int arg_iter = 1;
	int seed = 0;
	int error_case = 0;
	while (arg_iter < arc) {
		verbose = strcmp(argv[arg_iter], "-v") == 0;
		int arg_seed = atoi(argv[arg_iter]);
		if (arg_seed != 0)
			seed = arg_seed;
		// error case support
		if (argv[arg_iter][0] == 'e')
			error_case = atoi(argv[arg_iter] + 1) - 56;
		arg_iter++;
	}
	if (seed == 0)
		seed = time(0);
	srand(seed);

	// Error case:
	struct test_case error_cases_array[10];
	error_cases = error_cases_array;
	int errneous_count = build_error_test(error_cases);

	if (error_case) {
		cases_counter = 56 + error_case;
		execute_test_case(error_case);
		exit(0);
	}
	// Argument
	printf("=====================================\n");
	printf("Proj2-2 Unit Test by Zitao Fang\n");
	printf("Version: 1.3\n");
	printf("\n");
	printf("This program will use your part1 disassembler to show the instruction, so it is critical that Part 1 is correctly implemented.\n");
	printf("Please post your bug report to the Piazza thread.\n");
	printf("\n");
	printf("The random number seed for this test suite is %d.\n", seed);
	printf("If you need to reproduce this suite, enter the seed as a command line argument.\n");
	printf("To enable verbose mode (show all test case even if your code passed them), use \"-v\".\n");
	printf("==========Test Output==========\n");

	struct test_case cases_array[100];
	cases = cases_array;
	int test_count = build_test_suite(cases);
	// execute test cases
	int i;
	for (i = 0; i < test_count; i++) {
		execute_test_case(0);
	}
	// Execute errneous cases
	char command[1024];
	for(i = 0; i < errneous_count; i++) {
		memset(command, 0, 1024);
		int j;
		for (j = 0; j < arc; j++)
			strcat(strcat(command, argv[j]), " ");
		char buf[16];
		sprintf(buf, "e%d", cases_counter);
		strcat(command, buf);
		print_test_case(i + 1);
		int res = system(command);
		int assertion_result = assert_equal(i + 1, 0, WEXITSTATUS(res), 0xFF, "Erroneous case exit status assertion failed:\n");
		if(assertion_result)
			printf("DON\'T WORRY -- Test Passed\n\n");
		cases_counter++;
	}

	printf("==========Test Output==========\n");
	printf("\n");
	if(!verbose)
		printf("If the test output is empty except the errneous cases, your program pass all the tests.\n");
	printf("If a normal test case failed, you can set a breakpoint with \"b part2_unit_test.c:138 if cases_counter==<Failed Test Case #>\" and start debugging.\n");
	printf("e.g. If the test case labeled 16 failed, type \"b part2_unit_test.c:138 if cases_counter==16\" in (c)gdb.\n\n");
	printf("If a test case marked erroneous failed, it means your code didn't report an error which it should have done.\n");
	printf("For these cases, pass \"e<Failed Test Case #>\" (e.g. \"e57\" for case 57) as a command-line argument and set breakpoint with \"b part2_unit_test.c:138\".\n\n");
	printf("Some explanation:\n");
	printf("Test #57 and #58 is slli and srli/srai with wrong imm(high 7 bits). They are invalid instructions and should be reported.\n");
	printf("Test #59 and #60 are about out-of-bounds memory access. The address is greater than MEMORY_SPACE.\n");
	printf("Test #61-64 are also about out-of-bounds memory access, but they test whether you report"
		" an error when trying to read/write a WORD at an address like 1024*1024-2.\n");
	return 0;
}
