CC=gcc
CFLAGS=-std=c99

LDFLAGS=
POINTSOURCES= \
	point/point_clustering_sequential.c \
	point/kmeans.c

DNASOURCES= \
	dna/dna_clustering_sequential.c \
	dna/kmeans.c

POINTOBJECTS=$(POINTSOURCES:.cpp=.o)

DNAOBJECTS=$(DNASOURCES:.cpp=.o)

POINTEXECUTABLE=point/seq

DNAEXECUTABLE=dna/seq

all: \
	$(POINTSOURCES) $(POINTEXECUTABLE) \
	$(DNASOURCES) $(DNAEXECUTABLE)

$(POINTEXECUTABLE): $(POINTOBJECTS)
	$(CC) $(LDFLAGS) $(POINTOBJECTS) -o $@

$(DNAEXECUTABLE): $(DNAOBJECTS)
	$(CC) $(LDFLAGS) $(DNAOBJECTS) -o $@

.cpp.o:
	$(CC) $(CFLAGS) $< -o $@

clean:
	rm -rf $(POINTEXECUTABLE) $(DNAEXECUTABLE)

runpara:
	mpirun -np 4 -machinefile machinefile point_para.out