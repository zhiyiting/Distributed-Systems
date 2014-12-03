all:
	mpicc -std=c99 kmeans-para.c kmeans.c -o kmeans-para.out
	gcc -std=c99 kmeans-seq.c kmeans.c -o kmeans-seq.out

clean:
	rm -f *.o *.out *.exe
	rm -f *.bin  

runpara:
	mpirun -np 4 -machinefile machinefile kmeans-para.out