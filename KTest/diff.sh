if [ $# -eq 0 ] || [ $# -ne 2 ];
then
	exit
fi;

touch output.txt
rm output.txt
../riscv -d $1 > output.txt 2> /dev/null
echo "Starting $0..."
diff output.txt $2 >&1
echo "End of $0"
